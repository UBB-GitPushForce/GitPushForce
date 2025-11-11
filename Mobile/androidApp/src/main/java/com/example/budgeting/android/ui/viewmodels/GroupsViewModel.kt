package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.Group
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.GroupRepository
import com.example.budgeting.android.utils.GroupsErrorHandler
import com.example.budgeting.android.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GroupsViewModel(context: Context) : ViewModel() {
    private val tokenDataStore = TokenDataStore(context.applicationContext)
    private val repository = GroupRepository(RetrofitClient.groupInstance, tokenDataStore)

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Load token from TokenDataStore into TokenHolder when ViewModel is created
        viewModelScope.launch {
            val savedToken = tokenDataStore.tokenFlow.firstOrNull()
            if (!savedToken.isNullOrBlank()) {
                TokenHolder.token = savedToken
            }
        }
    }

    fun loadGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Ensure token is loaded
                if (TokenHolder.token.isNullOrBlank()) {
                    val savedToken = tokenDataStore.tokenFlow.firstOrNull()
                    if (!savedToken.isNullOrBlank()) {
                        TokenHolder.token = savedToken
                    }
                }

                // Get user ID from data store
                val userId = tokenDataStore.getUserId()
                if (userId == null || userId <= 0) {
                    _error.value = "User not logged in. Please log in again."
                    _isLoading.value = false
                    return@launch
                }
                
                val response = repository.getGroupsByUser(userId)
                if (response.isSuccessful) {
                    try {
                        val groups = response.body()?.filterNotNull() ?: emptyList()
                        // Filter out any groups that might be missing required fields
                        val validGroups = groups.filter { it.isValid }
                        _groups.value = validGroups
                    } catch (e: Exception) {
                        // JSON parsing error - response body might be malformed
                        val errorBody = try {
                            response.errorBody()?.string()
                        } catch (ex: Exception) {
                            null
                        }
                        _error.value = "Error parsing group data: ${e.message ?: "Invalid response format"}"
                        _groups.value = emptyList()
                    }
                } else {
                    val errorMessage = GroupsErrorHandler.parseErrorResponse(
                        response.code(),
                        response.errorBody()?.string()
                    )
                    _error.value = errorMessage
                }
            } catch (e: Exception) {
                val errorMessage = ErrorHandler.handleException(e)
                _error.value = "Error loading groups: $errorMessage"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinGroup(groupId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Ensure token is loaded
                if (TokenHolder.token.isNullOrBlank()) {
                    val savedToken = tokenDataStore.tokenFlow.firstOrNull()
                    if (!savedToken.isNullOrBlank()) {
                        TokenHolder.token = savedToken
                    }
                }

                // Get user ID from data store
                val userId = tokenDataStore.getUserId()
                if (userId == null || userId <= 0) {
                    _error.value = "User not logged in. Please log in again."
                    _isLoading.value = false
                    return@launch
                }
                
                if (groupId <= 0) {
                    _error.value = "Invalid group ID"
                    _isLoading.value = false
                    return@launch
                }
                
                val response = repository.addUserToGroup(groupId, userId)
                if (response.isSuccessful) {
                    // Reload groups to get the updated list from the server
                    loadGroups()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = GroupsErrorHandler.parseErrorResponse(
                        response.code(),
                        errorBody
                    )
                    
                    // Workaround: If the error is about 'id' missing (backend refresh issue),
                    // check if the user was actually added by reloading groups
                    // This handles the case where the backend successfully adds the user
                    // but fails on db.refresh() due to composite primary key
                    if (errorBody?.contains("required valued", ignoreCase = true) == true && 
                        errorBody.contains("id", ignoreCase = true)) {
                        // The backend might have succeeded but failed on refresh
                        // Check if we're now in the group by reloading
                        val checkResponse = repository.getGroupsByUser(userId)
                        if (checkResponse.isSuccessful) {
                            val userGroups = checkResponse.body()?.filterNotNull() ?: emptyList()
                            val wasAdded = userGroups.any { it.id == groupId }
                            if (wasAdded) {
                                // Success! The user was added despite the error
                                loadGroups()
                                onSuccess()
                                return@launch
                            }
                        }
                    }
                    
                    // For 404 errors, ErrorHandler already returns "Group not found"
                    _error.value = errorMessage
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                val errorMessage = ErrorHandler.handleException(e)
                _error.value = errorMessage
                _isLoading.value = false
            }
        }
    }

    fun createGroup(name: String, description: String? = null, onSuccess: (Group) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Ensure token is loaded
                if (TokenHolder.token.isNullOrBlank()) {
                    val savedToken = tokenDataStore.tokenFlow.firstOrNull()
                    if (!savedToken.isNullOrBlank()) {
                        TokenHolder.token = savedToken
                    }
                }

                val userId = tokenDataStore.getUserId()
                if (userId == null) {
                    _error.value = "User not logged in"
                    _isLoading.value = false
                    return@launch
                }
                
                val response = repository.createGroup(name, description)
                if (response.isSuccessful) {
                    val group = response.body()
                    if (group != null && group.id != null) {
                        // Automatically add the creator to the group
                        val addUserResponse = repository.addUserToGroup(group.id!!, userId)
                        if (addUserResponse.isSuccessful) {
                            // Reload groups to get the updated list from the server
                            loadGroups()
                            onSuccess(group)
                        } else {
                            // Group was created but failed to add user - check if user was actually added
                            // Sometimes the backend succeeds but returns an error due to refresh issues
                            val checkResponse = repository.getGroupsByUser(userId)
                            if (checkResponse.isSuccessful) {
                                val userGroups = checkResponse.body()?.filterNotNull() ?: emptyList()
                                val wasAdded = userGroups.any { it.id == group.id }
                                if (wasAdded) {
                                    // Success! The user was added despite the error
                                    loadGroups()
                                    onSuccess(group)
                                } else {
                                    // Group was created but user wasn't added - still reload groups
                                    loadGroups()
                                    _error.value = "Group created but failed to add you as a member. Please try joining manually."
                                    _isLoading.value = false
                                }
                            } else {
                                // Couldn't verify, but group was created
                                loadGroups()
                                _error.value = "Group created but failed to add you as a member. Please try joining manually."
                                _isLoading.value = false
                            }
                        }
                    } else {
                        _error.value = "Create failed: No group data received"
                        _isLoading.value = false
                    }
                } else {
                    val errorMessage = GroupsErrorHandler.parseErrorResponse(
                        response.code(),
                        response.errorBody()?.string()
                    )
                    _error.value = "Create failed: $errorMessage"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                val errorMessage = ErrorHandler.handleException(e)
                _error.value = "Create failed: $errorMessage"
                _isLoading.value = false
            }
        }
    }

    fun updateGroup(groupId: Int, name: String? = null, description: String? = null, onSuccess: (Group) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Ensure token is loaded
                if (TokenHolder.token.isNullOrBlank()) {
                    val savedToken = tokenDataStore.tokenFlow.firstOrNull()
                    if (!savedToken.isNullOrBlank()) {
                        TokenHolder.token = savedToken
                    }
                }

                val response = repository.updateGroup(groupId, name, description)
                if (response.isSuccessful) {
                    val group = response.body()
                    if (group != null) {
                        // Reload groups to get the updated list from the server
                        loadGroups()
                        onSuccess(group)
                    } else {
                        _error.value = "Update failed: No group data received"
                        _isLoading.value = false
                    }
                } else {
                    val errorMessage = GroupsErrorHandler.parseErrorResponse(
                        response.code(),
                        response.errorBody()?.string()
                    )
                    _error.value = "Update failed: $errorMessage"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                val errorMessage = ErrorHandler.handleException(e)
                _error.value = "Update failed: $errorMessage"
                _isLoading.value = false
            }
        }
    }

    fun deleteGroup(groupId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Ensure token is loaded
                if (TokenHolder.token.isNullOrBlank()) {
                    val savedToken = tokenDataStore.tokenFlow.firstOrNull()
                    if (!savedToken.isNullOrBlank()) {
                        TokenHolder.token = savedToken
                    }
                }

                val response = repository.deleteGroup(groupId)
                if (response.isSuccessful) {
                    // Reload groups to get the updated list from the server
                    loadGroups()
                    onSuccess()
                } else {
                    val errorMessage = GroupsErrorHandler.parseErrorResponse(
                        response.code(),
                        response.errorBody()?.string()
                    )
                    _error.value = "Delete failed: $errorMessage"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                val errorMessage = ErrorHandler.handleException(e)
                _error.value = "Delete failed: $errorMessage"
                _isLoading.value = false
            }
        }
    }
}

