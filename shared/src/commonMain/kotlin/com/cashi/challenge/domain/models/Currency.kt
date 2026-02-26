package com.cashi.challenge.domain.models

import kotlinx.serialization.Serializable

/**
 * Supported currencies for payment processing.
 * Includes major international currencies for global payment support.
 */
@Serializable
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
        /**
         * Checks if a currency string is supported.
         * Case-insensitive comparison.
         *
         * @param currency The currency code to check
         * @return true if supported, false otherwise
         */
        fun isSupported(currency: String): Boolean {
            return try {
                valueOf(currency.uppercase())
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }
    }
}
