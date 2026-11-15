package com.example.swapcampus.shared

import kotlinx.coroutines.flow.StateFlow

/**
 * Point d'accès unique aux annonces (MVVM, §8.5).
 * Une implémentation Room/Firestore pourra remplacer l'implémentation en mémoire
 * sans toucher à l'UI.
 */
interface ListingRepository {

    /** Toutes les annonces, observables : l'UI se met à jour à chaque changement. */
    val listings: StateFlow<List<Listing>>

    fun findById(id: Int): Listing?

    /** Recherche parmi les annonces disponibles (§5.3). */
    fun searchListings(
        keyword: String? = null,
        category: Category? = null,
        maxPrice: Double? = null
    ): List<Listing>

    /**
     * Publie une annonce (§5.1) : l'id, la date de création et l'état « Disponible »
     * sont attribués ici, pas par l'UI.
     * @throws InvalidListingException si le titre est vide ou le prix ≤ 0.
     */
    fun postListing(
        title: String,
        description: String,
        price: Double,
        category: Category,
        photoUrl: String?,
        author: UserAccount,
        position: Position? = null
    ): Listing

    /**
     * Modifie une annonce existante (§5.4).
     * @throws InvalidListingException si les règles de publication ne sont plus respectées.
     */
    fun updateListing(listing: Listing): Listing?

    /**
     * Change le statut (§5.5).
     * @throws IllegalStateException si la transition n'est pas autorisée.
     */
    fun changeStatus(id: Int, newStatus: ListingStatus): Listing?

    fun removeListing(id: Int): Boolean
}
