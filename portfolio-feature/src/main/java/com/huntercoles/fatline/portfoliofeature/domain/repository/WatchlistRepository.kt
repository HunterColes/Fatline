package com.huntercoles.fatline.portfoliofeature.domain.repository

import kotlinx.coroutines.flow.Flow
import com.huntercoles.fatline.portfoliofeature.domain.model.Watchlist
import com.huntercoles.fatline.portfoliofeature.domain.model.WatchlistStock
import com.huntercoles.fatline.portfoliofeature.domain.model.StockLot

/**
 * Repository interface for managing watchlists and stock tracking
 * 
 * Provides a clean abstraction layer for watchlist operations including:
 * - CRUD operations for watchlists
 * - Stock management within watchlists
 * - Portfolio tracking with real-time price updates
 * - Duplicate stock prevention across watchlists
 */
interface WatchlistRepository {
    
    // Watchlist management
    
    /**
     * Get all user watchlists as a reactive stream
     * @return Flow emitting list of watchlists, ordered by sort order and creation date
     */
    fun getAllWatchlists(): Flow<List<Watchlist>>
    
    /**
     * Get a specific watchlist by ID
     * @param id The watchlist ID
     * @return Flow emitting the watchlist or null if not found
     */
    fun getWatchlistById(id: Long): Flow<Watchlist?>
    
    /**
     * Get all stocks in a specific watchlist with real-time price data
     * @param watchlistId The ID of the watchlist
     * @return Flow emitting list of stocks with current market data
     */
    fun getWatchlistStocks(watchlistId: Long): Flow<List<WatchlistStock>>
    
    /**
     * Create a new watchlist
     * @param name Display name for the watchlist
     * @param color Color theme for the watchlist (hex color string)
     * @return The newly created watchlist
     */
    suspend fun createWatchlist(name: String, color: String = "#1976D2"): Watchlist
    
    /**
     * Update an existing watchlist
     * @param watchlist The watchlist with updated properties
     */
    suspend fun updateWatchlist(watchlist: Watchlist)
    
    /**
     * Delete a watchlist and all its stock associations
     * @param watchlistId The ID of the watchlist to delete
     */
    suspend fun deleteWatchlist(watchlistId: Long)
    
    // Stock management
    
    /**
     * Add a stock to a watchlist with price and optional holdings data
     * @param watchlistId Target watchlist ID
     * @param symbol Stock ticker symbol (e.g., "AAPL")
     * @param name Company name
     * @param price Current stock price
     * @param change Price change from previous close
     * @param changePercent Percentage change from previous close
     * @param shares Optional number of shares owned (for portfolio tracking)
     * @param averageCost Optional average cost per share (for portfolio tracking)
     */
    suspend fun addStockToWatchlist(
        watchlistId: Long, 
        symbol: String, 
        name: String,
        price: Double,
        change: Double,
        changePercent: Double,
        shares: Double? = null, 
        averageCost: Double? = null
    )
    
    /**
     * Remove a stock from a watchlist
     * @param watchlistId The watchlist ID
     * @param symbol The stock symbol to remove
     */
    suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String)
    
    /**
     * Update holdings information for a stock in a watchlist
     * @param watchlistStockId The ID of the watchlist-stock relationship
     * @param shares Updated number of shares owned
     * @param averageCost Updated average cost per share
     */
    suspend fun updateStockHoldings(watchlistStockId: Long, shares: Double?, averageCost: Double?)
    
    // Default watchlist management
    
    /**
     * Set a watchlist as the default (main) watchlist
     * @param watchlistId The watchlist to set as default
     */
    suspend fun setDefaultWatchlist(watchlistId: Long)
    
    /**
     * Get the current default watchlist
     * @return The default watchlist or null if none set
     */
    suspend fun getDefaultWatchlist(): Watchlist?
    
    // Utility functions
    
    /**
     * Check if a stock is already in a specific watchlist
     * Used for duplicate prevention in the UI
     * @param watchlistId The watchlist to check
     * @param symbol The stock symbol to check
     * @return True if the stock is already in the watchlist
     */
    suspend fun isStockInWatchlist(watchlistId: Long, symbol: String): Boolean
    
    /**
     * Refresh all stock prices across all watchlists
     * Implements rate limiting to prevent excessive API calls
     * Updates prices with realistic market simulation
     */
    suspend fun refreshAllStockPrices()
    
    // Lot management
    
    /**
     * Add a lot to a stock in a watchlist
     * @param lot The lot to add
     */
    suspend fun addLotToStock(lot: StockLot)
    
    /**
     * Remove a lot from a stock
     * @param lotId The ID of the lot to remove
     */
    suspend fun removeLotFromStock(lotId: Long)
    
    /**
     * Get all lots for a specific stock
     * @param watchlistStockId The ID of the watchlist-stock relationship
     * @return Flow emitting list of lots for the stock
     */
    fun getStockLots(watchlistStockId: Long): Flow<List<StockLot>>
}
