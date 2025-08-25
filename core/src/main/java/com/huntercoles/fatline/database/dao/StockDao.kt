package com.huntercoles.fatline.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.huntercoles.fatline.database.entity.StockEntity

@Dao
interface StockDao {
    
    /**
     * Get a stock by its symbol
     * @param symbol The stock symbol (e.g., "AAPL")
     * @return The stock entity or null if not found
     */
    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): StockEntity?
    
    /**
     * Get all unique stock symbols in the database
     * @return List of stock symbols
     */
    @Query("SELECT DISTINCT symbol FROM stocks")
    suspend fun getAllStockSymbols(): List<String>
    
    /**
     * Get a stock as a reactive Flow
     * @param symbol The stock symbol
     * @return Flow that emits the stock entity or null
     */
    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    fun getStockFlow(symbol: String): Flow<StockEntity?>
    
    /**
     * Get all stocks ordered by symbol
     * @return Flow that emits list of all stocks
     */
    @Query("SELECT * FROM stocks ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<StockEntity>>
    
    /**
     * Search for stocks by symbol or company name
     * @param query Search term to match against symbol or name
     * @return List of matching stocks
     */
    @Query("SELECT * FROM stocks WHERE name LIKE '%' || :query || '%' OR symbol LIKE '%' || :query || '%' ORDER BY symbol ASC")
    suspend fun searchStocks(query: String): List<StockEntity>
    
    // Insert/Update operations
    
    /**
     * Insert or update a single stock
     * @param stock The stock entity to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)
    
    /**
     * Insert or update multiple stocks
     * @param stocks List of stock entities to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)
    
    /**
     * Update an existing stock entity
     * @param stock The stock entity with updated values
     */
    @Update
    suspend fun updateStock(stock: StockEntity)
    
    // Delete operations
    
    /**
     * Delete a stock entity
     * @param stock The stock entity to delete
     */
    @Delete
    suspend fun deleteStock(stock: StockEntity)
    
    /**
     * Delete a stock by its symbol
     * @param symbol The symbol of the stock to delete
     */
    @Query("DELETE FROM stocks WHERE symbol = :symbol")
    suspend fun deleteStockBySymbol(symbol: String)
    
    // Price update operations
    
    /**
     * Update stock price information efficiently
     * @param symbol Stock symbol
     * @param price New current price
     * @param change Price change from previous close
     * @param changePercent Percentage change from previous close
     * @param timestamp Timestamp of the update (defaults to current time)
     */
    @Query("UPDATE stocks SET currentPrice = :price, change = :change, changePercent = :changePercent, lastUpdated = :timestamp WHERE symbol = :symbol")
    suspend fun updateStockPrice(
        symbol: String,
        price: Double,
        change: Double,
        changePercent: Double,
        timestamp: Long = System.currentTimeMillis()
    )
}
