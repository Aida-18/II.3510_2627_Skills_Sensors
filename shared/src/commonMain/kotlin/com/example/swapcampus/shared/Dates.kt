package com.example.swapcampus.shared

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Date du jour dans le fuseau de l'appareil. */
@OptIn(ExperimentalTime::class)
fun currentDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

/** Date et heure actuelles dans le fuseau de l'appareil. */
@OptIn(ExperimentalTime::class)
fun currentDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
