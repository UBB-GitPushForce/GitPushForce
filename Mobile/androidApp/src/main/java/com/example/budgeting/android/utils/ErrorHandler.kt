package com.example.budgeting.android.utils

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Utility class for handling network exceptions.
 * This is separate from HTTP error response handling (which is done by BaseErrorHandler subclasses).
 */
object ErrorHandler {

    /**
     * Handles exceptions and converts them to user-friendly error messages.
     * Used for network-related exceptions (timeouts, connection issues, etc.).
     */
    fun handleException(exception: Exception): String {
        return when (exception) {
            is SocketTimeoutException -> 
                "Request timed out. Please check your internet connection and try again."
            is UnknownHostException -> 
                "Unable to connect to server. Please check your internet connection."
            is SSLException -> 
                "Connection error. Please try again later."
            else -> {
                when {
                    exception.message?.contains("Unable to resolve host", ignoreCase = true) == true -> 
                        "No internet connection. Please check your network settings."
                    exception.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timed out. Please try again."
                    else -> "An unexpected error occurred: ${exception.message ?: "Please try again later"}"
                }
            }
        }
    }
}
