package com.huntercoles.fatline.portfoliofeature.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.huntercoles.fatline.portfoliofeature.domain.model.Watchlist
import com.huntercoles.fatline.portfoliofeature.domain.model.WatchlistStock
import com.huntercoles.fatline.portfoliofeature.domain.repository.WatchlistRepository
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {
    
    private val _selectedWatchlistId = MutableStateFlow<Long?>(null)
    
    val uiState: StateFlow<PortfolioUiState> = combine(
        watchlistRepository.getAllWatchlists(),
        _selectedWatchlistId
    ) { watchlists, selectedWatchlistId ->
        // Auto-select first watchlist if none selected
        val actualSelectedId = selectedWatchlistId ?: watchlists.firstOrNull()?.id
        if (actualSelectedId != selectedWatchlistId) {
            _selectedWatchlistId.value = actualSelectedId
        }
        
        Pair(watchlists, actualSelectedId)
    }.flatMapLatest { (watchlists, selectedId) ->
        if (selectedId != null) {
            watchlistRepository.getWatchlistStocks(selectedId).map { stocks ->
                val selectedWatchlist = watchlists.find { it.id == selectedId }
                PortfolioUiState(
                    watchlists = watchlists,
                    selectedWatchlistId = selectedId,
                    selectedWatchlist = selectedWatchlist,
                    selectedWatchlistStocks = stocks,
                    isLoading = false
                )
            }
        } else {
            flowOf(
                PortfolioUiState(
                    watchlists = watchlists,
                    selectedWatchlistId = null,
                    selectedWatchlist = null,
                    selectedWatchlistStocks = emptyList(),
                    isLoading = false
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PortfolioUiState()
    )
    
    fun selectWatchlist(watchlistId: Long) {
        _selectedWatchlistId.value = watchlistId
    }
    
    fun createWatchlist(name: String, color: String) {
        viewModelScope.launch {
            try {
                val newWatchlist = watchlistRepository.createWatchlist(name, color)
                // Auto-select the newly created watchlist
                _selectedWatchlistId.value = newWatchlist.id
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }
    
    fun addStockToWatchlist(symbol: String, shares: Double? = null, averageCost: Double? = null) {
        val currentWatchlistId = _selectedWatchlistId.value ?: return
        
        viewModelScope.launch {
            try {
                // Check if stock is already in watchlist
                val isAlreadyAdded = watchlistRepository.isStockInWatchlist(currentWatchlistId, symbol)
                if (!isAlreadyAdded) {
                    // For manually added stocks, we'll use placeholder data
                    // This should be enhanced to fetch real stock data
                    watchlistRepository.addStockToWatchlist(
                        watchlistId = currentWatchlistId,
                        symbol = symbol,
                        name = symbol, // Use symbol as name for now
                        price = 0.0, // Placeholder - should fetch real data
                        change = 0.0,
                        changePercent = 0.0,
                        shares = shares,
                        averageCost = averageCost
                    )
                }
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }
    
    fun removeStockFromWatchlist(symbol: String) {
        val currentWatchlistId = _selectedWatchlistId.value ?: return
        
        viewModelScope.launch {
            try {
                watchlistRepository.removeStockFromWatchlist(currentWatchlistId, symbol)
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }
    
    fun deleteWatchlist(watchlistId: Long) {
        viewModelScope.launch {
            try {
                watchlistRepository.deleteWatchlist(watchlistId)
                
                // If we deleted the currently selected watchlist, select another one
                if (_selectedWatchlistId.value == watchlistId) {
                    _selectedWatchlistId.value = null
                }
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }
    
    fun updateStockHoldings(watchlistStockId: Long, shares: Double?, averageCost: Double?) {
        viewModelScope.launch {
            try {
                watchlistRepository.updateStockHoldings(watchlistStockId, shares, averageCost)
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }
    
    fun refreshPortfolio() {
        viewModelScope.launch {
            try {
                watchlistRepository.refreshAllStockPrices()
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }
}

data class PortfolioUiState(
    val watchlists: List<Watchlist> = emptyList(),
    val selectedWatchlistId: Long? = null,
    val selectedWatchlist: Watchlist? = null,
    val selectedWatchlistStocks: List<WatchlistStock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
