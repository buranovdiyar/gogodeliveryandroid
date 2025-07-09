package com.example.gogomarket

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.gogomarket.data.UserPreferences
import com.example.gogomarket.ui.AppNavGraph
import com.example.gogomarket.ui.theme.GoGoMarketTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

// Вспомогательный класс для обертки контекста с нужной локалью
class LanguageContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, newLocale: Locale): ContextWrapper {
            var ctx = context
            val config = Configuration(ctx.resources.configuration)
            config.setLocale(newLocale)
            ctx = ctx.createConfigurationContext(config)
            return ContextWrapper(ctx)
        }
    }
}

class MainActivity : ComponentActivity() {

    // Этот метод вызывается до onCreate.
    // Он самый надежный для установки локали.
    override fun attachBaseContext(newBase: Context) {
        // Получаем сохраненный язык синхронно, до создания Activity
        val prefs = UserPreferences(newBase)
        val langCode = runBlocking { prefs.getLanguage().first() }
        val locale = Locale(langCode)
        // Оборачиваем базовый контекст в наш враппер с нужным языком
        super.attachBaseContext(LanguageContextWrapper.wrap(newBase, locale))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPrefs = remember { UserPreferences(applicationContext) }
            val currentLangCode by userPrefs.getLanguage().collectAsState(initial = "ru")

            // Этот эффект следит за языком. Если он меняется,
            // он просто вызывает recreate(), чтобы перезапустить Activity.
            LaunchedEffect(currentLangCode) {
                // Получаем язык, с которым Activity была создана
                val initialLangCode =
                    resources.configuration.locales.get(0)?.language ?: "ru"

                // Если сохраненный язык отличается от текущего, пересоздаем экран
                if (initialLangCode != currentLangCode) {
                    this@MainActivity.recreate()
                }
            }

            GoGoMarketTheme(darkTheme = false) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}