package com.huntercoles.fatline.portfoliofeature.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.huntercoles.fatline.database.dao.WatchlistDao
import com.huntercoles.fatline.database.dao.WatchlistStockDao
import com.huntercoles.fatline.database.dao.StockDao
import com.huntercoles.fatline.database.dao.WatchlistStockWithDetails
import com.huntercoles.fatline.database.entity.WatchlistEntity
import com.huntercoles.fatline.database.entity.WatchlistStockEntity
import com.huntercoles.fatline.database.entity.StockEntity
import com.huntercoles.fatline.portfoliofeature.domain.model.Watchlist
import com.huntercoles.fatline.portfoliofeature.domain.model.WatchlistStock
import com.huntercoles.fatline.portfoliofeature.domain.repository.WatchlistRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val watchlistStockDao: WatchlistStockDao,
    private val stockDao: StockDao
) : WatchlistRepository {
    
    override fun getAllWatchlists(): Flow<List<Watchlist>> {
        return watchlistDao.getAllWatchlists().map { entities ->
            entities.map { entity ->
                Watchlist(
                    id = entity.id,
                    name = entity.name,
                    color = entity.color,
                    isDefault = entity.isDefault,
                    createdAt = entity.createdAt,
                    sortOrder = entity.sortOrder
                )
            }
        }
    }
    
    override fun getWatchlistById(id: Long): Flow<Watchlist?> {
        return watchlistDao.getWatchlistByIdFlow(id).map { entity ->
            entity?.let {
                Watchlist(
                    id = it.id,
                    name = it.name,
                    color = it.color,
                    isDefault = it.isDefault,
                    createdAt = it.createdAt,
                    sortOrder = it.sortOrder
                )
            }
        }
    }
    
    override fun getWatchlistStocks(watchlistId: Long): Flow<List<WatchlistStock>> {
        Timber.d("Getting stocks for watchlist: $watchlistId")
        return watchlistStockDao.getWatchlistStocksWithDetails(watchlistId).map { entities ->
            Timber.d("Found ${entities.size} stocks in watchlist $watchlistId")
            entities.map { entity ->
                Timber.d("Stock: ${entity.symbol} - ${entity.name}")
                WatchlistStock(
                    id = entity.id,
                    watchlistId = entity.watchlistId,
                    symbol = entity.symbol,
                    name = entity.name,
                    currentPrice = entity.currentPrice,
                    change = entity.change,
                    changePercent = entity.changePercent,
                    currency = entity.currency,
                    addedAt = entity.addedAt,
                    sortOrder = entity.sortOrder,
                    shares = entity.shares,
                    averageCost = entity.averageCost
                )
            }
        }
    }
    
    override suspend fun createWatchlist(name: String, color: String): Watchlist {
        val entity = WatchlistEntity(
            name = name,
            color = color,
            isDefault = false,
            createdAt = System.currentTimeMillis(),
            sortOrder = 0
        )
        val id = watchlistDao.insertWatchlist(entity)
        return Watchlist(
            id = id,
            name = name,
            color = color,
            isDefault = false,
            createdAt = entity.createdAt,
            sortOrder = 0
        )
    }
    
    override suspend fun updateWatchlist(watchlist: Watchlist) {
        val entity = WatchlistEntity(
            id = watchlist.id,
            name = watchlist.name,
            color = watchlist.color,
            isDefault = watchlist.isDefault,
            createdAt = watchlist.createdAt,
            sortOrder = watchlist.sortOrder
        )
        watchlistDao.updateWatchlist(entity)
    }
    
    override suspend fun deleteWatchlist(watchlistId: Long) {
        watchlistDao.deleteWatchlistById(watchlistId)
    }
    
    override suspend fun addStockToWatchlist(
        watchlistId: Long,
        symbol: String,
        name: String,
        price: Double,
        change: Double,
        changePercent: Double,
        shares: Double?,
        averageCost: Double?
    ) {
        Timber.d("Adding stock to watchlist: $symbol to watchlist $watchlistId")
        
        try {
            // First, create or update the stock entity
            val stockEntity = StockEntity(
                symbol = symbol,
                name = name,
                exchange = null, // Will be filled by real API later
                currency = "USD",
                currentPrice = price,
                change = change,
                changePercent = changePercent,
                lastUpdated = System.currentTimeMillis()
            )
            stockDao.insertStock(stockEntity)
            Timber.d("Stock entity inserted: $symbol")
            
            // Then create the watchlist-stock relationship
            val watchlistStockEntity = WatchlistStockEntity(
                watchlistId = watchlistId,
                symbol = symbol,
                addedAt = System.currentTimeMillis(),
                sortOrder = 0,
                shares = shares,
                averageCost = averageCost
            )
            watchlistStockDao.insertWatchlistStock(watchlistStockEntity)
            Timber.d("Watchlist-stock relationship inserted: $symbol in watchlist $watchlistId")
            
        } catch (e: Exception) {
            Timber.e(e, "Error adding stock to watchlist")
            throw e
        }
    }
    
    override suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String) {
        watchlistStockDao.removeStockFromWatchlist(watchlistId, symbol)
    }
    
    override suspend fun updateStockHoldings(
        watchlistStockId: Long,
        shares: Double?,
        averageCost: Double?
    ) {
        watchlistStockDao.updateHoldings(watchlistStockId, shares, averageCost)
    }
    
    override suspend fun setDefaultWatchlist(watchlistId: Long) {
        watchlistDao.clearDefaultWatchlist()
        watchlistDao.setDefaultWatchlist(watchlistId)
    }
    
    override suspend fun getDefaultWatchlist(): Watchlist? {
        return watchlistDao.getDefaultWatchlist()?.let { entity ->
            Watchlist(
                id = entity.id,
                name = entity.name,
                color = entity.color,
                isDefault = entity.isDefault,
                createdAt = entity.createdAt,
                sortOrder = entity.sortOrder
            )
        }
    }
    
    override suspend fun isStockInWatchlist(watchlistId: Long, symbol: String): Boolean {
        return watchlistStockDao.isStockInWatchlist(watchlistId, symbol) > 0
    }
    
    override suspend fun refreshAllStockPrices() {
        Timber.d("RefreshAllStockPrices: Starting portfolio refresh")
        try {
            // Get all unique stock symbols across all watchlists
            val allSymbols = stockDao.getAllStockSymbols()
            Timber.d("RefreshAllStockPrices: Found ${allSymbols.size} unique symbols to refresh")
            
            // In a real app, you would call your stock price API here
            // For now, we'll simulate price updates with more realistic changes
            allSymbols.forEach { symbol ->
                val currentStock = stockDao.getStockBySymbol(symbol)
                val currentPrice = currentStock?.currentPrice
                if (currentStock != null && currentPrice != null) {
                    // Only update if stock hasn't been updated in the last minute (to prevent rapid changes)
                    val timeSinceLastUpdate = System.currentTimeMillis() - currentStock.lastUpdated
                    if (timeSinceLastUpdate > 60_000) { // 1 minute cooldown
                        // Simulate more realistic price changes between -2% and +2%
                        val changePercent = (kotlin.random.Random.nextDouble() - 0.5) * 0.04 // -2% to +2%
                        val newPrice = currentPrice * (1 + changePercent)
                        val priceChange = newPrice - currentPrice
                        
                        stockDao.updateStock(
                            currentStock.copy(
                                currentPrice = newPrice,
                                change = priceChange,
                                changePercent = changePercent * 100,
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                        Timber.d("RefreshAllStockPrices: Updated $symbol - new price: $newPrice, change: ${changePercent * 100}%")
                    } else {
                        Timber.d("RefreshAllStockPrices: Skipping $symbol - updated too recently")
                    }
                }
            }
            Timber.d("RefreshAllStockPrices: Portfolio refresh completed")
        } catch (e: Exception) {
            Timber.e(e, "RefreshAllStockPrices: Error refreshing portfolio")
            throw e
        }
    }
}
