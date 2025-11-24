package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.UserResponse
import com.example.budgeting.android.data.model.UserUpdateRequest
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserResponse? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(context: Context) : ViewModel() {

    private val tokenDataStore = TokenDataStore(context.applicationContext)
    private val userRepository = UserRepository(RetrofitClient.userInstance, tokenDataStore)

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)

            try {
                val userId = tokenDataStore.getUserId() ?: throw Exception("User ID not found")
                val user = userRepository.getUserById(userId)
                _uiState.value = ProfileUiState(user = user)

            } catch (e: Exception) {
                _uiState.value = ProfileUiState(error = e.message)
            }
        }
    }

    fun setEditing(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isEditing = enabled)
    }

    fun updateUser(updated: UserUpdateRequest) {
        viewModelScope.launch {
            val current = _uiState.value.user ?: return@launch

            try {
                userRepository.updateUser(current.id, updated)
                loadUser() // refresh
                setEditing(false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            val current = _uiState.value.user ?: return@launch

            try {
                userRepository.deleteUser(current.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            TokenHolder.token = null
            tokenDataStore.clearToken()
        }
    }
}
