package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Expense
import com.example.data.model.SavingsGoal
import com.example.data.model.Asset
import com.example.data.model.AssetHistory
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    val expensesState: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val budgetState: StateFlow<Double> = repository.monthlyBudget
    val categoryBudgetsState: StateFlow<Map<String, Double>> = repository.categoryBudgets
    val themeState: StateFlow<String> = repository.themeMode

    val savingsGoalsState: StateFlow<List<SavingsGoal>> = repository.allGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val assetsState: StateFlow<List<Asset>> = repository.allAssets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val assetHistoriesState: StateFlow<List<AssetHistory>> = repository.allAssetHistories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reconstruct portfolio valuation history off the main thread
    val portfolioTimelineState: StateFlow<List<Pair<Long, Double>>> = combine(
        repository.allAssets,
        repository.allAssetHistories
    ) { assets, histories ->
        if (histories.isEmpty()) {
            emptyList()
        } else {
            val sortedHistories = histories.sortedBy { it.timestamp }
            val timeline = mutableListOf<Pair<Long, Double>>()
            val lastKnownState = mutableMapOf<Int, Pair<Double, Double>>() // assetId -> (price, quantity)
            
            sortedHistories.forEach { history ->
                lastKnownState[history.assetId] = Pair(history.price, history.quantity)
                val totalValuation = lastKnownState.values.sumOf { (price, qty) -> price * qty }
                timeline.add(Pair(history.timestamp, totalValuation))
            }
            timeline
        }
    }.flowOn(Dispatchers.Default)
     .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = emptyList()
     )

    // Calculate total assets bought in the current calendar month off the main thread
    val monthlyInvestmentTotalState: StateFlow<Double> = combine(
        repository.allAssets,
        repository.allAssetHistories
    ) { assets, histories ->
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)
        var totalInvestedThisMonth = 0.0
        
        val grouped = histories.groupBy { it.assetId }
        grouped.forEach { (assetId, assetHistories) ->
            val sorted = assetHistories.sortedBy { it.timestamp }
            var prevQty = 0.0
            sorted.forEach { history ->
                val hCal = Calendar.getInstance().apply { timeInMillis = history.timestamp }
                val isThisMonth = hCal.get(Calendar.YEAR) == currentYear && hCal.get(Calendar.MONTH) == currentMonth
                
                if (isThisMonth) {
                    if (history.quantity > prevQty) {
                        val addedQty = history.quantity - prevQty
                        totalInvestedThisMonth += addedQty * history.price
                    }
                }
                prevQty = history.quantity
            }
        }
        totalInvestedThisMonth
    }.flowOn(Dispatchers.Default)
     .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = 0.0
     )

    // Derived State: Total Spending in the current calendar month
    val monthlySpendingState: StateFlow<Double> = expensesState.map { list ->
        list.filter { isCurrentMonth(it.date) }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Derived State: Category Breakdown for the current calendar month
    val categoryBreakdownState: StateFlow<Map<String, Double>> = expensesState.map { list ->
        list.filter { isCurrentMonth(it.date) }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Derived State: "Safe-to-Spend" daily calculator
    val safeToSpendTodayState: StateFlow<Double> = combine(budgetState, monthlySpendingState) { budget, spent ->
        val remaining = budget - spent
        val cal = Calendar.getInstance()
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val remainingDays = (maxDays - currentDay + 1).coerceAtLeast(1)
        (remaining / remainingDays).coerceAtLeast(0.0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addExpense(amount: Double, category: String, date: Long, note: String) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = category,
                    date = date,
                    note = note.trim()
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updateMonthlyBudget(budget: Double) {
        repository.setMonthlyBudget(budget)
    }

    fun updateCategoryBudget(category: String, budget: Double) {
        repository.setCategoryBudget(category, budget)
    }

    fun addSavingsGoal(name: String, targetAmount: Double) {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoal(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    isCompleted = false
                )
            )
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun transferToSavingsGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val newAmount = goal.currentAmount + amount
            repository.updateGoal(
                goal.copy(
                    currentAmount = newAmount,
                    isCompleted = newAmount >= goal.targetAmount
                )
            )
        }
    }

    fun updateThemeMode(theme: String) {
        repository.setThemeMode(theme)
    }

    fun buyAsset(name: String, category: String, quantity: Double, pricePerUnit: Double) {
        viewModelScope.launch {
            val trimmedName = name.trim().take(50)
            if (trimmedName.isEmpty() || quantity <= 0.0 || pricePerUnit <= 0.0) return@launch
            
            val existing = repository.getAssetByNameAndCategory(trimmedName, category)
            val now = System.currentTimeMillis()
            if (existing != null) {
                val newQty = existing.quantity + quantity
                val newCostBasis = existing.costBasis + (quantity * pricePerUnit)
                val updated = existing.copy(
                    quantity = newQty,
                    costBasis = newCostBasis,
                    currentPrice = pricePerUnit
                )
                repository.updateAsset(updated)
                repository.insertAssetHistory(
                    AssetHistory(
                        assetId = existing.id,
                        price = pricePerUnit,
                        quantity = newQty,
                        timestamp = now
                    )
                )
            } else {
                val newAsset = Asset(
                    name = trimmedName,
                    category = category,
                    quantity = quantity,
                    costBasis = quantity * pricePerUnit,
                    currentPrice = pricePerUnit
                )
                val newId = repository.insertAsset(newAsset).toInt()
                repository.insertAssetHistory(
                    AssetHistory(
                        assetId = newId,
                        price = pricePerUnit,
                        quantity = quantity,
                        timestamp = now
                    )
                )
            }
        }
    }

    suspend fun sellAsset(name: String, category: String, quantity: Double, pricePerUnit: Double): Boolean {
        return withContext(Dispatchers.IO) {
            val trimmedName = name.trim().take(50)
            if (trimmedName.isEmpty() || quantity <= 0.0 || pricePerUnit <= 0.0) return@withContext false
            
            val existing = repository.getAssetByNameAndCategory(trimmedName, category) ?: return@withContext false
            if (existing.quantity < quantity) {
                return@withContext false // Input Safeguard Lock (INV-004)
            }
            
            val newQty = existing.quantity - quantity
            val newCostBasis = if (newQty > 0.0) {
                existing.costBasis * (newQty / existing.quantity)
            } else {
                0.0
            }
            
            val updated = existing.copy(
                quantity = newQty,
                costBasis = newCostBasis,
                currentPrice = pricePerUnit
            )
            repository.updateAsset(updated)
            repository.insertAssetHistory(
                AssetHistory(
                    assetId = existing.id,
                    price = pricePerUnit,
                    quantity = newQty,
                    timestamp = System.currentTimeMillis()
                )
            )
            true
        }
    }

    fun updateAssetPrice(assetId: Int, newPricePerUnit: Double) {
        viewModelScope.launch {
            if (newPricePerUnit <= 0.0) return@launch
            val existing = repository.getAssetById(assetId) ?: return@launch
            val updated = existing.copy(currentPrice = newPricePerUnit)
            repository.updateAsset(updated)
            repository.insertAssetHistory(
                AssetHistory(
                    assetId = existing.id,
                    price = newPricePerUnit,
                    quantity = existing.quantity,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun isCurrentMonth(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        val itemCal = Calendar.getInstance()
        itemCal.timeInMillis = timestamp
        return itemCal.get(Calendar.YEAR) == currentYear && itemCal.get(Calendar.MONTH) == currentMonth
    }

    // Factory companion for standard ViewModel instantiation
    class Factory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                return FinanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
