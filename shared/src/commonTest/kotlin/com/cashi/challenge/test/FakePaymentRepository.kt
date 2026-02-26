package com.cashi.challenge.test

import com.cashi.challenge.data.repository.PaymentRepository
import com.cashi.challenge.domain.models.Payment
import com.cashi.challenge.domain.result.OperationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fake implementation of PaymentRepository for testing.
 * Uses in-memory storage instead of actual Firestore.
 */
class FakePaymentRepository : PaymentRepository {
    private val transactions = MutableStateFlow<List<Payment>>(emptyList())

    override suspend fun saveTransaction(payment: Payment): OperationResult<Unit> {
        transactions.update { current -> listOf(payment) + current }
        return OperationResult.Success(Unit)
    }

    override suspend fun createAndSaveTransaction(payment: Payment): OperationResult<Payment> {
        val paymentWithId = if (payment.id.isEmpty()) {
            payment.copy(id = "TXN-TEST-${transactions.value.size}")
        } else {
            payment
        }
        return saveTransaction(paymentWithId).let { OperationResult.Success(paymentWithId) }
    }

    override fun getAllTransactions(): Flow<List<Payment>> = transactions

    override fun getTransactionsByRecipient(email: String): Flow<List<Payment>> = transactions

    override suspend fun getTransactionById(transactionId: String): OperationResult<Payment?> {
        val payment = transactions.value.firstOrNull { it.id == transactionId }
        return OperationResult.Success(payment)
    }
}
