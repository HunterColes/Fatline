package com.huntercoles.fatline.basicfeature.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huntercoles.fatline.basicfeature.R
import com.huntercoles.fatline.basicfeature.presentation.StockSearchIntent
import com.huntercoles.fatline.basicfeature.presentation.StockSearchUiState
import com.huntercoles.fatline.basicfeature.presentation.StockSearchViewModel
import com.huntercoles.fatline.basicfeature.presentation.model.StockDisplayable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.huntercoles.fatline.portfoliofeature.domain.model.Watchlist

@Composable
fun StockSearchRoute(viewModel: StockSearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val watchlists by viewModel.watchlists.collectAsStateWithLifecycle(emptyList())

    StockSearchScreen(
        uiState = uiState,
        watchlists = watchlists,
        onIntent = viewModel::acceptIntent,
    )
}

@Composable
internal fun StockSearchScreen(
    uiState: StockSearchUiState,
    watchlists: List<Watchlist>,
    onIntent: (StockSearchIntent) -> Unit,
) {
    var showWatchlistDialog by remember { mutableStateOf(false) }
    var selectedStockSymbol by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onSearchConfirmed = { onIntent(StockSearchIntent.SearchQueryChanged(it)) },
            onQueryChange = { onIntent(StockSearchIntent.SearchQueryChanged("") ) }, // clear results on text change, optional
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Content
        when {
            uiState.isLoading -> LoadingContent()
            uiState.isError -> ErrorContent()
            uiState.stocks.isNotEmpty() -> StockListContent(
                stocks = uiState.stocks,
                watchlists = watchlists,
                onStockClick = { onIntent(StockSearchIntent.StockClicked(it)) },
                onAddToPortfolio = { symbol ->
                    selectedStockSymbol = symbol
                    showWatchlistDialog = true
                }
            )
            uiState.searchQuery.isEmpty() -> EmptySearchContent()
            else -> NoResultsContent()
        }
    }
    
    // Watchlist selection dialog
    if (showWatchlistDialog && selectedStockSymbol != null) {
        WatchlistSelectionDialog(
            watchlists = watchlists,
            stockSymbol = selectedStockSymbol!!,
            onWatchlistSelected = { watchlist ->
                selectedStockSymbol?.let { symbol ->
                    onIntent(StockSearchIntent.AddToWatchlist(symbol, watchlist.id))
                }
                showWatchlistDialog = false
                selectedStockSymbol = null
            },
            onCreateNew = {
                // TODO: Handle create new watchlist
                showWatchlistDialog = false
                selectedStockSymbol = null
            },
            onDismiss = {
                showWatchlistDialog = false
                selectedStockSymbol = null
            }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onSearchConfirmed: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(query) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search stocks (e.g., AAPL, Tesla)") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchConfirmed(text) }
        )
    )
}

@Composable
private fun StockListContent(
    stocks: List<StockDisplayable>,
    watchlists: List<Watchlist>,
    onStockClick: (String) -> Unit,
    onAddToPortfolio: (String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(stocks) { stock ->
            StockCard(
                stock = stock,
                watchlists = watchlists,
                onClick = { onStockClick(stock.symbol) },
                onAddToPortfolio = { onAddToPortfolio(stock.symbol) }
            )
        }
    }
}

@Composable
private fun StockCard(
    stock: StockDisplayable,
    watchlists: List<Watchlist>,
    onClick: () -> Unit,
    onAddToPortfolio: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stock.exchange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = stock.price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${stock.change} (${stock.changePercent})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (stock.isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            IconButton(onClick = onAddToPortfolio) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to Portfolio",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Error loading stocks. Please try again.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun EmptySearchContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Search for stocks",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Enter a stock symbol or company name",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoResultsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No stocks found.\nTry a different search term.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
