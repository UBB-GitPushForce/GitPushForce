package com.example.budgeting.android.ui.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.ui.viewmodels.GroupExpense

object GroupUtils {
    
    fun isExpenseAlreadyInGroup(
        expense: Expense,
        groupExpenses: List<GroupExpense>
    ): Boolean {
        return groupExpenses.any { groupExpense ->
            groupExpense.expense.title == expense.title &&
            groupExpense.expense.amount == expense.amount &&
            groupExpense.expense.categoryId == expense.categoryId &&
            groupExpense.expense.user_id == expense.user_id
        }
    }
    
    fun shareGroupInvite(context: Context, groupName: String, invitationCode: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join $groupName")
            putExtra(
                Intent.EXTRA_TEXT,
                "Join \"$groupName\" using this invitation code: $invitationCode"
            )
        }
        try {
            context.startActivity(
                Intent.createChooser(shareIntent, "Share group invite")
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open share options",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

