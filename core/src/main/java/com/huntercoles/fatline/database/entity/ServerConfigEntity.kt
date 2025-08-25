package com.huntercoles.fatline.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_config")
data class ServerConfigEntity(
    @PrimaryKey
    val id: Int = 1, // Single row table
    val serverUrl: String, // e.g., "http://192.168.1.100:8686"
    val username: String?,
    val authToken: String?, // Stored JWT token
    val isConnected: Boolean = false,
    val lastSync: Long = 0L,
)
