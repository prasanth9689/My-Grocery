package com.skyblue.mygrocery.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.ui.activity.OtpVerificationActivity
import com.skyblue.mygrocery.databinding.ActivityMainBinding
import com.skyblue.mygrocery.ui.activity.HomeActivity
import com.skyblue.mygrocery.utils.SimpleProgressDialog
import java.util.concurrent.TimeUnit
import kotlin.jvm.java
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.removePrefix
import kotlin.text.substring
import kotlin.text.trim

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var isHintShown = false

    // 1. Define the Launcher (You've likely done this, but for completeness)
    private val phoneNumberHintLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            // 2. THIS is where you need to check the result codes
            when (result.resultCode) {
                RESULT_OK -> {
                    // Success: The user selected a phone number
                    try {
                        val phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                        // Use the phone number here

                        if (phoneNumber.isNotEmpty()) {
                            val strippedNumber = phoneNumber.removePrefix("+")
                            val localPhoneNumber = extractLocalNumber(strippedNumber)
                            binding.phoneNo.setText(localPhoneNumber)
                        } else {
                            Toast.makeText(this, "No phone number found", Toast.LENGTH_SHORT).show()
                        }
                        //Toast.makeText(this, "Phone Number: $phoneNumber", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        // Handle parsing error
                        Toast.makeText(this, "Error parsing phone number: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                RESULT_CANCELED -> {
                    // User cancelled the operation
                    Toast.makeText(this, "Phone Hint Cancelled by user", Toast.LENGTH_SHORT).show()
                }
                // Add a case for other errors if needed, though RESULT_OK and CANCELED cover most success/failure cases.
                else -> {
                    // Handle other result codes (e.g., if Google Play Services returns a specific error)
                    Toast.makeText(this, "Unknown result code: ${result.resultCode}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    fun extractLocalNumber(e164Number: String): String {
        // 1. Remove the leading '+'
        val numberWithoutPlus = e164Number.removePrefix("+")

        // 2. Assume the number is 10 digits (common in many countries)
        //    and take the last 10 digits.
        if (numberWithoutPlus.length >= 10) {
            return numberWithoutPlus.substring(numberWithoutPlus.length - 10)
        }

        // 3. Fallback: If less than 10 digits, just return the number without the '+'
        return numberWithoutPlus
    }

//    private val phoneNumberHintLauncher =
//        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
//
//            if (result.resultCode == RESULT_OK && result.data != null) {
//                try {
//                    val credential = Identity.getSignInClient(this)
//                        .getSignInCredentialFromIntent(result.data)
//
//                    val phone = credential.phoneNumber
//
//                    if (!phone.isNullOrEmpty()) {
//                        binding.phoneNo.setText(phone)
//                    } else {
//                        Toast.makeText(this, "No phone number found", Toast.LENGTH_SHORT).show()
//                    }
//
//                } catch (e: Exception) {
//                    Toast.makeText(this, "Phone Hint Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
//            }
//        }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnContinue.setOnClickListener {
            val mobileNumber = binding.phoneNo.text.toString().trim()

            if (mobileNumber.isEmpty()){
                Toast.makeText(this, "Enter mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mobileNumber.length != 10){
                Toast.makeText(this, "Enter valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sentOtp(mobileNumber)
        }

        binding.phoneNo.setOnTouchListener { v, event ->
            // Check if the action is ACTION_UP (when the user lifts their finger)
            if (event.action == MotionEvent.ACTION_UP) {

                // Crucial: Check the state flag
                if (!isHintShown) {
                    // Set the flag immediately to prevent future launches
                    isHintShown = true

                    // Call your function to start the Phone Number Hint flow
                    requestPhoneNumber()
                }
            }
            // Let the system handle the touch event (e.g., show the keyboard)
            return@setOnTouchListener false
        }
    }

    private fun requestPhoneNumber() {
        val request = GetPhoneNumberHintIntentRequest.builder().build()

        Identity.getSignInClient(this)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.intentSender).build()

                    phoneNumberHintLauncher.launch(intentSenderRequest)

                } catch (e: IntentSender.SendIntentException) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Phone Hint Failed", Toast.LENGTH_SHORT).show()
            }
    }


    private fun sentOtp(mobileNumber: String) {

        auth = FirebaseAuth.getInstance()

        val progress = SimpleProgressDialog(this)
        progress.show("Sending OTP")

        val phone = "+91$mobileNumber"   // convert to full format

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification (rare case)
                    progress.dismiss()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progress.dismiss()
                    Toast.makeText(this@MainActivity, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    progress.dismiss()

                    resendToken = token

                    Toast.makeText(this@MainActivity, "OTP Sent!", Toast.LENGTH_SHORT).show()

                    // Navigate to OTP screen
                    val intent = Intent(this@MainActivity, OtpVerificationActivity::class.java)
                    intent.putExtra("verification_id", verificationId)
                    intent.putExtra("phone", mobileNumber)
                    startActivity(intent)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}