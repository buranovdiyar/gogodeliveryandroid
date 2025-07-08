package com.example.gogomarket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gogomarket.data.UserPreferences
import com.example.gogomarket.model.*
import com.example.gogomarket.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.CancellationException

class CourierViewModel(
    private val apiService: ApiService,
    val userPreferences: UserPreferences
) : ViewModel() {

    // --- ОБЩИЕ СОСТОЯНИЯ ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _userRole = MutableStateFlow<Int?>(null)
    val userRole: StateFlow<Int?> = _userRole

    private val _userName = MutableStateFlow("Курьер")
    val userName: StateFlow<String> = _userName

    // --- СОСТОЯНИЯ ДЛЯ РОЛИ 23 (КУРЬЕР ОТ ПРОДАВЦА) ---
    private val _stores = MutableStateFlow<List<Store>>(emptyList())
    val stores: StateFlow<List<Store>> = _stores

    private val _storeProducts = MutableStateFlow<ProductData?>(null)
    val storeProducts: StateFlow<ProductData?> = _storeProducts

    // --- СОСТОЯНИЯ ДЛЯ РОЛИ 24 (ДОСТАВЩИК) ---
    private val _newOrdersToDeliver = MutableStateFlow<List<OrderSummary>>(emptyList())
    val newOrdersToDeliver: StateFlow<List<OrderSummary>> = _newOrdersToDeliver

    private val _activeOrders = MutableStateFlow<List<OrderSummary>>(emptyList())
    val activeOrders: StateFlow<List<OrderSummary>> = _activeOrders

    private val _orderHistory = MutableStateFlow<List<OrderSummary>>(emptyList())
    val orderHistory: StateFlow<List<OrderSummary>> = _orderHistory

    private val _orderDetails = MutableStateFlow<OrderDetailsResponse?>(null)
    val orderDetails: StateFlow<OrderDetailsResponse?> = _orderDetails


    init {
        loadUserRole()
        loadUserName()
    }

    fun refreshUserData() {
        Log.d("USER_REFRESH", "Запущен принудительный рефреш данных пользователя.")
        loadUserRole()
        loadUserName()
    }

    fun loadUserRole() {
        viewModelScope.launch {
            _userRole.value = userPreferences.getUserRole()
        }
    }

    private fun loadUserName() {
        viewModelScope.launch {
            val firstName = userPreferences.getFirstName()
            val lastName = userPreferences.getLastName()
            if (!firstName.isNullOrBlank()) {
                _userName.value = "$firstName ${lastName ?: ""}".trim()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearAll()
            _userRole.value = null
        }
    }

    // --- МЕТОДЫ ДЛЯ РОЛИ 23 ---

    fun fetchStores() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _error.value = null
            try {
                val token = userPreferences.getAccessToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Ошибка авторизации: токен отсутствует"
                    return@launch
                }
                val response = apiService.getStores(token)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _stores.value = body.data?.data ?: emptyList()
                    } else {
                        _error.value = body?.message ?: "API вернул success=false."
                    }
                } else {
                    _error.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Неизвестная сетевая ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchStoreProducts(storeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _storeProducts.value = null
            try {
                val token = userPreferences.getAccessToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Ошибка авторизации: токен отсутствует"
                    return@launch
                }
                val response = apiService.getStoreProducts(token, storeId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _storeProducts.value = response.body()?.data
                } else {
                    _error.value = response.body()?.message ?: "Ошибка получения товаров"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStoreProducts() {
        _storeProducts.value = null
    }

    // ✅ ИЗМЕНЕНИЕ: Функция обновлена для обработки Boolean в ответе и без productId
    fun scanProductBarcode(barcode: String, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = userPreferences.getAccessToken()
                if (token.isNullOrEmpty()) {
                    onResult(false, "Ошибка авторизации: токен отсутствует")
                    _isLoading.value = false
                    return@launch
                }

                Log.d("SCAN_PRODUCT", "Сканирую штрихкод: $barcode")
                // Важно: этот вызов теперь ожидает Boolean в поле data
                val response = apiService.scanProductBarcode(token, barcode)

                val success = response.isSuccessful && response.body()?.success == true
                val message = response.body()?.message ?: if (success) "Успешно отсканирован" else "Ошибка сканирования"

                onResult(success, message)

            } catch (e: Exception) {
                Log.e("SCAN_PRODUCT", "Ошибка при сканировании продукта", e)
                onResult(false, "Ошибка ответа от сервера: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    // --- МЕТОДЫ ДЛЯ РОЛИ 24 ---

    fun fetchDelivererOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = userPreferences.getAccessToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Ошибка авторизации: токен отсутствует"
                    return@launch
                }
                val response = apiService.getDelivererOrders(token)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        val allOrders = body.data?.orders ?: emptyList()
                        _newOrdersToDeliver.value = allOrders.filter { it.status == DelivererOrderStatus.ASSEMBLING || it.status == DelivererOrderStatus.IN_PROGRESS }
                        _activeOrders.value = allOrders.filter { it.status == DelivererOrderStatus.IN_TRANSIT }
                        _orderHistory.value = allOrders.filter { it.status.isTerminal }
                    } else {
                        _error.value = body?.message ?: "Неизвестная ошибка API."
                    }
                } else {
                    _error.value = "Ошибка сети/сервера: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Сетевая ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchOrderDetails(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _orderDetails.value = null
            _error.value = null
            try {
                val token = userPreferences.getAccessToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Ошибка авторизации: токен отсутствует"
                    return@launch
                }
                val response = apiService.getOrderDetails(token, orderId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _orderDetails.value = response.body()?.data
                } else {
                    _error.value = response.body()?.message ?: "Ошибка загрузки деталей заказа"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearOrderDetails() {
        _orderDetails.value = null
    }

    fun takeOrderFromWarehouse(orderId: String, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = userPreferences.getAccessToken()
                if (token.isNullOrEmpty()) {
                    onResult(false, "Ошибка авторизации: токен отсутствует")
                    return@launch
                }
                // Теперь функция ПРАВИЛЬНО использует параметр orderId, который она получает
                val response = apiService.takeOrderFromWarehouse(token, orderId)
                val success = response.isSuccessful && response.body()?.success == true
                if (success) {
                    fetchDelivererOrders()
                }
                onResult(success, response.body()?.message ?: if (success) "Успешно" else "Ошибка")
            } catch (e: Exception) {
                onResult(false, "Сетевая ошибка: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmOrderDelivery(qrData: String, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = userPreferences.getAccessToken()
                if (token.isNullOrEmpty()) {
                    onResult(false, "Ошибка авторизации: токен отсутствует")
                    _isLoading.value = false
                    return@launch
                }

                Log.d("DELIVERY_CONFIRM", "Отправляю на сервер Order ID: $qrData")

                val response = apiService.confirmOrderDelivery(token, orderId = qrData)

                Log.d("DELIVERY_CONFIRM", "Получен код ответа от сервера: ${response.code()}")
                Log.d("DELIVERY_CONFIRM", "Получено тело ответа: ${response.body()}")

                // ✅ ИЗМЕНЕННАЯ ЛОГИКА
                // Проверяем, что в поле data пришло именно true
                val success = response.isSuccessful && response.body()?.data == true
                // Берем сообщение из верхнего уровня JSON
                val message = response.body()?.message ?: if(success) "Заказ успешно доставлен" else "Ошибка от сервера"

                Log.d("DELIVERY_CONFIRM", "Результат обработки: success=$success, message='$message'")

                if (success) {
                    fetchDelivererOrders()
                }

                onResult(success, message)
            } catch (e: Exception) {
                Log.e("DELIVERY_CONFIRM", "Произошла критическая ошибка", e)
                // Теперь в Toast будет выводиться именно сообщение от GSON, что мы и видим
                onResult(false, "Ошибка обработки ответа: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}