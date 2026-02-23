package com.skyblue.mygrocery.ui.activity

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.skyblue.mygrocery.databinding.ActivityProductDetailBinding
import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.ui.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportPostponeEnterTransition()

        val product = intent.getParcelableExtra<Product>("product_data")

        // Populate views
        product?.let { item ->
            binding.tvDetailName.text = item.name
            binding.tvDetailPrice.text = "$${item.price}"
            binding.tvDescription.text = item.description

            Glide.with(this)
                .load(item.image)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        supportStartPostponedEnterTransition() // Start transition even if image fails
                        return false // Allow Glide to show error drawable if you have one
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        supportStartPostponedEnterTransition() // Start transition when image is ready
                        return false // Let Glide handle setting the resource to the ImageView
                    }
                })
                .into(binding.detailImage) // Don't forget to call .into()
        }

        // 4. Handle FAB Animation with Version Safety
        setupFabAnimation()


        // Add to Cart Logic (Room Database)
        binding.fabAddToCart.setOnClickListener {
            product?.let {
                viewModel.addItemToCart(it) // This calls your Room DAO
                Snackbar.make(binding.root, "${it.name} added to cart", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.fabAddToCart)
                    .show()
            }
        }

        binding.btnBack.setOnClickListener { supportFinishAfterTransition() }
    }

    private fun setupFabAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use framework Transition classes for Window transitions
            window.sharedElementEnterTransition.addListener(object : android.transition.TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: android.transition.Transition) {
                    showFabWithAnimation()
                }
            })
        } else {
            // Fallback: Just show it immediately on API < 26
            binding.fabAddToCart.visibility = View.VISIBLE
        }
    }

    private fun showFabWithAnimation() {
        binding.fabAddToCart.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0f
            scaleY = 0f

            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(200) // Small delay looks more professional
                .start()
        }
    }
}