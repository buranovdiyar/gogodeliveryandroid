// AuthViewModel.kt
package com.example.gogomarket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gogomarket.data.UserPreferences
import com.example.gogomarket.model.LoginRequest
import com.example.gogomarket.model.UserData
import com.example.gogomarket.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(phoneNumber: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.login(LoginRequest(phoneNumber, password))
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()?.data
                    val accessToken = loginData?.accessToken
                    // ВРЕМЕННОЕ РЕШЕНИЕ: Мы не проверяем refresh_token, просто сохраняем его, если он есть
                    val refreshToken = loginData?.refreshToken ?: "" // Если null, сохраняем пустую строку

                    if (!accessToken.isNullOrEmpty()) {
                        // Сохраняем то, что есть. refreshToken будет пустой строкой, если не пришел.
                        userPreferences.saveTokens("Bearer $accessToken", refreshToken)
                        Log.d("AuthViewModel", "Токены сохранены (refresh_token может быть пустым).")
                        getUserInfo(onSuccess)
                    } else {
                        _error.value = "Не удалось получить access_token от сервера"
                        _isLoading.value = false
                    }
                } else {
                    _error.value = response.body()?.message ?: "Неправильный логин или пароль"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Сетевая ошибка: ${e.message}"
                _isLoading.value = false
                Log.e("AuthViewModel", "Ошибка при логине", e)
            }
        }
    }

    private fun getUserInfo(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = userPreferences.getAccessToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Токен отсутствует, не могу получить данные пользователя"
                    _isLoading.value = false
                    return@launch
                }

                val response = apiService.getUser(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()?.data
                    if (userData != null) {
                        saveUserData(userData)
                        onSuccess()
                    } else {
                        _error.value = "Не удалось получить данные пользователя"
                    }
                } else {
                    _error.value = response.body()?.message ?: "Ошибка получения данных пользователя"
                }
            } catch (e: Exception) {
                _error.value = "Сетевая ошибка: ${e.message}"
                Log.e("AuthViewModel", "Ошибка при получении данных пользователя", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveUserData(userData: UserData) {
        Log.d("AuthViewModel", "Сохраняю данные пользователя: ${userData.firstName} ${userData.lastName}, Роль: ${userData.role}")
        userPreferences.saveUserRole(userData.role)
        userPreferences.saveFullName(userData.firstName, userData.lastName)
    }
}