package com.cashi.challenge.test

import com.cashi.challenge.data.repository.PaymentRepository
import com.cashi.challenge.domain.models.Payment
import com.cashi.challenge.domain.models.PaymentRequest
import com.cashi.challenge.domain.models.PaymentResponse
import com.cashi.challenge.domain.result.OperationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakePaymentRepository : PaymentRepository {
    private val transactions = MutableStateFlow<List<Payment>>(emptyList())

    override suspend fun saveTransaction(payment: Payment): OperationResult<Unit> {
        transactions.update { current -> listOf(payment) + current }
        return OperationResult.Success(Unit)
    }

    override suspend fun createAndSaveTransaction(
        request: PaymentRequest,
        response: PaymentResponse
    ): OperationResult<Payment> {
        val payment = Payment(
            id = response.id,
            recipientEmail = request.recipientEmail,
            amount = request.amount,
            currency = com.cashi.challenge.domain.models.Currency.valueOf(request.currency.uppercase()),
            status = response.status,
            timestamp = response.timestamp,
            errorMessage = response.errorMessage
        )
        return saveTransaction(payment).let { OperationResult.Success(payment) }
    }

    override fun getAllTransactions(): Flow<List<Payment>> = transactions

    override fun getTransactionsByRecipient(email: String): Flow<List<Payment>> = transactions

    override suspend fun getTransactionById(transactionId: String): OperationResult<Payment?> {
        val payment = transactions.value.firstOrNull { it.id == transactionId }
        return OperationResult.Success(payment)
    }
}
