package com.cashi.challenge.domain.usecases

import com.cashi.challenge.data.api.PaymentApi
import com.cashi.challenge.data.mapper.PaymentMapper
import com.cashi.challenge.data.repository.PaymentRepository
import com.cashi.challenge.domain.models.Payment
import com.cashi.challenge.data.api.dto.PaymentRequest
import com.cashi.challenge.domain.result.OperationResult
import com.cashi.challenge.domain.validation.PaymentValidationException
import com.cashi.challenge.domain.validation.PaymentValidator
import com.cashi.challenge.domain.validation.ValidationResult

/**
 * Use case for processing a payment.
 * Orchestrates validation, API call, and Firestore storage.
 *
 * @param paymentValidator Validates payment requests
 * @param paymentApi Client for backend API communication
 * @param paymentRepository Repository for Firestore operations
 */
class ProcessPaymentUseCase(
    private val paymentValidator: PaymentValidator,
    private val paymentApi: PaymentApi,
    private val paymentRepository: PaymentRepository
) {

    /**
     * Executes the payment processing flow:
     * 1. Validates the request
     * 2. Calls the backend API with idempotency key
     * 3. Saves the result to Firestore
     *
     * @param request The payment request to process
     * @param idempotencyKey A unique key generated per user action to prevent duplicate charges on retry
     * @return Result containing the Payment on success, or exception on failure
     */
    suspend operator fun invoke(
        request: PaymentRequest,
        idempotencyKey: String
    ): OperationResult<Payment> {
        val validationResult = paymentValidator.validate(request)
        if (validationResult is ValidationResult.Error) {
            return OperationResult.Failure(
                PaymentValidationException(validationResult.messages)
            )
        }

        return when (val apiResult = paymentApi.processPayment(request, idempotencyKey)) {
            is OperationResult.Success -> {
                val payment = PaymentMapper.toDomain(request, apiResult.data)
                paymentRepository.createAndSaveTransaction(payment)
            }

            is OperationResult.Failure -> OperationResult.Failure(apiResult.error)
        }
    }
}

