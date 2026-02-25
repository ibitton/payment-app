package com.cashi.challenge.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a payment transaction in the system.
 * This is the core domain model used across all platforms.
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
) {
    companion object {
        const val MAX_AMOUNT = 10000.0
        const val MIN_AMOUNT = 0.01
    }
}

/**
 * Supported currencies for payment processing.
 * Includes major international currencies for global payment support.
 */
enum class Currency {
    USD, // US Dollar
    EUR, // Euro
    GBP, // British Pound
    JPY, // Japanese Yen
    CAD, // Canadian Dollar
    AUD, // Australian Dollar
    CHF, // Swiss Franc
    CNY, // Chinese Yuan
    INR, // Indian Rupee
    SGD, // Singapore Dollar
    NZD, // New Zealand Dollar
    SEK, // Swedish Krona
    NOK, // Norwegian Krone
    DKK, // Danish Krone
    PLN, // Polish Zloty
    MXN; // Mexican Peso

    companion object {
        fun isSupported(currencyCode: String): Boolean {
            return entries.any { it.name == currencyCode.uppercase() }
        }
    }
}

/**
 * Status of a payment transaction.
 */
enum class PaymentStatus {
    SUCCESS,
    FAILED,
    PENDING
}

/**
 * Request payload for initiating a payment.
 */
@Serializable
data class PaymentRequest(
    val recipientEmail: String,
    val amount: Double,
    val currency: String
)

/**
 * Response from the payment processing backend.
 */
@Serializable
data class PaymentResponse(
    val id: String,
    val status: PaymentStatus,
    val timestamp: Long,
    val errorMessage: String? = null
)
