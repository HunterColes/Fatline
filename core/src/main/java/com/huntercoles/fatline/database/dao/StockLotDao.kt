package com.huntercoles.fatline.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.huntercoles.fatline.database.entity.StockLotEntity

@Dao
interface StockLotDao {

    @Query("SELECT * FROM stock_lots WHERE watchlistStockId = :watchlistStockId ORDER BY purchaseDate ASC")
    fun getLotsForWatchlistStock(watchlistStockId: Long): Flow<List<StockLotEntity>>

    @Query("SELECT * FROM stock_lots WHERE watchlistStockId = :watchlistStockId ORDER BY purchaseDate ASC")
    suspend fun getLotsForWatchlistStockSync(watchlistStockId: Long): List<StockLotEntity>

    @Query("SELECT * FROM stock_lots WHERE id = :lotId LIMIT 1")
    suspend fun getLotById(lotId: Long): StockLotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: StockLotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLots(lots: List<StockLotEntity>)

    @Update
    suspend fun updateLot(lot: StockLotEntity)

    @Delete
    suspend fun deleteLot(lot: StockLotEntity)

    @Query("DELETE FROM stock_lots WHERE id = :lotId")
    suspend fun deleteLotById(lotId: Long)

    @Query("DELETE FROM stock_lots WHERE watchlistStockId = :watchlistStockId")
    suspend fun deleteLotsForWatchlistStock(watchlistStockId: Long)

    @Query("SELECT SUM(shares) FROM stock_lots WHERE watchlistStockId = :watchlistStockId")
    suspend fun getTotalSharesForWatchlistStock(watchlistStockId: Long): Double?

    @Query("SELECT AVG(pricePerShare) FROM stock_lots WHERE watchlistStockId = :watchlistStockId")
    suspend fun getAveragePriceForWatchlistStock(watchlistStockId: Long): Double?
}
