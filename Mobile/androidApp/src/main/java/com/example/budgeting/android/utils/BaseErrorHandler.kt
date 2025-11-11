package com.example.budgeting.android.utils

import org.json.JSONObject

/**
 * Base abstract class for domain-specific error handlers.
 * Provides common error parsing logic while allowing subclasses to customize error messages.
 */
abstract class BaseErrorHandler {

    /**
     * Parses HTTP error responses and returns user-friendly error messages.
     * This is the template method that uses domain-specific implementations.
     */
    fun parseErrorResponse(statusCode: Int, errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return getDefaultErrorMessage(statusCode)
        }

        return try {
            // Try to parse JSON error response
            val jsonObject = JSONObject(errorBody)
            val errorText = parseJsonError(jsonObject) ?: errorBody
            extractErrorMessageFromText(errorText, statusCode)
        } catch (e: Exception) {
            // If JSON parsing fails, try to extract meaningful text
            extractErrorMessageFromText(errorBody, statusCode)
        }
    }

    /**
     * Parses JSON error response to extract error text.
     */
    protected fun parseJsonError(jsonObject: JSONObject): String? {
        return when {
            jsonObject.has("detail") -> jsonObject.getString("detail")
            jsonObject.has("message") -> jsonObject.getString("message")
            else -> null
        }
    }

    /**
     * Extracts user-friendly error message from error text.
     * Subclasses should override this to provide domain-specific error handling.
     */
    protected abstract fun extractErrorMessageFromText(errorText: String, statusCode: Int): String

    /**
     * Returns default error message based on HTTP status code.
     * Subclasses can override this to provide domain-specific default messages.
     */
    protected open fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Invalid request. Please check your information and try again."
            401 -> "Authentication failed. Please log in again."
            403 -> "Access denied. Please contact support."
            404 -> "Resource not found. Please try again later."
            409 -> "Conflict occurred. Please try again."
            422 -> "Invalid data provided. Please check your details and try again."
            500 -> "Server error. Please try again later."
            503 -> "Service unavailable. Please try again later."
            else -> "An error occurred. Please try again."
        }
    }
}

