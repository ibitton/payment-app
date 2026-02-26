package com.cashi.challenge.data.mapper

import com.cashi.challenge.data.api.dto.PaymentRequest
import com.cashi.challenge.data.api.dto.PaymentResponse
import com.cashi.challenge.domain.models.Currency
import com.cashi.challenge.domain.models.Payment

/**
 * Mapper class for converting between data layer DTOs and domain layer models.
 * This ensures proper separation of concerns between layers.
 */
object PaymentMapper {

    /**
     * Converts a PaymentResponse and its original PaymentRequest to a Payment domain model.
     * Used when processing a successful API response.
     *
     * @param request The original payment request
     * @param response The response from the backend API
     * @return Payment domain model
     */
    fun toDomain(
        request: PaymentRequest,
        response: PaymentResponse
    ): Payment = Payment(
        id = response.id,
        recipientEmail = request.recipientEmail,
        amount = request.amount,
        currency = Currency.valueOf(request.currency.uppercase()),
        status = response.status,
        timestamp = response.timestamp,
        errorMessage = response.errorMessage
    )

    /**
     * Converts a Payment domain model to a PaymentRequest DTO.
     * Used when preparing a payment for API submission.
     *
     * @param payment The payment domain model
     * @return PaymentRequest DTO for API calls
     */
    fun toRequest(payment: Payment): PaymentRequest = PaymentRequest(
        recipientEmail = payment.recipientEmail,
        amount = payment.amount,
        currency = payment.currency.name
    )

    /**
     * Converts parameters to a PaymentRequest DTO.
     * Convenience method for UI layer to create requests.
     *
     * @param recipientEmail Email of the recipient
     * @param amount Payment amount
     * @param currency Currency code string
     * @return PaymentRequest DTO
     */
    fun toRequest(
        recipientEmail: String,
        amount: Double,
        currency: String
    ): PaymentRequest = PaymentRequest(
        recipientEmail = recipientEmail,
        amount = amount,
        currency = currency
    )
}
