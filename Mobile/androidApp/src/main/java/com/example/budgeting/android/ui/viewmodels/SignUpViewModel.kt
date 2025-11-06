package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents all the possible states of the Sign Up screen
data class SignUpUiState(
    val isLoading: Boolean = false,
    val signUpSuccess: Boolean = false,
    val error: String? = null
)

class SignUpViewModel(context: Context) : ViewModel() {

    // Normally injected (Hilt/Koin), but created manually for now
    private val tokenDataStore = TokenDataStore(context.applicationContext)
    private val authRepository = AuthRepository(RetrofitClient.authInstance, tokenDataStore)

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = authRepository.register(email, password, firstName, lastName, phoneNumber)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val user = body.user
                    _uiState.update { it.copy(isLoading = false, signUpSuccess = true) }
                    println("SIGN UP SUCCESS: ${body.message} (UserID: ${user.id})")
                } else {
                    val errorMessage = "Sign up failed: ${response.code()} - ${response.message()}"
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
            } catch (e: Exception) {
                val errorMessage = "An error occurred: ${e.message}"
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun onSignUpHandled() {
        _uiState.update { it.copy(signUpSuccess = false, error = null) }
    }
}
