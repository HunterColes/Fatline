package com.huntercoles.fatline.portfoliofeature.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.huntercoles.fatline.portfoliofeature.domain.model.StockLot
import com.huntercoles.fatline.portfoliofeature.domain.repository.WatchlistRepository
import com.huntercoles.fatline.portfoliofeature.domain.model.Watchlist
import com.huntercoles.fatline.portfoliofeature.domain.model.WatchlistStock
import com.huntercoles.fatline.core.presentation.ConnectionStatusManager
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    @ApplicationContext private val context: Context,
    private val connectionStatusManager: ConnectionStatusManager
) : ViewModel() {
    
    private val _selectedWatchlistId = MutableStateFlow<Long?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshTrigger = MutableStateFlow(0)
    private val _pendingRefresh = MutableStateFlow(false)
    private val _lotsDialogOpen = MutableStateFlow(false)
    
    val uiState: StateFlow<PortfolioUiState> = combine(
        watchlistRepository.getAllWatchlists(),
        _selectedWatchlistId,
        _isRefreshing,
        _refreshTrigger
    ) { watchlists, selectedWatchlistId, isRefreshing, _ ->
        // Auto-select first watchlist if none selected
        val actualSelectedId = selectedWatchlistId ?: watchlists.firstOrNull()?.id
        if (actualSelectedId != selectedWatchlistId) {
            _selectedWatchlistId.value = actualSelectedId
        }
        
        Pair(watchlists, actualSelectedId to isRefreshing)
    }.flatMapLatest { (watchlists, pair) ->
        val (selectedId, isRefreshing) = pair
        if (selectedId != null) {
            watchlistRepository.getWatchlistStocks(selectedId).map { stocks ->
                val selectedWatchlist = watchlists.find { it.id == selectedId }
                PortfolioUiState(
                    watchlists = watchlists,
                    selectedWatchlistId = selectedId,
                    selectedWatchlist = selectedWatchlist,
                    selectedWatchlistStocks = stocks,
                    isLoading = isRefreshing,
                    lotsDialogOpen = _lotsDialogOpen.value
                )
            }
        } else {
            flowOf(
                PortfolioUiState(
                    watchlists = watchlists,
                    selectedWatchlistId = null,
                    selectedWatchlist = null,
                    selectedWatchlistStocks = emptyList(),
                    isLoading = isRefreshing,
                    lotsDialogOpen = _lotsDialogOpen.value
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
                _isRefreshing.value = true
                watchlistRepository.refreshAllStockPrices()
                // Update connection status after refresh
                connectionStatusManager.updateConnectionStatus(context)
            } catch (e: Exception) {
                // TODO: Handle error
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun addLotToStock(watchlistStockId: Long, shares: Double, pricePerShare: Double, purchaseDate: Long) {
        viewModelScope.launch {
            try {
                val lot = StockLot(
                    watchlistStockId = watchlistStockId,
                    shares = shares,
                    pricePerShare = pricePerShare,
                    purchaseDate = purchaseDate
                )
                watchlistRepository.addLotToStock(lot)
                
                // If lots dialog is open, delay refresh until dialog is closed
                if (_lotsDialogOpen.value) {
                    _pendingRefresh.value = true
                } else {
                    _refreshTrigger.value++
                }
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }
    
    fun removeLotFromStock(lotId: Long) {
        viewModelScope.launch {
            try {
                watchlistRepository.removeLotFromStock(lotId)
                
                // If lots dialog is open, delay refresh until dialog is closed
                if (_lotsDialogOpen.value) {
                    _pendingRefresh.value = true
                } else {
                    _refreshTrigger.value++
                }
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }
    
    fun setLotsDialogOpen(open: Boolean) {
        _lotsDialogOpen.value = open
        // If closing the dialog and there's a pending refresh, trigger it now
        if (!open && _pendingRefresh.value) {
            _pendingRefresh.value = false
            _refreshTrigger.value++
        }
    }
    
    fun getStockLots(watchlistStockId: Long): Flow<List<StockLot>> {
        return watchlistRepository.getStockLots(watchlistStockId)
    }
}

data class PortfolioUiState(
    val watchlists: List<Watchlist> = emptyList(),
    val selectedWatchlistId: Long? = null,
    val selectedWatchlist: Watchlist? = null,
    val selectedWatchlistStocks: List<WatchlistStock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lotsDialogOpen: Boolean = false
)
