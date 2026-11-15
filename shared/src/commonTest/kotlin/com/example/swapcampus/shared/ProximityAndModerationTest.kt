package com.example.swapcampus.shared

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProximityTest {

    private val paris = Position(48.8566, 2.3522)
    private val london = Position(51.5074, -0.1278)

    @Test
    fun haversine_paris_london_about_344_km() {
        val d = paris.distanceKm(london)
        assertTrue(abs(d - 343.5) < 2.0, "distance obtenue : $d")
    }

    @Test
    fun distance_is_zero_and_symmetric() {
        assertEquals(0.0, paris.distanceKm(paris), 1e-9)
        assertEquals(paris.distanceKm(london), london.distanceKm(paris), 1e-9)
    }

    @Test
    fun invalid_position_is_rejected() {
        assertFailsWith<IllegalArgumentException> { Position(91.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { Position(0.0, 181.0) }
    }

    @Test
    fun sorted_nearest_first_without_position_last() {
        val ids = DemoData.listings.sortedByProximity(DemoData.campus).map { it.id }
        // 1 ≈ 0,2 km · 4 ≈ 0,9 km · 2 ≈ 2,2 km · 3 ≈ 6 km · 5 sans position
        assertEquals(listOf(1, 4, 2, 3, 5), ids)
    }

    @Test
    fun filter_by_radius() {
        val campus = DemoData.campus
        val withinOneKm = DemoData.listings.filter { it.isWithinRadius(campus, 1.0) }.map { it.id }
        assertEquals(listOf(1, 4), withinOneKm)
        // Sans position utilisateur ou sans rayon, le critère est ignoré.
        assertEquals(5, DemoData.listings.count { it.isWithinRadius(null, 1.0) })
        assertEquals(5, DemoData.listings.count { it.isWithinRadius(campus, null) })
    }

    @Test
    fun distance_unknown_if_a_position_is_missing() {
        val withoutPosition = DemoData.listings.first { it.position == null }
        assertNull(withoutPosition.distanceFrom(DemoData.campus))
        assertNull(DemoData.listings.first().distanceFrom(null))
    }

    @Test
    fun post_stores_the_position() {
        val repo = ListingRepositoryImpl(ListingCatalog()) { LocalDate(2026, 11, 15) }
        val a = repo.postListing("Lampe", "", 8.0, Category.FURNITURE, null, DemoData.student1, DemoData.campus)
        assertEquals(DemoData.campus, a.position)
    }
}

class ModerationTest {

    private val now = LocalDateTime(2026, 11, 15, 14, 30)

    private fun setup(): Pair<ListingRepository, ModerationRepository> {
        val listings = ListingRepositoryImpl(ListingCatalog(DemoData.listings))
        return listings to ModerationRepository(listings) { now }
    }

    @Test
    fun admin_removes_a_listing_and_the_action_is_logged() {
        val (listings, moderation) = setup()
        val action = moderation.removeListing(2, "  Objet interdit ", DemoData.admin)

        assertNull(listings.findById(2))
        assertEquals(ModerationTarget.ListingTarget(2), action?.target)
        assertEquals(ModerationActionType.LISTING_REMOVAL, action?.type)
        assertEquals("Objet interdit", action?.reason)
        assertEquals(now, action?.date)
        assertEquals(listOf(action), moderation.actionLog.value)
    }

    @Test
    fun reason_is_required() {
        val (listings, moderation) = setup()
        assertFailsWith<IllegalArgumentException> { moderation.removeListing(2, " ", DemoData.admin) }
        assertTrue(listings.findById(2) != null, "l'annonce ne doit pas être supprimée")
    }

    @Test
    fun administrators_only() {
        val (_, moderation) = setup()
        assertFailsWith<IllegalArgumentException> {
            moderation.removeListing(2, "Spam", DemoData.student1)
        }
    }

    @Test
    fun unknown_listing_nothing_is_logged() {
        val (_, moderation) = setup()
        assertNull(moderation.removeListing(999, "Spam", DemoData.admin))
        assertTrue(moderation.actionLog.value.isEmpty())
    }
}
