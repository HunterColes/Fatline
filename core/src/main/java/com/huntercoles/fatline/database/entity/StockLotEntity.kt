package com.huntercoles.fatline.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "stock_lots",
    foreignKeys = [
        ForeignKey(
            entity = WatchlistStockEntity::class,
            parentColumns = ["id"],
            childColumns = ["watchlistStockId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["watchlistStockId"]),
        Index(value = ["purchaseDate"])
    ]
)
data class StockLotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val watchlistStockId: Long,
    val shares: Double,
    val pricePerShare: Double,
    val purchaseDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
