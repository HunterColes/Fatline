package com.huntercoles.fatline.database

import androidx.room.Database
import androidx.room.RoomDatabase

private const val DATABASE_VERSION = 1

// Temporary placeholder entity until we add stock entities
@androidx.room.Entity(tableName = "placeholder")
data class PlaceholderEntity(
    @androidx.room.PrimaryKey val id: Int = 1
)

@androidx.room.Dao
interface PlaceholderDao {
    // Empty DAO for now
}

@Database(
    entities = [PlaceholderEntity::class],
    version = DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao
    
    // Future: Add stock-related DAOs here
    // abstract fun stockDao(): StockDao
    // abstract fun portfolioDao(): PortfolioDao
}
