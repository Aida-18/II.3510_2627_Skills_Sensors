package com.example.swapcampus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.swapcampus.shared.Position
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
actual fun rememberLocationProvider(): LocationProvider {
    val context = LocalContext.current.applicationContext
    val status = remember { mutableStateOf(if (hasLocationPermission(context)) PermissionState.GRANTED else PermissionState.NOT_REQUESTED) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        status.value = if (result.values.any { it }) PermissionState.GRANTED else PermissionState.DENIED
    }
    return remember(context, launcher) { AndroidLocationProvider(context, status, launcher) }
}

private fun hasLocationPermission(context: Context): Boolean = PERMISSIONS.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

private class AndroidLocationProvider(
    private val context: Context,
    private val status: MutableState<PermissionState>,
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) : LocationProvider {

    override val permission: PermissionState get() = status.value

    /**
     * TODO Exercice 4a : afficher la demande système d'autorisation.
     *  Indice : le `launcher` (créé dans rememberLocationProvider) attend le tableau PERMISSIONS.
     */
    override fun requestPermission() {
        // TODO Exercice 4a
    }

    /**
     * TODO Exercice 4b : lire la position de l'appareil.
     *  1. Si la permission n'est pas accordée (hasLocationPermission) → return null.
     *  2. Récupérer le LocationManager : context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
     *  3. Garder les fournisseurs actifs parmi GPS_PROVIDER et NETWORK_PROVIDER (isProviderEnabled).
     *  4. Version simple : manager.getLastKnownLocation(fournisseur) ; garder la plus récente (maxByOrNull { it.time }).
     *     Bonus : demander une position fraîche avec LocationManagerCompat.getCurrentLocation(...)
     *     dans suspendCancellableCoroutine, avec un délai max (withTimeoutOrNull).
     *  5. Convertir le résultat : Position(location.latitude, location.longitude).
     *  Testez sur l'émulateur : ⋯ › Location › Set Location (ou adb emu geo fix 2.2799 48.8245).
     */
    @SuppressLint("MissingPermission") // la permission est vérifiée à l'étape 1
    override suspend fun currentPosition(): Position? {
        // TODO Exercice 4b
        return null
    }
}
