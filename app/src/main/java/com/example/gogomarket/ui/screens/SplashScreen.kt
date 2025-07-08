// SplashScreen.kt
package com.example.gogomarket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gogomarket.R // ✅ Важный импорт для доступа к ресурсам
import com.example.gogomarket.data.UserPreferences

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    // Эта функция выполнится один раз при появлении экрана
    LaunchedEffect(key1 = true) {
        val token = userPrefs.getAccessToken()
        if (token.isNullOrEmpty()) {
            // Если токена нет, идем на экран логина
            navController.navigate("login") {
                // Очищаем стэк, чтобы нельзя было вернуться назад на сплэш-скрин
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // Если токен есть, идем на главный экран
            navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // Визуальная часть экрана
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Используем Column, чтобы разместить иконку над индикатором
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка вашего приложения
            Image(
                painter = painterResource(id = R.drawable.logo), // Используем круглую иконку
                contentDescription = "Логотип приложения",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Индикатор загрузки под иконкой
            CircularProgressIndicator()
        }
    }
}