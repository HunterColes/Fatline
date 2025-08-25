package com.huntercoles.fatline.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "stocks",
    indices = [Index(value = ["symbol"], unique = true)]
)
data class StockEntity(
    @PrimaryKey
    val symbol: String,
    val name: String,
    val exchange: String?,
    val currency: String = "USD",
    val lastUpdated: Long = System.currentTimeMillis(),
    // Cache current price locally
    val currentPrice: Double? = null,
    val change: Double? = null,
    val changePercent: Double? = null,
    val marketCap: Long? = null,
    val volume: Long? = null,
)
