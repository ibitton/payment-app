@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.cashi.challenge.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashi.challenge.domain.models.Currency
import com.cashi.challenge.domain.models.Payment
import com.cashi.challenge.domain.models.PaymentStatus
import com.cashi.challenge.ui.viewmodel.TransactionHistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Transaction History screen displaying all past payments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 28.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Text(
                            text = "↻",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 28.sp
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.transactions.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.transactions.isEmpty() -> {
                    EmptyTransactionsState()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.transactions) { transaction ->
                            TransactionCard(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying a single transaction.
 */
@Composable
private fun TransactionCard(transaction: Payment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.recipientEmail,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${getCurrencySymbol(transaction.currency)}${formatAmount(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = when (transaction.status) {
                        PaymentStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                        PaymentStatus.FAILED -> MaterialTheme.colorScheme.error
                        PaymentStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(status = transaction.status)
            }
        }
    }
}

/**
 * Displays a status chip for the transaction.
 */
@Composable
private fun StatusChip(status: PaymentStatus) {
    val color = when (status) {
        PaymentStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        PaymentStatus.FAILED -> MaterialTheme.colorScheme.error
        PaymentStatus.PENDING -> MaterialTheme.colorScheme.tertiary
    }

    val label = when (status) {
        PaymentStatus.SUCCESS -> "Success"
        PaymentStatus.FAILED -> "Failed"
        PaymentStatus.PENDING -> "Pending"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

/**
 * Empty state when no transactions exist.
 */
@Composable
private fun EmptyTransactionsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your payment history will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Formats a timestamp (epoch milliseconds) to a readable date string.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
private fun formatTimestamp(timestamp: Long): String {
    return formatInstant(kotlin.time.Instant.fromEpochMilliseconds(timestamp))
}

/**
 * Formats an amount with 2 decimal places.
 */
private fun formatAmount(amount: Double): String {
    return (kotlin.math.round(amount * 100) / 100).toString()
}

/**
 * Gets the currency symbol for display.
 */
private fun getCurrencySymbol(currency: Currency): String {
    return when (currency) {
        Currency.USD -> "$"
        Currency.EUR -> "€"
        Currency.GBP -> "£"
        Currency.JPY -> "¥"
        Currency.CAD -> "C$"
        Currency.AUD -> "A$"
        Currency.CHF -> "Fr"
        Currency.CNY -> "¥"
        Currency.INR -> "₹"
        Currency.SGD -> "S$"
        Currency.NZD -> "NZ$"
        Currency.SEK -> "kr"
        Currency.NOK -> "kr"
        Currency.DKK -> "kr"
        Currency.PLN -> "zł"
        Currency.MXN -> "$"
    }
}
