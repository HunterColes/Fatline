package com.huntercoles.fatline.portfoliofeature.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huntercoles.fatline.portfoliofeature.domain.model.Watchlist
import com.huntercoles.fatline.portfoliofeature.domain.model.WatchlistStock
import com.huntercoles.fatline.portfoliofeature.presentation.viewmodel.PortfolioViewModel
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = hiltViewModel(),
    onNavigateToStockSearch: () -> Unit = {},
    onNavigateToWatchlistDetail: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showCreateWatchlistDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with title and add watchlist button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Portfolio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { showCreateWatchlistDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Watchlist",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = { viewModel.refreshPortfolio() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh Portfolio",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onNavigateToStockSearch) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Stocks",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Watchlist tabs
        if (uiState.watchlists.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.watchlists) { watchlist ->
                    WatchlistTab(
                        watchlist = watchlist,
                        isSelected = watchlist.id == uiState.selectedWatchlistId,
                        onClick = { 
                            if (watchlist.id == uiState.selectedWatchlistId) {
                                // If already selected, navigate to detail page
                                onNavigateToWatchlistDetail(watchlist.id)
                            } else {
                                // Otherwise, just select it
                                viewModel.selectWatchlist(watchlist.id)
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Selected watchlist content
        uiState.selectedWatchlist?.let { selectedWatchlist ->
            WatchlistContent(
                watchlist = selectedWatchlist,
                stocks = uiState.selectedWatchlistStocks,
                onRemoveStock = { stock -> viewModel.removeStockFromWatchlist(stock.symbol) }
            )
        } ?: run {
            // Empty state
            EmptyPortfolioState(
                onCreateWatchlist = { showCreateWatchlistDialog = true },
                onSearchStocks = onNavigateToStockSearch
            )
        }
    }
    
    // Create watchlist dialog
    if (showCreateWatchlistDialog) {
        CreateWatchlistDialog(
            onCreateWatchlist = { name, color ->
                viewModel.createWatchlist(name, color)
                showCreateWatchlistDialog = false
            },
            onDismiss = { showCreateWatchlistDialog = false }
        )
    }
}

@Composable
private fun WatchlistTab(
    watchlist: Watchlist,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { Text(watchlist.name) },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(android.graphics.Color.parseColor(watchlist.color)),
            selectedLabelColor = Color.White
        ),
        leadingIcon = if (watchlist.isDefault) {
            { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null
    )
}

@Composable
private fun WatchlistContent(
    watchlist: Watchlist,
    stocks: List<WatchlistStock>,
    onRemoveStock: (WatchlistStock) -> Unit
) {
    Column {
        // Watchlist header with chart
        Text(
            text = watchlist.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (stocks.isNotEmpty()) {
            // Combined portfolio chart at top
            PortfolioChart(
                stocks = stocks,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 16.dp)
            )
            
            // Stocks table
            StocksTable(
                stocks = stocks,
                onRemoveStock = onRemoveStock
            )
        } else {
            EmptyWatchlistState()
        }
    }
}

@Composable
private fun EmptyPortfolioState(
    onCreateWatchlist: () -> Unit,
    onSearchStocks: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Start Building Your Portfolio",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create watchlists to track your favorite stocks",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onCreateWatchlist) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Watchlist")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(onClick = onSearchStocks) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search Stocks")
        }
    }
}

@Composable
private fun EmptyWatchlistState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No stocks yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Search for stocks to add to this watchlist",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MiniPriceChart(
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val chartColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    // Generate sample historical data points
    val points = remember(isPositive) {
        generateSamplePriceHistory(isPositive)
    }
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        if (points.size > 1) {
            val path = Path()
            
            // Calculate positions
            val xStep = width / (points.size - 1)
            val minValue = points.minOrNull() ?: 0f
            val maxValue = points.maxOrNull() ?: 1f
            val valueRange = maxValue - minValue
            
            points.forEachIndexed { index, value ->
                val x = index * xStep
                val y = height - ((value - minValue) / valueRange * height)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            // Draw the line
            drawPath(
                path = path,
                color = chartColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun generateSamplePriceHistory(isPositive: Boolean): List<Float> {
    val points = mutableListOf<Float>()
    var currentValue = 100f
    val trend = if (isPositive) 0.02f else -0.02f
    
    // Generate 20 data points with some randomness but overall trend
    for (i in 0 until 20) {
        // Add some randomness
        val noise = (Random.nextFloat() - 0.5f) * 5f
        currentValue += trend + noise
        points.add(currentValue.coerceAtLeast(1f))
    }
    
    return points
}

@Composable
private fun PortfolioChart(
    stocks: List<WatchlistStock>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerY = height / 2
                
                // Generate a combined portfolio line using average performance
                val points = (0..50).map { index ->
                    val progress = index / 50f
                    val x = width * progress
                    
                    // Simulate combined portfolio movement
                    val baseVariation = sin(progress * 12) * 0.3f
                    val trendVariation = progress * 0.2f - 0.1f
                    val randomVariation = (kotlin.random.Random.nextFloat() - 0.5f) * 0.1f
                    val totalVariation = baseVariation + trendVariation + randomVariation
                    
                    val y = centerY + (totalVariation * height * 0.3f)
                    Pair(x, y)
                }
                
                // Draw portfolio line
                val path = Path()
                points.forEachIndexed { index, (x, y) ->
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                // Determine color based on overall trend
                val startY = points.first().second
                val endY = points.last().second
                val isPositive = endY < startY
                val lineColor = if (isPositive) androidx.compose.ui.graphics.Color(0xFF4CAF50) else androidx.compose.ui.graphics.Color(0xFFF44336)
                
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            
            // Portfolio summary overlay
            Column(
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "Portfolio Performance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stocks.size} stocks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StocksTable(
    stocks: List<WatchlistStock>,
    onRemoveStock: (WatchlistStock) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Table header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Symbol",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Price",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.5f)
                )
                // Space for remove button
                Spacer(modifier = Modifier.weight(0.5f))
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Table rows
            LazyColumn {
                items(stocks) { stock ->
                    StockTableRow(
                        stock = stock,
                        onRemove = { onRemoveStock(stock) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockTableRow(
    stock: WatchlistStock,
    onRemove: () -> Unit
) {
    val changeColor = if ((stock.changePercent ?: 0.0) >= 0) {
        androidx.compose.ui.graphics.Color(0xFF4CAF50)
    } else {
        androidx.compose.ui.graphics.Color(0xFFF44336)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Symbol and name
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stock.name ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        
        // Price
        Text(
            text = "$${String.format("%.2f", stock.currentPrice ?: 0.0)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.5f)
        )
        
        // Change
        Column(
            modifier = Modifier.weight(1.5f),
            horizontalAlignment = Alignment.Start
        ) {
            val change = stock.change ?: 0.0
            val changePercent = stock.changePercent ?: 0.0
            Text(
                text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)}",
                style = MaterialTheme.typography.bodySmall,
                color = changeColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${if (changePercent >= 0) "+" else ""}${String.format("%.2f", changePercent)}%",
                style = MaterialTheme.typography.bodySmall,
                color = changeColor
            )
        }
        
        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.weight(0.5f)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
