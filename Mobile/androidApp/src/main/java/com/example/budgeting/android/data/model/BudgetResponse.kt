package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

class BudgetResponse(
    @Json(name = "budget")
    val budget: Double,

    @Json(name = "spent_this_month")
    val spentThisMonth: Double,

    @Json(name = "remaining_budget")
    val remainingBudget: Double
)