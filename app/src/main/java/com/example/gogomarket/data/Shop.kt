package com.example.gogomarket.data

data class Shop(
    val name: String,
    val address: String,
    val productCount: Int
)

val sampleShops = listOf(
    Shop("Онлайн магазин A", "улица Муофачилар, 29А", 2),
    Shop("Chinni Bazar", "Abu saxy chinni bozor", 2),
    Shop("Buranov Diyar", "Водопьянова 9", 1)
)

