package com.cashi.challenge.validation

import com.cashi.challenge.data.api.dto.PaymentRequest
import com.cashi.challenge.domain.validation.PaymentValidator
import com.cashi.challenge.domain.validation.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for PaymentValidator.
 * These tests run on all KMP platforms (commonTest).
 */
class PaymentValidatorTest {

    private val validator = PaymentValidator()

    @Test
    fun `valid payment request should return success`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 100.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `valid EUR payment request should return success`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 50.0,
            currency = "EUR"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `blank email should return email required error`() {
        val request = PaymentRequest(
            recipientEmail = "",
            amount = 100.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Recipient email is required"))
    }

    @Test
    fun `invalid email format should return invalid email error`() {
        val request = PaymentRequest(
            recipientEmail = "not-an-email",
            amount = 100.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Invalid email format"))
    }

    @Test
    fun `email without domain should return invalid email error`() {
        val request = PaymentRequest(
            recipientEmail = "user@",
            amount = 100.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Invalid email format"))
    }

    @Test
    fun `email without at symbol should return invalid email error`() {
        val request = PaymentRequest(
            recipientEmail = "userexample.com",
            amount = 100.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Invalid email format"))
    }

    @Test
    fun `zero amount should return amount must be greater than zero error`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 0.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Amount must be greater than 0"))
    }

    @Test
    fun `negative amount should return amount must be greater than zero error`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = -50.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Amount must be greater than 0"))
    }

    @Test
    fun `amount exceeding maximum should return maximum limit error`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 1_500_000.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Amount exceeds maximum limit of 1,000,000"))
    }

    @Test
    fun `amount with more than 2 decimal places should return decimal places error`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 100.999,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Amount cannot have more than 2 decimal places"))
    }

    @Test
    fun `amount with exactly 2 decimal places should return success`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 99.99,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `amount with exactly 1 decimal place should return success`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 50.5,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `amount with 3 decimal places should return decimal places error`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 10.123,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Amount cannot have more than 2 decimal places"))
    }

    @Test
    fun `amount at maximum limit should return success`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 1_000_000.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `blank currency should return currency required error`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 100.0,
            currency = ""
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.contains("Currency is required"))
    }

    @Test
    fun `valid GBP payment request should return success`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.co.uk",
            amount = 75.0,
            currency = "GBP"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `valid JPY payment request should return success`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.jp",
            amount = 5000.0,
            currency = "JPY"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `valid CAD payment request should return success`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.ca",
            amount = 150.0,
            currency = "CAD"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `lowercase currency should be accepted due to case insensitive comparison`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 100.0,
            currency = "usd"
        )

        val result = validator.validate(request)

        // Currency.isSupported converts to uppercase, so lowercase should work
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `unsupported currency should return unsupported currency error`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 100.0,
            currency = "XYZ"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        assertTrue(result.messages.any { it.contains("Unsupported currency") })
    }

    @Test
    fun `multiple validation errors should return all errors`() {
        val request = PaymentRequest(
            recipientEmail = "invalid",
            amount = -10.0,
            currency = "INVALID"
        )

        val result = validator.validate(request)

        assertTrue(result is ValidationResult.Error)
        val errors = result.messages
        assertEquals(3, errors.size)
        assertTrue(errors.any { it.contains("Invalid email format") })
        assertTrue(errors.any { it.contains("Amount must be greater than 0") })
        assertTrue(errors.any { it.contains("Unsupported currency") })
    }

    @Test
    fun `valid email with plus sign should be accepted`() {
        val request = PaymentRequest(
            recipientEmail = "user+tag@example.com",
            amount = 100.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `valid email with subdomain should be accepted`() {
        val request = PaymentRequest(
            recipientEmail = "user@mail.example.com",
            amount = 100.0,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `very small positive amount should be accepted`() {
        val request = PaymentRequest(
            recipientEmail = "user@example.com",
            amount = 0.01,
            currency = "USD"
        )

        val result = validator.validate(request)

        assertEquals(ValidationResult.Success, result)
    }
}
