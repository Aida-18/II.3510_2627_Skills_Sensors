package com.example.swapcampus.shared

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Coordonnées GPS en degrés décimaux (§4 item 10, §8.6). */
data class Position(val latitude: Double, val longitude: Double) {
    init {
        require(latitude in -90.0..90.0) { "Latitude out of range: $latitude" }
        require(longitude in -180.0..180.0) { "Longitude out of range: $longitude" }
    }
}

private const val EARTH_RADIUS_KM = 6371.0088

private fun Double.toRadians(): Double = this * kotlin.math.PI / 180.0

/**
 * Distance « à vol d'oiseau » en kilomètres entre deux points GPS (formule de Haversine).
 *
 * TODO Exercice 2 : implémenter la formule de Haversine.
 *  1. Calculer dLat et dLon : différences de latitude et de longitude, converties en radians (toRadians()).
 *  2. a = sin²(dLat / 2) + cos(lat1) · cos(lat2) · sin²(dLon / 2)   (lat1 et lat2 en radians)
 *  3. distance = 2 · R · asin(√a)   avec R = EARTH_RADIUS_KM
 *  Astuces : x.pow(2) pour le carré, sqrt(...) pour la racine ; a.coerceIn(0.0, 1.0) évite
 *  une erreur d'arrondi. Vérification : Paris → Londres ≈ 344 km (test ProximityTest).
 */
fun Position.distanceKm(other: Position): Double {
    // TODO Exercice 2 : remplacer cette valeur par le calcul.
    return 0.0
}

/** Distance entre l'utilisateur et l'annonce, ou `null` si l'une des deux positions est inconnue. */
fun Listing.distanceFrom(user: Position?): Double? =
    if (user == null || position == null) null else user.distanceKm(position)

/**
 * Annonces triées de la plus proche à la plus lointaine ; celles sans position vont à la fin.
 *
 * TODO Exercice 3a : utiliser sortedBy { ... } avec distanceFrom(user).
 *  Indice : une annonce sans position a une distance `null` → la remplacer par Double.MAX_VALUE (opérateur ?:).
 */
fun List<Listing>.sortedByProximity(user: Position): List<Listing> {
    // TODO Exercice 3a
    return this
}

/**
 * Vrai si l'annonce est à moins de [radiusKm] kilomètres de l'utilisateur.
 *
 * TODO Exercice 3b : règles attendues (vérifiées par les tests) :
 *  - si radiusKm ou user est null → le critère est ignoré → true ;
 *  - si l'annonce n'a pas de position → false ;
 *  - sinon → distance <= radiusKm.
 */
fun Listing.isWithinRadius(user: Position?, radiusKm: Double?): Boolean {
    // TODO Exercice 3b
    return true
}
