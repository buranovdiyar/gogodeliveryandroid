// StoreProductListScreen.kt
package com.example.gogomarket.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.gogomarket.model.OrderInfo
import com.example.gogomarket.ui.components.DetailRow
import com.example.gogomarket.model.ProductEntry
import com.example.gogomarket.model.StoreInfo
import com.example.gogomarket.viewmodel.CourierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreProductListScreen(
    storeId: String,
    viewModel: CourierViewModel,
    navController: NavController
) {
    val productData by viewModel.storeProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(storeId) {
        viewModel.fetchStoreProducts(storeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(productData?.store?.storeName ?: "Загрузка...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading && productData == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = "Ошибка: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                productData != null -> {
                    val store = productData!!.store
                    val products = productData!!.products

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Товары к получению",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                            )
                        }

                        items(products) { entry ->
                            ProductItemRow(entry = entry, navController = navController)
                        }

                        item {
                            StoreInfoSection(details = store)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreInfoSection(
    details: StoreInfo,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Text("Адрес магазина", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        DetailRow(icon = Icons.Default.Place, title = "Адрес", value = details.storeAddress)

        details.storePhone?.let { phone ->
            if (phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(icon = Icons.Default.Phone, title = "Телефон", value = phone)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (details.storeLatitude != null && details.storeLongitude != null) {
                        try {
                            val latitude = details.storeLatitude.toDouble()
                            val longitude = details.storeLongitude.toDouble()
                            val geoUri = "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(details.storeName)})"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Картографические приложения не найдены", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Неверные координаты адреса", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF808080))
            ) {
                Icon(Icons.Default.Map, contentDescription = "Маршрут")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Маршрут")
            }
            if (details.storePhone?.isNotBlank() == true) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${details.storePhone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF808080))
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Звонок")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Звонок")
                }
            }
        }
    }
}

@Composable
private fun ProductItemRow(entry: ProductEntry, navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = rememberAsyncImagePainter(entry.product.image?.url),
                contentDescription = entry.product.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.product.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "#${entry.order.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(
                                color = Color(0xFFFFE0B2), // Светло-оранжевый
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color(0xFFFF6B00)),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ✅ ИЗМЕНЕНИЕ: Row заменен на Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Количество: ${entry.order.qty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFF0F0F0), // Светло-серый
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Text(
                        text = "Статус: ${mapOrderStatusToString(entry.order.status)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFD1E9FF), // Светло-синий
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color(0xFF007BFF)),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { navController.navigate("scan/product") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = "Сканировать"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Сканировать")
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

private fun mapOrderStatusToString(status: Int): String {
    return when (status) {
        0 -> "Ожидание оплаты"
        1 -> "В процессе работы"
        2 -> "Сборка на складе"
        3 -> "В пути"
        4 -> "Товар доставлен"
        6 -> "Заказ отменен"
        7 -> "Отменен"
        8 -> "Отменен продавцом"
        10 -> "Отменен клиентом"
        else -> "Неизвестно"
    }
}