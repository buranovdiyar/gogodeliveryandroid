// AuthViewModelFactory.kt
package com.example.gogomarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gogomarket.data.UserPreferences
import com.example.gogomarket.network.ApiService

class AuthViewModelFactory(
    private val apiService: ApiService,
    private val userPrefs: UserPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // Передаем параметры в правильном порядке: сначала apiService, потом userPrefs
            return AuthViewModel(apiService, userPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}