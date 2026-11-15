package com.example.swapcampus

import androidx.compose.runtime.Composable
import com.example.swapcampus.shared.Position

/** État de la permission de localisation (§4 item 9). */
enum class PermissionState { NOT_REQUESTED, GRANTED, DENIED }

/**
 * Accès à la position de l'appareil (capteur GPS / réseau), implémenté par plateforme :
 * Android → `LocationManager` + permission à l'exécution ; iOS → `CLLocationManager`.
 * [permission] est un état Compose : l'UI se recompose quand l'utilisateur répond.
 */
interface LocationProvider {
    val permission: PermissionState

    /** Affiche la demande système d'autorisation. */
    fun requestPermission()

    /** Position actuelle, ou `null` si non autorisée / indisponible (délai max ~10 s). */
    suspend fun currentPosition(): Position?
}

@Composable
expect fun rememberLocationProvider(): LocationProvider

/** « 350 m » sous 1 km, sinon « 1,2 km » / « 1.2 km » selon la langue. */
fun formatDistance(km: Double): String =
    if (km < 1.0) "${(km * 1000).toInt()} m" else "${formatNumber(km, 1)} km"
