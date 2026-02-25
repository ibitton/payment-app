package com.cashi.challenge.domain.validation

import com.cashi.challenge.domain.models.Currency
import com.cashi.challenge.domain.models.PaymentRequest

/**
 * Validates payment requests before they are sent to the backend.
 * All validation logic is centralized here for consistency across platforms.
 */
class PaymentValidator {

    /**
     * Validates a payment request and returns a result indicating success or failure.
     *
     * @param request The payment request to validate
     * @return ValidationResult indicating success or containing error messages
     */
    fun validate(request: PaymentRequest): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate email
        if (request.recipientEmail.isBlank()) {
            errors.add("Recipient email is required")
        } else if (!isValidEmail(request.recipientEmail)) {
            errors.add("Invalid email format")
        }

        // Validate amount
        if (request.amount <= 0) {
            errors.add("Amount must be greater than 0")
        } else if (request.amount > 1_000_000) {
            errors.add("Amount exceeds maximum limit of 1,000,000")
        }

        // Validate currency
        if (request.currency.isBlank()) {
            errors.add("Currency is required")
        } else if (!Currency.isSupported(request.currency)) {
            errors.add("Unsupported currency. Supported currencies: USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, SGD, NZD, SEK, NOK, DKK, PLN, MXN")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }

    /**
     * Validates email format using a simple regex pattern.
     * Note: This is a basic validation. For production, consider more robust validation.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }
}

/**
 * Sealed class representing the result of a validation operation.
 */
sealed class ValidationResult {
    /**
     * Validation passed successfully.
     */
    data object Success : ValidationResult()

    /**
     * Validation failed with specific error messages.
     *
     * @param messages List of error messages describing what failed validation
     */
    data class Error(val messages: List<String>) : ValidationResult()
}

/**
 * Exception thrown when payment validation fails.
 */
class PaymentValidationException(val errors: List<String>) : Exception(errors.joinToString("; "))
