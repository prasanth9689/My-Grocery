package com.skyblue.mygrocery.ui.activity

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.transition.Transition
import android.transition.TransitionListenerAdapter
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
        binding.detailImage.transitionName = "product_image"

        // Populate views
        product?.let { item ->
            binding.tvDetailName.text = item.name
            binding.tvDetailPrice.text = "$${item.price}"
            binding.tvDescription.text = item.description

            Glide.with(this)
                .load(item.image)
                .addListener(object : RequestListener<Drawable> {
                    // Leave this empty and use the IDE's auto-fix
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        TODO("Not yet implemented")
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        TODO("Not yet implemented")
                    }
                })
        }

        // FAB Animation
        window.sharedElementEnterTransition.addListener(@RequiresApi(Build.VERSION_CODES.O)
        object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                binding.fabAddToCart.visibility = View.VISIBLE
                binding.fabAddToCart.alpha = 0f
                binding.fabAddToCart.scaleX = 0f
                binding.fabAddToCart.scaleY = 0f
                binding.fabAddToCart.animate()
                    .alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(300)
                    .start()
            }
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
        })

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
}