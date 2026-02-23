package com.skyblue.mygrocery.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.skyblue.mygrocery.databinding.ActivityNotificationPermissionBinding

class NotificationPermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationPermissionBinding
    var notification_activity: Boolean? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Whether granted or not, we move to Home.
        // We just tried our best to get permission.
        startMainActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notification_activity = intent.getBooleanExtra("notification_activity", false)

        binding.btnEnable.setOnClickListener {
            askForNotificationPermission()
        }

        binding.btnSkip.setOnClickListener {
            startMainActivity()
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                startMainActivity()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Permission not required for API < 33
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        // Declare the intent variable first
        val intent = if (notification_activity == true) {
            Intent(this, ProfileSetupActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}