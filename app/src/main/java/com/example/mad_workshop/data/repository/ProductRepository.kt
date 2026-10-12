package com.example.mad_workshop.data.repository

import com.example.mad_workshop.data.model.Product
import com.example.mad_workshop.data.model.User
import com.example.mad_workshop.data.remote.RetrofitInstance
import retrofit2.Response

class ProductRepository {

    suspend fun getProducts(): Response<List<Product>> {
        return RetrofitInstance.api.getProducts()
    }

    suspend fun getProductById(id: Int): Response<Product> {
        return RetrofitInstance.api.getProductById(id)
    }

    suspend fun getCategories(): Response<List<String>> {
        return RetrofitInstance.api.getCategories()
    }

    suspend fun getUsers(): Response<List<User>> {
        return RetrofitInstance.api.getUsers()
    }
}
