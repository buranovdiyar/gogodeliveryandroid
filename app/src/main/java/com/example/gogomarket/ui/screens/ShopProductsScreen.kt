package com.example.gogomarket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gogomarket.R

data class Product(
    val name: String,
    val stockId: String,
    val hasBarcode: Boolean,
    val imageRes: Int = R.drawable.ic_box
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopProductsScreen(
    shopName: String,
    shopAddress: String,
    navController: NavController
) {
    val colorScheme = MaterialTheme.colorScheme

    val products = listOf(
        Product("Пластиковое сито для раковины", "2014", false),
        Product("Пластиковое сито для раковины", "2014", false)
    )

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Товары магазина", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        navController.navigate("scan_product/")
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сканировать", color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = shopName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = shopAddress,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0F7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = product.imageRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text("Stock ID: ${product.stockId}", fontSize = 13.sp)
                                Text(
                                    text = if (product.hasBarcode) "Штрихкод есть" else "Нет штрихкода",
                                    fontSize = 13.sp,
                                    color = if (product.hasBarcode) Color.Green else Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}