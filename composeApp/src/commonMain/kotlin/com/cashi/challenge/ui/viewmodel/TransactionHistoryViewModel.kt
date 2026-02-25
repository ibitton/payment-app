package com.cashi.challenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashi.challenge.domain.models.Payment
import com.cashi.challenge.domain.usecases.GetTransactionHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the Transaction History screen.
 * Fetches and displays payment transactions from Firestore in real-time.
 */
class TransactionHistoryViewModel(
    private val getTransactionHistory: GetTransactionHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    /**
     * Loads transactions from Firestore and sets up real-time updates.
     */
    private fun loadTransactions() {
        getTransactionHistory()
            .onStart {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            .onEach { transactions ->
                _uiState.update {
                    it.copy(
                        transactions = transactions,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load transactions"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Refreshes the transaction list manually.
     */
    fun refresh() {
        loadTransactions()
    }

    /**
     * Clears any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for the Transaction History screen.
 */
data class TransactionHistoryUiState(
    val transactions: List<Payment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
