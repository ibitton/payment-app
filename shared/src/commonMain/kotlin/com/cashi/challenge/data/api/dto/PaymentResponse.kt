package com.cashi.challenge.data.api.dto

import com.cashi.challenge.domain.models.PaymentStatus
import kotlinx.serialization.Serializable

/**
 * Response from payment processing.
 *
 * @property id Unique transaction ID assigned by the backend
 * @property status Payment status (SUCCESS, FAILED, PENDING, etc.)
 * @property timestamp Unix timestamp when the payment was processed
 * @property errorMessage Error description if payment failed, null if successful
 */
@Serializable
data class PaymentResponse(
    val id: String,
    val status: PaymentStatus,
    val timestamp: Long,
    val errorMessage: String? = null
)
