package com.it342.g3.mobile.util

import java.text.SimpleDateFormat
import java.util.Locale

object UiFormat {
    private val isoDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDate = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val displayDateTime = SimpleDateFormat("MMM d, h:mm a", Locale.US)

    fun displayDate(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        return try {
            val date = isoDate.parse(value)
            if (date != null) displayDate.format(date) else value
        } catch (ex: Exception) {
            value
        }
    }

    fun displayDateTime(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        return try {
            val date = isoDateTime.parse(value)
            if (date != null) displayDateTime.format(date) else value
        } catch (ex: Exception) {
            value
        }
    }
}
