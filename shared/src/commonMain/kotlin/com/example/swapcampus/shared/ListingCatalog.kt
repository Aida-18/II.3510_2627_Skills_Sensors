package com.example.swapcampus.shared

/** Stockage en mémoire des annonces (TP1/TP2, §8.3). */
class ListingCatalog(initialListings: List<Listing> = emptyList()) {

    private val listings = initialListings.toMutableList()

    /** Renvoie une copie : l'appelant ne peut pas modifier le catalogue par erreur. */
    fun allListings(): List<Listing> = listings.toList()

    fun findById(id: Int): Listing? = listings.find { it.id == id }

    /** Identifiant unique suivant (pas de tirage aléatoire → pas de collision). */
    fun nextId(): Int = (listings.maxOfOrNull { it.id } ?: 0) + 1

    fun postListing(listing: Listing): Listing = listing.also { listings.add(it) }

    /**
     * Recherche parmi les annonces **disponibles** (§5.3 / §5.5) par mot-clé,
     * catégorie et prix maximum. Un critère `null` est ignoré.
     */
    fun searchListings(
        keyword: String? = null,
        category: Category? = null,
        maxPrice: Double? = null
    ): List<Listing> = listings.available().filter { it.matches(keyword, category, maxPrice) }

    fun changeStatus(id: Int, newStatus: ListingStatus): Listing? =
        replace(id) { it.copy(status = newStatus) }

    fun updateListing(updatedListing: Listing): Listing? =
        replace(updatedListing.id) { updatedListing }

    fun removeListing(id: Int): Boolean = listings.removeAll { it.id == id }

    private inline fun replace(id: Int, transformation: (Listing) -> Listing): Listing? {
        val index = listings.indexOfFirst { it.id == id }
        if (index == -1) return null
        return transformation(listings[index]).also { listings[index] = it }
    }
}

fun List<Listing>.available(): List<Listing> = filter { it.status is ListingStatus.Available }

/** Vrai si l'annonce respecte tous les critères non nuls. */
fun Listing.matches(keyword: String?, category: Category?, maxPrice: Double?): Boolean {
    val matchesKeyword = keyword.isNullOrBlank() ||
        title.contains(keyword, ignoreCase = true) ||
        description.contains(keyword, ignoreCase = true)
    val matchesCategory = category == null || this.category == category
    val matchesPrice = maxPrice == null || price <= maxPrice
    return matchesKeyword && matchesCategory && matchesPrice
}

/** Statistiques vendeur « core » (§5.7) : simples compteurs. */
data class SellerStats(
    val posted: Int,
    val sold: Int,
    val available: Int,
    val reserved: Int
)

fun List<Listing>.sellerStats(): SellerStats = SellerStats(
    posted = size,
    sold = count { it.status is ListingStatus.Sold },
    available = count { it.status is ListingStatus.Available },
    reserved = count { it.status is ListingStatus.Reserved }
)
