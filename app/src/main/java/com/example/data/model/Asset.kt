package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g. "Stocks", "Crypto", "Gold", "Real Estate", "Intellectual"
    val quantity: Double,
    val costBasis: Double, // total fiat spent
    val currentPrice: Double // current price per unit
)
