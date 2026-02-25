package com.cashi.challenge.data.api

import com.cashi.challenge.domain.models.PaymentRequest
import com.cashi.challenge.domain.models.PaymentResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * API client for communicating with the payment backend.
 *
 * @param httpClient The configured Ktor HTTP client
 */
class PaymentApiClient(private val httpClient: HttpClient) {

    companion object {
        // Use 10.0.2.2 for Android emulator to reach host localhost
        // For physical device or iOS, use actual server IP
        const val BASE_URL = "http://10.0.2.2:8080"
    }

    /**
     * Sends a payment request to the backend for processing.
     *
     * @param request The payment request containing recipient, amount, and currency
     * @param idempotencyKey A unique key generated per user action (not per API call).
     *   Reusing the same key on retry tells the server this is a duplicate attempt,
     *   preventing double charges even if the original response was lost.
     * @return Result containing PaymentResponse on success or exception on failure
     */
    suspend fun processPayment(request: PaymentRequest, idempotencyKey: String): Result<PaymentResponse> {
        return try {
            val response = httpClient.post("$BASE_URL/payments") {
                contentType(ContentType.Application.Json)
                header("Idempotency-Key", idempotencyKey)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorResponse = runCatching { response.body<PaymentResponse>() }.getOrNull()
                Result.failure(
                    PaymentApiException(
                        errorResponse?.errorMessage ?: "Payment failed with status: ${response.status}",
                        response.status.value
                    )
                )
            }
        } catch (e: ClientRequestException) {
            Result.failure(PaymentApiException("Client error: ${e.message}", e.response.status.value))
        } catch (e: ServerResponseException) {
            Result.failure(PaymentApiException("Server error: ${e.message}", e.response.status.value))
        } catch (e: Exception) {
            Result.failure(PaymentApiException("Network error: ${e.message}", -1))
        }
    }
}

/**
 * Exception thrown when payment API calls fail.
 */
class PaymentApiException(message: String, val statusCode: Int) : Exception(message)
