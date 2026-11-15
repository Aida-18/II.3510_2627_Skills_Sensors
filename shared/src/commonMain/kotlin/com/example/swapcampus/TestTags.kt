package com.example.swapcampus

import com.example.swapcampus.shared.Category

/**
 * Identifiants stables des éléments d'interface, utilisés par les tests UI automatisés (§8.8).
 * Ils ne dépendent pas de la langue, contrairement aux textes affichés.
 */
object TestTags {
    const val LOGIN_EMAIL = "login_email"
    const val LOGIN_PASSWORD = "login_password"
    const val LOGIN_BUTTON = "login_button"

    const val FORM_TITLE = "form_title"
    const val FORM_PRICE = "form_price"
    const val FORM_SUBMIT = "form_submit"
    const val FORM_ERROR = "form_error"

    const val LISTINGS_SEARCH = "listings_search"
    const val LISTINGS_LIST = "listings_list"
    const val LISTINGS_EMPTY = "listings_empty"
    const val MY_LISTINGS_LIST = "my_listings_list"

    /** Onglet de la barre de navigation (route : listings, myListings, post, profile). */
    fun nav(route: String) = "nav_$route"

    fun category(category: Category) = "chip_${category.name}"
}
