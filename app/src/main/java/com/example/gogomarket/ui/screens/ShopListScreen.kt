package com.example.gogomarket.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gogomarket.data.Shop
import com.example.gogomarket.data.sampleShops

@Composable
fun ShopListScreen(
    shops: List<Shop> = sampleShops,
    onShopClick: (Shop) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Магазины для забора товаров",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        shops.forEach { shop ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onShopClick(shop) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = shop.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shop.address,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Товаров: ${shop.productCount}",
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}