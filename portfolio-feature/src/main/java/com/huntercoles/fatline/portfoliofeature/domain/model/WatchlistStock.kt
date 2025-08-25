package com.huntercoles.fatline.portfoliofeature.domain.model

/**
 * Domain model representing a stock within a watchlist
 * 
 * Combines stock market data with optional portfolio holdings information.
 * Provides computed properties for investment calculations and performance metrics.
 * 
 * @property id Unique identifier for the watchlist-stock relationship
 * @property watchlistId ID of the parent watchlist
 * @property symbol Stock ticker symbol (e.g., "AAPL", "GOOGL")
 * @property name Company display name
 * @property currentPrice Latest market price per share
 * @property change Price change from previous close
 * @property changePercent Percentage change from previous close
 * @property currency Currency code (typically "USD")
 * @property addedAt Timestamp when stock was added to watchlist
 * @property sortOrder Custom sort order within the watchlist
 * @property shares Optional: Number of shares owned (for portfolio tracking)
 * @property averageCost Optional: Average cost per share (for portfolio tracking)
 */
data class WatchlistStock(
    val id: Long = 0,
    val watchlistId: Long,
    val symbol: String,
    val name: String?,
    val currentPrice: Double?,
    val change: Double?,
    val changePercent: Double?,
    val currency: String?,
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    // Portfolio holdings data
    val shares: Double? = null,
    val averageCost: Double? = null,
) {
    /** Whether the stock price change is positive */
    val isPositive: Boolean get() = (change ?: 0.0) >= 0
    
    /** Total market value of holdings (shares × current price) */
    val totalValue: Double? get() = shares?.let { currentPrice?.times(it) }
    
    /** Total unrealized gain/loss (market value - cost basis) */
    val totalReturn: Double? get() = shares?.let { s ->
        averageCost?.let { avgCost ->
            currentPrice?.let { price ->
                s * (price - avgCost)
            }
        }
    }
    
    /** Return percentage ((current price - average cost) / average cost × 100) */
    val returnPercent: Double? get() = averageCost?.let { avgCost ->
        currentPrice?.let { price ->
            ((price - avgCost) / avgCost) * 100
        }
    }
}
