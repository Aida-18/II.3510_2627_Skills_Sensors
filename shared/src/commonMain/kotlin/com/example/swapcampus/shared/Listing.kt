package com.example.swapcampus.shared

import kotlinx.datetime.LocalDate

/** Annonce publiée par un étudiant (§7.2). */
data class Listing(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val category: Category,
    val photoUrl: String?,
    val author: UserAccount,
    val status: ListingStatus,
    val creationDate: LocalDate,
    /** Lieu de remise de l'objet, capturé à la publication (facultatif). */
    val position: Position? = null
)

fun Listing.hasPhoto(): Boolean = !photoUrl.isNullOrBlank()

/** Erreurs de validation métier d'une annonce (§5.1, §10.5). */
enum class ListingError {
    EMPTY_TITLE,
    INVALID_PRICE
}

/**
 * Valide les règles de publication : titre non vide, prix > 0.
 * @return la première erreur trouvée, ou `null` si l'annonce est valide.
 */
fun validateListing(title: String, price: Double?): ListingError? = when {
    title.isBlank() -> ListingError.EMPTY_TITLE
    price == null || price <= 0.0 -> ListingError.INVALID_PRICE
    else -> null
}

/** Exception levée par le repository quand une règle métier n'est pas respectée. */
class InvalidListingException(val error: ListingError) :
    IllegalArgumentException(error.name)
