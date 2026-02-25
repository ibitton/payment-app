package com.cashi.challenge.domain.usecases

import com.cashi.challenge.data.repository.PaymentRepository
import com.cashi.challenge.domain.models.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving transaction history.
 *
 * @param paymentRepository Repository for accessing stored transactions
 */
class GetTransactionHistoryUseCase(
    private val paymentRepository: PaymentRepository
) {

    /**
     * Gets all transactions ordered by timestamp (newest first).
     *
     * @return Flow of transaction list that updates in real-time from Firestore
     */
    operator fun invoke(): Flow<List<Payment>> {
        return paymentRepository.getAllTransactions()
    }

    /**
     * Gets transactions for a specific recipient.
     *
     * @param email The recipient email to filter by
     * @return Flow of filtered transaction list
     */
    fun byRecipient(email: String): Flow<List<Payment>> {
        return paymentRepository.getTransactionsByRecipient(email)
    }
}
