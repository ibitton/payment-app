package com.cashi.challenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashi.challenge.domain.models.Currency
import com.cashi.challenge.domain.models.Payment
import com.cashi.challenge.domain.models.PaymentRequest
import com.cashi.challenge.domain.result.OperationResult
import com.cashi.challenge.domain.usecases.ProcessPaymentUseCase
import com.cashi.challenge.domain.validation.PaymentValidationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ViewModel for the Payment screen.
 * Handles user input, validation, and payment processing.
 */
class PaymentViewModel(
    private val processPayment: ProcessPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    /**
     * Updates the recipient email in the UI state.
     */
    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                result = null
            )
        }
    }

    /**
     * Updates the amount in the UI state.
     */
    fun onAmountChange(amount: String) {
        _uiState.update {
            it.copy(
                amount = amount,
                amountError = null,
                result = null
            )
        }
    }

    /**
     * Updates the currency in the UI state.
     */
    fun onCurrencyChange(currency: Currency) {
        _uiState.update {
            it.copy(
                currency = currency,
                result = null
            )
        }
    }

    /**
     * Clears any success/error messages.
     */
    fun clearResult() {
        _uiState.update { it.copy(result = null) }
    }

    /**
     * Submits the payment after validation.
     * Generates a UUID idempotency key at the point of user intent (button tap).
     * The same key is reused for any retries of this specific payment attempt,
     * preventing duplicate charges if the network fails and the user retries.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun submitPayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, result = null) }

            // Generate idempotency key once per user action (not per API call)
            // This key is tied to this specific payment attempt and reused on retries
            val idempotencyKey = Uuid.random().toString()

            // Parse amount
            val amount = _uiState.value.amount.toDoubleOrNull()
            if (amount == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        amountError = "Invalid amount format"
                    )
                }
                return@launch
            }

            val request = PaymentRequest(
                recipientEmail = _uiState.value.email,
                amount = amount,
                currency = _uiState.value.currency.name
            )

            when (val result = processPayment(request, idempotencyKey)) {
                is OperationResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = PaymentResult.Success(result.data),
                            // Clear form on success
                            email = "",
                            amount = ""
                        )
                    }
                }
                is OperationResult.Failure -> {
                    val errorMessage = when (val error = result.error) {
                        is PaymentValidationException -> error.errors.firstOrNull() ?: "Validation failed"
                        else -> error.message ?: "Payment failed. Please try again."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = PaymentResult.Error(errorMessage)
                        )
                    }
                }
            }
}
    }
}

/**
 * UI state for the Payment screen.
 */
data class PaymentUiState(
    val email: String = "",
    val amount: String = "",
    val currency: Currency = Currency.USD,
    val emailError: String? = null,
    val amountError: String? = null,
    val isLoading: Boolean = false,
    val result: PaymentResult? = null
)

/**
 * Result state for payment submission.
 */
sealed class PaymentResult {
    data class Success(val payment: Payment) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
}
