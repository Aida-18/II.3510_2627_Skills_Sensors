package com.example.swapcampus.shared

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListingRepositoryTest {

    private val today = LocalDate(2026, 11, 15)
    private val seller = DemoData.student1

    private fun repo() = ListingRepositoryImpl(ListingCatalog(DemoData.listings)) { today }

    @Test
    fun post_assigns_unique_id_date_and_available_status() {
        val repo = repo()
        val a = repo.postListing("Casque", "", 15.0, Category.ELECTRONICS, " ", seller)
        val b = repo.postListing("Raquette", "", 10.0, Category.SPORTS_LEISURE, null, seller)

        assertTrue(a.id != b.id)
        assertEquals(today, a.creationDate)
        assertEquals(ListingStatus.Available, a.status)
        assertNull(a.photoUrl, "une URL vide devient null")
    }

    @Test
    fun post_updates_the_flow_observed_by_the_ui() {
        val repo = repo()
        val before = repo.listings.value.size
        repo.postListing("Casque", "", 15.0, Category.ELECTRONICS, null, seller)
        assertEquals(before + 1, repo.listings.value.size)
    }

    @Test
    fun post_rejects_empty_title_and_zero_or_negative_price() {
        val repo = repo()
        val e1 = assertFailsWith<InvalidListingException> {
            repo.postListing("  ", "", 10.0, Category.OTHER, null, seller)
        }
        assertEquals(ListingError.EMPTY_TITLE, e1.error)
        val e2 = assertFailsWith<InvalidListingException> {
            repo.postListing("Objet", "", 0.0, Category.OTHER, null, seller)
        }
        assertEquals(ListingError.INVALID_PRICE, e2.error)
    }

    @Test
    fun search_excludes_reserved_and_sold() {
        val result = repo().searchListings()
        assertTrue(result.all { it.status is ListingStatus.Available })
        assertFalse(result.any { it.id == 4 || it.id == 5 })
    }

    @Test
    fun search_combines_keyword_category_and_price() {
        val repo = repo()
        assertEquals(listOf(1), repo.searchListings(keyword = "kotlin").map { it.id })
        assertEquals(listOf(2), repo.searchListings(category = Category.ELECTRONICS).map { it.id })
        assertEquals(listOf(1, 2), repo.searchListings(maxPrice = 50.0).map { it.id })
        assertTrue(repo.searchListings(keyword = "kotlin", category = Category.SPORTS_LEISURE).isEmpty())
    }

    @Test
    fun allowed_transitions_section_5_5() {
        val repo = repo()
        repo.changeStatus(1, ListingStatus.Reserved("acheteur@x.fr"))
        repo.changeStatus(1, ListingStatus.Sold(today))
        repo.changeStatus(1, ListingStatus.Available)
        assertEquals(ListingStatus.Available, repo.findById(1)?.status)

        repo.changeStatus(1, ListingStatus.Reserved("acheteur@x.fr"))
        repo.changeStatus(1, ListingStatus.Available) // annulation de réservation
        assertEquals(ListingStatus.Available, repo.findById(1)?.status)
    }

    @Test
    fun forbidden_transition_available_to_sold() {
        assertFailsWith<IllegalStateException> {
            repo().changeStatus(1, ListingStatus.Sold(today))
        }
    }

    @Test
    fun remove_deletes_the_listing() {
        val repo = repo()
        assertTrue(repo.removeListing(1))
        assertNull(repo.findById(1))
        assertFalse(repo.listings.value.any { it.id == 1 })
    }

    @Test
    fun seller_stats() {
        val stats = DemoData.listings.filter { it.author.id == seller.id }.sellerStats()
        assertEquals(SellerStats(posted = 3, sold = 1, available = 1, reserved = 1), stats)
    }

    @Test
    fun authentication() {
        val auth = AuthRepository()
        assertNull(auth.authenticate("etudiant1@swapcampus.com", "faux"))
        assertNull(auth.currentUser.value)
        assertEquals(1, auth.authenticate(" Etudiant1@SwapCampus.com ", "1234")?.id)
        assertEquals(1, auth.currentUser.value?.id)
        auth.logout()
        assertNull(auth.currentUser.value)
    }
}
