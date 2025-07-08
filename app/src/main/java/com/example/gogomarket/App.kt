package com.example.gogomarket

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // ВАЖНО: Замените YOUR_API_KEY на ваш реальный ключ от Яндекс Карт!
        // Если вы пока не используете карты Яндекса, эту строку можно временно закомментировать.
        MapKitFactory.setApiKey("YOUR_API_KEY")
    }
}