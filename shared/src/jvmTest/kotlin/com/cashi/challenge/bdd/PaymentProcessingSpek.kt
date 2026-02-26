package com.cashi.challenge.bdd

import com.cashi.challenge.data.api.PaymentApiClient
import com.cashi.challenge.data.api.dto.PaymentRequest
import com.cashi.challenge.domain.result.OperationResult
import com.cashi.challenge.domain.validation.PaymentValidator
import com.cashi.challenge.domain.validation.ValidationResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * BDD-style tests for Payment API Client using Spek framework.
 * These tests describe the payment API communication flow.
 */
object PaymentProcessingSpek : Spek({

    describe("Payment API Client") {

        describe("Given a valid payment request is sent to the API") {
            val validRequest = PaymentRequest(
                recipientEmail = "recipient@example.com",
                amount = 100.0,
                currency = "USD"
            )

            it("should return a successful response with transaction ID") {
                val mockHttpClient = createMockHttpClient(success = true)
                val apiClient = PaymentApiClient(mockHttpClient)

                val result = runBlocking { apiClient.processPayment(validRequest, "test-idempotency-key-1") }

                assertTrue(result is OperationResult.Success)
                val response = result.data
                assertNotNull(response)
                assertEquals("TXN-12345", response.id)
                assertEquals(com.cashi.challenge.domain.models.PaymentStatus.SUCCESS, response.status)
            }
        }

        describe("Given the API returns an error response") {
            val validRequest = PaymentRequest(
                recipientEmail = "user@example.com",
                amount = 75.0,
                currency = "EUR"
            )

            it("should return a failure result") {
                val mockHttpClient = createMockHttpClient(success = false)
                val apiClient = PaymentApiClient(mockHttpClient)

                val result = runBlocking { apiClient.processPayment(validRequest, "test-idempotency-key-2") }

                assertTrue(result is OperationResult.Failure)
}
        }
    }

    describe("Payment Validation Integration") {
        val validator by memoized { PaymentValidator() }

        describe("Given a user submits a payment with all valid fields") {
            val validRequest = PaymentRequest(
                recipientEmail = "test@example.com",
                amount = 250.0,
                currency = "USD"
            )

            it("should pass validation successfully") {
                val result = validator.validate(validRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user submits a payment with EUR currency") {
            val eurRequest = PaymentRequest(
                recipientEmail = "test@example.com",
                amount = 150.0,
                currency = "EUR"
            )

            it("should pass validation for EUR currency") {
                val result = validator.validate(eurRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }
    }

    describe("Currency Support") {
        val validator by memoized { PaymentValidator() }

        describe("Given a user selects USD as currency") {
            val usdRequest = PaymentRequest(
                recipientEmail = "test@example.com",
                amount = 100.0,
                currency = "USD"
            )

            it("should accept the payment") {
                val result = validator.validate(usdRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user selects EUR as currency") {
            val eurRequest = PaymentRequest(
                recipientEmail = "test@example.com",
                amount = 100.0,
                currency = "EUR"
            )

            it("should accept the payment") {
                val result = validator.validate(eurRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user selects an unsupported currency like ABC") {
            val unsupportedRequest = PaymentRequest(
                recipientEmail = "test@example.com",
                amount = 100.0,
                currency = "ABC"
            )

            it("should reject the payment with appropriate error") {
                val result = validator.validate(unsupportedRequest)
                assertTrue(result is ValidationResult.Error)
                assertTrue(result.messages.any { it.contains("Unsupported currency") })
            }
        }
    }

    describe("Transaction Amount Limits") {
        val validator by memoized { PaymentValidator() }

        describe("Given a user enters the maximum allowed amount of 1,000,000") {
            val maxAmountRequest = PaymentRequest(
                recipientEmail = "test@example.com",
                amount = 1_000_000.0,
                currency = "USD"
            )

            it("should accept the payment at the limit") {
                val result = validator.validate(maxAmountRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }

        describe("Given a user enters an amount exceeding 1,000,000") {
            val excessiveAmountRequest = PaymentRequest(
                recipientEmail = "test@example.com",
                amount = 1_500_000.0,
                currency = "USD"
            )

            it("should reject the payment for exceeding limit") {
                val result = validator.validate(excessiveAmountRequest)
                assertTrue(result is ValidationResult.Error)
                assertTrue((result).messages.any { it.contains("Amount exceeds maximum limit of 1,000,000") })
            }
        }

        describe("Given a user enters a very small valid amount") {
            val smallAmountRequest = PaymentRequest(
                recipientEmail = "test@example.com",
                amount = 0.01,
                currency = "USD"
            )

            it("should accept the small payment") {
                val result = validator.validate(smallAmountRequest)
                assertEquals(ValidationResult.Success, result)
            }
        }
    }
})

/**
 * Helper function to create a mock HttpClient for testing
 */
private fun createMockHttpClient(success: Boolean): HttpClient {
    val mockEngine = MockEngine {
        if (success) {
            respond(
                content = """{"id": "TXN-12345", "status": "SUCCESS", "timestamp": ${System.currentTimeMillis()}}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        } else {
            respond(
                content = """{"error": "Server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }
    }

    return HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}
