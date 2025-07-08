// OrderToDeliverDetailScreen.kt
package com.example.gogomarket.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.gogomarket.model.OrderAddress
import com.example.gogomarket.model.OrderClientInfo
import com.example.gogomarket.model.OrderItem
import com.example.gogomarket.ui.components.DetailRow
import com.example.gogomarket.viewmodel.CourierViewModel

// Функция для форматирования номера для звонка (просто добавляет "+")
private fun formatPhoneNumberForDialing(phoneNumber: String): String {
    val digitsOnly = phoneNumber.filter { it.isDigit() }
    if (digitsOnly.startsWith("998") && digitsOnly.length == 12) {
        return "+$digitsOnly"
    }
    return if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"
}

// ✅ НОВАЯ ФУНКЦИЯ для красивого отображения номера на экране
private fun formatPhoneNumberForDisplay(phoneNumber: String): String {
    val digitsOnly = phoneNumber.filter { it.isDigit() }
    if (digitsOnly.startsWith("998") && digitsOnly.length == 12) {
        val country = digitsOnly.substring(0, 3)      // 998
        val operator = digitsOnly.substring(3, 5)     // 90
        val part1 = digitsOnly.substring(5, 8)        // 820
        val part2 = digitsOnly.substring(8, 10)       // 99
        val part3 = digitsOnly.substring(10, 12)      // 46
        return "+$country $operator $part1 $part2 $part3" // -> +998 90 820 99 46
    }
    return phoneNumber // Возвращаем как есть, если формат не стандартный
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderToDeliverDetailScreen(
    orderId: Int,
    viewModel: CourierViewModel,
    navController: NavController
) {
    val orderDetails by viewModel.orderDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearOrderDetails()
        }
    }

    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetails(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заказ #$orderId") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                isLoading && orderDetails == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = "Ошибка: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                orderDetails != null -> {
                    val details = orderDetails!!

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ClientInfoSection(client = details.user)
                        }
                        item {
                            AddressSection(
                                address = details.address,
                                phoneNumber = details.user.phoneNumber
                            )
                        }
                        item {
                            Text("Товары в заказе", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(details.items) { item ->
                            OrderItemRow(item = item)
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    navController.navigate("scan/confirm_delivery")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Передать заказ клиенту")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientInfoSection(client: OrderClientInfo) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Клиент", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DetailRow(icon = Icons.Default.Person, title = "Имя", value = "${client.firstName} ${client.lastName}")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    // ✅ ИЗМЕНЕНИЕ: Форматируем номер для отображения
                    DetailRow(icon = Icons.Default.Phone, title = "Телефон", value = formatPhoneNumberForDisplay(client.phoneNumber))
                }
                IconButton(onClick = {
                    val formattedPhone = formatPhoneNumberForDialing(client.phoneNumber)
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$formattedPhone"))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Позвонить клиенту")
                }
            }
        }
    }
}

@Composable
private fun AddressSection(address: OrderAddress, phoneNumber: String) {
    val context = LocalContext.current
    val fullAddress = buildString {
        append(address.city)
        append(", ${address.district}")
        append(", ${address.street}")
        append(", ${address.house}")
        address.flat?.let { if(it.isNotBlank()) append(", кв. $it") }
        address.block?.let { if(it.isNotBlank()) append(", корп. $it") }
    }.trim()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Адрес доставки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DetailRow(icon = Icons.Default.Place, title = "Адрес", value = fullAddress)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (address.latitude != null && address.longitude != null) {
                    Button(
                        onClick = {
                            try {
                                val lat = address.latitude.toDoubleOrNull()
                                val lon = address.longitude.toDoubleOrNull()
                                if (lat != null && lon != null) {
                                    val geoUri = "geo:$lat,$lon?q=$lat,$lon(Адрес доставки)"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Неверные координаты адреса", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Не удалось открыть карту", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Маршрут")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Маршрут")
                    }
                }

                Button(
                    onClick = {
                        val formattedPhone = formatPhoneNumberForDialing(phoneNumber)
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$formattedPhone"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Позвонить")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Звонок")
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = item.image?.url),
            contentDescription = item.title,
            modifier = Modifier
                .size(60.dp)
                .padding(end = 12.dp),
            contentScale = ContentScale.Crop
        )
        Column {
            Text(item.title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
            Text("Количество: ${item.qty}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}