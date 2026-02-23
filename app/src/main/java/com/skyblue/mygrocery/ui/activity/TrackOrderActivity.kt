package com.skyblue.mygrocery.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityTrackOrderBinding
import com.skyblue.mygrocery.model.TrackingStep
import com.skyblue.mygrocery.ui.adapter.TimelineAdapter
import com.skyblue.mygrocery.ui.viewmodel.OrderViewModel
import com.skyblue.mygrocery.utils.DeliveryNotificationWorker
import com.skyblue.mygrocery.utils.OrderRatingDialog
import com.skyblue.mygrocery.utils.Resource
import com.skyblue.mygrocery.utils.SupportUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TrackOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackOrderBinding
    private val orderId by lazy { intent.getStringExtra("order_id") ?: "" }
    private val phoneNumber = "9876543210" // In production, get this from your API
    private var deliveryTimer: CountDownTimer? = null
    private val viewModel: OrderViewModel by viewModels()

    // Register the permission callback
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            makePhoneCall(phoneNumber)
        } else {
            Toast.makeText(this, "Permission denied to make calls", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTimeline()
        fetchRealtimeStatus()

        // Example: API says order arrives in 10 minutes
        val currentEta = 10
        startEtaCountdown(currentEta)      // Update UI Timer
        scheduleArrivalNotification(currentEta) // Schedule Background Notification

        binding.btnCall.setOnClickListener {
            checkPermissionAndCall()
        }

        binding.btnShare.setOnClickListener {
            shareOrderTracking(orderId)
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
            // Show an explanation dialog to the user why the app needs this permission
            // Then launch the request again
        }

        binding.btnHelp.setOnClickListener {
            val options = arrayOf("Chat on WhatsApp", "Send an Email", "Cancel")

            MaterialAlertDialogBuilder(this)
                .setTitle("How can we help?")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> SupportUtils.openWhatsApp(this, orderId)
                        1 -> SupportUtils.openEmailSupport(this, orderId)
                        else -> dialog.dismiss()
                    }
                }
                .show()
        }
    }

    private fun checkPermissionAndCall() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED -> {
                makePhoneCall(phoneNumber)
            }
            else -> {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    private fun makePhoneCall(number: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    private fun setupTimeline() {
        val adapter = TimelineAdapter()
        binding.rvTrackingTimeline.adapter = adapter

        val steps = listOf(
            TrackingStep("Order Placed", "10:00 AM", isCompleted = true, isCurrent = false),
            TrackingStep("Order Packed", "10:05 AM", isCompleted = true, isCurrent = false),
            TrackingStep("Out for Delivery", "10:10 AM", isCompleted = false, isCurrent = true),
            TrackingStep("Order Delivered", "--:--", isCompleted = false, isCurrent = false)
        )

        adapter.submitList(steps)
    }

    private fun fetchRealtimeStatus() {
        // In a real Zepto-style app, you would use WebSockets or Firebase Realtime DB
        // to listen for status changes (e.g., "Out for delivery")

        lifecycleScope.launch {
            // Simulated state update
            binding.tvEstimateTime.text = "5 MINS"
            // Update timeline UI based on server status
        }
    }

    private fun startEtaCountdown(initialMinutes: Int) {
        val durationMillis = (initialMinutes * 60 * 1000).toLong()

        deliveryTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60

                // Zepto style: "8 MINS" if > 1 min, or "55 SECS" if < 1 min
                if (minutes > 0) {
                    binding.tvEstimateTime.text = "$minutes MINS"
                } else {
                    binding.tvEstimateTime.text = "$seconds SECS"
                }
            }

            override fun onFinish() {
                binding.tvEstimateTime.text = "ARRIVING"
                // Optionally: Trigger a notification or status update
            }
        }.start()
    }

    // Crucial: Stop the timer if the user leaves the screen to save battery
    override fun onDestroy() {
        super.onDestroy()
        deliveryTimer?.cancel()
    }

    private fun startPulseAnimation() {
        val animation = AlphaAnimation(1.0f, 0.6f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.tvEstimateTime.startAnimation(animation)
    }

    private fun scheduleArrivalNotification(etaMinutes: Int) {
        if (etaMinutes <= 1) return // Too late to schedule

        // Calculate delay: If ETA is 10 mins, notify in 9 mins
        val delayInMinutes = (etaMinutes - 1).toLong()

        val notificationRequest = OneTimeWorkRequestBuilder<DeliveryNotificationWorker>()
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .addTag("delivery_notif")
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "order_arrival_check",
            ExistingWorkPolicy.REPLACE, // Replaces if ETA updates
            notificationRequest
        )
    }

    private fun onOrderDelivered() {
        // 1. Update the local UI
        binding.tvEstimateTime.text = "DELIVERED"

        // 2. Show the rating popup
        val ratingDialog = OrderRatingDialog(orderId) { rating, feedback ->
            // Send this data to your ViewModel -> Repository -> API
            viewModel.submitOrderRating(orderId, rating, feedback)

            Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_LONG).show()

            // Optionally redirect back to Home
            finish()
        }

        ratingDialog.show(supportFragmentManager, "RatingDialog")
    }

    private fun observeRatingStatus() {
        lifecycleScope.launch {
            viewModel.ratingStatus.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show a small progress bar if needed
                    }
                    is Resource.Success -> {
                        Toast.makeText(this@TrackOrderActivity, "Feedback Received!", Toast.LENGTH_SHORT).show()
                        // Maybe navigate to Home screen now
                        navigateToHome()
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@TrackOrderActivity, resource.message, Toast.LENGTH_LONG).show()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            // This clears all previous activities so "Home" becomes the new root
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Closes the current activity
    }

    private fun shareOrderTracking(orderId: String) {
        // In a real app, this would be a deep link to your website/app
        val trackingUrl = "https://yourapp.com/track/$orderId"
        val shareMessage = """
        Hey! My order #$orderId is on the way. 
        You can track it live here: $trackingUrl
    """.trimIndent()

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share tracking link via")
        startActivity(shareIntent)
    }
}