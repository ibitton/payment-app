package com.cashi.challenge.ui.util

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * JS/Web implementation of timestamp formatting using JS Date API.
 */
@OptIn(ExperimentalTime::class)
actual fun formatInstant(instant: Instant): String {
    val millis = instant.toEpochMilliseconds()
    val jsDate = js("function(millis) { return new Date(millis); }").call(millis)

    val year = jsDate.getFullYear().toString()
    val month = (jsDate.getMonth() + 1).toString().padStart(2, '0')
    val day = jsDate.getDate().toString().padStart(2, '0')
    val hour = jsDate.getHours().toString().padStart(2, '0')
    val minute = jsDate.getMinutes().toString().padStart(2, '0')

    return "$year-$month-$day $hour:$minute"
}
