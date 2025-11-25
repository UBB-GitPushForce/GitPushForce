package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import android.media.session.MediaSession
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// This data class represents all the possible states of our Login Screen
data class LoginUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null
)

class LoginViewModel(context: Context) : ViewModel() {

    // In a real app, this would be injected using Hilt or Koin.
    // For now, we'll create it directly.

    private val tokenDataStore = TokenDataStore(context.applicationContext)
    private val authRepository = AuthRepository(RetrofitClient.authInstance, tokenDataStore)

    // Private mutable state flow that can be updated only within the ViewModel
    private val _uiState = MutableStateFlow(LoginUiState())
    // Public, read-only state flow that the UI can observe
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // Use viewModelScope to launch a coroutine that is automatically
        // cancelled if the ViewModel is cleared.
        viewModelScope.launch {
            // Step 1: Set state to loading
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Step 2: Make the repository call
                val response = authRepository.login(email, password)

                if (response.isSuccessful && response.body() != null) {
                    // Step 3a: On success, update state
                    val body = response.body()!!
                    // Token and user ID are already saved by AuthRepository
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                    println("SUCCESS: Logged in as ${body.user.firstName} ${body.user.lastName} (ID: ${body.user.id})")
                } else {
                    // Step 3b: On server error (e.g. wrong password), update state with error
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Login failed: ${response.code()} - ${response.message()}"
                    } catch (e: Exception) {
                        "Login failed: ${response.code()} - ${response.message()}"
                    }
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
            } catch (e: Exception) {
                // Step 3c: On network/other error, update state with error
                val errorMessage = "An error occurred: ${e.message}"
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun onLoginHandled() {
        _uiState.update { it.copy(loginSuccess = false, error = null) }
    }

    fun resetPassword(email: String, password: String){
        // TODO - repo -> api call
    }


}