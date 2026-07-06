# SpendWise - Personal Finance Tracker

SpendWise is an offline-first, native Android personal finance tracker developed using modern Kotlin and Jetpack Compose. It allows users to log expenses, set a monthly spending limit, and visualize spending distributions using interactive, animated visuals.


---
## Download apk: https://github.com/aarav911/SpendWise/releases/tag/v1.0.3

---

## 🚀 Key Features

*   **Expense Management**: Log expenses instantly with custom amount values, specific category tags (Food, Transport, Entertainment, Bills, Others), custom note annotations, and native calendar date pickers.
*   **Intuitive Visual Budgeting**: Maintain a monthly spending cap. A dynamic progress tracker automatically changes colors (shifting to warnings/errors) as you approach or exceed your budget limit.
*   **Dynamic Analytics**: A gorgeous, animated custom Doughnut Chart drawn natively on a Canvas, showing proportional spending breakdowns per category.
*   **Completely Offline-First**: Built with a local Room SQLite database and persistent shared preferences. Works with zero network latency, keeping user data private and offline.
*   **Polish & Edge Cases**: Input sanitization prevents non-numeric records or negative thresholds. Features friendly Empty States to guide new users.

---

## 🛠️ Tech Stack & Architecture

*   **Language**: Kotlin
*   **UI Engine**: Jetpack Compose (Material Design 3)
*   **Persistence**: Room DB (SQLite) for structured entries; SharedPreferences for configurations
*   **Concurrency**: Kotlin Coroutines & Flows
*   **Architecture**: Model-View-ViewModel (MVVM) with Unidirectional Data Flow (UDF)
*   **Build Pipeline**: Gradle Kotlin DSL (.gradle.kts) & KSP

---

## 📂 Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                       # Entry Activity, injects storage & triggers VM
├── data/
│   ├── local/
│   │   ├── BudgetPreferences.kt          # SharedPreferences persistence wrapper
│   │   ├── ExpenseDao.kt                 # Room SQLite DAO (Data Access Object)
│   │   └── SpendWiseDatabase.kt          # Room SQLite Database holder
│   ├── model/
│   │   └── Expense.kt                    # Expense Database Entity
│   └── repository/
│       └── FinanceRepository.kt          # Unified abstraction layer
└── ui/
    ├── components/
    │   ├── BudgetProgressBar.kt          # Animated status indicators (primary -> crimson warning)
    │   ├── CategoryChart.kt              # Spring-animated Donut Chart on Custom Canvas
    │   └── ExpenseDialogs.kt             # Add, Edit, and Budget validation forms
    ├── screens/
    │   └── DashboardScreen.kt            # Main Single-Screen Dashboard (List + Chart + Budget)
    └── theme/
        ├── CategoryStyle.kt              # Category Color and Icon mappings
        ├── Color.kt                      # Application color palettes
        ├── Theme.kt                      # M3 Dark/Light Theme definitions
        └── Type.kt                       # Heading and Display typography
```

---

## 🧪 Testing Suite

SpendWise includes an automated test suite supporting local JVM execution:

*   **Unit Tests (`ExampleUnitTest.kt`)**: Validates logical calculations.
*   **Robolectric Tests (`ExampleRobolectricTest.kt`)**: Verifies context string configurations.
*   **Screenshot Tests (`GreetingScreenshotTest.kt`)**: Leverages **Roborazzi** to capture and assert pixel-perfect UI regressions.

### To Run Tests Locally:
```bash
gradle :app:testDebugUnitTest
```

---

## ⚡ How to Build & Run
1.  **Clone / Export** the project repository.
2.  Open the project in **Android Studio (Ladybug or higher)**.
3.  Ensure your JDK is set to **Java 17 / Java 21** in Gradle settings.
4.  Build or Run the `:app` configuration directly onto a virtual emulator or physical device.
