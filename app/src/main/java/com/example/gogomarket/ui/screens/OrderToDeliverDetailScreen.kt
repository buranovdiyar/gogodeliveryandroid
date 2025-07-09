// OrderToDeliverDetailScreen.kt
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// Функции-помощники остаются без изменений...
private fun mapOrderStatusToString(status: Int): String {
    return when (status) {
        0 -> "Ожидание оплаты"; 1 -> "Ожидаем подтверждение от продавца"; 2 -> "Продавец подтвердил"
        3 -> "Курьер везет на склад"; 4 -> "На складе"; 5 -> "В пути"; 6 -> "Клиент забрал товар"
        7 -> "Отменен"; 8 -> "Отменен продавцом"; 9 -> "Частично отменен"; 10 -> "Отменен клиентом"
        else -> "Неизвестный статус"
    }
}
private fun formatPhoneNumberForDialing(phoneNumber: String): String {
    val digitsOnly = phoneNumber.filter { it.isDigit() }
    if (digitsOnly.startsWith("998") && digitsOnly.length == 12) return "+$digitsOnly"
    return if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"
}
private fun formatPhoneNumberForDisplay(phoneNumber: String): String {
    val digitsOnly = phoneNumber.filter { it.isDigit() }
    if (digitsOnly.startsWith("998") && digitsOnly.length == 12) {
        val country = digitsOnly.substring(0, 3); val operator = digitsOnly.substring(3, 5)
        val part1 = digitsOnly.substring(5, 8); val part2 = digitsOnly.substring(8, 10)
        val part3 = digitsOnly.substring(10, 12)
        return "+$country $operator $part1 $part2 $part3"
    }
    return phoneNumber
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

    DisposableEffect(Unit) { onDispose { viewModel.clearOrderDetails() } }
    LaunchedEffect(orderId) { viewModel.fetchOrderDetails(orderId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заказа") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading && orderDetails == null -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(text = "Ошибка: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                orderDetails != null -> {
                    val details = orderDetails!!
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                StyledDetailChip(text = "Заказ #${details.order.id}", color = Color(0xFFFF6B00))
                            }
                        }
                        item { ClientInfoSection(client = details.user) }
                        item { AddressSection(address = details.address, phoneNumber = details.user.phoneNumber) }
                        item { Text(text = "ТОВАРЫ В ЗАКАЗЕ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Gray) }
                        items(details.items) { item -> OrderItemRow(item = item) }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate("scan/confirm_delivery") }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
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
private fun StyledDetailChip(text: String, modifier: Modifier = Modifier, color: Color = Color.Gray) {
    Text(
        text = text, style = MaterialTheme.typography.bodyMedium, color = color,
        modifier = modifier.background(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, color), shape = RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// ✅ ИЗМЕНЕНИЕ: Убрана иконка звонка
@Composable
private fun ClientInfoSection(client: OrderClientInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("КЛИЕНТ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            DetailRow(icon = Icons.Default.Person, title = "Имя", value = "${client.firstName} ${client.lastName}")
            DetailRow(icon = Icons.Default.Phone, title = "Телефон", value = formatPhoneNumberForDisplay(client.phoneNumber))
        }
    }
}

// ✅ ИЗМЕНЕНИЕ: Возвращена кнопка "Позвонить"
@Composable
private fun AddressSection(address: OrderAddress, phoneNumber: String) {
    val context = LocalContext.current
    val fullAddress = buildString {
        append(address.city); append(", ${address.district}"); append(", ${address.street}"); append(", ${address.house}")
        address.flat?.let { if (it.isNotBlank()) append(", кв. $it") }
        address.block?.let { if (it.isNotBlank()) append(", корп. $it") }
    }.trim()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("АДРЕС ДОСТАВКИ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            DetailRow(icon = Icons.Default.Place, title = "Адрес", value = fullAddress)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (address.latitude != null && address.longitude != null) {
                    Button(
                        onClick = {
                            try {
                                val lat = address.latitude.toDoubleOrNull(); val lon = address.longitude.toDoubleOrNull()
                                if (lat != null && lon != null) {
                                    val geoUri = "geo:$lat,$lon?q=$lat,$lon(Адрес доставки)"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                                    context.startActivity(intent)
                                } else Toast.makeText(context, "Неверные координаты адреса", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Не удалось найти приложение для карт", Toast.LENGTH_SHORT).show()
                            }
                        }, modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Маршрут"); Spacer(modifier = Modifier.width(8.dp)); Text("Маршрут")
                    }
                }
                Button(
                    onClick = {
                        val formattedPhone = formatPhoneNumberForDialing(phoneNumber)
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$formattedPhone"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Не удалось найти приложение для звонков", Toast.LENGTH_SHORT).show()
                        }
                    }, modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Позвонить"); Spacer(modifier = Modifier.width(8.dp)); Text("Звонок")
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(model = item.image?.url), contentDescription = item.title,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = item.title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    StyledDetailChip(text = "Кол-во: ${item.qty}")
                    StyledDetailChip(text = mapOrderStatusToString(item.status), color = Color(0xFF007BFF))
                }
            }
        }
    }
}