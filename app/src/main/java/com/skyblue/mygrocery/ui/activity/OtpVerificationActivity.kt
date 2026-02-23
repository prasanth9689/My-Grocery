package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityOtpVerificationBinding
import com.skyblue.mygrocery.ui.AuthViewModel
import com.skyblue.mygrocery.utils.Resource
import com.skyblue.mygrocery.utils.SessionHandler
import com.skyblue.mygrocery.utils.showErrorSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private val viewModel: AuthViewModel by viewModels()
    private var verificationId: String? = null
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificationId = intent.getStringExtra("verificationId")
        phone = intent.getStringExtra("phoneNumber").toString()
        Log.d("PROFILE_REQ", "Phone 0: $phone")
        binding.tvOtpSubHeader.text = "OTP sent to +91 $phone"

        viewModel.setVerificationId(verificationId ?: "")

        setupOtpInputLogic()
        startResendTimer()
        observeAuthState()

        binding.btnVerify.setOnClickListener {
            val otp = getOtpFromInputs()
            if (otp.length == 6) {
                viewModel.verifyOtp(otp)
            } else {
                showErrorSnackbar(getString(R.string.please_enter_6_digit_otp))
            }
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupOtpInputLogic() {
        val inputs = arrayOf(binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4, binding.etOtp5, binding.etOtp6)

        for (i in inputs.indices) {
            inputs[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < inputs.size - 1) {
                        inputs[i + 1].requestFocus()
                    }
                }
            })

            inputs[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (inputs[i].text.isEmpty() && i > 0) {
                        inputs[i - 1].requestFocus()
                    }
                }
                false
            }
        }
    }

    private fun getOtpFromInputs(): String {
        return binding.etOtp1.text.toString() +
                binding.etOtp2.text.toString() +
                binding.etOtp3.text.toString() +
                binding.etOtp4.text.toString() +
                binding.etOtp5.text.toString() +
                binding.etOtp6.text.toString()
    }

    private fun startResendTimer() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = "Resend code in 00:${millisUntilFinished / 1000}"
            }
            override fun onFinish() {
                binding.tvTimer.text = "Resend OTP"
                binding.tvTimer.setTextColor(resources.getColor(R.color.action_blue, null))
                binding.tvTimer.setOnClickListener {
                    // Trigger resend logic here
                }
            }
        }.start()
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                if (state is Resource.Success) {
                    val uid = state.data

                    viewModel.syncUserWithServer(uid, phone)
                    Log.d("RECEIVED_INTENT", "Phone: $phone")

                } else if (state is Resource.Error) {
                    showErrorSnackbar(state.message)

                    // Clear the OTP fields
                    binding.etOtp1.text?.clear()
                    binding.etOtp2.text?.clear()
                    binding.etOtp3.text?.clear()
                    binding.etOtp4.text?.clear()
                    binding.etOtp5.text?.clear()
                    binding.etOtp6.text?.clear()
                    binding.etOtp1.requestFocus() // Put cursor back at the start
                }
            }
        }

        // Stage 2: Server Verification Result
        lifecycleScope.launch {
            viewModel.syncState.collect { state ->
                when (state) {
                    is Resource.Success -> {
                        val userResponse = state.data

                        if (userResponse.isNewUser) {
                            val phone = intent.getStringExtra("phoneNumber") ?: ""
                            userResponse.data?.let { userData ->
                                SessionHandler.savePhoneNumberAndUserId(phone, userData.userId ?: "")
                            }

                            navigateTo(true) // true means open profile setup activity for new user's
                        } else {
                            userResponse.data?.let { userData ->
                                SessionHandler.loginUser(
                                    userData.userId ?: "",
                                    userData.name ?: "",
                                    userData.email ?: ""
                                )
                            }

                            Log.d("SERVER_RESPONSE", "Data: ${userResponse.data}")
                            navigateTo(false)
                        }
                    }
                    is Resource.Error -> {
                        showErrorSnackbar(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateTo(boolean: Boolean) {
        val intent = Intent(this, NotificationPermissionActivity::class.java)
        if (boolean){
            intent.putExtra("notification_activity", boolean)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

//    private fun observeAuthState() {
//        lifecycleScope.launch {
//            viewModel.authState.collect { state ->
//                when(state){
//                    is Resource.Loading -> {
//                        binding.btnVerify.isEnabled = false
//                        // Optional: Show a progress bar or loading state
//                    }
//                    is Resource.Success -> {
//                        val uid = state.data
//                        SessionHandler.loginUser(uid)
//                        // Proceed to check if user profile exists on your server
//                       // viewModel.checkUserRegistration(uid)
//                    }
//                    is Resource.Error -> {
//                        binding.btnVerify.isEnabled = true
//                        showErrorSnackbar(state.message)
//
//                        // Clear the OTP fields
//                        binding.etOtp1.text?.clear()
//                        binding.etOtp2.text?.clear()
//                        binding.etOtp3.text?.clear()
//                        binding.etOtp4.text?.clear()
//                        binding.etOtp5.text?.clear()
//                        binding.etOtp6.text?.clear()
//                        binding.etOtp1.requestFocus() // Put cursor back at the start
//                    }
//                    else -> {}
//                }
//            }
//        }
//
//        lifecycleScope.launch {
//            viewModel.syncState.collect { state ->
//                when (state) {
//                    is Resource.Loading -> {
//                        binding.btnVerify.isEnabled = false
//                        // Show loading indicator
//                    }
//                    is Resource.Success -> {
//                        // User exists! Sync data and go to Home
//                        val userName = state.data.name ?: ""
//                        val userEmail = state.data.email ?: ""
//
//                        Log.d("PROFILE_REQ", "UserName 1: $userName")
//                        Log.d("PROFILE_REQ", "Email 1: $userEmail")
//
//                        val phone = intent.getStringExtra("phoneNumber")
//
//                        SessionHandler.savePhoneNumber(phone.toString())
//                        SessionHandler.updateUserProfile(userName, userEmail)
//                        val intent = Intent(this@OtpVerificationActivity, NotificationPermissionActivity::class.java)
//                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        startActivity(intent)
//                    }
//                    is Resource.Error -> {
//                        val phone = intent.getStringExtra("phoneNumber") ?: ""
//                        SessionHandler.savePhoneNumber(phone)
//                        binding.btnVerify.isEnabled = true
//                        val intent = Intent(this@OtpVerificationActivity, ProfileSetupActivity::class.java)
//                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        startActivity(intent)
//                    }
//                    else -> {}
//                }
//            }
//        }
//    }
}