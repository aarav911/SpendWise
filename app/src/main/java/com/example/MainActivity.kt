package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.local.BudgetPreferences
import com.example.data.local.SpendWiseDatabase
import com.example.data.repository.FinanceRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Local Persistence Initialization
        val database = SpendWiseDatabase.getDatabase(applicationContext)
        val budgetPrefs = BudgetPreferences(applicationContext)
        val repository = FinanceRepository(database.expenseDao(), database.savingsGoalDao(), database.assetDao(), budgetPrefs)
        
        // Factory-driven ViewModel Instantiation (handles rotation/configuration changes)
        val viewModel: FinanceViewModel by viewModels {
            FinanceViewModel.Factory(repository)
        }

        setContent {
            val themeMode by viewModel.themeState.collectAsState()
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                var currentScreen by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf("dashboard")
                }

                androidx.compose.animation.Crossfade(
                    targetState = currentScreen,
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        "dashboard" -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToAnalytics = { currentScreen = "analytics" },
                                onNavigateToInvestments = { currentScreen = "investments" },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        "analytics" -> {
                            com.example.ui.screens.AnalyticsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = "dashboard" },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        "investments" -> {
                            com.example.ui.screens.InvestmentsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = "dashboard" },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
