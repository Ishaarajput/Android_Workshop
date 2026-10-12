package com.example.mad_workshop.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.mad_workshop.data.model.CartItem
import com.example.mad_workshop.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CartManager {

    private const val PREFS_NAME = "ecommerce_prefs"
    private const val KEY_FAVORITES = "favorite_ids"

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadFavorites()
        }
    }

    private fun loadFavorites() {
        val saved = sharedPreferences?.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        _favorites.value = saved.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleFavorite(productId: Int) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _favorites.value = current
        saveFavorites(current)
    }

    private fun saveFavorites(set: Set<Int>) {
        sharedPreferences?.edit()?.putStringSet(KEY_FAVORITES, set.map { it.toString() }.toSet())?.apply()
    }

    fun isFavorite(productId: Int): Boolean {
        return _favorites.value.contains(productId)
    }

    fun addToCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(CartItem(product, 1))
        }
        updateCart(currentList)
    }

    fun updateQuantity(productId: Int, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val item = currentList[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = item.copy(quantity = newQty)
            }
            updateCart(currentList)
        }
    }

    fun removeFromCart(productId: Int) {
        val currentList = _cartItems.value.filterNot { it.product.id == productId }
        updateCart(currentList)
    }

    fun clearCart() {
        updateCart(emptyList())
    }

    private fun updateCart(newList: List<CartItem>) {
        _cartItems.value = newList
        _cartCount.value = newList.sumOf { it.quantity }
    }

    fun calculateSubtotal(): Double {
        return _cartItems.value.sumOf { it.totalPrice }
    }
}
