package com.huntercoles.fatline.basicfeature.presentation

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.huntercoles.fatline.core.presentation.mvi.BaseViewModel
import com.huntercoles.fatline.basicfeature.presentation.StockSearchEvent.AddedToPortfolio
import com.huntercoles.fatline.basicfeature.presentation.StockSearchEvent.ShowMessage
import com.huntercoles.fatline.basicfeature.presentation.StockSearchEvent.ShowStockDetails
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.AddToPortfolio
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.AddToWatchlist
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.RefreshStocks
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.SearchQueryChanged
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.StockClicked
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState.Error
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState.Fetched
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState.Loading
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState.SearchQueryChanged as SearchQueryChangedState
import com.huntercoles.fatline.basicfeature.presentation.model.StockDisplayable
import com.huntercoles.fatline.portfoliofeature.domain.repository.WatchlistRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import com.huntercoles.fatline.core.network.StockApiService
import com.huntercoles.fatline.core.network.model.StockSearchResponse

@HiltViewModel
class StockSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stockSearchUiState: StockSearchUiState,
    private val watchlistRepository: WatchlistRepository,
    private val stockApiService: StockApiService,
) : BaseViewModel<StockSearchUiState, PartialState, StockSearchEvent, StockSearchIntent>(
    savedStateHandle = savedStateHandle,
    initialState = stockSearchUiState,
) {

    // Expose watchlists for selection dialog
    val watchlists = watchlistRepository.getAllWatchlists()
    
    suspend fun isStockInWatchlist(watchlistId: Long, symbol: String): Boolean {
        return watchlistRepository.isStockInWatchlist(watchlistId, symbol)
    }

    override fun mapIntents(intent: StockSearchIntent): Flow<PartialState> = when (intent) {
        RefreshStocks -> refreshStocks()
        is SearchQueryChanged -> searchQueryChanged(intent.query)
        is StockClicked -> stockClicked(intent.symbol)
        is AddToPortfolio -> addToPortfolio(intent.symbol)
        is AddToWatchlist -> addToWatchlist(intent.symbol, intent.watchlistId)
    }

    override fun reduceUiState(
        previousState: StockSearchUiState,
        partialState: PartialState,
    ): StockSearchUiState = when (partialState) {
        is Loading -> previousState.copy(isLoading = true, isError = false)
        is Fetched -> previousState.copy(
            isLoading = false,
            isError = false,
            stocks = partialState.list
        )
        is SearchQueryChangedState -> previousState.copy(
            searchQuery = partialState.query,
            stocks = emptyList(), // Clear previous results
            isError = false
        )
        is Error -> {
            Timber.e(partialState.throwable)
            previousState.copy(isLoading = false, isError = true)
        }
    }

    private fun refreshStocks(): Flow<PartialState> = flow {
        // Refresh could re-run the current search query if needed
        emit(Fetched(emptyList()))
    }

    private fun searchQueryChanged(query: String): Flow<PartialState> = flow {
        emit(SearchQueryChangedState(query))
        if (query.length >= 2) { // Require at least 2 characters
            emit(Loading)
            delay(500) // Debounce
            try {
                val stocks = searchStocksFromServer(query)
                emit(Fetched(stocks))
            } catch (e: Exception) {
                Timber.e(e, "Error searching stocks")
                emit(Error(e))
            }
        } else {
            emit(Fetched(emptyList()))
        }
    }

    private suspend fun searchStocksFromServer(query: String): List<StockDisplayable> {
        return try {
            val response = stockApiService.searchStocks(query)
            if (response.isSuccessful) {
                val searchResults = response.body() ?: emptyList()
                searchResults.map { result ->
                    StockDisplayable(
                        symbol = result.symbol,
                        name = result.name,
                        price = "$${String.format("%.2f", result.price)}",
                        change = if (result.change >= 0) "+${String.format("%.2f", result.change)}" else "${String.format("%.2f", result.change)}",
                        changePercent = "${String.format("%.2f", result.changePercent)}%",
                        marketCapFormatted = result.marketCap?.let { formatNumber(it) } ?: "",
                        volumeFormatted = result.volume?.let { formatNumber(it) } ?: "",
                        exchange = result.exchange,
                        isPositive = result.change >= 0
                    )
                }
            } else {
                Timber.w("Search failed: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching stocks")
            emptyList()
        }
    }

    private fun formatNumber(number: Long): String {
        return when {
            number >= 1_000_000_000_000 -> "${number / 1_000_000_000_000}T"
            number >= 1_000_000_000 -> "${number / 1_000_000_000}B"
            number >= 1_000_000 -> "${number / 1_000_000}M"
            number >= 1_000 -> "${number / 1_000}K"
            else -> number.toString()
        }
    }

    private fun stockClicked(symbol: String): Flow<PartialState> = flow {
        setEvent(ShowStockDetails(symbol))
    }

    private fun addToPortfolio(symbol: String): Flow<PartialState> = flow {
        try {
            // Get or create the default watchlist
            val defaultWatchlist = watchlistRepository.getDefaultWatchlist() 
                ?: watchlistRepository.createWatchlist("My Watchlist", "#4CAF50")
            
            // Find the stock data from current search results
            val currentState = uiState.value
            val stockData = currentState.stocks.find { it.symbol == symbol }
            
            if (stockData != null) {
                // Add the stock to the default watchlist
                watchlistRepository.addStockToWatchlist(
                    watchlistId = defaultWatchlist.id,
                    symbol = symbol,
                    name = stockData.name,
                    price = stockData.price.replace("$", "").toDoubleOrNull() ?: 0.0,
                    change = stockData.change.replace("+", "").toDoubleOrNull() ?: 0.0,
                    changePercent = stockData.changePercent.replace("%", "").toDoubleOrNull() ?: 0.0,
                    shares = 0.0 // Default to 0 shares (watchlist only)
                )
                
                setEvent(AddedToPortfolio(symbol))
                setEvent(ShowMessage("$symbol added to ${defaultWatchlist.name}"))
            } else {
                setEvent(ShowMessage("Unable to add $symbol to portfolio"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding stock to portfolio")
            setEvent(ShowMessage("Error adding $symbol to portfolio"))
        }
    }

    private fun addToWatchlist(symbol: String, watchlistId: Long): Flow<PartialState> = flow {
        try {
            Timber.d("Adding $symbol to watchlist $watchlistId")
            
            // Find the stock data from current search results
            val currentState = uiState.value
            val stockData = currentState.stocks.find { it.symbol == symbol }
            
            if (stockData != null) {
                Timber.d("Found stock data: ${stockData.name} - ${stockData.price}")
                
                // Check if stock is already in watchlist
                val isAlreadyAdded = watchlistRepository.isStockInWatchlist(watchlistId, symbol)
                if (isAlreadyAdded) {
                    setEvent(ShowMessage("$symbol is already in this watchlist"))
                    return@flow
                }
                
                // Add the stock to the specified watchlist
                watchlistRepository.addStockToWatchlist(
                    watchlistId = watchlistId,
                    symbol = symbol,
                    name = stockData.name,
                    price = stockData.price.replace("$", "").toDoubleOrNull() ?: 0.0,
                    change = stockData.change.replace("+", "").replace("$", "").toDoubleOrNull() ?: 0.0,
                    changePercent = stockData.changePercent.replace("%", "").toDoubleOrNull() ?: 0.0,
                    shares = 0.0 // Default to 0 shares (watchlist only)
                )
                
                Timber.d("Successfully added $symbol to watchlist")
                setEvent(AddedToPortfolio(symbol))
                setEvent(ShowMessage("$symbol added to watchlist"))
            } else {
                Timber.w("Stock data not found for $symbol")
                setEvent(ShowMessage("Unable to add $symbol to watchlist"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding stock to watchlist")
            setEvent(ShowMessage("Error adding $symbol to watchlist: ${e.message}"))
        }
    }

}
