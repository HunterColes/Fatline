package com.huntercoles.fatline.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockSearchResponse(
    @SerialName("symbol") val symbol: String,
    @SerialName("name") val name: String,
    @SerialName("price") val price: Double,
    @SerialName("change") val change: Double,
    @SerialName("changePercent") val changePercent: Double,
    @SerialName("marketCap") val marketCap: Long? = null,
    @SerialName("volume") val volume: Long? = null,
    @SerialName("exchange") val exchange: String,
    @SerialName("currency") val currency: String = "USD"
)

@Serializable
data class StockQuoteResponse(
    @SerialName("symbol") val symbol: String,
    @SerialName("price") val price: Double? = null,
    @SerialName("currency") val currency: String = "USD",
    @SerialName("name") val name: String? = null
)
