package com.skyblue.mygrocery.utils

import android.content.Context
import android.content.SharedPreferences
import com.skyblue.mygrocery.model.User
import java.util.*

object SessionHandler {
    private var PREFS_KEY = "prefs"
    private const val MODE = Context.MODE_PRIVATE
    private var EMAIL_KEY = "email"
    private var PWD_KEY = "pwd"
    private var USER_ID_KEY = "user_id"
    private const val KEY_EXPIRES = "expires"
    private const val KEY_EMPTY = ""

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_KEY, MODE)
    }

    // TODO step 1: call `AppPreferences.setup(applicationContext)` in your MainActivity's `onCreate` method
    // TODO step 2: set your app name here
    fun loginUser(userId: String, email: String, password: String){
        val editer : SharedPreferences.Editor = sharedPreferences.edit()
        editer.putString(USER_ID_KEY, userId) // Save ID
        editer.putString(EMAIL_KEY, email)
        editer.putString(PWD_KEY, password)

        val date = Date()
        val millis = date.time + 7 * 24 * 60 * 60 * 1000
        editer.putLong(KEY_EXPIRES, millis)
        editer.apply()
    }

    fun getUserId(): String {
        return sharedPreferences.getString(USER_ID_KEY, KEY_EMPTY) ?: KEY_EMPTY
    }

    /**
     * Fetches and returns user details
     *
     * @return user details
     */
    fun getUserDetails(): User? {
        if (!isLoggedIn()) return null

        val user = User()
        user.userId = sharedPreferences.getString(USER_ID_KEY, KEY_EMPTY) // Ensure User model has id
        user.email = sharedPreferences.getString(EMAIL_KEY, KEY_EMPTY)
        user.password = sharedPreferences.getString(PWD_KEY, KEY_EMPTY)
        user.sessionExpiryDate = Date(sharedPreferences.getLong(KEY_EXPIRES, 0))

        return user
    }


    /**
     * Logs out user by clearing the session
     */
    fun logoutUser() {
        val editer : SharedPreferences.Editor = sharedPreferences.edit()
        editer.clear()
        editer.commit()
    }

    /**
     * Checks whether user is logged in
     *
     * @return
     */
    fun isLoggedIn(): Boolean {
        val currentDate = Date()
        val millis: Long = sharedPreferences.getLong(KEY_EXPIRES, 0)

        /* If shared preferences does not have a value
         then user is not logged in
         */if (millis == 0L) {
            return false
        }
        val expiryDate = Date(millis)

        /* Check if session is expired by comparing
        current date and Session expiry date
        */return currentDate.before(expiryDate)
    }
}