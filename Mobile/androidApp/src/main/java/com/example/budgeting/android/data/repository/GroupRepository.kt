package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.CreateGroupRequest
import com.example.budgeting.android.data.model.UpdateGroupRequest
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.ExpenseCreateRequest
import com.example.budgeting.android.data.model.Group
import com.example.budgeting.android.data.model.GroupLog
import com.example.budgeting.android.data.model.UserData
import com.example.budgeting.android.data.network.GroupApiService
import com.example.budgeting.android.data.network.ExpenseApiService
import retrofit2.Response
import okhttp3.ResponseBody
import android.util.Base64

class GroupRepository(
    private val apiService: GroupApiService,
    private val expenseApiService: ExpenseApiService,
    private val tokenDataStore: TokenDataStore
) {

    suspend fun getGroup(id: Int): Response<Group> {
        val response = apiService.getGroup(id)
        return if (response.isSuccessful && response.body()?.data != null) {
            Response.success(response.body()!!.data!!)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

    suspend fun createGroup(name: String, description: String? = null): Response<Int> {
        val response = apiService.createGroup(CreateGroupRequest(name, description))
        return if (response.isSuccessful && response.body()?.data != null) {
            Response.success(response.body()!!.data!!.id)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

    suspend fun updateGroup(groupId: Int, name: String? = null, description: String? = null): Response<Group> {
        val response = apiService.updateGroup(groupId, UpdateGroupRequest(name, description))
        // Update returns id, so we need to fetch the group again
        return if (response.isSuccessful && response.body()?.data != null) {
            getGroup(response.body()!!.data!!.id)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

    suspend fun deleteGroup(groupId: Int): Response<Unit> {
        val response = apiService.deleteGroup(groupId)
        return if (response.isSuccessful) {
            Response.success(Unit)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

    suspend fun addUserToGroup(groupId: Int, userId: Int): Response<Unit> {
        val response = apiService.addUserToGroup(groupId, userId)
        return if (response.isSuccessful) {
            Response.success(Unit)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

    suspend fun getGroupsByUser(userId: Int): Response<List<Group>> {
        val response = apiService.getGroupsByUser(userId)
        return if (response.isSuccessful && response.body()?.data != null) {
            Response.success(response.body()!!.data!!)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

    suspend fun getUsersByGroup(groupId: Int): Response<List<UserData>> {
        val response = apiService.getUsersByGroup(groupId)
        return if (response.isSuccessful && response.body()?.data != null) {
            Response.success(response.body()!!.data!!)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

    suspend fun getNrOfUsersFromGroup(groupId: Int): Response<Int> {
        val response = apiService.getNrOfUsersFromGroup(groupId)
        return if (response.isSuccessful && response.body()?.data != null) {
            Response.success(response.body()!!.data!!)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

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
        
        val apiResponse = rawResponse.body()
        if (apiResponse?.data == null) {
            return Response.success(emptyList())
        }
        
        // The data field contains a list of expenses (already parsed by Moshi)
        val expenses = apiResponse.data
        if (expenses.isEmpty()) {
            return Response.success(emptyList())
        }
        
        // Filter out invalid expenses
        val validExpenses = expenses.filter { expense ->
            expense.id != null && expense.title.isNotBlank()
        }
        
        return Response.success(validExpenses)
    }

    suspend fun getExpenseById(id: Int) = expenseApiService.getExpenseById(id).body()?.data ?: throw Exception("Failed to fetch expense")

    suspend fun addExpenseToGroup(expense: Expense): Int {
        // TODO: Implement proper category lookup by name
        return expenseApiService.addExpense(expense).body()?.data?.id ?: throw Exception("Failed to add expense")
    }

    suspend fun getGroupInviteQr(groupId: Int): Response<ResponseBody> {
        val response = apiService.getGroupInviteQr(groupId)
        return if (response.isSuccessful && response.body() != null) {
            val apiResponse = response.body()!!
            // Backend returns bytes in APIResponse.data field, which FastAPI/Pydantic serializes as base64 string
            // The data field contains the base64-encoded PNG image bytes
            val data = apiResponse.data
            if (data == null) {
                Response.error(
                    response.code(), 
                    ResponseBody.create(null, "QR code data is null")
                )
            } else {
                try {
                    // Ensure data is a string (base64 encoded)
                    val base64String = when (data) {
                        is String -> data
                        else -> data.toString()
                    }
                    
                    // Clean the base64 string - remove whitespace, newlines, and any JSON escaping
                    val cleanedBase64 = base64String
                        .trim()
                        .replace("\n", "")
                        .replace("\r", "")
                        .replace(" ", "")
                        .replace("\"", "") // Remove quotes if JSON escaped
                        .replace("\\", "") // Remove backslashes if escaped
                    
                    if (cleanedBase64.isEmpty()) {
                        Response.error(
                            response.code(), 
                            ResponseBody.create(null, "Empty QR code data")
                        )
                    } else {
                        // Decode base64 to bytes
                        val bytes = Base64.decode(cleanedBase64, Base64.DEFAULT)
                        if (bytes.isEmpty()) {
                            Response.error(
                                response.code(), 
                                ResponseBody.create(null, "Failed to decode QR code: empty result")
                            )
                        } else {
                            // Verify it's a valid PNG (starts with PNG signature)
                            if (bytes.size >= 8 && 
                                bytes[0] == 0x89.toByte() && 
                                bytes[1] == 0x50.toByte() && 
                                bytes[2] == 0x4E.toByte() && 
                                bytes[3] == 0x47.toByte()) {
                                Response.success(ResponseBody.create(null, bytes))
                            } else {
                                // Still return it even if signature check fails, might be valid
                                Response.success(ResponseBody.create(null, bytes))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Response.error(
                        response.code(), 
                        ResponseBody.create(null, "Failed to decode QR code: ${e.message ?: e.javaClass.simpleName}")
                    )
                }
            }
        } else {
            val errorBody = try {
                response.errorBody()?.string() ?: "Unknown error (${response.code()})"
            } catch (e: Exception) {
                "Failed to read error response: ${e.message}"
            }
            Response.error(response.code(), ResponseBody.create(null, errorBody))
        }
    }

    suspend fun joinGroupByInvitationCode(invitationCode: String): Response<Unit> {
        val response = apiService.joinGroupByInvitationCode(invitationCode)
        return if (response.isSuccessful) {
            Response.success(Unit)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }

    suspend fun getGroupLogs(groupId: Int): Response<List<GroupLog>> {
        val response = apiService.getGroupLogs(groupId)
        return if (response.isSuccessful && response.body()?.data != null) {
            Response.success(response.body()!!.data!!)
        } else {
            Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, ""))
        }
    }
}
