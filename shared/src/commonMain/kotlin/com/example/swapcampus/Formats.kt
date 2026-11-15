package com.example.swapcampus

import androidx.compose.runtime.Composable
import com.example.swapcampus.shared.Category
import com.example.swapcampus.shared.ListingStatus
import com.example.swapcampus.shared.UserRole
import com.example.swapcampus.shared.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Prix au format monétaire de la langue de l'appareil (ex. « 25,00 € » / « €25.00 »). */
expect fun formatPrice(price: Double): String

/** Date au format court de la langue de l'appareil (ex. « 7 oct. 2026 » / « Oct 7, 2026 »). */
expect fun formatDate(date: LocalDate): String

/** Nombre avec [decimals] chiffres après la virgule, séparateur de la langue (« 1,2 » / « 1.2 »). */
expect fun formatNumber(value: Double, decimals: Int): String

val Category.label: StringResource
    get() = when (this) {
        Category.BOOKS -> Res.string.category_books
        Category.COURSES -> Res.string.category_courses
        Category.ELECTRONICS -> Res.string.category_electronics
        Category.SPORTS_LEISURE -> Res.string.category_sport
        Category.FURNITURE -> Res.string.category_furniture
        Category.CLOTHING -> Res.string.category_clothes
        Category.OTHER -> Res.string.category_other
    }

@Composable
fun ListingStatus.label(): String = when (this) {
    ListingStatus.Available -> stringResource(Res.string.status_available)
    is ListingStatus.Reserved -> stringResource(Res.string.status_reserved, buyer)
    is ListingStatus.Sold -> stringResource(Res.string.status_sold, formatDate(saleDate))
}

val UserRole.label: StringResource
    get() = when (this) {
        UserRole.STUDENT -> Res.string.role_student
        UserRole.ADMINISTRATOR -> Res.string.role_admin
    }
