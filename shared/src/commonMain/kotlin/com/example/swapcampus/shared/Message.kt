package com.example.swapcampus.shared

import kotlinx.datetime.LocalDateTime

/** Message acheteur ↔ vendeur, toujours rattaché à une annonce (§6.3, §7.5). */
data class Message(
    val id: Int,
    val listing: Listing,
    val sender: UserAccount,
    val recipient: UserAccount,
    val content: String,
    val sentDate: LocalDateTime
)
