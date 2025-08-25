package com.huntercoles.fatline.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.huntercoles.fatline.database.entity.WatchlistEntity

@Dao
interface WatchlistDao {
    
    @Query("SELECT * FROM watchlists ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllWatchlists(): Flow<List<WatchlistEntity>>
    
    @Query("SELECT * FROM watchlists WHERE id = :id")
    suspend fun getWatchlistById(id: Long): WatchlistEntity?
    
    @Query("SELECT * FROM watchlists WHERE id = :id")
    fun getWatchlistByIdFlow(id: Long): Flow<WatchlistEntity?>
    
    @Query("SELECT * FROM watchlists WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultWatchlist(): WatchlistEntity?
    
    @Query("SELECT * FROM watchlists WHERE name = :name LIMIT 1")
    suspend fun getWatchlistByName(name: String): WatchlistEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(watchlist: WatchlistEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlists(watchlists: List<WatchlistEntity>)
    
    @Update
    suspend fun updateWatchlist(watchlist: WatchlistEntity)
    
    @Delete
    suspend fun deleteWatchlist(watchlist: WatchlistEntity)
    
    @Query("DELETE FROM watchlists WHERE id = :id")
    suspend fun deleteWatchlistById(id: Long)
    
    @Query("UPDATE watchlists SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
    
    @Query("UPDATE watchlists SET isDefault = 0")
    suspend fun clearDefaultWatchlist()
    
    @Query("UPDATE watchlists SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultWatchlist(id: Long)
    
    @Query("SELECT COUNT(*) FROM watchlists")
    suspend fun getWatchlistCount(): Int
}
