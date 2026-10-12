package com.example.mad_workshop.ui

import android.content.Context
import android.content.Intent
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
import coil.load
import com.example.mad_workshop.data.manager.CartManager
import com.example.mad_workshop.data.model.Product
import com.example.mad_workshop.databinding.ActivityProductDetailBinding
import kotlinx.coroutines.launch
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductViewModel by viewModels()
    private var currentProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CartManager.init(this)

        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId == -1) {
            showError("Invalid Product ID")
            return
        }

        setupToolbar()
        observeViewModel(productId)

        viewModel.fetchProductById(productId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnFavoriteDetail.setOnClickListener {
            currentProduct?.let { product ->
                viewModel.toggleFavorite(product.id)
            }
        }

        binding.btnAddToCartDetail.setOnClickListener {
            currentProduct?.let { product ->
                viewModel.addToCart(product)
                Toast.makeText(this, "Added ${product.title} to Cart!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel(productId: Int) {
        // Observe detail state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productDetailState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> showSuccess(state.data)
                        is UiState.Error -> showError(state.message)
                    }
                }
            }
        }

        // Observe favorites for heart button state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { favoriteIds ->
                    val isFav = favoriteIds.contains(productId)
                    binding.btnFavoriteDetail.setImageResource(
                        if (isFav) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
                    )
                }
            }
        }

        binding.tvError.setOnClickListener {
            viewModel.fetchProductById(productId)
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE
        binding.cardAddToCartBottom.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun showSuccess(product: Product) {
        currentProduct = product
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.contentLayout.visibility = View.VISIBLE
        binding.cardAddToCartBottom.visibility = View.VISIBLE

        binding.tvProductTitle.text = product.title
        binding.tvProductCategory.text = "Category: ${product.category}"
        binding.tvProductPrice.text = String.format(Locale.US, "$%.2f", product.price)
        binding.tvBottomPrice.text = String.format(Locale.US, "$%.2f", product.price)
        binding.tvProductDescription.text = product.description

        product.rating?.let {
            binding.tvProductRating.text = "★ ${it.rate} (${it.count} reviews)"
        } ?: run {
            binding.tvProductRating.text = "N/A"
        }

        binding.ivProductImage.load(product.image) {
            crossfade(true)
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentLayout.visibility = View.GONE
        binding.cardAddToCartBottom.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = "$message\n\nTap to Retry"
    }

    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"

        fun newIntent(context: Context, productId: Int): Intent {
            return Intent(context, ProductDetailActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT_ID, productId)
            }
        }
    }
}
