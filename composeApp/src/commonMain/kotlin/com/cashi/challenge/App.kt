package com.cashi.challenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cashi.challenge.ui.screens.PaymentScreen
import com.cashi.challenge.ui.screens.TransactionHistoryScreen

/**
 * Main app entry point.
 * Handles simple navigation between Payment and Transaction History screens.
 */
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Payment) }

    when (currentScreen) {
        Screen.Payment -> {
            PaymentScreen(
                onNavigateToHistory = { currentScreen = Screen.History }
            )
        }
        Screen.History -> {
            TransactionHistoryScreen(
                onNavigateBack = { currentScreen = Screen.Payment }
            )
        }
    }
}

/**
 * Simple sealed class for navigation screens.
 */
private sealed class Screen {
    data object Payment : Screen()
    data object History : Screen()
}