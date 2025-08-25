package com.huntercoles.fatline.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "portfolio_holdings",
    foreignKeys = [
        ForeignKey(
            entity = StockEntity::class,
            parentColumns = ["symbol"],
            childColumns = ["symbol"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["symbol"]),
        Index(value = ["symbol", "addedAt"])
    ]
)
data class PortfolioHoldingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val shares: Double,
    val averageCost: Double,
    val addedAt: Long = System.currentTimeMillis(),
    // For tracking multiple purchases of the same stock
    val purchaseDate: Long = System.currentTimeMillis(),
    val purchasePrice: Double = averageCost,
)
