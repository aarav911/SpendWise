package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String, // e.g., "Food", "Transport", "Entertainment", "Bills", "Others"
    val date: Long,       // Epoch millisecond timestamp
    val note: String      // Optional note
)
