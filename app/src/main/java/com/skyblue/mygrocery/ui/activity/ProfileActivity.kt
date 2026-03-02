package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.skyblue.mygrocery.databinding.ActivityProfileBinding
import com.skyblue.mygrocery.utils.SessionHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserDetails()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun setupUserDetails() {
        val name = SessionHandler.getUserName()
        val email = SessionHandler.getUserEmail()
        val phone = SessionHandler.getPhoneNumber()

        binding.tvProfileName.text = name.ifEmpty { "Guest User" }
        binding.tvProfileEmail.text = if (email.isNotEmpty()) email else "No email linked"
        phone?.let { binding.tvProfilePhone.text = if (it.isNotEmpty()) phone else "No phone linked" }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        SessionHandler.logoutUser()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}