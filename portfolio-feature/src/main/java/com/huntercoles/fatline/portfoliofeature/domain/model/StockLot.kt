package com.huntercoles.fatline.portfoliofeature.domain.model

/**
 * Domain model representing an individual lot/purchase of a stock
 *
 * @property id Unique identifier for the lot
 * @property watchlistStockId ID of the parent watchlist-stock relationship
 * @property shares Number of shares in this lot
 * @property pricePerShare Price paid per share
 * @property purchaseDate Date when the lot was purchased (timestamp)
 * @property notes Optional notes about the purchase
 */
data class StockLot(
    val id: Long = 0,
    val watchlistStockId: Long,
    val shares: Double,
    val pricePerShare: Double,
    val purchaseDate: Long = System.currentTimeMillis(),
    val notes: String? = null
) {
    /** Total cost of this lot (shares × price per share) */
    val totalCost: Double get() = shares * pricePerShare

    /** Current market value of this lot */
    fun currentValue(currentPrice: Double?): Double? = currentPrice?.times(shares)

    /** Unrealized gain/loss for this lot */
    fun totalReturn(currentPrice: Double?): Double? = currentPrice?.let { (it - pricePerShare) * shares }

    /** Return percentage for this lot */
    fun returnPercent(currentPrice: Double?): Double? = currentPrice?.let { ((it - pricePerShare) / pricePerShare) * 100 }
}
