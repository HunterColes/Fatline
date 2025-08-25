package com.huntercoles.fatline.portfoliofeature.domain.model

data class Watchlist(
    val id: Long = 0,
    val name: String,
    val color: String = "#1976D2",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val stocks: List<WatchlistStock> = emptyList()
)
