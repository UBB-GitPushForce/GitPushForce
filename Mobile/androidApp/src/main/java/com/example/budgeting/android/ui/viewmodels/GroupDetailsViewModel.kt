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
import com.example.budgeting.android.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class GroupExpense(
    val expense: Expense,
    val userName: String
)

class GroupDetailsViewModel(context: Context) : ViewModel() {
    private val tokenDataStore = TokenDataStore(context.applicationContext)
    private val groupRepository = GroupRepository(RetrofitClient.groupInstance, tokenDataStore)
    private val expenseRepository = ExpenseRepository(RetrofitClient.expenseInstance, tokenDataStore)

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
        // Load token from TokenDataStore into TokenHolder when ViewModel is created
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
                // Ensure token is loaded
                if (TokenHolder.token.isNullOrBlank()) {
                    val savedToken = tokenDataStore.tokenFlow.firstOrNull()
                    if (!savedToken.isNullOrBlank()) {
                        TokenHolder.token = savedToken
                    }
                }

                // Load group details
                val groupResponse = groupRepository.getGroup(groupId)
                if (groupResponse.isSuccessful) {
                    val group = groupResponse.body()
                    if (group != null && group.isValid) {
                        _group.value = group
                    } else {
                        _error.value = "Invalid group data received"
                        _isLoading.value = false
                        return@launch
                    }
                } else {
                    _error.value = "Failed to load group: ${groupResponse.code()}"
                    _isLoading.value = false
                    return@launch
                }

                // Load group members
                val membersResponse = groupRepository.getUsersByGroup(groupId)
                if (membersResponse.isSuccessful) {
                    _members.value = membersResponse.body() ?: emptyList()
                }

                // Use mock expenses for now
                val mockExpenses = createMockExpenses(_members.value)
                _expenses.value = mockExpenses

            } catch (e: Exception) {
                _error.value = "Error loading group details: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun createMockExpenses(members: List<UserData>): List<GroupExpense> {
        val mockExpenses = listOf(
            Expense(
                title = "Groceries",
                category = "Food",
                amount = 45.50
            ),
            Expense(
                title = "Uber Ride",
                category = "Transportation",
                amount = 12.75
            ),
            Expense(
                title = "Movie Tickets",
                category = "Entertainment",
                amount = 28.00
            ),
            Expense(
                title = "Restaurant Dinner",
                category = "Food",
                amount = 67.30
            ),
            Expense(
                title = "Coffee",
                category = "Food",
                amount = 8.50
            )
        )

        // Create a map of user IDs to names for display
        val memberMap = members.associateBy { it.id }
        
        return mockExpenses.mapIndexed { index, expense ->
            // Assign expenses to different members in a round-robin fashion
            val memberIndex = index % members.size.coerceAtLeast(1)
            val userName = if (members.isNotEmpty()) {
                val member = members[memberIndex]
                "${member.firstName} ${member.lastName}"
            } else {
                "Group Member"
            }
            GroupExpense(
                expense = expense,
                userName = userName
            )
        }
    }
}

