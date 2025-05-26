package com.example.mobilsmartwear.data.remote

import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.model.AuthRequest
import com.example.mobilsmartwear.data.model.AuthResponse
import com.example.mobilsmartwear.data.model.RegisterRequest
import com.example.mobilsmartwear.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Web API ile iletişim için servis arayüzü
 */
interface ApiService {
    
    // Tüm ürünleri getir
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>
    
    // Öne çıkan ürünleri getir
    @GET("products/featured")
    suspend fun getFeaturedProducts(): Response<List<Product>>
    
    // ID'ye göre ürün detayı getir
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<Product>
    
    // Kategori filtrelemesi
    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): Response<List<Product>>
    
    // Arama
    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): Response<List<Product>>
    
    // Yeni ürünler
    @GET("products/new")
    suspend fun getNewProducts(): Response<List<Product>>
    
    // Çok satanlar
    @GET("products/bestsellers")
    suspend fun getBestSellers(): Response<List<Product>>
    
    // Kullanıcı girişi
    @POST("auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<AuthResponse>
    
    // Kullanıcı kaydı
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>
    
    // Kullanıcı profili bilgilerini getir (Eski endpoint)
    @GET("users/profile")
    suspend fun getUserProfile(): Response<User>
    
    // Kullanıcı bilgileri ve adresini getir (Yeni endpoint)
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    // Kullanıcı adresini güncelle
    @PUT("auth/update-profile")
    suspend fun updateUserAddress(@Body addressUpdate: Map<String, String>): Response<User>
    
    // Yeni ürün ekleme
    @POST("products")
    suspend fun addProduct(@Body product: Product): Response<Product>
    
    // Ürün güncelleme
    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): Response<Product>
    
    // Ürün silme
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Void>
} 