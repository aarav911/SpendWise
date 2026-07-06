package com.example.data.repository

import com.example.data.local.AssetDao
import com.example.data.local.BudgetPreferences
import com.example.data.local.ExpenseDao
import com.example.data.local.SavingsGoalDao
import com.example.data.model.Asset
import com.example.data.model.AssetHistory
import com.example.data.model.Expense
import com.example.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class FinanceRepository(
    private val expenseDao: ExpenseDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val assetDao: AssetDao,
    private val budgetPrefs: BudgetPreferences
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()
    val allAssets: Flow<List<Asset>> = assetDao.getAllAssets()
    val allAssetHistories: Flow<List<AssetHistory>> = assetDao.getAllAssetHistories()
    val monthlyBudget: StateFlow<Double> = budgetPrefs.budgetFlow
    val categoryBudgets: StateFlow<Map<String, Double>> = budgetPrefs.categoryBudgetsFlow
    val themeMode: StateFlow<String> = budgetPrefs.themeFlow

    suspend fun getAssetByNameAndCategory(name: String, category: String): Asset? {
        return assetDao.getAssetByNameAndCategory(name, category)
    }

    suspend fun getAssetById(id: Int): Asset? {
        return assetDao.getAssetById(id)
    }

    suspend fun insertAsset(asset: Asset): Long {
        return assetDao.insertAsset(asset)
    }

    suspend fun updateAsset(asset: Asset) {
        assetDao.updateAsset(asset)
    }

    suspend fun deleteAsset(asset: Asset) {
        assetDao.deleteAsset(asset)
    }

    suspend fun insertAssetHistory(history: AssetHistory): Long {
        return assetDao.insertAssetHistory(history)
    }

    fun getHistoryForAsset(assetId: Int): Flow<List<AssetHistory>> {
        return assetDao.getHistoryForAsset(assetId)
    }

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun insertGoal(goal: SavingsGoal) {
        savingsGoalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: SavingsGoal) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoal) {
        savingsGoalDao.deleteGoal(goal)
    }

    fun setMonthlyBudget(budget: Double) {
        budgetPrefs.setBudget(budget)
    }

    fun setCategoryBudget(category: String, budget: Double) {
        budgetPrefs.setCategoryBudget(category, budget)
    }

    fun setThemeMode(theme: String) {
        budgetPrefs.setTheme(theme)
    }
}
