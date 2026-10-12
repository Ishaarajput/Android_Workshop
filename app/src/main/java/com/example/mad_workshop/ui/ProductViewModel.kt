package com.example.mad_workshop.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_workshop.data.manager.CartManager
import com.example.mad_workshop.data.manager.UserManager
import com.example.mad_workshop.data.model.CartItem
import com.example.mad_workshop.data.model.Product
import com.example.mad_workshop.data.model.User
import com.example.mad_workshop.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val allProducts = MutableStateFlow<List<Product>>(emptyList())

    private val _productsState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    
    private val _productDetailState = MutableStateFlow<UiState<Product>>(UiState.Loading)
    val productDetailState: StateFlow<UiState<Product>> = _productDetailState

    private val _categoriesState = MutableStateFlow<List<String>>(emptyList())
    val categoriesState: StateFlow<List<String>> = _categoriesState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val cartCount: StateFlow<Int> = CartManager.cartCount
    val cartItems: StateFlow<List<CartItem>> = CartManager.cartItems
    val favorites: StateFlow<Set<Int>> = CartManager.favorites

    val currentUser: StateFlow<User?> = UserManager.currentUser
    val availableUsers: StateFlow<List<User>> = UserManager.availableUsers

    // Filter products dynamically based on search query and category selection
    val filteredProductsState: StateFlow<UiState<List<Product>>> = combine(
        _productsState,
        _searchQuery,
        _selectedCategory
    ) { state, query, category ->
        when (state) {
            is UiState.Success -> {
                val filtered = state.data.filter { product ->
                    val matchesQuery = query.isBlank() || 
                            product.title.contains(query, ignoreCase = true) ||
                            product.category.contains(query, ignoreCase = true)
                    
                    val matchesCategory = category.equals("All", ignoreCase = true) ||
                            product.category.equals(category, ignoreCase = true)

                    matchesQuery && matchesCategory
                }
                UiState.Success(filtered)
            }
            else -> state
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun fetchProducts() {
        viewModelScope.launch {
            _productsState.value = UiState.Loading
            try {
                val response = repository.getProducts()
                if (response.isSuccessful && response.body() != null) {
                    allProducts.value = response.body()!!
                    _productsState.value = UiState.Success(allProducts.value)
                } else {
                    _productsState.value = UiState.Error("HTTP Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _productsState.value = UiState.Error(e.localizedMessage ?: "An unknown network error occurred")
            }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (response.isSuccessful && response.body() != null) {
                    val list = mutableListOf("All")
                    list.addAll(response.body()!!)
                    _categoriesState.value = list
                }
            } catch (_: Exception) {
                // Ignore category fetch failure, fallback to "All"
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = repository.getUsers()
                if (response.isSuccessful && response.body() != null) {
                    UserManager.setAvailableUsers(response.body()!!)
                }
            } catch (_: Exception) {
                // Keep default user on network error
            }
        }
    }

    fun fetchProductById(id: Int) {
        viewModelScope.launch {
            _productDetailState.value = UiState.Loading
            try {
                val response = repository.getProductById(id)
                if (response.isSuccessful && response.body() != null) {
                    _productDetailState.value = UiState.Success(response.body()!!)
                } else {
                    _productDetailState.value = UiState.Error("HTTP Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _productDetailState.value = UiState.Error(e.localizedMessage ?: "An unknown network error occurred")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(productId: Int) {
        CartManager.toggleFavorite(productId)
    }

    fun addToCart(product: Product) {
        CartManager.addToCart(product)
    }

    fun updateCartQuantity(productId: Int, delta: Int) {
        CartManager.updateQuantity(productId, delta)
    }

    fun removeFromCart(productId: Int) {
        CartManager.removeFromCart(productId)
    }

    fun clearCart() {
        CartManager.clearCart()
    }

    fun getCartSubtotal(): Double {
        return CartManager.calculateSubtotal()
    }

    fun selectUser(user: User) {
        UserManager.selectUser(user)
    }
}
