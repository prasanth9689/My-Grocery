package com.skyblue.mygrocery.utils

import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

fun AppCompatActivity.showErrorSnackbar(message: String, length: Int = Snackbar.LENGTH_LONG) {
    // findViewById(android.R.id.content) gets the root view of any Activity
    val rootView = this.findViewById<View>(android.R.id.content)
    val snackbar = Snackbar.make(rootView, message, length)

    snackbar.setBackgroundTint(Color.parseColor("#B00020")) // Error Red
    snackbar.setTextColor(Color.WHITE)
    snackbar.show()
}