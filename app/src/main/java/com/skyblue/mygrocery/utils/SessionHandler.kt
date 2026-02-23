package com.skyblue.mygrocery.utils

import android.content.Context
import android.content.SharedPreferences
import com.skyblue.mygrocery.model.User
import java.util.*
import androidx.core.content.edit

object SessionHandler {
    private var PREFS_KEY = "prefs"
    private const val MODE = Context.MODE_PRIVATE
    private var EMAIL_KEY = "email"
    private var PWD_KEY = "pwd"
    private var KEY_USER_UID = "user_uid"
    private var KEY_USER_ID = "user_id"
    private const val KEY_PHONE = "user_phone"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_EXPIRES = "expires"
    private const val KEY_EMPTY = ""

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_KEY, MODE)
    }

    fun savePhoneNumberAndUserId(phone: String, userId: String) {
        // This extension automatically calls .apply() at the end of the block
            sharedPreferences.edit(commit = false) {
            putString(KEY_PHONE, phone)
                putString(KEY_USER_ID, userId)
        }
    }

    // You will need this to retrieve it in ProfileSetupActivity
    fun getPhoneNumber(): String? {
        return sharedPreferences.getString(KEY_PHONE, null)
    }

    fun loginUser(userId: String, phone: String, email: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_PHONE, phone)
            putString(EMAIL_KEY, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserId(): String {
        return sharedPreferences.getString(KEY_USER_ID, KEY_EMPTY) ?: KEY_EMPTY
    }

    fun getUserName(): String = sharedPreferences.getString(KEY_USER_NAME, "Guest") ?: "Guest"
    fun getUserEmail(): String = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""

    fun updateUserProfile(name: String, email: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun logoutUser() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Fetches and returns user details
     *
     * @return user details
     */
    fun getUserDetails(): User? {
        if (!isLoggedIn()) return null

        val user = User()
        user.userId =
            sharedPreferences.getString(KEY_USER_ID, KEY_EMPTY) // Ensure User model has id
        user.email = sharedPreferences.getString(EMAIL_KEY, KEY_EMPTY)
        user.password = sharedPreferences.getString(PWD_KEY, KEY_EMPTY)
        user.sessionExpiryDate = Date(sharedPreferences.getLong(KEY_EXPIRES, 0))

        return user
    }

}