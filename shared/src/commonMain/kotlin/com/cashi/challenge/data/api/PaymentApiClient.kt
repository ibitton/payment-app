package com.cashi.challenge.data.api

import com.cashi.challenge.data.api.dto.PaymentRequest
import com.cashi.challenge.data.api.dto.PaymentResponse
import com.cashi.challenge.domain.result.OperationResult
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
class PaymentApiClient(private val httpClient: HttpClient) : PaymentApi {

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
    override suspend fun processPayment(request: PaymentRequest, idempotencyKey: String): OperationResult<PaymentResponse> {
        return try {
val response = httpClient.post("$BASE_URL/payments") {
                contentType(ContentType.Application.Json)
                header("Idempotency-Key", idempotencyKey)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                OperationResult.Success(response.body())
            } else {
                val errorResponse = runCatching { response.body<PaymentResponse>() }.getOrNull()
                OperationResult.Failure(
                    PaymentApiException(
                        errorResponse?.errorMessage ?: "Payment failed with status: ${response.status}",
                        response.status.value
                    )
                )
            }
        } catch (e: ClientRequestException) {
            OperationResult.Failure(PaymentApiException("Client error: ${e.message}", e.response.status.value))
        } catch (e: ServerResponseException) {
            OperationResult.Failure(PaymentApiException("Server error: ${e.message}", e.response.status.value))
        } catch (e: Exception) {
            OperationResult.Failure(PaymentApiException("Network error: ${e.message}", -1))
        }
}
}

/**
 * Exception thrown when payment API calls fail.
 */
class PaymentApiException(message: String, val statusCode: Int) : Exception(message)
