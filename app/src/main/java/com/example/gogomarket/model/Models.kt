// Models.kt
package com.example.gogomarket.model

import com.google.gson.annotations.SerializedName

// --- Общая обертка для ответов ---
data class BaseResponse<T>(
    val success: Boolean,
    val status: Int,
    val data: T?,
    val message: String?
)

// --- Модели для Авторизации ---

data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

// Models.kt
data class LoginData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String // <-- ДОБАВЛЕНО

)



data class UserData(
    val id: Int,
    val email: String?,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val role: Int
)

// --- Модели для Роли 23 (Курьер от продавца) ---
data class StoreResponseData(
    val data: List<Store>,
    val total: String,
    @SerializedName("current_page") val currentPage: Int
)

// --- Модели для Роли 23 (Детали магазина) ---

data class ProductResponse(
    val data: ProductData
)

data class ProductData(
    val store: StoreInfo,
    val products: List<ProductEntry>
)

data class StoreInfo(
    @SerializedName("store_name") val storeName: String,
    @SerializedName("store_address") val storeAddress: String,
    @SerializedName("store_latitude") val storeLatitude: String?,
    @SerializedName("store_longitude") val storeLongitude: String?,
    @SerializedName("store_phone") val storePhone: String?
)

data class ProductEntry(
    val order: OrderInfo,
    val product: ProductInfo
)

// OrderInfo уже есть выше, но если его нет, используй этот
// data class OrderInfo(
//     val id: Int,
//     val qty: Int,
//     val status: Int
// )

data class ProductInfo(
    val id: String,
    @SerializedName("stock_id") val stockId: String,
    val barcode: String?,
    val status: Int,
    val title: String,
    @SerializedName("stock_data") val stockData: String?,
    val image: ImageInfo?
)

data class Store(
    val id: String,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("store_address") val storeAddress: String,
    @SerializedName("total_order_items") val totalOrderItems: String
)

// --- Модели для Роли 24 (Доставщик) ---

// Для списка заказов (get-orders)
data class OrdersListResponse(
    val orders: List<OrderSummary>,
    val total: String,
    @SerializedName("current_page") val currentPage: Int
)

data class OrderSummary(
    val id: Int,
    val qty: String,
    val status: DelivererOrderStatus,
    val time: String
)

enum class DelivererOrderStatus(val rawValue: Int) {
    WAITING_FOR_PAYMENT(0),
    IN_PROGRESS(1),
    ASSEMBLING(2),
    IN_TRANSIT(3),
    DELIVERED(4),
    CANCELLED_BY_SYSTEM(6),
    CANCELLED(7),
    CANCELLED_BY_SELLER(8),
    CANCELLED_BY_CLIENT(10),
    UNKNOWN(-1);

    val title: String get() = when (this) {
        WAITING_FOR_PAYMENT -> "Ожидание оплаты"
        IN_PROGRESS -> "В процессе работы"
        ASSEMBLING -> "Сборка на складе"
        IN_TRANSIT -> "В пути"
        DELIVERED -> "Товар доставлен"
        CANCELLED_BY_SYSTEM, CANCELLED, CANCELLED_BY_SELLER, CANCELLED_BY_CLIENT -> "Отменен"
        UNKNOWN -> "Неизвестный статус"
    }

    val isTerminal: Boolean get() = when (this) {
        DELIVERED, CANCELLED_BY_SYSTEM, CANCELLED, CANCELLED_BY_SELLER, CANCELLED_BY_CLIENT -> true
        else -> false
    }

    companion object {
        fun fromInt(value: Int) = values().find { it.rawValue == value } ?: UNKNOWN
    }
}


// Для деталей заказа (get-order-details)
data class OrderDetailsResponse(
    val user: OrderClientInfo,
    val order: OrderInfo,
    val address: OrderAddress,
    val items: List<OrderItem>
)

data class OrderClientInfo(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String
)

data class OrderInfo(
    val id: Int,
    val qty: Int,
    val status: Int

)

data class OrderAddress(
    val city: String,
    val district: String,
    val street: String,
    val house: String,
    val block: String?,
    val flat: String?,
    val latitude: String?,
    val longitude: String?
)

data class OrderItem(
    val id: String,
    val title: String,
    val image: ImageInfo?,
    val qty: Int
)

data class ImageInfo(
    val url: String?
)

// Для ответа от подтверждения доставки (confirm-order-delivery)
data class ConfirmDeliveryResponseData(
    val errorCode: Int,
    val message: String,
    val result: String?
)

data class ScannedProductInfo(
    @SerializedName("product_id") val productId: String,
)