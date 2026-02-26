package com.cashi.challenge.test

import com.cashi.challenge.data.api.PaymentApi
import com.cashi.challenge.domain.models.PaymentRequest
import com.cashi.challenge.domain.models.PaymentResponse
import com.cashi.challenge.domain.models.PaymentStatus
import com.cashi.challenge.domain.result.OperationResult

class FakePaymentApi(
    private val shouldFail: Boolean = false
) : PaymentApi {
    override suspend fun processPayment(request: PaymentRequest, idempotencyKey: String): OperationResult<PaymentResponse> {
        return if (shouldFail) {
            OperationResult.Failure(IllegalStateException("Simulated API failure"))
        } else {
            OperationResult.Success(
                PaymentResponse(
                    id = "TXN-TEST-123",
                    status = PaymentStatus.SUCCESS,
                    timestamp = 1234567890L,
                    errorMessage = null
                )
            )
        }
    }
}
