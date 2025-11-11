package com.example.budgeting.android.utils

/**
 * Error handler specifically for groups-related errors.
 */
object GroupsErrorHandler : BaseErrorHandler() {

    /**
     * Extracts user-friendly error message from error text for groups.
     */
    override fun extractErrorMessageFromText(errorText: String, statusCode: Int): String {
        return when {
            errorText.contains("already a member", ignoreCase = true) -> 
                "You are already a member of this group"
            errorText.contains("group", ignoreCase = true) && errorText.contains("not found", ignoreCase = true) -> 
                "Group not found"
            errorText.contains("not found", ignoreCase = true) -> 
                "Group not found"
            errorText.contains("unauthorized", ignoreCase = true) || 
            errorText.contains("authentication", ignoreCase = true) -> 
                "Authentication failed. Please log in again."
            errorText.contains("required valued", ignoreCase = true) -> 
                "Invalid data provided. Please check your information and try again."
            else -> getDefaultErrorMessage(statusCode)
        }
    }

    /**
     * Returns default error message based on HTTP status code for groups.
     */
    override fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Invalid request. Please check your information and try again."
            401 -> "Authentication failed. Please log in again."
            403 -> "Access denied. Please contact support."
            404 -> "Group not found"
            409 -> "Conflict occurred. Please try again."
            422 -> "Invalid data provided. Please check your details and try again."
            500 -> "Server error. Please try again later."
            503 -> "Service unavailable. Please try again later."
            else -> "An error occurred. Please try again."
        }
    }
}

