package com.cashi.challenge.data.api

import com.cashi.challenge.domain.models.PaymentRequest
import com.cashi.challenge.domain.models.PaymentResponse
import com.cashi.challenge.domain.result.OperationResult

/**
 * Contract for payment API communication.
 * Allows mocking and test fakes without binding to a concrete HTTP client.
 */
interface PaymentApi {
    /**
     * Sends a payment request to the backend for processing.
     *
     * @param request The payment request containing recipient, amount, and currency
     * @param idempotencyKey A unique key generated per user action (not per API call).
     */
    suspend fun processPayment(request: PaymentRequest, idempotencyKey: String): OperationResult<PaymentResponse>
}
