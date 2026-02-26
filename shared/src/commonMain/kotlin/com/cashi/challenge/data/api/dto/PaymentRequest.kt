package com.cashi.challenge.data.api.dto

import kotlinx.serialization.Serializable

/**
 * Request to process a payment.
 *
 * @property recipientEmail Email address of the payment recipient
 * @property amount Payment amount (must be positive, max 2 decimal places)
 * @property currency ISO currency code (e.g., USD, EUR)
 */
@Serializable
data class PaymentRequest(
    val recipientEmail: String,
    val amount: Double,
    val currency: String
)
