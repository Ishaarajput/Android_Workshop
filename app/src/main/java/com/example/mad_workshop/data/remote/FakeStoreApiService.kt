package com.example.mad_workshop.data.remote

import com.example.mad_workshop.data.model.Product
import com.example.mad_workshop.data.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FakeStoreApiService {

    /**
     * GET /products
     * Fetches a list of all products from FakeStore API.
     */
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>

    /**
     * GET /products/{id}
     * Fetches a single product details by its unique ID.
     */
    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int
    ): Response<Product>

    /**
     * GET /products/categories
     * Fetches all available product categories.
     */
    @GET("products/categories")
    suspend fun getCategories(): Response<List<String>>

    /**
     * GET /users
     * Fetches list of users from FakeStore API.
     */
    @GET("users")
    suspend fun getUsers(): Response<List<User>>
}
