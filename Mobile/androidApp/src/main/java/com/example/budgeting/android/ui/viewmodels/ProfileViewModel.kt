package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import kotlinx.coroutines.launch

class ProfileViewModel(context: Context) : ViewModel() {
    private val tokenDataStore = TokenDataStore(context.applicationContext)

    fun logout() {
        viewModelScope.launch {
            TokenHolder.token = null
            tokenDataStore.clearToken()
        }
    }

}