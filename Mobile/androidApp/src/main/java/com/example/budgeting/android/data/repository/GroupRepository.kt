package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.CreateGroupRequest
import com.example.budgeting.android.data.model.UpdateGroupRequest
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.network.GroupApiService
import com.example.budgeting.android.data.network.ExpenseApiService
import com.example.budgeting.android.data.network.RetrofitClient
import com.squareup.moshi.JsonAdapter
import org.json.JSONArray
import retrofit2.Response
import okhttp3.ResponseBody

class GroupRepository(
    private val apiService: GroupApiService,
    private val expenseApiService: ExpenseApiService,
    private val tokenDataStore: TokenDataStore
) {

    suspend fun getGroup(id: Int) = apiService.getGroup(id)

    suspend fun createGroup(name: String, description: String? = null) = 
        apiService.createGroup(CreateGroupRequest(name, description))

    suspend fun updateGroup(groupId: Int, name: String? = null, description: String? = null) = 
        apiService.updateGroup(groupId, UpdateGroupRequest(name, description))

    suspend fun deleteGroup(groupId: Int) = apiService.deleteGroup(groupId)

    suspend fun addUserToGroup(groupId: Int, userId: Int) = 
        apiService.addUserToGroup(groupId, userId)

    suspend fun getGroupsByUser(userId: Int) = apiService.getGroupsByUser(userId)

    suspend fun getUsersByGroup(groupId: Int) = apiService.getUsersByGroup(groupId)

    suspend fun getExpensesByGroup(
        groupId: Int,
        offset: Int = 0,
        limit: Int = 100,
        sortBy: String = "created_at",
        order: String = "desc"
    ): Response<List<Expense>> {
        val rawResponse = apiService.getExpensesByGroup(groupId, offset, limit, sortBy, order)
        
        if (!rawResponse.isSuccessful) {
            return Response.error(rawResponse.code(), rawResponse.errorBody() ?: ResponseBody.create(null, ""))
        }
        
        val responseBody = rawResponse.body() ?: return Response.success(emptyList())
        val jsonString = responseBody.string().trim()
        
        if (jsonString.isEmpty() || jsonString == "[]") {
            return Response.success(emptyList())
        }
        
        return try {
            val moshi = RetrofitClient.getMoshi()
            val expenseAdapter: JsonAdapter<Expense> = moshi.adapter(Expense::class.java)
            val jsonArray = JSONArray(jsonString)
            val validExpenses = mutableListOf<Expense>()
            
            for (i in 0 until jsonArray.length()) {
                try {
                    val expenseJson = jsonArray.getJSONObject(i)
                    val expense = expenseAdapter.fromJson(expenseJson.toString())
                    if (expense != null && expense.id != null && expense.title.isNotBlank()) {
                        validExpenses.add(expense)
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            
            Response.success(validExpenses)
        } catch (e: Exception) {
            Response.success(emptyList())
        }
    }

    suspend fun addExpenseToGroup(expense: Expense): Response<Expense> {
        return expenseApiService.addExpense(expense)
    }

    suspend fun getGroupInviteQr(groupId: Int): Response<ResponseBody> =
        apiService.getGroupInviteQr(groupId)

    suspend fun joinGroupByInvitationCode(invitationCode: String): Response<Unit> =
        apiService.joinGroupByInvitationCode(invitationCode)
}
