package com.example.gogomarket

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.gogomarket.data.UserPreferences
import com.example.gogomarket.ui.AppNavGraph
import com.example.gogomarket.ui.theme.GoGoMarketTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPrefs = UserPreferences(applicationContext)
            val langCode by userPrefs.getLanguage().collectAsState(initial = "ru")

            // 1. Создаем обертку контекста с нужной локалью
            val localizedContext = remember(langCode) {
                ContextUtils.updateResources(this, langCode)
            }

            // 2. Явно передаем этот локализованный контекст всему приложению
            CompositionLocalProvider(LocalContext provides localizedContext) {
                GoGoMarketTheme(darkTheme = false) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}

// 3. Вспомогательный объект для работы с контекстом
object ContextUtils {
    fun updateResources(context: Context, language: String): ContextWrapper {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        val newContext = context.createConfigurationContext(configuration)
        return ContextWrapper(newContext)
    }
}