package com.huntercoles.fatline.core.network

import com.huntercoles.fatline.core.network.model.StockQuoteResponse
import com.huntercoles.fatline.core.network.model.StockSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StockApiService {

    @GET("stocks/search")
    suspend fun searchStocks(@Query("q") query: String): Response<List<StockSearchResponse>>

    @GET("stocks/{symbol}/quote")
    suspend fun getStockQuote(@Path("symbol") symbol: String): Response<StockQuoteResponse>

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
