package com.skyblue.mygrocery.ui.activity

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityUpdateLocationBinding
import com.skyblue.mygrocery.model.UserLocation
import com.skyblue.mygrocery.ui.viewmodel.LocationViewModel
import com.skyblue.mygrocery.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateLocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateLocationBinding
    private val viewModel: LocationViewModel by viewModels()
    private lateinit var existingLocation: UserLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the object passed from the List screen
        val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("location_data", UserLocation::class.java)
        } else {
            // For older versions
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<UserLocation>("location_data")
        }

        preFillData()
        setupListeners()
        observeUpdateStatus()
    }

    private fun preFillData() {
        binding.etName.setText(existingLocation.receiverName)
        binding.etPhone.setText(existingLocation.receiverPhone)
        binding.etFloor.setText(existingLocation.floorDetail)
        binding.etLandmark.setText(existingLocation.areaLandmark)
        // Select the toggle button based on type
        when(existingLocation.locationType) {
            "Home" -> binding.toggleGroup.check(R.id.btnHome)
            "Work" -> binding.toggleGroup.check(R.id.btnWork)
        }
    }

    private fun setupListeners() {
        binding.btnUpdateAddress.setOnClickListener {
            val updatedLocation = existingLocation.copy(
                receiverName = binding.etName.text.toString(),
                receiverPhone = binding.etPhone.text.toString(),
                floorDetail = binding.etFloor.text.toString(),
                areaLandmark = binding.etLandmark.text.toString()
            )
            viewModel.updateAddress(updatedLocation)
        }
    }

    private fun observeUpdateStatus() {
        lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                if (state is Resource.Success) {
                    Toast.makeText(this@UpdateLocationActivity, "Address Updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}