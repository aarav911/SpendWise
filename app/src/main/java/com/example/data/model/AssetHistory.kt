package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "asset_histories",
    foreignKeys = [
        ForeignKey(
            entity = Asset::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assetId"])]
)
data class AssetHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetId: Int,
    val price: Double,
    val quantity: Double,
    val timestamp: Long
)
