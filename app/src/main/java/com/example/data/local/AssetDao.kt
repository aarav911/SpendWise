package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Asset
import com.example.data.model.AssetHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY id ASC")
    fun getAllAssets(): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: Int): Asset?

    @Query("SELECT * FROM assets WHERE name = :name AND category = :category LIMIT 1")
    suspend fun getAssetByNameAndCategory(name: String, category: String): Asset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: Asset): Long

    @Update
    suspend fun updateAsset(asset: Asset)

    @Delete
    suspend fun deleteAsset(asset: Asset)

    @Query("SELECT * FROM asset_histories ORDER BY timestamp ASC")
    fun getAllAssetHistories(): Flow<List<AssetHistory>>

    @Query("SELECT * FROM asset_histories WHERE assetId = :assetId ORDER BY timestamp ASC")
    fun getHistoryForAsset(assetId: Int): Flow<List<AssetHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetHistory(history: AssetHistory): Long

    @Delete
    suspend fun deleteAssetHistory(history: AssetHistory)
}
