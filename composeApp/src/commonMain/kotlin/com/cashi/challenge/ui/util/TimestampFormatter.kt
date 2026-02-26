package com.cashi.challenge.ui.util

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Platform-independent expect declaration for formatting timestamps.
 * Each platform provides its own actual implementation.
 */
@ExperimentalTime
expect fun formatInstant(instant: Instant): String
