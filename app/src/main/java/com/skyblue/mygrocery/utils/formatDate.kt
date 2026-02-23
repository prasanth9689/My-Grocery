package com.skyblue.mygrocery.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(dateString: String): String {
    return try {
        // 1. Parse the incoming string (Adjust pattern if your API format is different)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(dateString)

        // 2. Format it for the UI
        val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString // Return original if parsing fails
    }
}