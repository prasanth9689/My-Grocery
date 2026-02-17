package com.skyblue.mygrocery.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivitySplashBinding
import com.skyblue.mygrocery.utils.SessionHandler

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Play the "Pop" animation we created earlier
        val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)
        binding.ivLogo.startAnimation(anim)
        binding.tvTagline.startAnimation(anim)

        // 2. Wait for 2 seconds then decide where to go
        Handler(Looper.getMainLooper()).postDelayed({
            checkSessionAndNavigate()
        }, 2000)
    }

    private fun checkSessionAndNavigate() {
        // If not logged in -> Go to Login
        if (!SessionHandler.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        // If logged in but name is missing -> Go to Profile Setup
        else if (SessionHandler.getUserName() == "Guest" || SessionHandler.getUserName().isEmpty()) {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }
        // Everything is set -> Go to Home
        else {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // Always finish the splash activity so the user can't go back to it
        finish()
    }
}