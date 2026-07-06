package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.components.CategoryBudgetTracker
import com.example.ui.components.EditCategoryBudgetsDialog
import com.example.ui.components.AddSavingsGoalDialog
import com.example.ui.components.TransferSavingsDialog
import com.example.ui.components.SavingsGoalsTracker
import com.example.ui.components.CelebrationConfetti
import com.example.ui.components.SafeToSpendCard
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.ui.components.AddEditExpenseDialog
import com.example.ui.components.BudgetProgressBar
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.theme.CategoryStyles
import com.example.ui.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expensesState.collectAsState()
    val budget by viewModel.budgetState.collectAsState()
    val categoryBudgets by viewModel.categoryBudgetsState.collectAsState()
    val savingsGoals by viewModel.savingsGoalsState.collectAsState()
    val monthlySpending by viewModel.monthlySpendingState.collectAsState()
    val categoryAmounts by viewModel.categoryBreakdownState.collectAsState()
    val safeToSpend by viewModel.safeToSpendTodayState.collectAsState()
    val themeMode by viewModel.themeState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCategoryBudgetsDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalForTransfer by remember { mutableStateOf<com.example.data.model.SavingsGoal?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var triggerConfetti by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val currentMonthName = remember { SimpleDateFormat("MMMM yyyy", Locale.US).format(Date()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("add_expense_fab")
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WELCOME BACK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SpendWise",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToInvestments,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .testTag("wealth_nav_trigger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wealth Portfolio",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToAnalytics,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .testTag("analytics_nav_trigger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Advanced Insights",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Circular Profile Avatar Clickable Theme Selection
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .clickable { showThemeDialog = true }
                                .padding(2.dp)
                                .testTag("theme_selector_trigger"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SW",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Budget Tracker Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = "Payments Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentMonthName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { showBudgetDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("edit_budget_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Budget",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        BudgetProgressBar(
                            spent = monthlySpending,
                            budget = budget
                        )
                    }
                }
            }

            // Safe-to-Spend Card
            item {
                SafeToSpendCard(
                    allowance = safeToSpend,
                    remainingBudget = budget - monthlySpending
                )
            }

            // Budget Surplus Bridge Card (INV-003)
            val surplus = budget - monthlySpending
            if (surplus > 0.0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToInvestments() }
                            .testTag("budget_surplus_bridge_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet Icon",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Positive Budget Surplus!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "You are currently ${currencyFormat.format(surplus)} under budget this month. Protect your savings by investing your surplus into your portfolio!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Navigate to Investments",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Analytics / Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Analytics Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Category Breakdown",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        CategoryDonutChart(
                            categoryAmounts = categoryAmounts
                        )
                    }
                }
            }

            // Category-Specific Spending Limits
            item {
                CategoryBudgetTracker(
                    spentMap = categoryAmounts,
                    budgetMap = categoryBudgets,
                    onEditClick = { showCategoryBudgetsDialog = true }
                )
            }

            // Gamified Savings Goals and Wallets
            item {
                SavingsGoalsTracker(
                    goals = savingsGoals,
                    onAddGoalClick = { showAddGoalDialog = true },
                    onTransferClick = { goalForTransfer = it },
                    onDeleteClick = { viewModel.deleteSavingsGoal(it) }
                )
            }

            // Expense List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${expenses.size} total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expense List Items or Empty State
            if (expenses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = "No Expenses Icon",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Transactions Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the '+' button below to log your first expense.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 32.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                items(
                    items = expenses,
                    key = { it.id }
                ) { expense ->
                    val style = CategoryStyles.getStyle(expense.category)
                    
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.animateItem()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_card_${expense.id}")
                                .combinedClickable(
                                    onClick = { expenseToEdit = expense },
                                    onLongClick = { expenseToEdit = expense }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Icon Indicator
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            color = style.color.copy(alpha = 0.12f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = style.icon,
                                        contentDescription = expense.category,
                                        tint = style.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Expense details (Note and Date)
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = expense.note.ifEmpty { expense.category },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${expense.category} • ${dateFormatter.format(Date(expense.date))}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }

                                // Amount details
                                Text(
                                    text = currencyFormat.format(expense.amount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Explicit Delete Button
                                IconButton(
                                    onClick = { viewModel.deleteExpense(expense) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("delete_expense_button_${expense.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete transaction",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // Elegant footer padding spacing
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        // Dialog managers
        if (showAddDialog) {
            AddEditExpenseDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { amount, category, date, note ->
                    viewModel.addExpense(amount, category, date, note)
                    showAddDialog = false
                }
            )
        }

        expenseToEdit?.let { expense ->
            AddEditExpenseDialog(
                expense = expense,
                onDismiss = { expenseToEdit = null },
                onConfirm = { amount, category, date, note ->
                    viewModel.updateExpense(
                        expense.copy(
                            amount = amount,
                            category = category,
                            date = date,
                            note = note
                        )
                    )
                    expenseToEdit = null
                }
            )
        }

        if (showBudgetDialog) {
            EditBudgetDialog(
                currentBudget = budget,
                onDismiss = { showBudgetDialog = false },
                onConfirm = { newBudget ->
                    viewModel.updateMonthlyBudget(newBudget)
                    showBudgetDialog = false
                }
            )
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = themeMode,
                onDismiss = { showThemeDialog = false },
                onConfirm = { selectedTheme ->
                    viewModel.updateThemeMode(selectedTheme)
                }
            )
        }

        if (showCategoryBudgetsDialog) {
            EditCategoryBudgetsDialog(
                currentBudgets = categoryBudgets,
                onDismiss = { showCategoryBudgetsDialog = false },
                onConfirm = { updatedBudgets ->
                    updatedBudgets.forEach { (cat, b) ->
                        viewModel.updateCategoryBudget(cat, b)
                    }
                    showCategoryBudgetsDialog = false
                }
            )
        }

        if (showAddGoalDialog) {
            AddSavingsGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { name, target ->
                    viewModel.addSavingsGoal(name, target)
                    showAddGoalDialog = false
                }
            )
        }

        goalForTransfer?.let { goal ->
            TransferSavingsDialog(
                goal = goal,
                onDismiss = { goalForTransfer = null },
                onConfirm = { amount ->
                    viewModel.transferToSavingsGoal(goal, amount)
                    if (goal.currentAmount + amount >= goal.targetAmount) {
                        triggerConfetti = true
                    }
                    goalForTransfer = null
                }
            )
        }

        if (triggerConfetti) {
            CelebrationConfetti(
                onFinished = { triggerConfetti = false }
            )
        }
    }
}
