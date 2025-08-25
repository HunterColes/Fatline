package com.huntercoles.fatline.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.huntercoles.fatline.database.entity.ServerConfigEntity

@Dao
interface ServerConfigDao {
    
    @Query("SELECT * FROM server_config WHERE id = 1")
    suspend fun getServerConfig(): ServerConfigEntity?
    
    @Query("SELECT * FROM server_config WHERE id = 1")
    fun getServerConfigFlow(): Flow<ServerConfigEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServerConfig(config: ServerConfigEntity)
    
    @Update
    suspend fun updateServerConfig(config: ServerConfigEntity)
    
    @Query("UPDATE server_config SET authToken = :token, isConnected = :isConnected WHERE id = 1")
    suspend fun updateAuthToken(token: String?, isConnected: Boolean)
    
    @Query("UPDATE server_config SET lastSync = :timestamp WHERE id = 1")
    suspend fun updateLastSync(timestamp: Long)
    
    @Query("UPDATE server_config SET isConnected = :isConnected WHERE id = 1")
    suspend fun updateConnectionStatus(isConnected: Boolean)
    
    @Query("DELETE FROM server_config")
    suspend fun deleteServerConfig()
}
