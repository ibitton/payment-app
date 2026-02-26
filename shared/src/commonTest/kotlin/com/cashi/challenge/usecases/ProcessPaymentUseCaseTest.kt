package com.cashi.challenge.usecases

import com.cashi.challenge.domain.models.PaymentRequest
import com.cashi.challenge.domain.result.OperationResult
import com.cashi.challenge.domain.usecases.ProcessPaymentUseCase
import com.cashi.challenge.domain.validation.PaymentValidator
import com.cashi.challenge.test.FakePaymentApi
import com.cashi.challenge.test.FakePaymentRepository
import kotlin.test.Test
import kotlin.test.assertTrue

class ProcessPaymentUseCaseTest {

    @Test
    fun `process payment returns success for valid request`() = kotlinx.coroutines.runBlocking {
        val useCase = ProcessPaymentUseCase(
            paymentValidator = PaymentValidator(),
            paymentApi = FakePaymentApi(shouldFail = false),
            paymentRepository = FakePaymentRepository()
        )

        val result = useCase(
            request = PaymentRequest(
                recipientEmail = "user@example.com",
                amount = 100.0,
                currency = "USD"
            ),
            idempotencyKey = "test-key"
        )

        assertTrue(result is OperationResult.Success)
    }

    @Test
    fun `process payment returns failure for invalid request`() = kotlinx.coroutines.runBlocking {
        val useCase = ProcessPaymentUseCase(
            paymentValidator = PaymentValidator(),
            paymentApi = FakePaymentApi(shouldFail = false),
            paymentRepository = FakePaymentRepository()
        )

        val result = useCase(
            request = PaymentRequest(
                recipientEmail = "invalid-email",
                amount = 100.0,
                currency = "USD"
            ),
            idempotencyKey = "test-key"
        )

        assertTrue(result is OperationResult.Failure)
    }
}
