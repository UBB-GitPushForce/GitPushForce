package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.AuthRepository
import com.example.budgeting.android.utils.ErrorHandler
import com.example.budgeting.android.utils.RegistrationErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents all the possible states of the Sign Up screen
data class SignUpUiState(
    val isLoading: Boolean = false,
    val signUpSuccess: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class SignUpViewModel(context: Context) : ViewModel() {

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
            _uiState.update { it.copy(isLoading = true, error = null, fieldErrors = emptyMap()) }

            // Input validation using RegistrationErrorHandler
            val validationErrors = RegistrationErrorHandler.validateRegistrationInputs(
                firstName, lastName, email, password, phoneNumber
            )
            if (validationErrors.isNotEmpty()) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Please fix the errors below",
                        fieldErrors = validationErrors
                    ) 
                }
                return@launch
            }

            try {
                val response = authRepository.register(firstName, lastName, email, password, phoneNumber)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.update { it.copy(isLoading = false, signUpSuccess = true) }
                } else {
                    val errorMessage = RegistrationErrorHandler.parseErrorResponse(
                        response.code(), 
                        response.errorBody()?.string()
                    )
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
            } catch (e: Exception) {
                val errorMessage = ErrorHandler.handleException(e)
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun onSignUpHandled() {
        _uiState.update { it.copy(signUpSuccess = false, error = null, fieldErrors = emptyMap()) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, fieldErrors = emptyMap()) }
    }

    fun clearFieldError(fieldName: String) {
        _uiState.update { state ->
            val updatedFieldErrors = state.fieldErrors.toMutableMap().apply {
                remove(fieldName)
            }
            state.copy(
                error = if (updatedFieldErrors.isEmpty()) null else state.error,
                fieldErrors = updatedFieldErrors
            )
        }
    }
}
