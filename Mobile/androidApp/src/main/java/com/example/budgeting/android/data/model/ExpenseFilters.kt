package com.example.budgeting.android.data.model

data class ExpenseFilters(
    val search: String = "",
    val category: String = "",
    val sortOption: SortOption = SortOption.TITLE_ASC
)

enum class SortOption {
    AMOUNT_ASC,
    AMOUNT_DESC,
    TITLE_ASC,
    TITLE_DESC
}
