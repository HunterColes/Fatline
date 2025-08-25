package com.huntercoles.fatline.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "watchlist_stocks",
    foreignKeys = [
        ForeignKey(
            entity = WatchlistEntity::class,
            parentColumns = ["id"],
            childColumns = ["watchlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StockEntity::class,
            parentColumns = ["symbol"],
            childColumns = ["symbol"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["watchlistId"]),
        Index(value = ["symbol"]),
        Index(value = ["watchlistId", "symbol"], unique = true)
    ]
)
data class WatchlistStockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val watchlistId: Long,
    val symbol: String,
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    // Optional: Track position if this is a holdings list
    val shares: Double? = null,
    val averageCost: Double? = null,
)
