package com.example.mad_workshop.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mad_workshop.data.model.CartItem
import com.example.mad_workshop.databinding.ItemCartBinding

class CartAdapter(
    private val onIncreaseQuantity: (CartItem) -> Unit,
    private val onDecreaseQuantity: (CartItem) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvCartItemTitle.text = item.product.title
            binding.tvCartItemPrice.text = "$${item.product.price}"
            binding.tvQuantity.text = item.quantity.toString()

            binding.ivCartItemImage.load(item.product.image) {
                crossfade(true)
            }

            binding.btnIncrease.setOnClickListener {
                onIncreaseQuantity(item)
            }

            binding.btnDecrease.setOnClickListener {
                onDecreaseQuantity(item)
            }

            binding.btnRemove.setOnClickListener {
                onRemoveItem(item)
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
