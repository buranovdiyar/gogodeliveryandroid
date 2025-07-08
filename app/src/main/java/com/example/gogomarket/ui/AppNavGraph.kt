package com.example.gogomarket.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gogomarket.data.UserPreferences
import com.example.gogomarket.network.NetworkModule
import com.example.gogomarket.ui.components.MainScaffold
import com.example.gogomarket.ui.screens.LoginScreen
import com.example.gogomarket.ui.screens.OrderToDeliverDetailScreen
import com.example.gogomarket.ui.screens.ScanProductScreen
import com.example.gogomarket.ui.screens.SplashScreen
import com.example.gogomarket.ui.screens.StoreProductListScreen
import com.example.gogomarket.viewmodel.AuthViewModel
import com.example.gogomarket.viewmodel.AuthViewModelFactory
import com.example.gogomarket.viewmodel.CourierViewModel
import com.example.gogomarket.viewmodel.CourierViewModelFactory

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context: Context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val apiService = remember { NetworkModule.provideApiService(userPrefs) }

    val courierViewModel: CourierViewModel = viewModel(
        factory = CourierViewModelFactory(apiService, userPrefs)
    )

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("login") {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(apiService, userPrefs)
            )
            LoginScreen(
                viewModel = authViewModel,
                onNavigate = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScaffold(
                navController = navController,
                viewModel = courierViewModel
            )
        }

        composable(
            route = "store_products/{storeId}",
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            StoreProductListScreen(
                storeId = storeId,
                viewModel = courierViewModel,
                navController = navController
            )
        }

        composable(
            route = "order_details/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            OrderToDeliverDetailScreen(
                orderId = orderId,
                viewModel = courierViewModel,
                navController = navController
            )
        }

        composable(
            route = "scan/{scanType}",
            arguments = listOf(navArgument("scanType") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanType = backStackEntry.arguments?.getString("scanType") ?: ""
            ScanProductScreen(
                scanType = scanType,
                viewModel = courierViewModel,
                navController = navController
            )
        }
    }
}