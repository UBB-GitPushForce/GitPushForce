package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class GroupStatistics(
    @Json(name = "total_group_spend")
    val totalGroupSpend: Double,
    
    @Json(name = "my_total_paid")
    val myTotalPaid: Double,
    
    @Json(name = "my_share_of_expenses")
    val myShareOfExpenses: Double,
    
    @Json(name = "net_balance_paid_for_others")
    val netBalancePaidForOthers: Double,
    
    @Json(name = "rest_of_group_expenses")
    val restOfGroupExpenses: Double
)

