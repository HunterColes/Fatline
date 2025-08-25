package com.huntercoles.fatline.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.huntercoles.fatline.database.entity.StockHistoryEntity

@Dao
interface StockHistoryDao {
    
    @Query("SELECT * FROM stock_history WHERE symbol = :symbol AND timeRange = :timeRange ORDER BY timestamp ASC")
    suspend fun getStockHistory(symbol: String, timeRange: String): List<StockHistoryEntity>
    
    @Query("SELECT * FROM stock_history WHERE symbol = :symbol AND timeRange = :timeRange ORDER BY timestamp ASC")
    fun getStockHistoryFlow(symbol: String, timeRange: String): Flow<List<StockHistoryEntity>>
    
    @Query("SELECT * FROM stock_history WHERE symbol = :symbol AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getStockHistoryBetween(symbol: String, startTime: Long, endTime: Long): List<StockHistoryEntity>
    
    @Query("SELECT MAX(timestamp) FROM stock_history WHERE symbol = :symbol AND timeRange = :timeRange")
    suspend fun getLatestTimestamp(symbol: String, timeRange: String): Long?
    
    @Query("SELECT COUNT(*) FROM stock_history WHERE symbol = :symbol AND timeRange = :timeRange")
    suspend fun getHistoryCount(symbol: String, timeRange: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: StockHistoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(historyList: List<StockHistoryEntity>)
    
    @Update
    suspend fun updateHistory(history: StockHistoryEntity)
    
    @Delete
    suspend fun deleteHistory(history: StockHistoryEntity)
    
    @Query("DELETE FROM stock_history WHERE symbol = :symbol")
    suspend fun deleteHistoryBySymbol(symbol: String)
    
    @Query("DELETE FROM stock_history WHERE symbol = :symbol AND timeRange = :timeRange")
    suspend fun deleteHistoryBySymbolAndRange(symbol: String, timeRange: String)
    
    @Query("DELETE FROM stock_history WHERE cachedAt < :expireTime")
    suspend fun deleteExpiredHistory(expireTime: Long)
    
    @Query("DELETE FROM stock_history")
    suspend fun deleteAllHistory()
    
    // Get the most recent price for a symbol
    @Query("SELECT * FROM stock_history WHERE symbol = :symbol ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPrice(symbol: String): StockHistoryEntity?
}
