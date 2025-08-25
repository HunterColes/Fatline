package com.huntercoles.fatline.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.huntercoles.fatline.database.entity.PortfolioHoldingEntity

@Dao
interface PortfolioDao {
    
    @Query("SELECT * FROM portfolio_holdings ORDER BY addedAt DESC")
    fun getAllHoldings(): Flow<List<PortfolioHoldingEntity>>
    
    @Query("SELECT * FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun getHoldingsBySymbol(symbol: String): List<PortfolioHoldingEntity>
    
    @Query("SELECT * FROM portfolio_holdings WHERE symbol = :symbol")
    fun getHoldingsBySymbolFlow(symbol: String): Flow<List<PortfolioHoldingEntity>>
    
    @Query("SELECT DISTINCT symbol FROM portfolio_holdings")
    suspend fun getAllSymbols(): List<String>
    
    @Query("SELECT SUM(shares) FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun getTotalShares(symbol: String): Double?
    
    @Query("SELECT AVG(averageCost) FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun getAverageCost(symbol: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: PortfolioHoldingEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoldings(holdings: List<PortfolioHoldingEntity>)
    
    @Update
    suspend fun updateHolding(holding: PortfolioHoldingEntity)
    
    @Delete
    suspend fun deleteHolding(holding: PortfolioHoldingEntity)
    
    @Query("DELETE FROM portfolio_holdings WHERE id = :id")
    suspend fun deleteHoldingById(id: Long)
    
    @Query("DELETE FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun deleteAllHoldingsBySymbol(symbol: String)
    
    @Query("DELETE FROM portfolio_holdings")
    suspend fun deleteAllHoldings()
    
    // Aggregate queries for portfolio summary
    @Query("""
        SELECT SUM(h.shares * COALESCE(s.currentPrice, h.averageCost)) as totalValue
        FROM portfolio_holdings h
        LEFT JOIN stocks s ON h.symbol = s.symbol
    """)
    suspend fun getTotalPortfolioValue(): Double?
    
    @Query("""
        SELECT SUM(h.shares * h.averageCost) as totalCost
        FROM portfolio_holdings h
    """)
    suspend fun getTotalPortfolioCost(): Double?
}
