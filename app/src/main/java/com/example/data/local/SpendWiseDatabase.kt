package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.ExpenseDao
import com.example.data.local.SavingsGoalDao
import com.example.data.local.AssetDao
import com.example.data.model.Expense
import com.example.data.model.SavingsGoal
import com.example.data.model.Asset
import com.example.data.model.AssetHistory

@Database(entities = [Expense::class, SavingsGoal::class, Asset::class, AssetHistory::class], version = 3, exportSchema = false)
abstract class SpendWiseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: SpendWiseDatabase? = null

        fun getDatabase(context: Context): SpendWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendWiseDatabase::class.java,
                    "spendwise_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
