package com.example.budgeting.android.data.model

data class ExpenseFilters(
    val search: String = "",
    val category: String = "All",
    val sortOption: SortOption = SortOption.NEWEST,
    val minAmount: Float? = null,
    val maxAmount: Float? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

enum class SortOption(
    val sortBy: String,
    val order: String,
    val label: String
) {
    NEWEST("created_at", "desc", "Newest"),
    OLDEST("created_at", "asc", "Oldest"),
    AMOUNT_LOW("amount", "asc", "Amount Low → High"),
    AMOUNT_HIGH("amount", "desc", "Amount High → Low"),
    TITLE_ASC("title", "asc", "Title A → Z"),
    TITLE_DESC("title", "desc", "Title Z → A")
}
