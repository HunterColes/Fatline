package com.huntercoles.fatline.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "stock_history",
    foreignKeys = [
        ForeignKey(
            entity = StockEntity::class,
            parentColumns = ["symbol"],
            childColumns = ["symbol"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["symbol", "timestamp"], unique = true),
        Index(value = ["symbol"]),
        Index(value = ["timestamp"])
    ]
)
data class StockHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val timestamp: Long, // Unix timestamp
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val timeRange: String, // "1D", "1W", "1M", "3M", "1Y", etc.
    val interval: String, // "1m", "5m", "1h", "1d", etc.
    val cachedAt: Long = System.currentTimeMillis(),
)
