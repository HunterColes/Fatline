package com.huntercoles.fatline.portfoliofeature.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import com.huntercoles.fatline.portfoliofeature.domain.model.Watchlist
import com.huntercoles.fatline.portfoliofeature.domain.model.WatchlistStock
import com.huntercoles.fatline.portfoliofeature.domain.model.StockLot
import com.huntercoles.fatline.portfoliofeature.presentation.viewmodel.PortfolioViewModel
import kotlin.random.Random
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = hiltViewModel(),
    onNavigateToStockSearch: () -> Unit = {},
    onNavigateToWatchlistDetail: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showCreateWatchlistDialog by remember { mutableStateOf(false) }
    var selectedStockForLots by remember { mutableStateOf<WatchlistStock?>(null) }
    var showAddLotDialog by remember { mutableStateOf(false) }
    
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
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh Portfolio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
                items(uiState.watchlists.size) { index ->
                    val watchlist = uiState.watchlists[index]
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
                isLoading = uiState.isLoading,
                onRemoveStock = { stock -> viewModel.removeStockFromWatchlist(stock.symbol) },
                onManageLots = { stock -> 
                    selectedStockForLots = stock
                    viewModel.setLotsDialogOpen(true)
                }
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
    
    // Manage lots dialog
    selectedStockForLots?.let { stock ->
        ManageLotsDialog(
            stock = stock,
            viewModel = viewModel,
            onAddLot = { 
                showAddLotDialog = true
                viewModel.setLotsDialogOpen(true)
            },
            onRemoveLot = { lotId -> viewModel.removeLotFromStock(lotId) },
            onDismiss = { 
                selectedStockForLots = null
                viewModel.setLotsDialogOpen(false)
            }
        )
    }
    
    // Add lot dialog
    if (showAddLotDialog && selectedStockForLots != null) {
        AddLotDialog(
            stock = selectedStockForLots!!,
            onAddLot = { shares, price, date ->
                viewModel.addLotToStock(selectedStockForLots!!.id, shares, price, date)
                showAddLotDialog = false
            },
            onDismiss = { showAddLotDialog = false }
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
    isLoading: Boolean,
    onRemoveStock: (WatchlistStock) -> Unit,
    onManageLots: (WatchlistStock) -> Unit
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
            // Portfolio total value display
            val totalPortfolioValue = stocks.sumOf { it.totalValue ?: 0.0 }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Portfolio Value",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$${String.format("%.2f", totalPortfolioValue)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Stocks table
            Box(modifier = Modifier.fillMaxWidth()) {
                StocksTable(
                    stocks = stocks,
                    onRemoveStock = onRemoveStock,
                    onManageLots = onManageLots
                )
                
                // Loading overlay
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Refreshing prices...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
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
            val xStep = width / (points.size - 1).toFloat()
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
                style = Stroke(width = 2f)
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
                    style = Stroke(width = 3f)
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
    onRemoveStock: (WatchlistStock) -> Unit,
    onManageLots: (WatchlistStock) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Table header - horizontally scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Symbol",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(120.dp),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Lots",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Price",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Change $",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Change %",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Avg Cost",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Return $",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Return %",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Value",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(100.dp),
                    textAlign = TextAlign.End
                )
                // Space for remove button
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Table rows - horizontally scrollable
            LazyColumn {
                items(stocks.size) { index ->
                    val stock = stocks[index]
                    StockTableRow(
                        stock = stock,
                        scrollState = scrollState,
                        onRemove = { onRemoveStock(stock) },
                        onManageLots = onManageLots
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
    scrollState: androidx.compose.foundation.ScrollState,
    onRemove: () -> Unit,
    onManageLots: (WatchlistStock) -> Unit
) {
    val changeColor = if ((stock.changePercent ?: 0.0) >= 0) {
        androidx.compose.ui.graphics.Color(0xFF4CAF50)
    } else {
        androidx.compose.ui.graphics.Color(0xFFF44336)
    }
    
    val returnColor = if ((stock.returnPercent ?: 0.0) >= 0) {
        androidx.compose.ui.graphics.Color(0xFF4CAF50)
    } else {
        androidx.compose.ui.graphics.Color(0xFFF44336)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Symbol
        Text(
            text = stock.symbol,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.Start
        )
        
        // Name
        Text(
            text = stock.name ?: "Unknown",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp),
            textAlign = TextAlign.Start,
            maxLines = 1
        )
        
        // Lots (Shares) - Clickable
        Text(
            text = stock.shares?.let { String.format("%.0f", it) } ?: "-",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .width(60.dp)
                .clickable { onManageLots(stock) }
                .background(
                    color = if (stock.shares != null) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            textAlign = TextAlign.End,
            color = if (stock.shares != null) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified
        )
        
        // Price
        Text(
            text = stock.currentPrice?.let { "$${String.format("%.2f", it)}" } ?: "-",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
        
        // Change $
        Text(
            text = stock.change?.let { 
                "${if (it >= 0) "+" else ""}${String.format("%.2f", it)}"
            } ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = changeColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
        
        // Change %
        Text(
            text = stock.changePercent?.let { 
                "${if (it >= 0) "+" else ""}${String.format("%.2f", it)}%"
            } ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = changeColor,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
        
        // Avg Cost
        Text(
            text = stock.averageCost?.let { "$${String.format("%.2f", it)}" } ?: "-",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
        
        // Return $
        Text(
            text = stock.totalReturn?.let { 
                "${if (it >= 0) "+" else ""}${String.format("%.2f", it)}"
            } ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = returnColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
        
        // Return %
        Text(
            text = stock.returnPercent?.let { 
                "${if (it >= 0) "+" else ""}${String.format("%.2f", it)}%"
            } ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = returnColor,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
        
        // Value
        Text(
            text = stock.totalValue?.let { "$${String.format("%.2f", it)}" } ?: "-",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp),
            textAlign = TextAlign.End
        )
        
        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.width(48.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageLotsDialog(
    stock: WatchlistStock,
    viewModel: PortfolioViewModel,
    onAddLot: () -> Unit,
    onRemoveLot: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val lots: List<StockLot> by viewModel.getStockLots(stock.id).collectAsState(initial = emptyList())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${stock.symbol} Lots",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                if (lots.isEmpty()) {
                    Text(
                        text = "No lots yet. Add your first lot below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(lots.size) { index ->
                            val lot = lots[index]
                            LotItem(
                                lot = lot,
                                currentPrice = stock.currentPrice,
                                onRemove = { onRemoveLot(lot.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAddLot) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Lot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun LotItem(
    lot: com.huntercoles.fatline.portfoliofeature.domain.model.StockLot,
    currentPrice: Double?,
    onRemove: () -> Unit
) {
    val returnColor = lot.returnPercent(currentPrice)?.let { percent ->
        if (percent >= 0) androidx.compose.ui.graphics.Color(0xFF4CAF50)
        else androidx.compose.ui.graphics.Color(0xFFF44336)
    } ?: MaterialTheme.colorScheme.onSurfaceVariant
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${String.format("%.0f", lot.shares)} shares",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$${String.format("%.2f", lot.pricePerShare)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total: $${String.format("%.2f", lot.totalCost)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        lot.returnPercent(currentPrice)?.let { percent ->
                            Text(
                                text = "${if (percent >= 0) "+" else ""}${String.format("%.1f", percent)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = returnColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove lot",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLotDialog(
    stock: WatchlistStock,
    onAddLot: (Double, Double, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var shares by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Lot - ${stock.symbol}",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = shares,
                    onValueChange = { shares = it },
                    label = { Text("Number of Shares") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price per Share") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = date,
                    onValueChange = { 
                        date = it
                        dateError = null // Clear error when user types
                    },
                    label = { Text("Purchase Date (MM/DD/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = dateError != null,
                    supportingText = dateError?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sharesValue = shares.toDoubleOrNull()
                    val priceValue = price.toDoubleOrNull()
                    
                    // Parse and validate date
                    val dateValue = parseDate(date)
                    if (dateValue == null) {
                        dateError = "Invalid date format. Use MM/DD/YYYY"
                        return@Button
                    }
                    
                    if (dateValue > System.currentTimeMillis()) {
                        dateError = "Purchase date cannot be in the future"
                        return@Button
                    }
                    
                    if (sharesValue != null && priceValue != null && sharesValue > 0 && priceValue > 0) {
                        onAddLot(sharesValue, priceValue, dateValue)
                    }
                },
                enabled = shares.isNotBlank() && price.isNotBlank() && date.isNotBlank() && dateError == null
            ) {
                Text("Add Lot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun parseDate(dateString: String): Long? {
    return try {
        val parts = dateString.split("/")
        if (parts.size != 3) return null
        
        val month = parts[0].toIntOrNull()?.minus(1) ?: return null // Calendar.MONTH is 0-based
        val day = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        
        if (month !in 0..11 || day !in 1..31 || year < 1900 || year > 2100) return null
        
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    } catch (e: Exception) {
        null
    }
}
