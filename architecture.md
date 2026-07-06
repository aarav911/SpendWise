# SpendWise Architecture Documentation

SpendWise is a fully native, offline-first personal finance application for Android. It is engineered with strict adherence to modern Android development practices, ensuring separation of concerns, high-performance data processing, and seamless UX across configuration changes.

---

## 1. Architectural Pattern: MVVM (Model-View-ViewModel)

The application follows the recommended **Unidirectional Data Flow (UDF)** model using Android's MVVM architecture.

```
       +---------------------------------------------+
       |                   UI LAYER                  |
       |  (Jetpack Compose Dashboard & Dialogs)      |
       +----------------------+----------------------+
                              | (User Actions)
                              v
       +----------------------+----------------------+
       |                VIEWMODEL LAYER              |
       |  (FinanceViewModel / State Flow Streams)    |
       +----------------------+----------------------+
                              | (Query / Insert / Update)
                              v
       +----------------------+----------------------+
       |                REPOSITORY LAYER             |
       |  (FinanceRepository / Unified Data Source)  |
       +-----------+---------------------+-----------+
                   |                     |
                   v                     v
       +-----------+---------+ +---------+-----------+
       |      DATA LAYER     | |     DATA LAYER      |
       | (Room Database SQL) | | (SharedPreferences) |
       |     [Expenses]      | |  [Monthly Budget]   |
       +---------------------+ +---------------------+
```

### Components Summary

1. **The Model Layer (`com.example.data`)**
   - **Entity (`Expense`)**: A data class declaring the columns and SQLite schema for expenses.
   - **Local Database (`SpendWiseDatabase` & `ExpenseDao`)**: SQLite schema wrapper compiled using **KSP (Kotlin Symbol Processing)** for type-safe compile-time query verification. Exposes transactional data as reactive `Flow<List<Expense>>`.
   - **Preferences (`BudgetPreferences`)**: Light, robust wrapper around Android's native `SharedPreferences` to persist user-defined budget boundaries. Exposes states reactively as a `StateFlow<Double>`.

2. **The Repository Layer (`com.example.data.repository`)**
   - **`FinanceRepository`**: Acts as the single point of entry to all domain data. Decouples the storage engines (Room vs SharedPreferences) from the business layer, allowing easier migrations, mock-injection for tests, or cloud synchronization in the future.

3. **The ViewModel Layer (`com.example.ui.viewmodel`)**
   - **`FinanceViewModel`**: Exposes persistent application state directly to the composables. It subscribes to database flows, calculates monthly spending distributions, performs filtering/sorting off the main thread, and handles CRUD queries asynchronously using Kotlin Coroutines (`viewModelScope`).
   - Uses `ViewModelProvider.Factory` to guarantee that state is successfully retained during device rotation, multi-window layout resizing, or standard configuration changes.

4. **The UI Layer (`com.example.ui`)**
   - Written 100% in declarative **Jetpack Compose** using **Material Design 3 (M3)**.
   - Observes ViewModel streams safely using lifecycle-aware collectors. 
   - Uses test tags for reliable automated integration testing.

---

## 2. Key Engineering & Scalability Features

### A. Offline-First Reactivity
By returning `Flow<List<Expense>>` from Room, the UI automatically and instantly reflects updates whenever a record is created, edited, or deleted. 

### B. High-Performance State Derivation
Rather than storing calculated balances in a fragile database table, the ViewModel reactively derives:
- **`monthlySpendingState`**
- **`categoryBreakdownState`**

by filtering and mapping streams in memory. When the database emits a new collection, the coroutine pipes run asynchronously in the background.

### C. Graceful Inputs & Boundary Verification
The app implements robust client-side validation:
- Numeric inputs are parsed safely with `toDoubleOrNull()`.
- Negative numbers or blank records display a visible warning status.
- Value boundaries are limited to a reasonable maximum to protect SQLite types.

### D. Visual Polish & M3 Adherence
- **Custom Doughnut Chart**: Constructed using a native `Canvas` with spring rotation animations rather than heavy external charting libraries.
- **Dynamic Accent Shifting**: The budget status card transitions color organically (from primary blue/green to caution orange to warning crimson) based on actual spending ratios.
- **Touch Targets & Contrast**: High touch targets (>= 48dp) and accessible text contrasts.
