package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.Group
import com.example.budgeting.android.data.model.UserData
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class GroupExpense(
    val expense: Expense,
    val userName: String,
    val description: String? = null
)

class GroupDetailsViewModel(context: Context) : ViewModel() {
    private val tokenDataStore = TokenDataStore(context.applicationContext)
    private val groupRepository = GroupRepository(
        RetrofitClient.groupInstance,
        RetrofitClient.expenseInstance,
        tokenDataStore
    )

    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group.asStateFlow()

    private val _members = MutableStateFlow<List<UserData>>(emptyList())
    val members: StateFlow<List<UserData>> = _members.asStateFlow()

    private val _expenses = MutableStateFlow<List<GroupExpense>>(emptyList())
    val expenses: StateFlow<List<GroupExpense>> = _expenses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            val savedToken = tokenDataStore.tokenFlow.firstOrNull()
            if (!savedToken.isNullOrBlank()) {
                TokenHolder.token = savedToken
            }
        }
    }

    fun loadGroup(groupId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                ensureTokenLoaded()

                // Load group details
                val groupResponse = groupRepository.getGroup(groupId)
                if (!groupResponse.isSuccessful || groupResponse.body() == null) {
                    _error.value = "Failed to load group"
                    _isLoading.value = false
                    return@launch
                }
                _group.value = groupResponse.body()

                // Load group members
                val membersResponse = groupRepository.getUsersByGroup(groupId)
                val membersList = if (membersResponse.isSuccessful && membersResponse.body() != null) {
                    membersResponse.body()!!
                } else {
                    emptyList()
                }
                _members.value = membersList

                val expensesResponse = groupRepository.getExpensesByGroup(
                    groupId = groupId,
                    offset = 0,
                    limit = 100,
                    sortBy = "created_at",
                    order = "asc"
                )
                
                if (expensesResponse.isSuccessful) {
                    val expenses = expensesResponse.body() ?: emptyList()
                    val groupExpenses = mapExpensesToGroupExpenses(expenses, membersList)
                    _expenses.value = groupExpenses
                } else {
                    _error.value = "Error: ${expensesResponse.code()} - ${expensesResponse.message()}"
                    _expenses.value = emptyList()
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _expenses.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapExpensesToGroupExpenses(
        expenses: List<Expense>, 
        members: List<UserData>
    ): List<GroupExpense> {
        val userMap = members.associateBy { it.id }
        
        return expenses.map { expense ->
            val userName = expense.user_id?.let { userId ->
                userMap[userId]?.let { user ->
                    "${user.firstName} ${user.lastName}"
                } ?: "Unknown User"
            } ?: "Group Member"
            
            GroupExpense(
                expense = expense,
                userName = userName,
                description = null
            )
        }
    }

    fun addExpenseFromPersonal(expense: Expense, description: String?, userName: String = "You") {
        addExpensesFromPersonal(listOf(expense), description, userName)
    }

    fun addExpensesFromPersonal(expenses: List<Expense>, description: String?, userName: String = "You") {
        if (expenses.isEmpty()) return
        
        val groupId = _group.value?.id
        if (groupId == null) {
            _error.value = "Group not loaded"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                ensureTokenLoaded()
                
                val userId = tokenDataStore.getUserId()
                if (userId == null) {
                    _error.value = "User not logged in"
                    _isLoading.value = false
                    return@launch
                }
                
                val existingExpenses = _expenses.value
                val createdExpenses = mutableListOf<Expense>()
                val skippedExpenses = mutableListOf<String>()
                
                for (expense in expenses) {
                    // Check if expense is already in the group
                    val isDuplicate = existingExpenses.any { groupExpense ->
                        groupExpense.expense.title == expense.title &&
                        groupExpense.expense.amount == expense.amount &&
                        groupExpense.expense.category == expense.category &&
                        groupExpense.expense.user_id == userId
                    }
                    
                    if (isDuplicate) {
                        skippedExpenses.add(expense.title)
                        continue
                    }
                    
                    val expenseToCreate = expense.copy(
                        id = null,
                        user_id = userId,
                        group_id = groupId,
                        created_at = null
                    )
                    
                    val response = groupRepository.addExpenseToGroup(expenseToCreate)
                    if (response.isSuccessful && response.body() != null) {
                        createdExpenses.add(response.body()!!)
                    } else {
                        _error.value = "Failed to add expense: ${response.code()}"
                        _isLoading.value = false
                        return@launch
                    }
                }
                
                if (skippedExpenses.isNotEmpty()) {
                    val skippedMessage = if (skippedExpenses.size == 1) {
                        "${skippedExpenses.first()} is already in this group"
                    } else {
                        "${skippedExpenses.size} expenses are already in this group"
                    }
                    _error.value = skippedMessage
                }
                
                val membersList = _members.value
                val descriptionText = description?.takeIf { it.isNotBlank() }
                val newGroupExpenses = createdExpenses.map { expense ->
                    val userName = expense.user_id?.let { userId ->
                        membersList.find { it.id == userId }?.let { user ->
                            "${user.firstName} ${user.lastName}"
                        } ?: "Unknown User"
                    } ?: "Group Member"
                    
                    GroupExpense(
                        expense = expense,
                        userName = userName,
                        description = descriptionText
                    )
                }
                
                _expenses.value = _expenses.value + newGroupExpenses
                
            } catch (e: Exception) {
                _error.value = "Error adding expenses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun ensureTokenLoaded() {
        if (TokenHolder.token.isNullOrBlank()) {
            val savedToken = tokenDataStore.tokenFlow.firstOrNull()
            if (!savedToken.isNullOrBlank()) {
                TokenHolder.token = savedToken
            }
        }
    }
}
