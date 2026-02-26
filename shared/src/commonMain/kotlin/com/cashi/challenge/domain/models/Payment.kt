package com.cashi.challenge.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a payment transaction.
 *
 * @property id Unique transaction ID
 * @property recipientEmail Email of the payment recipient
 * @property amount Payment amount
 * @property currency Currency of the payment
 * @property status Current status of the payment
 * @property timestamp Unix timestamp when the payment was processed
 * @property errorMessage Error message if payment failed, null if successful
 */
@Serializable
data class Payment(
    val id: String,
    val recipientEmail: String,
    val amount: Double,
    val currency: Currency,
    val status: PaymentStatus,
    val timestamp: Long,
    val errorMessage: String? = null
)
