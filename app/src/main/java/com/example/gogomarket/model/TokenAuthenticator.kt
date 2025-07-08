// TokenAuthenticator.kt
package com.example.gogomarket.network

import com.example.gogomarket.data.UserPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val userPreferences: UserPreferences
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Получаем текущий refresh_token ИСПРАВЛЕННЫМ способом
        val refreshToken = runBlocking { userPreferences.getRefreshToken() }

        if (refreshToken.isNullOrBlank()) {
            return null // Обновлять нечем
        }

        // Синхронно выполняем запрос на обновление токена
        val apiServiceForRefresh = NetworkModule.provideApiServiceForRefresh(userPreferences)
        val newTokensResponse = runBlocking {
            try {
                apiServiceForRefresh.refreshToken(mapOf("refresh_token" to refreshToken))
            } catch (e: Exception) {
                null
            }
        }

        val newAccessToken = newTokensResponse?.body()?.data?.accessToken
        val newRefreshToken = newTokensResponse?.body()?.data?.refreshToken

        // Если мы получили новые токены, сохраняем их и повторяем запрос.
        return if (newTokensResponse != null && newTokensResponse.isSuccessful && !newAccessToken.isNullOrEmpty() && !newRefreshToken.isNullOrEmpty()) {
            runBlocking {
                userPreferences.saveTokens("Bearer $newAccessToken", newRefreshToken)
            }
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            // Если обновить токен не удалось, выходим из аккаунта
            runBlocking { userPreferences.clearAll() }
            null
        }
    }
}