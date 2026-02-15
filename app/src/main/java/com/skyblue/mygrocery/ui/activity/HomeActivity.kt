package com.skyblue.mygrocery.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityHomeBinding
import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.ui.adapter.ProductAdapter
import com.skyblue.mygrocery.ui.viewmodel.LocationState
import com.skyblue.mygrocery.ui.viewmodel.ProductViewModel
import com.skyblue.mygrocery.ui.viewmodel.LocationViewModel
import com.skyblue.mygrocery.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
import com.skyblue.mygrocery.ui.viewmodel.CartViewModel

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
   // private val productAdapter by lazy { ProductAdapter() }
    private val viewModelLocation: LocationViewModel by viewModels()

//    private val productAdapter by lazy {
//        ProductAdapter { product, imageView ->
//            val intent = Intent(this, ProductDetailActivity::class.java).apply {
//                putExtra("product_data", product)
//            }
//            // Transition Animation
//            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                this, imageView, "product_image_transition"
//            )
//            startActivity(intent, options.toBundle())
//        }
//    }

//    private val productAdapter = ProductAdapter { product, imageView ->
//        // This is where your click logic goes!
//        val intent = Intent(this, ProductDetailActivity::class.java)
//        intent.putExtra("product", product)
//
//        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//            this, imageView, "product_image" // Must match transitionName in XML
//        )
//        startActivity(intent, options.toBundle())
//    }


    // In HomeActivity
    private val productAdapter = ProductAdapter { product, imageView ->
        Log.d("HOME_CLICK", "=== Product Clicked ===")
        Log.d("HOME_CLICK", "Product: ${product.name}")
        Log.d("HOME_CLICK", "ImageView TransitionName: ${imageView.transitionName}")

        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("product_data", product)
        }

        try {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                imageView,
                "product_image_${product.id}"
            )
            startActivity(intent, options.toBundle())
            Log.d("HOME_CLICK", "Activity started successfully")
        } catch (e: Exception) {
            Log.e("HOME_CLICK", "Error starting activity", e)
            // Fallback without transition
            startActivity(intent)
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            viewModelLocation.fetchCurrentLocation()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val shouldShowRationale = permissions.keys.any {
                    shouldShowRequestPermissionRationale(it)
                }

                if (!shouldShowRationale) {
                    showSettingsDialog()
                } else {
                    Toast.makeText(
                        this,
                        "Location permission is required for delivery",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupListeners()
        observeCart()

        observeLocationState()
        observeLocations()
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.toolbar.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = productAdapter
            setHasFixedSize(true)

            // Scroll Listener for "Load More"
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0) { // Check for scroll down
                        val visibleItemCount = gridLayoutManager.childCount
                        val totalItemCount = gridLayoutManager.itemCount
                        val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()

                        // If not currently loading and reached the bottom threshold
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
        // Collect Main UI State
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.res.collect { state ->
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
                            binding.toolbar.recyclerView.visibility = View.VISIBLE
                            binding.toolbar.layoutState.root.visibility = View.GONE
                            productAdapter.submitList(state.data)
                        }
                        is Resource.Error -> {
                            binding.toolbar.shimmerLayout.visibility = View.GONE
                            binding.toolbar.recyclerView.visibility = View.GONE
                            binding.toolbar.layoutState.root.visibility = View.VISIBLE
                            binding.toolbar.layoutState.tvStateMsg.text = state.message
                            binding.toolbar.layoutState.imgState.setImageResource(R.drawable.ic_error)
                        }

                        else -> {

                        }
                    }
                }
            }
        }

        // Collect Pagination Progress State
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPaginationLoading.collect { isPagination ->
                    binding.toolbar.progressBar.visibility = if (isPagination) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.layoutState.btnRetry.setOnClickListener {
            viewModel.fetchProducts(isPagination = false)
        }

        // Clear search logic (optional)
        binding.toolbar.clearSearch.setOnClickListener {
            binding.toolbar.search.text?.clear()
        }
    }

    private fun showLoading() {
        binding.toolbar.shimmerLayout.startShimmer()
        binding.toolbar.shimmerLayout.visibility = View.VISIBLE
        binding.toolbar.recyclerView.visibility = View.GONE
        binding.toolbar.layoutState.root.visibility = View.GONE
    }

    private fun showSuccess(products: List<Product>) {
        binding.toolbar.shimmerLayout.stopShimmer()
        binding.toolbar.shimmerLayout.visibility = View.GONE
        binding.toolbar.recyclerView.visibility = View.VISIBLE
        binding.toolbar.layoutState.root.visibility = View.GONE
        productAdapter.submitList(products)
    }

    private fun showError(message: String, isNetwork: Boolean) {
        binding.toolbar.shimmerLayout.stopShimmer()
        binding.toolbar.shimmerLayout.visibility = View.GONE
        binding.toolbar.recyclerView.visibility = View.GONE
        binding.toolbar.layoutState.root.visibility = View.VISIBLE

        binding.toolbar.layoutState.tvStateMsg.text = message
        if (isNetwork) {
            binding.toolbar.layoutState.imgState.setImageResource(R.drawable.ic_no_internet)
        } else {
            binding.toolbar.layoutState.imgState.setImageResource(R.drawable.ic_error)
        }
    }

    private fun showEmpty() {
        binding.toolbar.shimmerLayout.stopShimmer()
        binding.toolbar.shimmerLayout.visibility = View.GONE
        binding.toolbar.recyclerView.visibility = View.GONE
        binding.toolbar.layoutState.root.visibility = View.VISIBLE

        binding.toolbar.layoutState.tvStateMsg.text = "No products found"
        binding.toolbar.layoutState.imgState.setImageResource(R.drawable.ic_empty_box)
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

                        val shortAddress = state.location.address?.take(20) + "..."
                        binding.toolbar.locationTxt.text = shortAddress
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

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Location permission is permanently denied. Please enable it in app settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeCart() {
        // Using the cartViewModel to observe the items
        cartViewModel.cartItems.observe(this) { items ->
            if (items.isNullOrEmpty()) {
                binding.toolbar.bottomCartCard.visibility = View.GONE
            } else {
                binding.toolbar.bottomCartCard.visibility = View.VISIBLE

                // Set Item Count
                val count = items.sumOf { it.quantity }
                binding.toolbar.tvBottomCartCount.text = "$count ${if (count > 1) "ITEMS" else "ITEM"}"

                // Calculate Total Price (Zepto style shows total on the bar)
                val total = items.sumOf { it.price.toDouble() * it.quantity }
                binding.toolbar.tvBottomCartPrice.text = String.format("$%.2f", total)

                // Optional: Add a small pop animation when items change
                binding.toolbar.bottomCartCard.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).withEndAction {
                    binding.toolbar.bottomCartCard.animate().scaleX(1f).scaleY(1f).setDuration(100)
                }
            }
        }

        // Set Click Listener to open Cart
        binding.toolbar.bottomCartCard.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

//    private fun observeCartCount() {
//        // Inject CartViewModel or use the same ProductViewModel if it handles cart
//        cartViewModel.cartItems.observe(this) { items ->
//            val count = items.size
//            if (count > 0) {
//                binding.tvCartBadge.visibility = View.VISIBLE
//                binding.tvCartBadge.text = count.toString()
//            } else {
//                binding.tvCartBadge.visibility = View.GONE
//            }
//        }
//    }
}