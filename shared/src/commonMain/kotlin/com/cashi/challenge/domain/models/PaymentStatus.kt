package com.cashi.challenge.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents the status of a payment transaction.
 */
@Serializable
enum class PaymentStatus {
    /** Payment is being processed */
    PENDING,

    /** Payment completed successfully */
    SUCCESS,

    /** Payment failed */
    FAILED,

    /** Payment was cancelled */
    CANCELLED
}
