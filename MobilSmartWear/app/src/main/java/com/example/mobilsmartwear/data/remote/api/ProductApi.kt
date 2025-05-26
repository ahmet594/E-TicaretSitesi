package com.example.mobilsmartwear.data.remote.api

import com.example.mobilsmartwear.data.model.Product
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>
    
    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): Response<List<Product>>
    
    @GET("products/featured")
    suspend fun getFeaturedProducts(): Response<List<Product>>
    
    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): Response<List<Product>>
    
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<Product>
    
    @POST("products")
    suspend fun addProduct(@Body product: Product): Response<Product>
    
    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): Response<Product>
    
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Void>
} 