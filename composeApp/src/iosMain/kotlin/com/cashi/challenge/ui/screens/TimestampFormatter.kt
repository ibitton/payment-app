package com.cashi.challenge.ui.screens

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone

/**
 * iOS implementation of timestamp formatting using NSDateFormatter.
 */
@OptIn(ExperimentalTime::class)
actual fun formatInstant(instant: Instant): String {
    val seconds = instant.toEpochMilliseconds() / 1000.0
    val nsDate = NSDate.dateWithTimeIntervalSince1970(seconds)

    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm"
    formatter.timeZone = NSTimeZone.localTimeZone

    return formatter.stringFromDate(nsDate)
}
