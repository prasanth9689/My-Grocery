package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.skyblue.mygrocery.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val options = FirebaseOptions.Builder()
//            .setApplicationId("1:515942337063:android:6c065863c3142e9aa39c02") // Found in google-services.json as mobilesdk_app_id
//            .setApiKey("AIzaSyDYBJCOvX71so8SqTkDTzV11c2n7UqKKOk")       // Found in google-services.json as current_key
//            .setProjectId("my-grocery-71445") // Found in google-services.json as project_id
//            .build()
//
//        if (FirebaseApp.getApps(this).isEmpty()) {
//            FirebaseApp.initializeApp(this)
//        }

        setupPrivacyPolicyText()

        binding.btnContinue.setOnClickListener {
            val phone = binding.etPhoneNumber.text.toString().trim()
            if (phone.length == 10) {
                startPhoneNumberVerification("+91$phone")
            } else {
                binding.etPhoneNumber.error = "Enter valid 10-digit number"
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnContinue.isEnabled = false

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
            // Auto-retrieval or instant verification
            val code = credential.smsCode
            if (code != null) {
                // You can skip OTP activity or pass this code to it
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            binding.progressBar.visibility = View.GONE
            binding.btnContinue.isEnabled = true
            Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            binding.progressBar.visibility = View.GONE
            val intent = Intent(this@LoginActivity, OtpVerificationActivity::class.java)
            intent.putExtra("verificationId", verificationId)
            intent.putExtra("phoneNumber", binding.etPhoneNumber.text.toString())
            startActivity(intent)
        }
    }

    private fun setupPrivacyPolicyText() {
        val fullText = "By continuing, you agree to our Terms of Service and Privacy Policy"
        val spannableString = SpannableString(fullText)

        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open WebView/Browser for Privacy Policy
            }
        }

        spannableString.setSpan(privacyClickable, 32, 48, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), 32, 48, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(privacyClickable, 53, 67, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), 53, 67, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvPolicy.text = spannableString
        binding.tvPolicy.movementMethod = LinkMovementMethod.getInstance()
    }
}