package com.skyblue.mygrocery.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityAddLocationBinding
import com.skyblue.mygrocery.model.UserLocation
import com.skyblue.mygrocery.ui.viewmodel.LocationViewModel
import com.skyblue.mygrocery.utils.Resource
import com.skyblue.mygrocery.utils.SessionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLocationBinding
    private val viewModel: LocationViewModel by viewModels()
    lateinit var session: SessionHandler

    // Variables to hold data passed from HomeActivity
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var fullAddress: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler

        // 1. Get data passed from HomeActivity
        latitude = intent.getDoubleExtra("lat", 0.0)
        longitude = intent.getDoubleExtra("long", 0.0)
        fullAddress = intent.getStringExtra("address")

        // Display the fetched address in a non-editable text view or hint
        binding.tvSelectedAddress.text = fullAddress

        setupListeners()
        observeSaveStatus()
        observeSaveStatus()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveAddress.setOnClickListener {
            validateAndSave()
        }
    }

    private fun validateAndSave() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val floor = binding.etFloor.text.toString().trim()
        val landmark = binding.etLandmark.text.toString().trim()

        // Get selected location type from ToggleGroup
        val locationType = when (binding.toggleGroupLocation.checkedButtonId) {
            R.id.btnHome -> "Home"
            R.id.btnWork -> "Work"
            R.id.btnOther -> "Friend"
            else -> "Other"
        }

        if (name.isEmpty() || phone.isEmpty() || floor.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = SessionHandler.getUserId()

        val newUserLocation = UserLocation(
            userId = currentUserId,
            latitude = latitude,
            longitude = longitude,
            address = fullAddress,
            locationType = locationType,
            receiverName = name,
            receiverPhone = phone,
            floorDetail = floor,
            areaLandmark = landmark,
            isSavedAddress = true,
            isCurrentLocation = true // Set as the active delivery address
        )

        viewModel.saveAddress(newUserLocation)
    }

    private fun observeSaveStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is Resource.Idle -> {
                            // Ensure button is enabled when nothing is happening
                            binding.btnSaveAddress.isEnabled = true
                            binding.progressBar.visibility = View.GONE
                        }
                        is Resource.Loading -> {
                            binding.btnSaveAddress.isEnabled = false
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@AddLocationActivity, "Address Saved!", Toast.LENGTH_SHORT).show()
                            finish() // Go back to Home
                        }
                        is Resource.Error -> {
                            binding.btnSaveAddress.isEnabled = true
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@AddLocationActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

}