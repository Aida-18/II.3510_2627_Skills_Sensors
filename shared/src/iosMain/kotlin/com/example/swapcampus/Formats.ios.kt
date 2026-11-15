package com.example.swapcampus

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.currentLocale

actual fun formatPrice(price: Double): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        currencyCode = "EUR"
        locale = NSLocale.currentLocale
    }
    return formatter.stringFromNumber(NSNumber(double = price)) ?: "$price €"
}

actual fun formatDate(date: LocalDate): String {
    val components = NSDateComponents().apply {
        year = date.year.toLong()
        month = date.month.number.toLong()
        day = date.day.toLong()
    }
    val nsDate = NSCalendar.currentCalendar.dateFromComponents(components) ?: return date.toString()
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterMediumStyle
        timeStyle = NSDateFormatterNoStyle
        locale = NSLocale.currentLocale
    }
    return formatter.stringFromDate(nsDate)
}

actual fun formatNumber(value: Double, decimals: Int): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        minimumFractionDigits = decimals.toULong()
        maximumFractionDigits = decimals.toULong()
        locale = NSLocale.currentLocale
    }
    return formatter.stringFromNumber(NSNumber(double = value)) ?: value.toString()
}
