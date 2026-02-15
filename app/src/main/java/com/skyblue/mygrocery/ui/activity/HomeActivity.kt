package com.skyblue.mygrocery.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityHomeBinding
import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.ui.adapter.ProductAdapter
import com.skyblue.mygrocery.ui.viewmodel.CartViewModel
import com.skyblue.mygrocery.ui.viewmodel.LocationState
import com.skyblue.mygrocery.ui.viewmodel.LocationViewModel
import com.skyblue.mygrocery.ui.viewmodel.ProductViewModel
import com.skyblue.mygrocery.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val viewModelLocation: LocationViewModel by viewModels()

    private var searchJob: Job? = null

    private val productAdapter = ProductAdapter { product ->
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("product_data", product)
        }
        startActivity(intent)
    }

    @SuppressLint("ObsoleteSdkInt")
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModelLocation.fetchCurrentLocation()
        } else {
            handlePermissionDenied(permissions.keys.toList())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateGreetingUI()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        observeCart()
        observeLocationState()
        observeLocations()

        if (isLocationPermissionGranted()) {
            viewModelLocation.fetchCurrentLocation()
        } else {
            // Request permissions using your existing launcher
            requestLocationPermission()
        }

        binding.toolbar.menu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.toolbar.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = productAdapter
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        val visibleItemCount = gridLayoutManager.childCount
                        val totalItemCount = gridLayoutManager.itemCount
                        val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()

                        val isNotLoading = viewModel.isPaginationLoading.value == false
                        if (isNotLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                            viewModel.fetchProducts(isPagination = true)
                        }
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        // Main Product State Observer
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.res.collect { state ->
                    handleUIState(state)
                }
            }
        }

        // Pagination State Observer
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPaginationLoading.collect { isPagination ->
                    binding.toolbar.progressBar.visibility = if (isPagination) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun handleUIState(state: Resource<List<Product>>) {
        when (state) {
            is Resource.Loading -> {
                binding.toolbar.shimmerLayout.visibility = View.VISIBLE
                binding.toolbar.shimmerLayout.startShimmer()
                binding.toolbar.recyclerView.visibility = View.GONE
                binding.toolbar.layoutState.root.visibility = View.GONE
            }
            is Resource.Success -> {
                binding.toolbar.shimmerLayout.stopShimmer()
                binding.toolbar.shimmerLayout.visibility = View.GONE

                if (state.data.isNullOrEmpty()) {
                    showEmptySearchUI(getString(R.string.no_products_found))
                } else {
                    binding.toolbar.recyclerView.visibility = View.VISIBLE
                    binding.toolbar.layoutState.root.visibility = View.GONE
                    productAdapter.submitList(state.data)
                }
            }
            is Resource.Error -> {
                binding.toolbar.shimmerLayout.stopShimmer()
                binding.toolbar.shimmerLayout.visibility = View.GONE
                showEmptySearchUI(state.message ?: getString(R.string.something_went_wrong))
            }
            else -> {}
        }
    }

    private fun setupListeners() {
        // Debounced Search Listener
        binding.toolbar.search.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(500) // Wait for user to stop typing
                if (query.isNotEmpty()) {
                    viewModel.searchProducts(query)
                } else {
                    viewModel.fetchProducts(isPagination = false)
                }
            }
        }

        binding.toolbar.clearSearch.setOnClickListener {
            binding.toolbar.search.text?.clear()
        }

        binding.toolbar.layoutState.btnRetry.setOnClickListener {
            viewModel.fetchProducts(isPagination = false)
        }
    }

    private fun showEmptySearchUI(message: String) {
        binding.toolbar.recyclerView.visibility = View.GONE
        binding.toolbar.layoutState.apply {
            root.visibility = View.VISIBLE
            tvStateMsg.text = message
            imgState.setImageResource(R.drawable.ic_search_no_result)
        }
    }

    private fun observeCart() {
        cartViewModel.cartItems.observe(this) { items ->
            if (items.isNullOrEmpty()) {
                binding.toolbar.bottomCartCard.visibility = View.GONE
            } else {
                binding.toolbar.bottomCartCard.visibility = View.VISIBLE

                val count = items.sumOf { it.quantity }
                binding.toolbar.tvBottomCartCount.text = "$count ${if (count > 1) "ITEMS" else "ITEM"}"

                val total = items.sumOf { it.price.toDouble() * it.quantity }
                binding.toolbar.tvBottomCartPrice.text = String.format("₹%.2f", total)

                // Animation for Cart Update
                binding.toolbar.bottomCartCard.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).withEndAction {
                    binding.toolbar.bottomCartCard.animate().scaleX(1f).scaleY(1f).setDuration(100)
                }
            }
        }

        binding.toolbar.bottomCartCard.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun updateGreetingUI() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val (greeting, iconRes) = when (hour) {
            in 0..11 -> "Good morning," to R.drawable.ic_sun
            in 12..15 -> "Good afternoon," to R.drawable.ic_sun
            in 16..20 -> "Good evening," to R.drawable.ic_moon
            else -> "Good night," to R.drawable.ic_moon
        }

        binding.toolbar.tvGreeting.apply {
            text = greeting
            setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
        }
    }

    private fun observeLocationState() {
        lifecycleScope.launch {
            viewModelLocation.locationState.collect { state ->
                when (state) {
                    is LocationState.Idle -> {

                    }
                    is LocationState.Loading -> {
                        Toast.makeText(this@HomeActivity, "Fetching location...", Toast.LENGTH_SHORT).show()
                    }
                    is LocationState.Success -> {
                        Toast.makeText(
                            this@HomeActivity,
                            "Location: ${state.location.latitude}, ${state.location.longitude}",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.d("Location", "Latitude ${state.location.latitude} /n" +
                                "                       Longitude ${state.location.longitude}")

                        val shortAddress = state.location.address?.take(20) + "..."
                        binding.toolbar.locationTxt.text = shortAddress

                        Log.d("Location", "Location short address $shortAddress")
                    }
                    is LocationState.Error -> {
                        Toast.makeText(this@HomeActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observeLocations() {
        lifecycleScope.launch {
            viewModelLocation.allLocations.collect { locations ->
                Log.d("MainActivity", "Saved locations: ${locations.size}")
            }
        }
    }

    private fun handlePermissionDenied(permissions: List<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val shouldShowRationale = permissions.any { shouldShowRequestPermissionRationale(it) }
            if (!shouldShowRationale) showSettingsDialog()
            else Toast.makeText(this, getString(R.string.location_permission_is_required_for_delivery), Toast.LENGTH_LONG).show()
        }
    }

    private fun requestLocationPermission() {
        when {
            viewModelLocation.checkPermissionStatus() -> {
                viewModelLocation.fetchCurrentLocation()
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    viewModelLocation.getRequiredPermissions().any {
                        shouldShowRequestPermissionRationale(it)
                    } -> {
                showPermissionRationale()
            }

            else -> {
                locationPermissionLauncher.launch(viewModelLocation.getRequiredPermissions())
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("We need your location to show nearby restaurants and calculate delivery time.")
            .setPositiveButton("Grant") { _, _ ->
                locationPermissionLauncher.launch(viewModelLocation.getRequiredPermissions())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Location permission is required for delivery. Please enable it in app settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isLocationPermissionGranted(): Boolean {
        // Use this@HomeActivity instead of just this
        val fineLocation = ContextCompat.checkSelfPermission(
            this@HomeActivity,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            this@HomeActivity,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }
}