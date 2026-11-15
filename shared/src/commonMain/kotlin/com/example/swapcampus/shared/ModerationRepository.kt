package com.example.swapcampus.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime

/**
 * Modération par l'administrateur (§3.2, §5.6) : chaque action est tracée
 * dans un journal d'[ModerationAction] avec un motif obligatoire.
 * @param maintenant source de la date/heure (injectable pour les tests).
 */
class ModerationRepository(
    private val listingRepository: ListingRepository,
    private val now: () -> LocalDateTime = ::currentDateTime
) {

    private val _actionLog = MutableStateFlow<List<ModerationAction>>(emptyList())

    /** Journal des actions, de la plus récente à la plus ancienne. */
    val actionLog: StateFlow<List<ModerationAction>> = _actionLog.asStateFlow()

    /**
     * Supprime définitivement une annonce et trace l'action.
     * @throws IllegalArgumentException si [admin] n'est pas administrateur ou si le motif est vide.
     * @return l'action enregistrée, ou `null` si l'annonce n'existe pas.
     */
    fun removeListing(listingId: Int, reason: String, admin: UserAccount): ModerationAction? {
        require(admin.role == UserRole.ADMINISTRATOR) { "Administrators only" }
        require(reason.isNotBlank()) { "A reason is required" }
        if (!listingRepository.removeListing(listingId)) return null

        return ModerationAction(
            id = (_actionLog.value.maxOfOrNull { it.id } ?: 0) + 1,
            target = ModerationTarget.ListingTarget(listingId),
            type = ModerationActionType.LISTING_REMOVAL,
            reason = reason.trim(),
            administrator = admin,
            date = now()
        ).also { action -> _actionLog.value = listOf(action) + _actionLog.value }
    }
}
