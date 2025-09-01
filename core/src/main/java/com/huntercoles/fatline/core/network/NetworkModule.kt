package com.huntercoles.fatline.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.huntercoles.fatline.core.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    private const val INTERCEPTOR_LOGGING_NAME = "INTERCEPTOR_LOGGING"

    @Provides
    @Named(INTERCEPTOR_LOGGING_NAME)
    fun provideHttpLoggingInterceptor(): Interceptor = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    } else {
        noOpInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named(INTERCEPTOR_LOGGING_NAME) loggingInterceptor: Interceptor,
    ): OkHttpClient = OkHttpClient
        .Builder()
        .apply {
            addNetworkInterceptor(loggingInterceptor)
        }
        .build()

    /**
     * Provides configured Retrofit instance for API calls
     * Uses kotlinx.serialization for JSON parsing with sensible defaults
     * 
     * @param okHttpClient Pre-configured HTTP client with logging and timeouts
     * @return Retrofit instance ready for service creation
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = false
        }
        val contentType = "application/json".toMediaType()

        return Retrofit
            .Builder()
            .addConverterFactory(json.asConverterFactory(contentType))
            .baseUrl("http://10.0.2.2:8686/") // Android emulator localhost
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideStockApiService(retrofit: Retrofit): StockApiService {
        return retrofit.create(StockApiService::class.java)
    }

    private fun noOpInterceptor(): Interceptor = Interceptor { chain ->
        chain.proceed(chain.request())
    }
}
