package com.huntercoles.fatline.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.huntercoles.fatline.database.entity.WatchlistStockEntity

@Dao
interface WatchlistStockDao {
    
    @Query("SELECT * FROM watchlist_stocks WHERE watchlistId = :watchlistId ORDER BY sortOrder ASC, addedAt ASC")
    fun getStocksInWatchlist(watchlistId: Long): Flow<List<WatchlistStockEntity>>
    
    @Query("SELECT * FROM watchlist_stocks WHERE watchlistId = :watchlistId ORDER BY sortOrder ASC, addedAt ASC")
    suspend fun getStocksInWatchlistSync(watchlistId: Long): List<WatchlistStockEntity>
    
    @Query("SELECT * FROM watchlist_stocks WHERE symbol = :symbol")
    suspend fun getWatchlistsForStock(symbol: String): List<WatchlistStockEntity>
    
    @Query("SELECT * FROM watchlist_stocks WHERE watchlistId = :watchlistId AND symbol = :symbol LIMIT 1")
    suspend fun getWatchlistStock(watchlistId: Long, symbol: String): WatchlistStockEntity?
    
    @Query("SELECT COUNT(*) FROM watchlist_stocks WHERE watchlistId = :watchlistId AND symbol = :symbol")
    suspend fun isStockInWatchlist(watchlistId: Long, symbol: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistStock(watchlistStock: WatchlistStockEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistStocks(watchlistStocks: List<WatchlistStockEntity>)
    
    @Update
    suspend fun updateWatchlistStock(watchlistStock: WatchlistStockEntity)
    
    @Delete
    suspend fun deleteWatchlistStock(watchlistStock: WatchlistStockEntity)
    
    @Query("DELETE FROM watchlist_stocks WHERE watchlistId = :watchlistId AND symbol = :symbol")
    suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String)
    
    @Query("DELETE FROM watchlist_stocks WHERE watchlistId = :watchlistId")
    suspend fun removeAllStocksFromWatchlist(watchlistId: Long)
    
    @Query("UPDATE watchlist_stocks SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
    
    @Query("UPDATE watchlist_stocks SET shares = :shares, averageCost = :averageCost WHERE id = :id")
    suspend fun updateHoldings(id: Long, shares: Double?, averageCost: Double?)
    
    // Get watchlist stocks with stock details
    @Query("""
        SELECT ws.*, s.name, s.currentPrice, s.change, s.changePercent, s.currency
        FROM watchlist_stocks ws
        LEFT JOIN stocks s ON ws.symbol = s.symbol
        WHERE ws.watchlistId = :watchlistId
        ORDER BY ws.sortOrder ASC, ws.addedAt ASC
    """)
    fun getWatchlistStocksWithDetails(watchlistId: Long): Flow<List<WatchlistStockWithDetails>>
}

data class WatchlistStockWithDetails(
    val id: Long,
    val watchlistId: Long,
    val symbol: String,
    val addedAt: Long,
    val sortOrder: Int,
    val shares: Double?,
    val averageCost: Double?,
    val name: String?,
    val currentPrice: Double?,
    val change: Double?,
    val changePercent: Double?,
    val currency: String?
)
