package com.cashi.challenge.bdd

import com.cashi.challenge.data.api.dto.PaymentRequest
import com.cashi.challenge.domain.validation.PaymentValidator
import com.cashi.challenge.domain.validation.ValidationResult
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * BDD-style tests for Payment Validation using Spek framework.
 * These tests describe the expected behavior of the payment validation logic
 * in a readable, business-friendly format.
 */
object PaymentValidationSpek : Spek({
    describe("Payment Validation") {
        val validator by memoized { PaymentValidator() }

        describe("Given a user enters valid payment details") {
            val validRequest = PaymentRequest(
                recipientEmail = "john@example.com",
                amount = 100.00,
                currency = "USD"
            )

            it("should validate successfully") {
                val result = validator.validate(validRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user enters valid payment details with EUR currency") {
            val validEurRequest = PaymentRequest(
                recipientEmail = "jane@example.com",
                amount = 250.50,
                currency = "EUR"
            )

            it("should validate successfully") {
                val result = validator.validate(validEurRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user enters valid payment details with GBP currency") {
            val validGbpRequest = PaymentRequest(
                recipientEmail = "john@example.co.uk",
                amount = 150.00,
                currency = "GBP"
            )

            it("should validate successfully for GBP") {
                val result = validator.validate(validGbpRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user enters valid payment details with JPY currency") {
            val validJpyRequest = PaymentRequest(
                recipientEmail = "tanaka@example.jp",
                amount = 10000.00,
                currency = "JPY"
            )

            it("should validate successfully for JPY") {
                val result = validator.validate(validJpyRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user enters valid payment details with CAD currency") {
            val validCadRequest = PaymentRequest(
                recipientEmail = "user@example.ca",
                amount = 200.00,
                currency = "CAD"
            )

            it("should validate successfully for CAD") {
                val result = validator.validate(validCadRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user enters an invalid email address") {
            val invalidEmailRequest = PaymentRequest(
                recipientEmail = "not-an-email",
                amount = 50.00,
                currency = "USD"
            )

            it("should fail with 'Invalid email format' error") {
                val result = validator.validate(invalidEmailRequest)
                assertTrue(result is ValidationResult.Error)
                assertTrue(result.messages.contains("Invalid email format"))
            }
        }

        describe("Given a user enters a blank email address") {
            val blankEmailRequest = PaymentRequest(
                recipientEmail = "",
                amount = 50.00,
                currency = "USD"
            )

            it("should fail with 'Recipient email is required' error") {
                val result = validator.validate(blankEmailRequest)
                assertTrue(result is ValidationResult.Error)
                assertTrue(result.messages.contains("Recipient email is required"))
            }
        }

        describe("Given a user enters an amount less than or equal to zero") {
            val negativeAmountRequest = PaymentRequest(
                recipientEmail = "user@example.com",
                amount = -10.00,
                currency = "USD"
            )

            it("should fail with 'Amount must be greater than 0' error") {
                val result = validator.validate(negativeAmountRequest)
                assertTrue(result is ValidationResult.Error)
                assertTrue(result.messages.contains("Amount must be greater than 0"))
            }
        }

        describe("Given a user enters zero as the amount") {
            val zeroAmountRequest = PaymentRequest(
                recipientEmail = "user@example.com",
                amount = 0.0,
                currency = "USD"
            )

            it("should fail with 'Amount must be greater than 0' error") {
                val result = validator.validate(zeroAmountRequest)
                assertTrue(result is ValidationResult.Error)
                assertTrue(result.messages.contains("Amount must be greater than 0"))
            }
        }

        describe("Given a user enters an amount exceeding the maximum limit") {
            val excessiveAmountRequest = PaymentRequest(
                recipientEmail = "user@example.com",
                amount = 1_500_000.00,
                currency = "USD"
            )

            it("should fail with 'Amount exceeds maximum limit' error") {
                val result = validator.validate(excessiveAmountRequest)
                assertTrue(result is ValidationResult.Error)
                assertTrue(result.messages.contains("Amount exceeds maximum limit of 1,000,000"))
            }
        }

        describe("Given a user enters an unsupported currency") {
            val unsupportedCurrencyRequest = PaymentRequest(
                recipientEmail = "user@example.com",
                amount = 100.00,
                currency = "XYZ"
            )

            it("should fail with 'Unsupported currency' error") {
                val result = validator.validate(unsupportedCurrencyRequest)
                assertTrue(result is ValidationResult.Error)
                val errors = result.messages
                assertTrue(errors.any { it.contains("Unsupported currency") })
            }
        }

        describe("Given a user enters a blank currency") {
            val blankCurrencyRequest = PaymentRequest(
                recipientEmail = "user@example.com",
                amount = 100.00,
                currency = ""
            )

            it("should fail with 'Currency is required' error") {
                val result = validator.validate(blankCurrencyRequest)
                assertTrue(result is ValidationResult.Error)
                assertTrue(result.messages.contains("Currency is required"))
            }
        }

        describe("Given a user enters multiple invalid fields") {
            val multipleErrorsRequest = PaymentRequest(
                recipientEmail = "invalid",
                amount = -5.0,
                currency = "XYZ"
            )

            it("should return all validation errors") {
                val result = validator.validate(multipleErrorsRequest)
                assertTrue(result is ValidationResult.Error)
                val errors = result.messages
                assertEquals(3, errors.size)
                assertTrue(errors.any { it.contains("Invalid email format") })
                assertTrue(errors.any { it.contains("Amount must be greater than 0") })
                assertTrue(errors.any { it.contains("Unsupported currency") })
            }
        }
    }
})
