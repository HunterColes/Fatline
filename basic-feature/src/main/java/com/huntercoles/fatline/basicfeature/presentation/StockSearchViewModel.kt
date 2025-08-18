package com.huntercoles.fatline.basicfeature.presentation

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.huntercoles.fatline.core.presentation.mvi.BaseViewModel
import com.huntercoles.fatline.basicfeature.presentation.StockSearchEvent.AddedToPortfolio
import com.huntercoles.fatline.basicfeature.presentation.StockSearchEvent.ShowMessage
import com.huntercoles.fatline.basicfeature.presentation.StockSearchEvent.ShowStockDetails
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.AddToPortfolio
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.RefreshStocks
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.SearchQueryChanged
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent.StockClicked
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState.Error
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState.Fetched
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState.Loading
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState.PartialState.SearchQueryChanged as SearchQueryChangedState
import com.huntercoles.fatline.basicfeature.presentation.model.StockDisplayable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray

@HiltViewModel
class StockSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stockSearchUiState: StockSearchUiState
) : BaseViewModel<StockSearchUiState, PartialState, StockSearchEvent, StockSearchIntent>(
    savedStateHandle = savedStateHandle,
    initialState = stockSearchUiState,
) {

    override fun mapIntents(intent: StockSearchIntent): Flow<PartialState> = when (intent) {
        RefreshStocks -> refreshStocks()
        is SearchQueryChanged -> searchQueryChanged(intent.query)
        is StockClicked -> stockClicked(intent.symbol)
        is AddToPortfolio -> addToPortfolio(intent.symbol)
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
                val stocks = withContext(Dispatchers.IO) {
                    searchStocksFromServer(query)
                }
                emit(Fetched(stocks))
            } catch (e: Exception) {
                Timber.e(e, "Error searching stocks")
                emit(Error(e))
            }
        } else {
            emit(Fetched(emptyList()))
        }
    }

    private fun searchStocksFromServer(query: String): List<StockDisplayable> {
        val url = URL("http://10.0.2.2:8686/stocks/search?q=${query}")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        
        return try {
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(response)
                val stocks = mutableListOf<StockDisplayable>()
                
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val change = json.optDouble("change", 0.0)
                    val changePercent = json.optDouble("changePercent", 0.0)
                    val price = json.optDouble("price", 0.0)
                    
                    stocks.add(StockDisplayable(
                        symbol = json.optString("symbol"),
                        name = json.optString("name"),
                        price = "$${String.format("%.2f", price)}",
                        change = if (change >= 0) "+${String.format("%.2f", change)}" else "${String.format("%.2f", change)}",
                        changePercent = "${String.format("%.2f", changePercent)}%",
                        marketCapFormatted = formatNumber(json.optLong("marketCap")),
                        volumeFormatted = formatNumber(json.optLong("volume")),
                        exchange = json.optString("exchange"),
                        isPositive = change >= 0
                    ))
                }
                stocks
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching stocks")
            emptyList()
        } finally {
            conn.disconnect()
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
        setEvent(AddedToPortfolio(symbol))
        setEvent(ShowMessage("$symbol added to portfolio"))
    }

}
