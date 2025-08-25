package com.huntercoles.fatline.basicfeature.presentation.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.huntercoles.fatline.portfoliofeature.domain.model.Watchlist
import com.huntercoles.fatline.basicfeature.presentation.StockSearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistSelectionDialog(
    watchlists: List<Watchlist>,
    stockSymbol: String,
    onWatchlistSelected: (Watchlist) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: StockSearchViewModel = hiltViewModel()
) {
    var selectedWatchlist by remember { mutableStateOf<Watchlist?>(null) }
    var stockMembership by remember { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check which watchlists already contain this stock
    LaunchedEffect(watchlists, stockSymbol) {
        val membership = mutableMapOf<Long, Boolean>()
        watchlists.forEach { watchlist ->
            membership[watchlist.id] = viewModel.isStockInWatchlist(watchlist.id, stockSymbol)
        }
        stockMembership = membership
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add $stockSymbol to Watchlist",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (watchlists.isEmpty()) {
                    // No watchlists exist, prompt to create one
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No watchlists yet",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Create your first watchlist to start tracking stocks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Button(onClick = onCreateNew) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Watchlist")
                        }
                    }
                } else {
                    // Show existing watchlists
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(watchlists) { watchlist ->
                            val isSelected = selectedWatchlist?.id == watchlist.id
                            val isAlreadyAdded = stockMembership[watchlist.id] == true
                            
                            Card(
                                onClick = { 
                                    if (!isAlreadyAdded) {
                                        selectedWatchlist = watchlist 
                                    }
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isAlreadyAdded -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer 
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                                border = if (isSelected && !isAlreadyAdded) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (watchlist.isDefault) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Default",
                                            tint = if (isAlreadyAdded) 
                                                Color.Gray 
                                            else 
                                                Color(android.graphics.Color.parseColor(watchlist.color)),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    
                                    Text(
                                        text = watchlist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            isAlreadyAdded -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer 
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    when {
                                        isAlreadyAdded -> {
                                            Text(
                                                text = "Added",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        isSelected -> {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Create new watchlist option
                    OutlinedButton(
                        onClick = onCreateNew,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Watchlist")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            selectedWatchlist?.let { watchlist ->
                                onWatchlistSelected(watchlist)
                            }
                        },
                        enabled = selectedWatchlist != null && stockMembership[selectedWatchlist?.id] != true
                    ) {
                        Text("Add to List")
                    }
                }
            }
        }
    }
}
