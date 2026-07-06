package com.example.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BudgetPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("spendwise_prefs", Context.MODE_PRIVATE)

    private val _budgetFlow = MutableStateFlow(getBudget())
    val budgetFlow: StateFlow<Double> = _budgetFlow

    private val _themeFlow = MutableStateFlow(getTheme())
    val themeFlow: StateFlow<String> = _themeFlow

    private val _categoryBudgetsFlow = MutableStateFlow(getAllCategoryBudgets())
    val categoryBudgetsFlow: StateFlow<Map<String, Double>> = _categoryBudgetsFlow

    fun getBudget(): Double {
        return prefs.getFloat("monthly_budget", 1000.0f).toDouble()
    }

    fun setBudget(budget: Double) {
        prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
        _budgetFlow.value = budget
    }

    fun getAllCategoryBudgets(): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        listOf("Food", "Transport", "Entertainment", "Bills", "Others").forEach { category ->
            val b = prefs.getFloat("category_budget_$category", 0.0f).toDouble()
            if (b > 0.0) {
                map[category] = b
            }
        }
        return map
    }

    fun setCategoryBudget(category: String, budget: Double) {
        prefs.edit().putFloat("category_budget_$category", budget.toFloat()).apply()
        _categoryBudgetsFlow.value = getAllCategoryBudgets()
    }

    fun getTheme(): String {
        return prefs.getString("theme_mode", "system") ?: "system"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString("theme_mode", theme).apply()
        _themeFlow.value = theme
    }
}
