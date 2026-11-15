package com.example.swapcampus

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

actual fun formatPrice(price: Double): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault())
        .apply { currency = Currency.getInstance("EUR") }
        .format(price)

actual fun formatDate(date: LocalDate): String {
    val calendar = Calendar.getInstance().apply {
        clear()
        set(date.year, date.month.number - 1, date.day)
    }
    return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(calendar.time)
}

actual fun formatNumber(value: Double, decimals: Int): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }.format(value)
