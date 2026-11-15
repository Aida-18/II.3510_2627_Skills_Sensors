package com.example.swapcampus.shared

import kotlinx.datetime.LocalDateTime

/** Cible d'une action de modération : une annonce ou un compte (§7.6). */
sealed class ModerationTarget {
    data class ListingTarget(val listingId: Int) : ModerationTarget()
    data class AccountTarget(val accountId: Int) : ModerationTarget()
}

/** Trace d'une action d'un administrateur, avec motif obligatoire (§5.6, §6.2). */
data class ModerationAction(
    val id: Int,
    val target: ModerationTarget,
    val type: ModerationActionType,
    val reason: String,
    val administrator: UserAccount,
    val date: LocalDateTime
)
