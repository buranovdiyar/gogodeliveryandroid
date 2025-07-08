// NetworkModule.kt
package com.example.gogomarket.network

import com.example.gogomarket.data.UserPreferences
import com.google.gson.GsonBuilder
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://admin.gogomarket.uz/api/v2/courier-app/"

    // 1. Основной HTTP-клиент с аутентификатором
    private fun provideOkHttpClient(authenticator: Authenticator): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .authenticator(authenticator) // Наш обработчик ошибок 401
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // 2. HTTP-клиент для запроса обновления токена (БЕЗ аутентификатора)
    private fun provideOkHttpClientForRefresh(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // 3. Основной сервис API
    fun provideApiService(userPreferences: UserPreferences): ApiService {
        val okHttpClient = provideOkHttpClient(TokenAuthenticator(userPreferences))
        val gson = GsonBuilder()
            .registerTypeAdapter(com.example.gogomarket.model.DelivererOrderStatus::class.java, com.example.gogomarket.model.DelivererOrderStatusDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    // 4. Сервис API специально для обновления токена
    fun provideApiServiceForRefresh(userPreferences: UserPreferences): ApiService {
        val okHttpClient = provideOkHttpClientForRefresh()
        val gson = GsonBuilder()
            .registerTypeAdapter(com.example.gogomarket.model.DelivererOrderStatus::class.java, com.example.gogomarket.model.DelivererOrderStatusDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}