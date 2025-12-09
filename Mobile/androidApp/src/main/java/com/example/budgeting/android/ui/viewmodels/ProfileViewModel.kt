package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.BudgetResponse
import com.example.budgeting.android.data.model.UserResponse
import com.example.budgeting.android.data.model.UserUpdateRequest
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserResponse? = null,
    val budget: BudgetResponse? = null,
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
        loadUserAndBudget()
    }

    private fun loadUserAndBudget() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val userId = tokenDataStore.getUserId()
                    ?: throw Exception("User ID not found")

                val user = userRepository.getUserById(userId)

                val budget = userRepository.getBudgetData(user.id)

                _uiState.value = _uiState.value.copy(
                    user = user,
                    budget = budget,
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
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
                loadUserAndBudget() // refresh
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

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            val current = _uiState.value.user ?: return@launch
            try {
                userRepository.changePassword(current.id, oldPassword, newPassword)
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
