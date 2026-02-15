package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.skyblue.mygrocery.R
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import com.skyblue.mygrocery.databinding.ActivityOtpVerificationBinding
import com.skyblue.mygrocery.utils.OtpHelper


class OtpVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    private lateinit var otpHelper: OtpHelper
    private lateinit var etList: List<EditText>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        verificationId = intent.getStringExtra("verification_id")

        etList = listOf(
            findViewById(R.id.et_otp_1),
            findViewById(R.id.et_otp_2),
            findViewById(R.id.et_otp_3),
            findViewById(R.id.et_otp_4),
            findViewById(R.id.et_otp_5),
            findViewById(R.id.et_otp_6)
        )

        otpHelper = OtpHelper(this, etList) { code ->
            // OTP completed automatically by user typing or paste
            // You may auto-click verify or just enable verify button
            verifyCode(code)
        }

        binding.verify.setOnClickListener {
            val code = etList.joinToString("") { it.text.toString().trim() }
            if (code.length == 6) verifyCode(code) else Toast.makeText(this, "Enter 6 digit code", Toast.LENGTH_SHORT).show()
        }

        binding.tvResend.setOnClickListener {
            // Trigger resend via Firebase (call sendVerificationCode again)
            val phone = intent.getStringExtra("phone")
            phone?.let { startPhoneNumberVerification(it) }
        }



        // Start SMS Retriever to boost auto-fill success (no SMS permission required)
        startSmsRetriever()
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-retrieved — might contain SMS code or directly be credential
            val smsCode = credential.smsCode
            if (!smsCode.isNullOrEmpty()) {
                // fill UI
                runOnUiThread {
                    otpHelper.handlePaste(smsCode) // note: handlePaste is private in helper; change to public if needed
                }
                // optionally sign in immediately
                signInWithPhoneAuthCredential(credential)
            } else {
                // Sometimes credential has no smsCode but can sign in directly
                signInWithPhoneAuthCredential(credential)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@OtpVerificationActivity, "Verification failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@OtpVerificationActivity.verificationId = verificationId
            Toast.makeText(this@OtpVerificationActivity, "Code sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyCode(code: String) {
        val id = verificationId
        if (id == null) {
            Toast.makeText(this, "Verification id is null. Try resend.", Toast.LENGTH_SHORT).show()
            return
        }
        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // success — user signed in
                    Toast.makeText(this, "Authenticated!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EnableNotificationActivity::class.java)
                    startActivity(intent)
                    // proceed to next screen
                } else {
                    Toast.makeText(this, "Invalid code or error: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // --- SMS Retriever API (optional) ---
    private fun startSmsRetriever() {
        val client = SmsRetriever.getClient(this)
        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener {
            // Successfully started retriever, waits for broadcast with SMS.
            // You need to register a BroadcastReceiver listening for com.google.android.gms.auth.api.phone.SMS_RETRIEVED
            // In onActivityResult or receiver, extract SMS and parse code.
        }
        task.addOnFailureListener {
            // failed to start
        }
    }
}