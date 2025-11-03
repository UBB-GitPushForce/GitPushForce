package com.example.budgeting.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "auth_prefs"

private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class TokenDataStore(private val appContext: Context) {

    private object Keys {
        val authToken = stringPreferencesKey("auth_token")
    }

    val tokenFlow: Flow<String?> = appContext.dataStore.data.map { prefs ->
        prefs[Keys.authToken]
    }

    // save token
    suspend fun saveToken(token: String?) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.authToken] = token as String
        }
    }

    // clear token for logout
    suspend fun clearToken() {
        appContext.dataStore.edit { prefs ->
            prefs.remove(Keys.authToken)
        }
    }
}


