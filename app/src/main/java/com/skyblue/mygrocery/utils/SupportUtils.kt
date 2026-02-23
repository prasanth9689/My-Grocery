package com.skyblue.mygrocery.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object SupportUtils {

    // Opens WhatsApp with a pre-filled message
    fun openWhatsApp(context: Context, orderId: String) {
        val phoneNumber = "919876543210" // Replace with your support number (with country code)
        val message = "Hello Support, I need help with my Order ID: #$orderId"

        try {
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    // Opens Email client with subject and body
    fun openEmailSupport(context: Context, orderId: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@yourapp.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Support Request: Order #$orderId")
            putExtra(Intent.EXTRA_TEXT, "Hi Team,\n\nI'm having an issue with my order #$orderId.\n[Describe your issue here]")
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Send Email using..."))
        } catch (e: Exception) {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}