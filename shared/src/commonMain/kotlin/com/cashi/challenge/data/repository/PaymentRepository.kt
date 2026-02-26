package com.cashi.challenge.data.repository

import com.cashi.challenge.domain.models.Payment
import com.cashi.challenge.domain.models.PaymentRequest
import com.cashi.challenge.domain.models.PaymentResponse
import com.cashi.challenge.domain.result.OperationResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for managing payment transactions.
 * Defines the interface for payment storage operations, allowing different
 * implementations (Firestore, in-memory fakes for testing, etc.)
 */
interface PaymentRepository {
    /**
     * Saves a payment transaction to storage.
     *
     * @param payment The payment to save
     * @return OperationResult containing Unit on success or failure
     */
    suspend fun saveTransaction(payment: Payment): OperationResult<Unit>

    /**
     * Creates and saves a payment transaction from a request and response.
     * Convenience method for saving after backend processing.
     *
     * @param request The original payment request
     * @param response The response from the backend
     * @return OperationResult containing the saved Payment on success
     */
    suspend fun createAndSaveTransaction(
        request: PaymentRequest,
        response: PaymentResponse
    ): OperationResult<Payment>

    /**
     * Gets all transactions ordered by timestamp (newest first).
     *
     * @return Flow of list of payments that updates in real-time
     */
    fun getAllTransactions(): Flow<List<Payment>>

    /**
     * Gets transactions for a specific email recipient.
     *
     * @param email The recipient email to filter by
     * @return Flow of list of matching payments
     */
    fun getTransactionsByRecipient(email: String): Flow<List<Payment>>

    /**
     * Gets a single transaction by ID.
     *
     * @param transactionId The transaction ID to look up
     * @return OperationResult containing the Payment or null if not found
     */
    suspend fun getTransactionById(transactionId: String): OperationResult<Payment?>
}
