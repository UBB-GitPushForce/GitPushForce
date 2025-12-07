package com.example.budgeting.android.ui.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Utility functions for date parsing and formatting
 */
object DateUtils {
    
    /**
     * Parses a date string into a LocalDate object.
     * Handles multiple date formats including ISO 8601, LocalDateTime, and simple date formats.
     */
    fun parseDateFromString(dateString: String?): LocalDate? {
        if (dateString == null) return null
        
        return try {
            // Try ISO 8601 with timezone
            try {
                val zonedDateTime = java.time.ZonedDateTime.parse(dateString)
                return zonedDateTime.toLocalDate()
            } catch (e: Exception) {
            }
            
            // Try LocalDateTime format
            try {
                val dateTime = java.time.LocalDateTime.parse(dateString.take(19))
                return dateTime.toLocalDate()
            } catch (e: Exception) {
            }
            
            // Try simple date format (yyyy-MM-dd)
            try {
                return LocalDate.parse(dateString.take(10))
            } catch (e: Exception) {
            }
            
            // Try custom format (yyyy-MM-dd HH:mm:ss)
            try {
                val cleaned = dateString.replace("T", " ").take(19)
                val dateTime = java.time.LocalDateTime.parse(cleaned, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                return dateTime.toLocalDate()
            } catch (e: Exception) {
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Formats a LocalDate for display.
     * Shows "Today", "Yesterday", or formatted date based on the date.
     */
    fun formatDateForDisplay(date: LocalDate): String {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        return when {
            date == today -> "Today"
            date == yesterday -> "Yesterday"
            date.year == today.year -> date.format(DateTimeFormatter.ofPattern("MMM d"))
            else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        }
    }
}

