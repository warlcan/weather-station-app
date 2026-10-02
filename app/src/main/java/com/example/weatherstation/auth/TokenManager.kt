package com.example.weatherstation.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    suspend fun getToken(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[TOKEN_KEY] }
            .first()
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
}

