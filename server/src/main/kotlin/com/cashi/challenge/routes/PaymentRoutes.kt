package com.cashi.challenge.routes

import com.cashi.challenge.domain.models.PaymentRequest
import com.cashi.challenge.domain.models.PaymentResponse
import com.cashi.challenge.domain.models.PaymentStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/**
 * Configures the payment routes for the Ktor server.
 * Includes server-side validation (independent of client validation) and idempotency support.
 */
fun Application.configurePaymentRoutes() {
    // In-memory store for idempotency keys (use Redis/database in production)
    val processedPayments = mutableSetOf<String>()

    routing {
        post("/payments") {
            try {
                val request = call.receive<PaymentRequest>()

                // Check idempotency key (client-provided unique key to prevent duplicates)
                val idempotencyKey = call.request.header("Idempotency-Key")
                if (idempotencyKey != null && idempotencyKey in processedPayments) {
                    // Return cached response for duplicate request
                    call.respond(
                        HttpStatusCode.OK,
                        PaymentResponse(
                            id = "IDEMPOTENT-${idempotencyKey.take(8)}",
                            status = PaymentStatus.SUCCESS,
                            timestamp = System.currentTimeMillis(),
                            errorMessage = null
                        )
                    )
                    return@post
                }

                // Server-side validation (independent of client validation)
                // Server has higher limits and stricter rules than client
                val validationErrors = validatePaymentRequestServerSide(request)

                if (validationErrors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        PaymentResponse(
                            id = "",
                            status = PaymentStatus.FAILED,
                            timestamp = System.currentTimeMillis(),
                            errorMessage = validationErrors.joinToString("; ")
                        )
                    )
                    return@post
                }

                // Mock payment processing - generate a transaction ID
                val transactionId = generateTransactionId()

                // Store idempotency key if provided
                if (idempotencyKey != null) {
                    processedPayments.add(idempotencyKey)
                }

                // Simulate occasional processing delays or failures (5% failure rate for testing)
                val shouldFail = (0..99).random() < 5

                if (shouldFail) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        PaymentResponse(
                            id = transactionId,
                            status = PaymentStatus.FAILED,
                            timestamp = System.currentTimeMillis(),
                            errorMessage = "Payment processing failed. Please try again."
                        )
                    )
                } else {
                    // Success response
                    call.respond(
                        HttpStatusCode.OK,
                        PaymentResponse(
                            id = transactionId,
                            status = PaymentStatus.SUCCESS,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    PaymentResponse(
                        id = "",
                        status = PaymentStatus.FAILED,
                        timestamp = System.currentTimeMillis(),
                        errorMessage = "Invalid request format: ${e.message}"
                    )
                )
            }
        }
    }
}

/**
 * Server-side validation (independent of client validation).
 * Server has higher limits and stricter security rules than client.
 */
private fun validatePaymentRequestServerSide(request: PaymentRequest): List<String> {
    val errors = mutableListOf<String>()

    // Validate email (server uses same regex but additional checks)
    if (request.recipientEmail.isBlank()) {
        errors.add("Recipient email is required")
    } else if (!isValidEmail(request.recipientEmail)) {
        errors.add("Invalid email format")
    } else if (request.recipientEmail.length > 254) {
        errors.add("Email exceeds maximum length")
    }

    // Validate amount (server allows higher limits than client)
    // Client: $1M max, Server: $10M max (can handle larger institutional payments)
    if (request.amount <= 0) {
        errors.add("Amount must be greater than 0")
    } else if (request.amount > 10_000_000) {
        errors.add("Amount exceeds server maximum limit of 10,000,000")
    } else if (!request.amount.isFinite()) {
        errors.add("Invalid amount value")
    }

    // Validate currency (server supports all standard ISO currencies)
    if (request.currency.isBlank()) {
        errors.add("Currency is required")
    } else if (!isValidCurrencyCode(request.currency)) {
        errors.add("Invalid currency code. Must be 3-letter ISO code (e.g., USD, EUR)")
    }

    return errors
}

/**
 * Validates currency code format (3-letter ISO code).
 * Server accepts any valid ISO currency code, not just the app's supported list.
 */
private fun isValidCurrencyCode(currencyCode: String): Boolean {
    // ISO 4217: 3 letters, case-insensitive
    return currencyCode.length == 3 && currencyCode.all { it.isLetter() }
}

/**
 * Validates email format.
 */
private fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return emailRegex.matches(email)
}

/**
 * Generates a unique transaction ID.
 */
private fun generateTransactionId(): String {
    return "TXN-${System.currentTimeMillis()}-${(1000..9999).random()}"
}
