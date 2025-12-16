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
    private val repository = GroupRepository(
        RetrofitClient.groupInstance,
        RetrofitClient.expenseInstance,
        tokenDataStore
    )

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
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val groups = response.body()!!.filterNotNull()
                        // Filter out any groups that might be missing required fields
                        val validGroups = groups.filter { it.isValid }
                        _groups.value = validGroups
                        _error.value = null // Clear any previous errors on success
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
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    val errorMessage = GroupsErrorHandler.parseErrorResponse(
                        response.code(),
                        errorBody
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
                
                val existingGroup = _groups.value.firstOrNull { it.id == groupId }
                if (existingGroup != null) {
                    val name = existingGroup.name ?: "this group"
                    _error.value = "You already belong to \"$name\". As the creator you're added automatically."
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
                    loadGroups()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = GroupsErrorHandler.parseErrorResponse(
                        response.code(),
                        errorBody
                    )
                    
                    if (errorBody?.contains("required valued", ignoreCase = true) == true && 
                        errorBody.contains("id", ignoreCase = true)) {
                        val checkResponse = repository.getGroupsByUser(userId)
                        if (checkResponse.isSuccessful && checkResponse.body() != null) {
                            val userGroups = checkResponse.body()!!.filterNotNull()
                            val wasAdded = userGroups.any { it.id == groupId }
                            if (wasAdded) {
                                loadGroups()
                                onSuccess()
                                return@launch
                            }
                        }
                    }
                    
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

    fun joinGroupByInvitationCode(invitationCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (TokenHolder.token.isNullOrBlank()) {
                    val savedToken = tokenDataStore.tokenFlow.firstOrNull()
                    if (!savedToken.isNullOrBlank()) {
                        TokenHolder.token = savedToken
                    }
                }

                val userId = tokenDataStore.getUserId()
                if (userId == null || userId <= 0) {
                    _error.value = "User not logged in. Please log in again."
                    _isLoading.value = false
                    return@launch
                }

                val sanitizedCode = invitationCode.trim().uppercase()
                if (sanitizedCode.isBlank()) {
                    _error.value = "Invalid invitation code"
                    _isLoading.value = false
                    return@launch
                }

                val existingGroup = _groups.value.firstOrNull {
                    it.invitationCode?.equals(sanitizedCode, ignoreCase = true) == true
                }
                if (existingGroup != null) {
                    val name = existingGroup.name ?: "this group"
                    _error.value = "You're already part of \"$name\". Share its code instead of joining again."
                    _isLoading.value = false
                    return@launch
                }

                val response = repository.joinGroupByInvitationCode(sanitizedCode)
                if (response.isSuccessful) {
                    loadGroups()
                    onSuccess()
                } else {
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    val errorMessage = GroupsErrorHandler.parseErrorResponse(
                        response.code(),
                        errorBody
                    )
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
                if (response.isSuccessful && response.body() != null) {
                    val groupId = response.body()!!
                    val addUserResponse = repository.addUserToGroup(groupId, userId)
                    if (addUserResponse.isSuccessful) {
                        loadGroups()
                        val groupResponse = repository.getGroup(groupId)
                        if (groupResponse.isSuccessful && groupResponse.body() != null) {
                            onSuccess(groupResponse.body()!!)
                        } else {
                            onSuccess(Group(id = groupId, name = name, description = description))
                        }
                    } else {
                        val checkResponse = repository.getGroupsByUser(userId)
                        if (checkResponse.isSuccessful && checkResponse.body() != null) {
                            val userGroups = checkResponse.body()!!.filterNotNull()
                            val wasAdded = userGroups.any { it.id == groupId }
                            if (wasAdded) {
                                loadGroups()
                                val groupResponse = repository.getGroup(groupId)
                                if (groupResponse.isSuccessful && groupResponse.body() != null) {
                                    onSuccess(groupResponse.body()!!)
                                } else {
                                    onSuccess(Group(id = groupId, name = name, description = description))
                                }
                            } else {
                                loadGroups()
                                _error.value = "Group created but failed to add you as a member. Please try joining manually."
                                _isLoading.value = false
                            }
                        } else {
                            loadGroups()
                            _error.value = "Group created but failed to add you as a member. Please try joining manually."
                            _isLoading.value = false
                        }
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
                if (TokenHolder.token.isNullOrBlank()) {
                    val savedToken = tokenDataStore.tokenFlow.firstOrNull()
                    if (!savedToken.isNullOrBlank()) {
                        TokenHolder.token = savedToken
                    }
                }

                val response = repository.updateGroup(groupId, name, description)
                if (response.isSuccessful && response.body() != null) {
                    val group = response.body()!!
                    loadGroups()
                    onSuccess(group)
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

