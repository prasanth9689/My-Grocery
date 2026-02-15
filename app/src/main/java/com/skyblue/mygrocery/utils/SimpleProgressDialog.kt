package com.skyblue.mygrocery.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.skyblue.mygrocery.R

class SimpleProgressDialog(private val ctx: Context) {
    private var dialog: AlertDialog? = null

    @SuppressLint("MissingInflatedId")
    fun show(message: String = "Please wait...", cancelable: Boolean = false) {
        if (dialog?.isShowing == true) return

        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_progress, null)
        view.findViewById<TextView>(R.id.progress_text).text = message

        dialog = AlertDialog.Builder(ctx)
            .setView(view)
            .setCancelable(cancelable)
            .create().apply {
                // Show the dialog instance
                try {
                    show()  // Changed from Dialog.show() to show()
                } catch (_: Exception) {}
            }
    }

    fun dismiss() {
        try {
            dialog?.dismiss()
        } catch (_: Exception) {}
        dialog = null
    }

    fun isShowing(): Boolean = dialog?.isShowing == true
}