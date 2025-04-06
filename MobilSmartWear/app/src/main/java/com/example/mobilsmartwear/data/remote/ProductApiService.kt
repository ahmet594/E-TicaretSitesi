package com.example.mobilsmartwear.data.remote

import com.example.mobilsmartwear.data.model.Product
import retrofit2.Response
import retrofit2.http.*

interface ProductApiService {
    @GET("products")
    suspend fun getAllProducts(): Response<List<Product>>
    
    @GET("products/featured")
    suspend fun getFeaturedProducts(): Response<List<Product>>
    
    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): Response<List<Product>>
    
    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): Response<List<Product>>
    
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<Product>
} 