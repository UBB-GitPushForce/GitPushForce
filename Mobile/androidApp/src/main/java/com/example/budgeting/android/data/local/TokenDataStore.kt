package com.example.budgeting.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

private const val DATASTORE_NAME = "auth_prefs"

private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class TokenDataStore(private val appContext: Context) {

    private object Keys {
        val authToken = stringPreferencesKey("auth_token")
        val userId = intPreferencesKey("user_id")
    }

    val tokenFlow: Flow<String?> = appContext.dataStore.data.map { prefs ->
        prefs[Keys.authToken]
    }

    val userIdFlow: Flow<Int?> = appContext.dataStore.data.map { prefs ->
        prefs[Keys.userId]
    }

    // save token
    suspend fun saveToken(token: String?) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.authToken] = token as String
        }
    }

    // save user ID
    suspend fun saveUserId(userId: Int?) {
        appContext.dataStore.edit { prefs ->
            if (userId != null) {
                prefs[Keys.userId] = userId
            } else {
                prefs.remove(Keys.userId)
            }
        }
    }

    // Get user ID synchronously (for use in coroutines)
    suspend fun getUserId(): Int? {
        return try {
            userIdFlow.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // clear token and user ID for logout
    suspend fun clearToken() {
        appContext.dataStore.edit { prefs ->
            prefs.remove(Keys.authToken)
            prefs.remove(Keys.userId)
        }
    }
}


