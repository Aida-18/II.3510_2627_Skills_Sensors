package com.example.swapcampus.shared

import kotlinx.datetime.LocalDate

/** Statut d'une annonce (§7.4). Les transitions autorisées sont décrites au §5.5. */
sealed class ListingStatus {

    data object Available : ListingStatus()

    data class Reserved(
        val buyer: String
    ) : ListingStatus()

    data class Sold(
        val saleDate: LocalDate
    ) : ListingStatus()
}

/**
 * Règle métier §5.5 :
 * Disponible → Réservée(acheteur) ; Réservée → Vendue(date) ;
 * Réservée/Vendue → Disponible (annulation).
 */
fun ListingStatus.canTransitionTo(target: ListingStatus): Boolean = when (this) {
    ListingStatus.Available -> target is ListingStatus.Reserved
    is ListingStatus.Reserved -> target is ListingStatus.Sold || target is ListingStatus.Available
    is ListingStatus.Sold -> target is ListingStatus.Available
}
