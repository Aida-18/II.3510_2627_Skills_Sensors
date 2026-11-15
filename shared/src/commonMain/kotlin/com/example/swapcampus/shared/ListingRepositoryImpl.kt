package com.example.swapcampus.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

/**
 * Implémentation en mémoire du [ListingRepository].
 * @param today source de la date du jour (injectable pour les tests).
 */
class ListingRepositoryImpl(
    private val catalog: ListingCatalog,
    private val today: () -> LocalDate = ::currentDate
) : ListingRepository {

    private val _listings = MutableStateFlow(catalog.allListings())
    override val listings: StateFlow<List<Listing>> = _listings.asStateFlow()

    override fun findById(id: Int): Listing? = catalog.findById(id)

    override fun searchListings(
        keyword: String?,
        category: Category?,
        maxPrice: Double?
    ): List<Listing> = catalog.searchListings(keyword, category, maxPrice)

    override fun postListing(
        title: String,
        description: String,
        price: Double,
        category: Category,
        photoUrl: String?,
        author: UserAccount,
        position: Position?
    ): Listing {
        requireValid(title, price)
        val listing = Listing(
            id = catalog.nextId(),
            title = title.trim(),
            description = description.trim(),
            price = price,
            category = category,
            photoUrl = photoUrl?.trim()?.ifBlank { null },
            author = author,
            status = ListingStatus.Available,
            creationDate = today(),
            position = position
        )
        return catalog.postListing(listing).also { emitListings() }
    }

    override fun updateListing(listing: Listing): Listing? {
        requireValid(listing.title, listing.price)
        return catalog.updateListing(listing).also { emitListings() }
    }

    override fun changeStatus(id: Int, newStatus: ListingStatus): Listing? {
        val current = catalog.findById(id) ?: return null
        check(current.status.canTransitionTo(newStatus)) {
            "Forbidden transition: ${current.status} → $newStatus"
        }
        return catalog.changeStatus(id, newStatus).also { emitListings() }
    }

    override fun removeListing(id: Int): Boolean =
        catalog.removeListing(id).also { emitListings() }

    private fun requireValid(title: String, price: Double) {
        validateListing(title, price)?.let { throw InvalidListingException(it) }
    }

    private fun emitListings() {
        _listings.value = catalog.allListings()
    }
}
