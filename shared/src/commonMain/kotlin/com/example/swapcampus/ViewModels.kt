package com.example.swapcampus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapcampus.shared.Listing
import com.example.swapcampus.shared.InvalidListingException
import com.example.swapcampus.shared.AuthRepository
import com.example.swapcampus.shared.Category
import com.example.swapcampus.shared.ListingError
import com.example.swapcampus.shared.ListingStatus
import com.example.swapcampus.shared.ListingRepository
import com.example.swapcampus.shared.UserAccount
import com.example.swapcampus.shared.ModerationRepository
import com.example.swapcampus.shared.Position
import com.example.swapcampus.shared.isWithinRadius
import com.example.swapcampus.shared.sortedByProximity
import com.example.swapcampus.shared.SellerStats
import com.example.swapcampus.shared.UiState
import com.example.swapcampus.shared.matches
import com.example.swapcampus.shared.currentDate
import com.example.swapcampus.shared.available
import com.example.swapcampus.shared.sellerStats
import com.example.swapcampus.shared.validateListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Critères de recherche combinés (§5.3). */
data class SearchFilters(
    val keyword: String = "",
    val category: Category? = null,
    val maxPrice: String = "",
    /** Tri « les plus proches » (§4 item 10). */
    val sortByProximity: Boolean = false,
    /** Rayon maximum en km, actif seulement avec le tri par proximité. */
    val radiusKm: Int? = null
)

/**
 * Liste publique : annonces disponibles filtrées par mot-clé, catégorie, prix
 * et distance, éventuellement triées par proximité (§5.2, §5.3, §4 item 10).
 */
class ListingsViewModel(repository: ListingRepository) : ViewModel() {

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    private val _position = MutableStateFlow<Position?>(null)

    /** Dernière position connue de l'utilisateur (null tant qu'elle n'a pas été obtenue). */
    val position: StateFlow<Position?> = _position.asStateFlow()

    val uiState: StateFlow<UiState<List<Listing>>> =
        combine(repository.listings, _filters, _position) { listings, f, position ->
            val activePosition = position.takeIf { f.sortByProximity }
            val result = listings.available().filter {
                it.matches(f.keyword, f.category, f.maxPrice.toPriceOrNull()) &&
                    it.isWithinRadius(activePosition, f.radiusKm?.toDouble())
            }
            UiState.Success(activePosition?.let(result::sortedByProximity) ?: result)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun onSortByProximity(active: Boolean) =
        _filters.update { it.copy(sortByProximity = active, radiusKm = it.radiusKm.takeIf { active }) }

    /** Re-cliquer sur le rayon sélectionné le désactive. */
    fun onRadius(km: Int) = _filters.update { f -> f.copy(radiusKm = km.takeIf { km != f.radiusKm }) }

    fun onPosition(position: Position?) {
        _position.value = position
    }

    fun onKeyword(value: String) = _filters.update { it.copy(keyword = value) }
    fun onCategory(value: Category?) = _filters.update { it.copy(category = value) }
    fun onMaxPrice(value: String) = _filters.update { it.copy(maxPrice = value) }
}

/** État de l'écran « Mes annonces » : annonces du vendeur + compteurs (§5.7). */
data class MyListingsState(
    val listings: List<Listing> = emptyList(),
    val stats: SellerStats = emptyList<Listing>().sellerStats()
)

/** Annonces du vendeur connecté et changements de statut (§5.5, §5.6). */
class MyListingsViewModel(
    private val repository: ListingRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val state: StateFlow<MyListingsState> =
        combine(repository.listings, authRepository.currentUser) { listings, user ->
            val mine = listings.filter { it.author.id == user?.id }
            MyListingsState(mine, mine.sellerStats())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyListingsState())

    fun reserve(listing: Listing, buyer: String) =
        updateStatus(listing, ListingStatus.Reserved(buyer.trim()))

    fun markAsSold(listing: Listing) = updateStatus(listing, ListingStatus.Sold(currentDate()))

    /** Annulation : Réservée/Vendue → Disponible. */
    fun putBackOnSale(listing: Listing) = updateStatus(listing, ListingStatus.Available)

    fun delete(listing: Listing) {
        repository.removeListing(listing.id)
    }

    private fun updateStatus(listing: Listing, status: ListingStatus) {
        // La règle de transition est vérifiée par le repository ; l'UI ne propose
        // que les actions valides, donc un échec ici serait un bug de programmation.
        repository.changeStatus(listing.id, status)
    }
}

/** Champs du formulaire publier/modifier. */
data class ListingForm(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: Category = Category.OTHER,
    val photoUrl: String = "",
    val position: Position? = null,
    val error: ListingError? = null
)

/**
 * Formulaire de publication (`listingId == null`) ou de modification (§5.1, §5.4).
 * La validation est faite par le domaine ([validateListing]), pas par l'UI.
 */
class ListingFormViewModel(
    private val repository: ListingRepository,
    private val authRepository: AuthRepository,
    private val listingId: Int?
) : ViewModel() {

    private val initialListing: Listing? = listingId?.let(repository::findById)

    /** Faux si l'annonce n'existe pas ou n'appartient pas à l'utilisateur connecté. */
    val isAuthorized: Boolean
        get() = listingId == null ||
            (initialListing != null && initialListing.author.id == authRepository.currentUser.value?.id)

    private val _form = MutableStateFlow(
        initialListing?.let {
            ListingForm(
                title = it.title,
                description = it.description,
                price = it.price.toString(),
                category = it.category,
                photoUrl = it.photoUrl.orEmpty(),
                position = it.position
            )
        } ?: ListingForm()
    )
    val form: StateFlow<ListingForm> = _form.asStateFlow()

    fun onChange(transformation: (ListingForm) -> ListingForm) {
        _form.value = transformation(_form.value).copy(error = null)
    }

    /** @return true si l'annonce a été enregistrée. */
    fun save(): Boolean {
        val f = _form.value
        val price = f.price.toPriceOrNull()
        validateListing(f.title, price)?.let { error ->
            _form.value = f.copy(error = error)
            return false
        }
        val author = authRepository.currentUser.value ?: return false
        return try {
            if (initialListing == null) {
                repository.postListing(f.title, f.description, price!!, f.category, f.photoUrl, author, f.position)
                _form.value = ListingForm()
            } else {
                repository.updateListing(
                    initialListing.copy(
                        title = f.title.trim(),
                        description = f.description.trim(),
                        price = price!!,
                        category = f.category,
                        photoUrl = f.photoUrl.trim().ifBlank { null },
                        position = f.position
                    )
                )
            }
            true
        } catch (e: InvalidListingException) {
            _form.value = f.copy(error = e.error)
            false
        }
    }
}

/** Accepte « 12,5 » comme « 12.5 » (saisie française). */
fun String.toPriceOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Expose une annonce par id, mise à jour en direct (écran détail), et sa modération. */
class ListingDetailViewModel(
    repository: ListingRepository,
    private val moderation: ModerationRepository,
    private val id: Int
) : ViewModel() {

    /** Retrait par un administrateur avec motif obligatoire (§5.6). @return true si retirée. */
    fun remove(reason: String, admin: UserAccount): Boolean =
        moderation.removeListing(id, reason, admin) != null

    val listing: StateFlow<Listing?> = repository.listings
        .map { currentListings -> currentListings.find { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.findById(id))
}
