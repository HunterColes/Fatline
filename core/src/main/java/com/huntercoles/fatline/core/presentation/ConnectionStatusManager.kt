package com.huntercoles.fatline.core.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionStatusManager @Inject constructor() {

    private val _cloudColor = MutableStateFlow(Color.Red)
    val cloudColor: StateFlow<Color> = _cloudColor.asStateFlow()

    fun updateConnectionStatus(context: Context) {
        val connected = checkConnection(context)
        _cloudColor.value = if (connected) Color.Green else Color.Red
    }

    fun checkConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
