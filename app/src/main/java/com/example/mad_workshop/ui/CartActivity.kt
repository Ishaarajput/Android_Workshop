package com.example.mad_workshop.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mad_workshop.data.model.CartItem
import com.example.mad_workshop.databinding.ActivityCartBinding
import kotlinx.coroutines.launch
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupRecyclerView()
        observeCart()

        binding.btnCheckout.setOnClickListener {
            handleCheckout()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onIncreaseQuantity = { item -> viewModel.updateCartQuantity(item.product.id, 1) },
            onDecreaseQuantity = { item -> viewModel.updateCartQuantity(item.product.id, -1) },
            onRemoveItem = { item -> viewModel.removeFromCart(item.product.id) }
        )
        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun observeCart() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartItems.collect { items ->
                    updateCartUi(items)
                }
            }
        }
    }

    private fun updateCartUi(items: List<CartItem>) {
        if (items.isEmpty()) {
            binding.rvCartItems.visibility = View.GONE
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.cardCheckoutSummary.visibility = View.GONE
        } else {
            binding.rvCartItems.visibility = View.VISIBLE
            binding.tvEmptyCart.visibility = View.GONE
            binding.cardCheckoutSummary.visibility = View.VISIBLE

            cartAdapter.submitList(items)

            val subtotal = items.sumOf { it.totalPrice }
            val tax = subtotal * 0.05
            val total = subtotal + tax

            binding.tvSubtotal.text = String.format(Locale.US, "$%.2f", subtotal)
            binding.tvTax.text = String.format(Locale.US, "$%.2f", tax)
            binding.tvTotalAmount.text = String.format(Locale.US, "$%.2f", total)
        }
    }

    private fun handleCheckout() {
        val subtotal = viewModel.getCartSubtotal()
        val total = subtotal * 1.05

        AlertDialog.Builder(this)
            .setTitle("Order Placed!")
            .setMessage(String.format(Locale.US, "Thank you for shopping!\nYour order of $%.2f has been successfully placed.", total))
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                viewModel.clearCart()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, CartActivity::class.java)
        }
    }
}
