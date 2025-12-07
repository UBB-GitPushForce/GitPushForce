package com.example.budgeting.android.ui.components.group

import com.example.budgeting.android.data.model.GroupLog
import com.example.budgeting.android.ui.viewmodels.GroupExpense

sealed class TimelineItem {
    data class ExpenseItem(
        val expense: GroupExpense,
        val timestamp: String?
    ) : TimelineItem()
    
    data class LogItem(
        val log: GroupLog,
        val timestamp: String?
    ) : TimelineItem()
}

