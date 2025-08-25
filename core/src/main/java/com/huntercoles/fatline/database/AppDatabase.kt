package com.huntercoles.fatline.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.huntercoles.fatline.database.entity.*
import com.huntercoles.fatline.database.dao.*

/**
 * Current database version
 * Increment this when making schema changes and add corresponding migration
 */
private const val DATABASE_VERSION = 3

/**
 * Main Room database for Fatline stock tracking application
 * 
 * Database Schema Evolution:
 * - Version 1: Initial placeholder structure with rocket data
 * - Version 2: Added core stock tracking tables (stocks, portfolio_holdings, stock_history, server_config)
 * - Version 3: Added watchlist system (watchlists, watchlist_stocks) with foreign key relationships
 * 
 * Key Features:
 * - Real-time stock price caching with historical data
 * - Multi-watchlist portfolio management
 * - Server synchronization capabilities
 * - Comprehensive indexing for performance
 */
@Database(
    entities = [
        StockEntity::class,              // Core stock information and price cache
        PortfolioHoldingEntity::class,   // User portfolio holdings (legacy, being phased out)
        StockHistoryEntity::class,       // Historical stock price data for charting
        ServerConfigEntity::class,       // Server connection and sync configuration
        WatchlistEntity::class,          // User-created watchlists
        WatchlistStockEntity::class      // Many-to-many relationship: watchlists ↔ stocks
    ],
    version = DATABASE_VERSION,
    exportSchema = true // Enable schema export for version control and migration testing
)
abstract class AppDatabase : RoomDatabase() {
    
    // Data Access Objects (DAOs)
    abstract fun stockDao(): StockDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun stockHistoryDao(): StockHistoryDao
    abstract fun serverConfigDao(): ServerConfigDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun watchlistStockDao(): WatchlistStockDao
}

/**
 * Database Migration from Version 1 to 2
 * Removes placeholder rocket data and introduces stock tracking schema
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Clean up old demo data
        database.execSQL("DROP TABLE IF EXISTS RocketCached")
        
        // Room will automatically create new tables based on @Entity annotations
        // No explicit CREATE TABLE needed as Room handles this
    }
}

/**
 * Database Migration from Version 2 to 3
 * Introduces the watchlist system for better portfolio organization
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create watchlists table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `watchlists` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `color` TEXT NOT NULL,
                `isDefault` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL
            )
        """)
        
        // Create watchlist_stocks junction table with foreign key constraints
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `watchlist_stocks` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `watchlistId` INTEGER NOT NULL,
                `symbol` TEXT NOT NULL,
                `addedAt` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                `shares` REAL,
                `averageCost` REAL,
                FOREIGN KEY(`watchlistId`) REFERENCES `watchlists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`symbol`) REFERENCES `stocks`(`symbol`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """)
        
        // Create performance-optimized indexes
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_watchlists_name` ON `watchlists` (`name`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_watchlist_stocks_watchlistId` ON `watchlist_stocks` (`watchlistId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_watchlist_stocks_symbol` ON `watchlist_stocks` (`symbol`)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_watchlist_stocks_watchlistId_symbol` ON `watchlist_stocks` (`watchlistId`, `symbol`)")
        
        // Initialize with a default watchlist for existing users
        database.execSQL("""
            INSERT INTO watchlists (name, color, isDefault, createdAt, sortOrder)
            VALUES ('My Watchlist', '#1976D2', 1, ${System.currentTimeMillis()}, 0)
        """)
    }
}
