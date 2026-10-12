package com.example.mad_workshop.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mad_workshop.data.manager.CartManager
import com.example.mad_workshop.data.manager.UserManager
import com.example.mad_workshop.data.model.User
import com.example.mad_workshop.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CartManager.init(this)
        UserManager.init(this)

        setupToolbar()
        observeViewModel()

        viewModel.fetchUsers()

        binding.btnSwitchUser.setOnClickListener {
            showProfileSwitcherDialog()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        // Observe Current Active User
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { user ->
                    user?.let { renderUser(it) }
                }
            }
        }

        // Observe Cart Stats
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartCount.collect { count ->
                    binding.tvCartCountStat.text = count.toString()
                }
            }
        }

        // Observe Favorites Stats
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { favoritesSet ->
                    binding.tvFavoritesCountStat.text = favoritesSet.size.toString()
                }
            }
        }
    }

    private fun renderUser(user: User) {
        binding.tvAvatarInitial.text = user.fullName.take(1).uppercase()
        binding.tvFullName.text = user.fullName
        binding.tvUsername.text = "@${user.username}"
        binding.tvMembershipTier.text = user.membershipTier
        binding.tvEmail.text = user.email
        binding.tvPhone.text = user.phone ?: "N/A"
        binding.tvAddress.text = user.fullAddress
    }

    private fun showProfileSwitcherDialog() {
        val usersList = viewModel.availableUsers.value
        if (usersList.isEmpty()) {
            Toast.makeText(this, "Loading users from API...", Toast.LENGTH_SHORT).show()
            viewModel.fetchUsers()
            return
        }

        val userNames = usersList.map { "${it.fullName} (@${it.username})" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Active Profile Account")
            .setItems(userNames) { dialog, which ->
                val selectedUser = usersList[which]
                viewModel.selectUser(selectedUser)
                Toast.makeText(this, "Active profile switched to ${selectedUser.fullName}!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ProfileActivity::class.java)
        }
    }
}
