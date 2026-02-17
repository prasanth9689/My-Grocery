package com.skyblue.mygrocery.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivitySavedAddressesBinding
import com.skyblue.mygrocery.ui.adapter.SavedLocationAdapter
import com.skyblue.mygrocery.ui.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedAddressesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedAddressesBinding
    private val viewModel: LocationViewModel by viewModels()
    private lateinit var addressAdapter: SavedLocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedAddressesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeLocations()
        setupSwipeToDelete()

        binding.btnAddNew.setOnClickListener {
            // This takes user back to a map or direct home to fetch GPS
            finish()
        }
    }

    private fun setupRecyclerView() {
        addressAdapter = SavedLocationAdapter { selectedLocation ->
            // If it's already active, no need to show dialog or update
            if (selectedLocation.isCurrentLocation) {
                finish()
                return@SavedLocationAdapter
            }

            // Show Confirmation Dialog
            MaterialAlertDialogBuilder(this)
                .setTitle("Change Delivery Address?")
                .setMessage("Would you like to set '${selectedLocation.locationType}' as your active delivery location?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Set as Active") { _, _ ->
                    // Now call the ViewModel to switch address
                    viewModel.updateActiveAddress(selectedLocation)

                    Toast.makeText(this, "Address changed to ${selectedLocation.locationType}", Toast.LENGTH_SHORT).show()
                    finish() // Go back to Home/Cart
                }
                .show()
        }
        binding.rvSavedAddresses.adapter = addressAdapter
    }

    private fun observeLocations() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allLocations.collect { locations ->
                    // Filter to only show user-saved addresses, not every GPS ping
                    val savedOnly = locations.filter { it.isSavedAddress }
                    addressAdapter.submitList(savedOnly)

                    binding.emptyAnimation.visibility = if (savedOnly.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val locationToDelete = addressAdapter.currentList[position]

                // Step 1: Remove from list visually
                val currentList = addressAdapter.currentList.toMutableList()
                currentList.removeAt(position)
                addressAdapter.submitList(currentList)

                // Step 2: Show Snackbar with Undo
                Snackbar.make(binding.rvSavedAddresses, "Address deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        // If user clicks Undo, put it back
                        addressAdapter.submitList(addressAdapter.currentList)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                // User did NOT click undo, now delete from DB and Server
                                viewModel.deleteLocation(locationToDelete)
                            }
                        }
                    })
                    .show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvSavedAddresses)
    }
}