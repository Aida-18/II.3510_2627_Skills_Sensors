package com.example.swapcampus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.swapcampus.shared.Position
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Composable
actual fun rememberLocationProvider(): LocationProvider = remember { IosLocationProvider() }

private fun CLAuthorizationStatus.toPermissionState(): PermissionState = when (this) {
    kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> PermissionState.GRANTED
    kCLAuthorizationStatusNotDetermined -> PermissionState.NOT_REQUESTED
    else -> PermissionState.DENIED // refusée ou restreinte (contrôle parental…)
}

private class IosLocationProvider : LocationProvider {

    private val manager = CLLocationManager()
    private val status = mutableStateOf(manager.authorizationStatus.toPermissionState())
    private var pending: CancellableContinuation<Position?>? = null

    /** Délégué Objective-C : reçoit les réponses de CoreLocation sur le thread principal. */
    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            status.value = manager.authorizationStatus.toPermissionState()
        }

        @OptIn(ExperimentalForeignApi::class)
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val position = (didUpdateLocations.lastOrNull() as? CLLocation)
                ?.coordinate?.useContents { Position(latitude, longitude) }
            complete(position)
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            complete(null)
        }
    }

    init {
        manager.delegate = delegate
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    override val permission: PermissionState get() = status.value

    override fun requestPermission() = manager.requestWhenInUseAuthorization()

    override suspend fun currentPosition(): Position? {
        if (permission != PermissionState.GRANTED) return null
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine { cont ->
                pending = cont
                manager.requestLocation()
            }
        }
    }

    private fun complete(position: Position?) {
        pending?.takeIf { it.isActive }?.resume(position)
        pending = null
    }
}
