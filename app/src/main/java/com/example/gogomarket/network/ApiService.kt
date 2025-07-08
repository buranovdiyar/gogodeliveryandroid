// ApiService.kt
package com.example.gogomarket.network

import com.example.gogomarket.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<BaseResponse<LoginData>>

    // Новый метод для обновления токена
    @POST("refresh-token") // Убедитесь, что эндпоинт совпадает с тем, что на бэкенде
    suspend fun refreshToken(@Body request: Map<String, String>): Response<BaseResponse<LoginData>>

    @GET("get-user")
    suspend fun getUser(@Header("Authorization") token: String): Response<BaseResponse<UserData>>

    // --- Роль 23: Курьер от продавца ---

    @GET("get-stores")
    suspend fun getStores(@Header("Authorization") token: String): Response<BaseResponse<StoreResponseData>>

    @GET("get-store-products")
    suspend fun getStoreProducts(
        @Header("Authorization") token: String,
        @Query("store_id") storeId: String
    ): Response<BaseResponse<ProductData>>

    @GET("scan-product-barcode")
    suspend fun scanProductBarcode(
        @Header("Authorization") token: String,
        @Query("barcode") barcode: String
    ): Response<BaseResponse<Boolean>> // TODO: Уточнить модель ответа

    // --- Роль 24: Доставщик до клиента ---

    @GET("get-orders")
    suspend fun getDelivererOrders(@Header("Authorization") token: String): Response<BaseResponse<OrdersListResponse>>

    @GET("get-order-details")
    suspend fun getOrderDetails(
        @Header("Authorization") token: String,
        @Query("order_id") orderId: Int
    ): Response<BaseResponse<OrderDetailsResponse>>

    @GET("take-order-from-warehouse")
    suspend fun takeOrderFromWarehouse(
        @Header("Authorization") token: String,
        @Query("order_id") orderId: String
    ): Response<BaseResponse<Boolean>>

    @GET("confirm-order-delivery")
    suspend fun confirmOrderDelivery(
        @Header("Authorization") token: String,
        @Query("order_id") orderId: String
    ): Response<BaseResponse<Boolean>> //
}