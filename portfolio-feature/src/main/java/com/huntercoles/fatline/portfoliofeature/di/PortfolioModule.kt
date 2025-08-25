package com.huntercoles.fatline.portfoliofeature.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.huntercoles.fatline.portfoliofeature.data.repository.WatchlistRepositoryImpl
import com.huntercoles.fatline.portfoliofeature.domain.repository.WatchlistRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PortfolioModule {
    
    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(
        watchlistRepositoryImpl: WatchlistRepositoryImpl
    ): WatchlistRepository
}
