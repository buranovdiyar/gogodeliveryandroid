package com.example.gogomarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gogomarket.data.UserPreferences
import com.example.gogomarket.network.ApiService

class CourierViewModelFactory(
    private val apiService: ApiService,
    private val userPrefs: UserPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourierViewModel::class.java)) {
            return CourierViewModel(apiService, userPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

