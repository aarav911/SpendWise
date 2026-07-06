package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.model.SavingsGoal
import com.example.ui.theme.CategoryStyles
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddEditExpenseDialog(
    expense: Expense? = null,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, date: Long, note: String) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = expense != null

    var amountText by remember { mutableStateOf(expense?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(expense?.category ?: "Food") }
    var selectedDateMillis by remember { mutableLongStateOf(expense?.date ?: System.currentTimeMillis()) }
    var noteText by remember { mutableStateOf(expense?.note ?: "") }

    var amountError by remember { mutableStateOf<String?>(null) }
    var noteError by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.US) }

    // Validation helper
    val validateInputs = {
        var isValid = true
        val amount = amountText.trim().toDoubleOrNull()
        if (amount == null) {
            amountError = "Please enter a valid number"
            isValid = false
        } else if (amount <= 0.0) {
            amountError = "Amount must be greater than 0"
            isValid = false
        } else if (amount > 1_000_000_000.0) {
            amountError = "Amount is too large"
            isValid = false
        } else {
            amountError = null
        }

        if (noteText.trim().length > 120) {
            noteError = "Note cannot exceed 120 characters"
            isValid = false
        } else {
            noteError = null
        }

        isValid
    }

    // Live validation check as amount changes
    LaunchedEffect(amountText) {
        if (amountText.isNotEmpty()) {
            val amount = amountText.toDoubleOrNull()
            amountError = when {
                amount == null -> "Please enter a valid number"
                amount <= 0.0 -> "Amount must be greater than 0"
                amount > 1_000_000_000.0 -> "Amount is too large"
                else -> null
            }
        } else {
            amountError = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Edit Expense" else "New Expense",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($)") },
                    placeholder = { Text("0.00") },
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Amount Icon"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                // Category Chips Selector
                Column {
                    Text(
                        text = "Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryStyles.list.forEach { style ->
                            val isSelected = selectedCategory.equals(style.name, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = style.name },
                                label = { Text(style.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = style.icon,
                                        contentDescription = style.name,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = style.color.copy(alpha = 0.25f),
                                    selectedLabelColor = style.color,
                                    selectedLeadingIconColor = style.color
                                ),
                                modifier = Modifier.testTag("category_chip_${style.name.lowercase()}")
                            )
                        }
                    }
                }

                // Date Picker trigger
                OutlinedTextField(
                    value = dateFormat.format(selectedDateMillis),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date Icon"
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = selectedDateMillis
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCal = Calendar.getInstance()
                                        newCal.set(year, month, dayOfMonth)
                                        selectedDateMillis = newCal.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select Date"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_date_input")
                )

                // Optional Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("E.g. Lunch with friends") },
                    isError = noteError != null,
                    supportingText = noteError?.let { { Text(it) } },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Note Icon"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validateInputs()) {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        onConfirm(amount, selectedCategory, selectedDateMillis, noteText)
                    }
                },
                modifier = Modifier.testTag("expense_save_button")
            ) {
                Text(if (isEditMode) "Save Changes" else "Add Expense")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("expense_cancel_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (budget: Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toString()) }
    var budgetError by remember { mutableStateOf<String?>(null) }

    val validateBudget = {
        val amount = budgetText.trim().toDoubleOrNull()
        if (amount == null) {
            budgetError = "Please enter a valid number"
            false
        } else if (amount < 0.0) {
            budgetError = "Budget cannot be negative"
            false
        } else if (amount > 1_000_000_000.0) {
            budgetError = "Budget is too large"
            false
        } else {
            budgetError = null
            true
        }
    }

    LaunchedEffect(budgetText) {
        if (budgetText.isNotEmpty()) {
            val amount = budgetText.toDoubleOrNull()
            budgetError = when {
                amount == null -> "Please enter a valid number"
                amount < 0.0 -> "Budget cannot be negative"
                amount > 1_000_000_000.0 -> "Budget is too large"
                else -> null
            }
        } else {
            budgetError = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Monthly Budget",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Set your global monthly budget limit. SpendWise will alert you when you exceed this limit.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Budget Limit ($)") },
                    placeholder = { Text("1000.00") },
                    isError = budgetError != null,
                    supportingText = budgetError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Budget Icon"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validateBudget()) {
                        val amount = budgetText.toDoubleOrNull() ?: 0.0
                        onConfirm(amount)
                    }
                },
                modifier = Modifier.testTag("budget_save_button")
            ) {
                Text("Save Limit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("budget_cancel_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onConfirm: (theme: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Appearance Theme",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Choose how SpendWise displays its sleek interface on your device.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val options = listOf(
                    Triple("light", "☀️ Light Mode", "A clean, crisp, and high-contrast theme"),
                    Triple("dark", "🌙 Dark Mode", "An eye-safe, midnight-slate visual palette"),
                    Triple("system", "⚙️ System Default", "Automatically sync with system settings")
                )

                options.forEach { (key, title, desc) ->
                    val isSelected = currentTheme == key
                    Surface(
                        onClick = { onConfirm(key) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("theme_option_$key")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("theme_close_button")
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EditCategoryBudgetsDialog(
    currentBudgets: Map<String, Double>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Double>) -> Unit
) {
    val categories = listOf("Food", "Transport", "Entertainment", "Bills", "Others")
    val budgetStates = remember {
        mutableStateMapOf<String, String>().apply {
            categories.forEach { category ->
                val v = currentBudgets[category]
                this[category] = if (v == null || v == 0.0) "" else v.toString()
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Category Limits",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Set optional monthly limits for categories to restrict and monitor your spending.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                categories.forEach { category ->
                    val style = CategoryStyles.getStyle(category)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = style.color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = style.icon,
                                    contentDescription = category,
                                    tint = style.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = category,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = budgetStates[category] ?: "",
                            onValueChange = { value ->
                                if (value.isEmpty() || value.toDoubleOrNull() != null) {
                                    budgetStates[category] = value
                                }
                            },
                            placeholder = { Text("No limit", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            modifier = Modifier
                                .width(110.dp)
                                .height(50.dp)
                                .testTag("cat_budget_input_$category")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = budgetStates.mapValues { (_, value) ->
                        value.toDoubleOrNull() ?: 0.0
                    }
                    onConfirm(result)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_cat_budgets_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_cat_budgets_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: Double) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Savings Goal",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Goal Name (e.g., Vacation)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_name_input")
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it
                        isError = it.toDoubleOrNull() == null || it.toDouble() <= 0
                    },
                    label = { Text("Target Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth().testTag("goal_target_input")
                )
                if (isError) {
                    Text(
                        text = "Please enter a valid positive number",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull()
                    if (nameText.isNotBlank() && target != null && target > 0) {
                        onConfirm(nameText, target)
                        onDismiss()
                    } else {
                        isError = true
                    }
                },
                enabled = nameText.isNotBlank() && !isError,
                modifier = Modifier.testTag("save_goal_button")
            ) {
                Text("Add Goal")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_goal_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun TransferSavingsDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Funds to ${goal.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Transfer leftover budget or savings directly into this goal.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        isError = it.toDoubleOrNull() == null || it.toDouble() <= 0
                    },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth().testTag("transfer_amount_input")
                )
                if (isError) {
                    Text(
                        text = "Please enter a valid positive amount",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
                        onDismiss()
                    } else {
                        isError = true
                    }
                },
                enabled = !isError,
                modifier = Modifier.testTag("confirm_transfer_button")
            ) {
                Text("Transfer")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_transfer_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

