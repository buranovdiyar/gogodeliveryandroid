package com.example.gogomarket.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val PHONE_KEY = stringPreferencesKey("phone")
        private val FIRST_NAME_KEY = stringPreferencesKey("user_first_name")
        private val LAST_NAME_KEY = stringPreferencesKey("user_last_name")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun savePhone(phone: String) {
        context.dataStore.edit { prefs ->
            prefs[PHONE_KEY] = phone
        }
    }

    suspend fun saveFullName(firstName: String, lastName: String) {
        context.dataStore.edit { prefs ->
            prefs[FIRST_NAME_KEY] = firstName
            prefs[LAST_NAME_KEY] = lastName
        }
    }

    suspend fun saveUserRole(role: Int) {
        context.dataStore.edit { prefs ->
            prefs[ROLE_KEY] = role.toString()
        }
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = languageCode
        }
    }

    fun getLanguage(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: "ru" // "ru" as default
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()
    }

    suspend fun getPhone(): String? {
        return context.dataStore.data.map { it[PHONE_KEY] }.first()
    }

    suspend fun getFirstName(): String? {
        return context.dataStore.data.map { it[FIRST_NAME_KEY] }.first()
    }

    suspend fun getLastName(): String? {
        return context.dataStore.data.map { it[LAST_NAME_KEY] }.first()
    }

    suspend fun getUserRole(): Int? {
        return context.dataStore.data.map { it[ROLE_KEY]?.toIntOrNull() }.first()
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}