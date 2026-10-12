package com.example.mad_workshop.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mad_workshop.data.model.Product
import com.example.mad_workshop.databinding.ItemProductBinding

class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    var favoriteIds: Set<Int> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductTitle.text = product.title
            binding.tvProductCategory.text = product.category
            binding.tvProductPrice.text = "$${product.price}"

            product.rating?.let {
                binding.tvProductRating.text = "★ ${it.rate}"
            } ?: run {
                binding.tvProductRating.text = "N/A"
            }

            // Favorite Icon state
            val isFav = favoriteIds.contains(product.id)
            binding.btnFavorite.setImageResource(
                if (isFav) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )

            // Load product image
            binding.ivProductImage.load(product.image) {
                crossfade(true)
            }

            binding.root.setOnClickListener {
                onItemClick(product)
            }

            binding.btnFavorite.setOnClickListener {
                onFavoriteClick(product)
            }

            binding.btnAddToCart.setOnClickListener {
                onAddToCartClick(product)
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
