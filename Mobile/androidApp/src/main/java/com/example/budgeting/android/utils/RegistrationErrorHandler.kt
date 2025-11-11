package com.example.budgeting.android.utils

/**
 * Error handler specifically for registration-related errors.
 */
object RegistrationErrorHandler : BaseErrorHandler() {

    /**
     * Extracts user-friendly error message from error text for registration.
     */
    override fun extractErrorMessageFromText(errorText: String, statusCode: Int): String {
        return when {
            errorText.contains("already exists", ignoreCase = true) -> 
                "An account with this email already exists. Please log in instead."
            errorText.contains("email", ignoreCase = true) && errorText.contains("invalid", ignoreCase = true) -> 
                "Please enter a valid email address."
            errorText.contains("email", ignoreCase = true) -> 
                "Invalid email address. Please check and try again."
            errorText.contains("password", ignoreCase = true) -> 
                "Password does not meet requirements. Please choose a stronger password."
            errorText.contains("unauthorized", ignoreCase = true) || 
            errorText.contains("authentication", ignoreCase = true) -> 
                "Authentication failed. Please log in again."
            errorText.contains("required valued", ignoreCase = true) -> 
                "Invalid data provided. Please check your information and try again."
            else -> getDefaultErrorMessage(statusCode)
        }
    }

    /**
     * Returns default error message based on HTTP status code for registration.
     */
    override fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Invalid request. Please check your information and try again."
            401 -> "Authentication failed. Please try again."
            403 -> "Access denied. Please contact support."
            404 -> "Service not found. Please try again later."
            409 -> "An account with this email already exists."
            422 -> "Invalid data provided. Please check your details and try again."
            500 -> "Server error. Please try again later."
            503 -> "Service unavailable. Please try again later."
            else -> "Registration failed. Please try again."
        }
    }

    /**
     * Validates email format.
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return emailRegex.toRegex().matches(email)
    }

    /**
     * Validates phone number format.
     */
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneRegex = "^[0-9]{6,15}\$"
        return phoneRegex.toRegex().matches(phoneNumber)
    }

    /**
     * Validates registration form inputs and returns field-specific errors.
     */
    fun validateRegistrationInputs(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (firstName.isBlank()) {
            errors["firstName"] = "First name is required"
        } else if (firstName.length < 2) {
            errors["firstName"] = "First name must be at least 2 characters"
        }

        if (lastName.isBlank()) {
            errors["lastName"] = "Last name is required"
        } else if (lastName.length < 2) {
            errors["lastName"] = "Last name must be at least 2 characters"
        }

        if (email.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!isValidEmail(email)) {
            errors["email"] = "Please enter a valid email address"
        }

        if (password.isBlank()) {
            errors["password"] = "Password is required"
        } else if (password.length < 6) {
            errors["password"] = "Password must be at least 6 characters"
        }

        if (phoneNumber.isBlank()) {
            errors["phoneNumber"] = "Phone number is required"
        } else if (!isValidPhoneNumber(phoneNumber)) {
            errors["phoneNumber"] = "Please enter a valid phone number"
        }

        return errors
    }
}

