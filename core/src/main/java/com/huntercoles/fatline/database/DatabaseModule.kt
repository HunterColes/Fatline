package com.huntercoles.fatline.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val APP_DATABASE_NAME = "fatline_database"

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        APP_DATABASE_NAME,
    )
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
    .build()
    
    @Provides
    fun provideStockDao(database: AppDatabase) = database.stockDao()
    
    @Provides
    fun providePortfolioDao(database: AppDatabase) = database.portfolioDao()
    
    @Provides
    fun provideStockHistoryDao(database: AppDatabase) = database.stockHistoryDao()
    
    @Provides
    fun provideServerConfigDao(database: AppDatabase) = database.serverConfigDao()
    
    @Provides
    fun provideWatchlistDao(database: AppDatabase) = database.watchlistDao()
    
    @Provides
    fun provideWatchlistStockDao(database: AppDatabase) = database.watchlistStockDao()
    
    @Provides
    fun provideStockLotDao(database: AppDatabase) = database.stockLotDao()
}
