package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skyblue.mygrocery.databinding.ActivityProfileSetupBinding
import com.skyblue.mygrocery.utils.SessionHandler

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmitProfile.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Please enter your name"
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            return
        }

        // Clear errors
        binding.tilName.error = null
        binding.tilEmail.error = null

        saveProfile(name, email)
    }

    private fun saveProfile(name: String, email: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitProfile.isEnabled = false

        // Update session handler with the extra details
        // Note: You might want to update your SessionHandler to include 'name'
        SessionHandler.updateUserProfile(name, email)

        // Simulate a small delay for quality feel or call your API here
        binding.root.postDelayed({
            binding.progressBar.visibility = View.GONE

            // Move to Notification Permission screen
            val intent = Intent(this, NotificationPermissionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1000)
    }
}