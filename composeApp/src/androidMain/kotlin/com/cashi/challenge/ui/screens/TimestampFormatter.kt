package com.cashi.challenge.ui.screens

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Android implementation of timestamp formatting using java.time (API 26+).
 */
@OptIn(ExperimentalTime::class)
actual fun formatInstant(instant: Instant): String {
    val javaInstant = java.time.Instant.ofEpochMilli(instant.toEpochMilliseconds())
    val dateTime = java.time.ZonedDateTime.ofInstant(javaInstant, java.time.ZoneId.systemDefault())

    val date = "${dateTime.year}-${dateTime.monthValue.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')}"
    val time = "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    return "$date $time"
}