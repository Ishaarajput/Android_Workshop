package com.example.mad_workshop

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mad_workshop.data.manager.CartManager
import com.example.mad_workshop.data.manager.UserManager
import com.example.mad_workshop.data.model.Product
import com.example.mad_workshop.databinding.ActivityMainBinding
import com.example.mad_workshop.ui.CartActivity
import com.example.mad_workshop.ui.ProductAdapter
import com.example.mad_workshop.ui.ProductDetailActivity
import com.example.mad_workshop.ui.ProfileActivity
import com.example.mad_workshop.ui.ProductViewModel
import com.example.mad_workshop.ui.UiState
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CartManager.init(this)
        UserManager.init(this)

        setupRecyclerView()
        setupSearchAndFilters()
        setupCartButton()
        setupProfileButton()
        observeViewModel()

        viewModel.fetchProducts()
        viewModel.fetchCategories()
        viewModel.fetchUsers()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                val intent = ProductDetailActivity.newIntent(this, product.id)
                startActivity(intent)
            },
            onFavoriteClick = { product ->
                viewModel.toggleFavorite(product.id)
            },
            onAddToCartClick = { product ->
                viewModel.addToCart(product)
                Toast.makeText(this, "Added ${product.title} to Cart!", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = productAdapter
        }
    }

    private fun setupSearchAndFilters() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tvError.setOnClickListener {
            viewModel.fetchProducts()
            viewModel.fetchCategories()
        }
    }

    private fun setupCartButton() {
        binding.layoutCartIcon.setOnClickListener {
            startActivity(CartActivity.newIntent(this))
        }
    }

    private fun setupProfileButton() {
        binding.btnProfile.setOnClickListener {
            startActivity(ProfileActivity.newIntent(this))
        }
    }

    private fun observeViewModel() {
        // Filtered products flow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredProductsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> showSuccess(state.data)
                        is UiState.Error -> showError(state.message)
                    }
                }
            }
        }

        // Categories list flow for chips
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoriesState.collect { categories ->
                    setupCategoryChips(categories)
                }
            }
        }

        // Favorites flow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { favoriteIds ->
                    productAdapter.favoriteIds = favoriteIds
                }
            }
        }

        // Cart count badge flow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartCount.collect { count ->
                    if (count > 0) {
                        binding.tvCartBadge.visibility = View.VISIBLE
                        binding.tvCartBadge.text = count.toString()
                    } else {
                        binding.tvCartBadge.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupCategoryChips(categories: List<String>) {
        binding.chipGroupCategories.removeAllViews()
        for (category in categories) {
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = category.equals("All", ignoreCase = true)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.selectCategory(category)
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvProducts.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun showSuccess(products: List<Product>) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.rvProducts.visibility = View.VISIBLE
        productAdapter.submitList(products)
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvProducts.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = "$message\n\nTap to Retry"
    }
}
