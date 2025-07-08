package com.example.gogomarket.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
// ✅ ДОБАВЛЕН НОВЫЙ ИМПОРТ:
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.gogomarket.R
import com.example.gogomarket.ui.screens.ActiveOrdersScreen
import com.example.gogomarket.ui.screens.OrderHistoryScreen
import com.example.gogomarket.ui.screens.OrdersToDeliverScreen
import com.example.gogomarket.ui.screens.ProfileScreen
import com.example.gogomarket.ui.screens.StoreListScreen
import com.example.gogomarket.viewmodel.CourierViewModel


private enum class Tab {
    Stores, Orders, Active, History, Profile
}


@Composable
fun MainScaffold(
    navController: NavController,
    viewModel: CourierViewModel
) {
    val userRole by viewModel.userRole.collectAsState()

    val startTab = remember(userRole) {
        when (userRole) {
            23 -> Tab.Stores
            24 -> Tab.Orders
            else -> null
        }
    }

    var selectedTab by remember(startTab) { mutableStateOf(startTab) }

    LaunchedEffect(key1 = Unit) {
        viewModel.refreshUserData()
    }

    Scaffold(
        bottomBar = {
            if (selectedTab != null) {
                NavigationBar {
                    when (userRole) {
                        23 -> {
                            // Вкладки для Роли 23
                            NavigationBarItem(
                                selected = selectedTab == Tab.Stores,
                                onClick = { selectedTab = Tab.Stores },
                                icon = { Icon(Icons.Default.Storefront, contentDescription = stringResource(R.string.nav_shops)) },
                                label = { Text(stringResource(R.string.nav_shops)) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == Tab.Profile,
                                onClick = { selectedTab = Tab.Profile },
                                icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.nav_profile)) },
                                label = { Text(stringResource(R.string.nav_profile)) }
                            )
                        }
                        24 -> {
                            // Вкладки для Роли 24
                            NavigationBarItem(
                                selected = selectedTab == Tab.Orders,
                                onClick = { selectedTab = Tab.Orders },
                                icon = { Icon(Icons.Default.ListAlt, contentDescription = stringResource(R.string.nav_new)) },
                                label = { Text(stringResource(R.string.nav_new)) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == Tab.Active,
                                onClick = {
                                    selectedTab = Tab.Active
                                    viewModel.fetchDelivererOrders()
                                },
                                // ✅ ИЗМЕНЕНИЕ: Новая иконка для активных заказов
                                icon = { Icon(Icons.Default.LocalShipping, contentDescription = stringResource(R.string.nav_active)) },
                                label = { Text(stringResource(R.string.nav_active)) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == Tab.History,
                                onClick = {
                                    selectedTab = Tab.History
                                    viewModel.fetchDelivererOrders()
                                },
                                icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.nav_history)) },
                                label = { Text(stringResource(R.string.nav_history)) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == Tab.Profile,
                                onClick = { selectedTab = Tab.Profile },
                                icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.nav_profile)) },
                                label = { Text(stringResource(R.string.nav_profile)) }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                Tab.Stores -> StoreListScreen(navController = navController, viewModel = viewModel)
                Tab.Orders -> OrdersToDeliverScreen(navController = navController, viewModel = viewModel)
                Tab.Active -> ActiveOrdersScreen(navController = navController, viewModel = viewModel)
                Tab.History -> OrderHistoryScreen(navController = navController, viewModel = viewModel)
                Tab.Profile -> ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}