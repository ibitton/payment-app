package com.cashi.challenge.data.repository

import com.cashi.challenge.domain.models.Currency
import com.cashi.challenge.domain.models.Payment
import com.cashi.challenge.domain.models.PaymentStatus
import com.cashi.challenge.domain.result.OperationResult
import com.cashi.challenge.domain.result.map
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlin.time.Clock

/**
 * Firestore implementation of [PaymentRepository].
 * Handles saving transactions and querying transaction history from Firebase Firestore.
 *
 * @param firestore The Firebase Firestore instance
 */
class FirestorePaymentRepository(private val firestore: FirebaseFirestore) : PaymentRepository {

    companion object {
        private const val COLLECTION_TRANSACTIONS = "transactions"
    }

    /**
     * Saves a payment transaction to Firestore.
     * This is called after receiving a response from the backend API.
     * Stores timestamp as Firebase Timestamp type for proper querying.
     *
     * @param payment The payment to save
     * @return OperationResult containing Unit on success or failure
     */
    override suspend fun saveTransaction(payment: Payment): OperationResult<Unit> {
        return try {
            firestore.collection(COLLECTION_TRANSACTIONS)
                .document(payment.id)
                .set(payment.toFirestoreMap())
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Failure(e)
        }
    }

    /**
     * Saves a payment transaction and returns the saved payment.
     * Convenience method for saving after backend processing.
     *
     * @param payment The payment domain model to save
     * @return OperationResult containing the saved Payment on success
     */
    override suspend fun createAndSaveTransaction(payment: Payment): OperationResult<Payment> {
        val paymentWithId = if (payment.id.isEmpty()) {
            payment.copy(id = generateTransactionId())
        } else {
            payment
        }
        return saveTransaction(paymentWithId).map { paymentWithId }
    }

    /**
     * Gets all transactions ordered by timestamp (newest first).
     *
     * @return Flow of list of payments that updates in real-time
     */
    override fun getAllTransactions(): Flow<List<Payment>> {
        return firestore.collection(COLLECTION_TRANSACTIONS)
            .orderBy("timestamp", dev.gitlive.firebase.firestore.Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        document.toPayment()
                    } catch (e: Exception) {
                        println("DEBUG: Failed to parse document ${document.id}: ${e.message}")
                        null // Skip documents that can't be deserialized
                    }
                }
            }
    }

    /**
     * Gets transactions for a specific email recipient.
     *
     * @param email The recipient email to filter by
     * @return Flow of list of matching payments
     */
    override fun getTransactionsByRecipient(email: String): Flow<List<Payment>> {
        return firestore.collection(COLLECTION_TRANSACTIONS)
            .where { "recipientEmail" equalTo email }
            .orderBy("timestamp", dev.gitlive.firebase.firestore.Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        document.toPayment()
                    } catch (e: Exception) {
                        println("DEBUG: Failed to parse document ${document.id}: ${e.message}")
                        null
                    }
                }
            }
    }

    /**
     * Gets a single transaction by ID.
     *
     * @param transactionId The transaction ID to look up
     * @return OperationResult containing the Payment or null if not found
     */
    override suspend fun getTransactionById(transactionId: String): OperationResult<Payment?> {
        return try {
            val document = firestore.collection(COLLECTION_TRANSACTIONS)
                .document(transactionId)
                .get()

            if (document.exists) {
                OperationResult.Success(document.toPayment())
            } else {
                OperationResult.Success(null)
            }
        } catch (e: Exception) {
            OperationResult.Failure(e)
        }
    }

    /**
     * Generates a unique transaction ID for local use.
     * Note: Uses a simple timestamp + random approach. In production, consider using UUID.
     */
    private fun generateTransactionId(): String {
        // Using System.currentTimeMillis() equivalent that's available in KMP
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val randomSuffix = (1000..9999).random()
        return "TXN-$timestamp-$randomSuffix"
    }

    /**
     * Converts a Payment domain model to a Firestore-compatible map.
     * Stores timestamp as a Firebase Timestamp for proper type support.
     */
    private fun Payment.toFirestoreMap(): Map<String, Any?> {
        val seconds = timestamp / 1000
        val nanoseconds = ((timestamp % 1000) * 1_000_000).toInt()
        return mapOf(
            "id" to id,
            "recipientEmail" to recipientEmail,
            "amount" to amount,
            "currency" to currency.name,
            "status" to status.name,
            "timestamp" to Timestamp(seconds, nanoseconds),
            "errorMessage" to errorMessage
        )
    }

    /**
     * Converts a Firestore DocumentSnapshot to a Payment domain model.
     * Uses a DTO with proper Timestamp support.
     */
    private fun DocumentSnapshot.toPayment(): Payment {
        // Use the data class deserialization which should handle Timestamp properly
        val dto = data<FirestorePaymentDto>()
        return dto.toPayment()
    }
}

/**
 * DTO for Firestore serialization with proper Timestamp support.
 */
@Serializable
data class FirestorePaymentDto(
    val id: String = "",
    val recipientEmail: String = "",
    val amount: Double = 0.0,
    val currency: String = "USD",
    val status: String = "PENDING",
    val timestamp: Timestamp? = null,
    val errorMessage: String? = null
) {
    fun toPayment(): Payment {
        val epochMillis = timestamp?.let { 
            it.seconds * 1000 + it.nanoseconds / 1_000_000 
        } ?: Clock.System.now().toEpochMilliseconds()

        return Payment(
            id = id,
            recipientEmail = recipientEmail,
            amount = amount,
            currency = try {
                Currency.valueOf(currency)
            } catch (_: IllegalArgumentException) {
                Currency.USD
            },
            status = try {
                PaymentStatus.valueOf(status)
            } catch (_: IllegalArgumentException) {
                PaymentStatus.PENDING
            },
            timestamp = epochMillis,
            errorMessage = errorMessage
        )
    }
}
