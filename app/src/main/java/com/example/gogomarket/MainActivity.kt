package com.example.gogomarket

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.rememberNavController
import com.example.gogomarket.data.UserPreferences
import com.example.gogomarket.ui.AppNavGraph
import com.example.gogomarket.ui.theme.GoGoMarketTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливаем язык до того, как Activity начнет создаваться.
        val userPrefs = UserPreferences(applicationContext)
        val langCode = runBlocking { userPrefs.getLanguage().first() }

        // ✅ ДОБАВЛЕН ЛОГ ДЛЯ ПРОВЕРКИ ✅
        Log.d("LANGUAGE_DEBUG", "Применяю язык при старте: $langCode")

        val appLocale = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GoGoMarketTheme(darkTheme = false) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}