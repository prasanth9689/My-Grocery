package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skyblue.mygrocery.databinding.ActivityProfileSetupBinding
import com.skyblue.mygrocery.ui.AuthViewModel
import com.skyblue.mygrocery.utils.Resource
import com.skyblue.mygrocery.utils.SessionHandler
import com.skyblue.mygrocery.utils.showErrorSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val phone = SessionHandler.getPhoneNumber() ?: ""
        Log.d("PROFILE_", "Phone: $phone")

        observeViewModel()
        observeProfileUpdate()

        binding.btnSubmitProfile.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = SessionHandler.getPhoneNumber() ?: ""

        if (name.isEmpty()) {
            binding.tilName.error = "Please enter your name"
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            return
        }

        binding.tilName.error = null
        binding.tilEmail.error = null

        viewModel.saveProfile(name, email, phone)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.profileResponse.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSubmitProfile.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        navigateToHome()
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitProfile.isEnabled = true
                        Toast.makeText(this@ProfileSetupActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun observeProfileUpdate() {
        lifecycleScope.launch {
            viewModel.profileResponse.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSubmitProfile.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@ProfileSetupActivity, "Profile Saved!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitProfile.isEnabled = true
                        showErrorSnackbar(resource.message ?: "Unknown Error")
                    }
                    else -> Unit
                }
            }
        }
    }
}