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
    @GET("api/products")
    suspend fun getProducts(): Response<List<Product>>
    
    // Öne çıkan ürünleri getir
    @GET("api/products/featured")
    suspend fun getFeaturedProducts(): Response<List<Product>>
    
    // ID'ye göre ürün detayı getir
    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<Product>
    
    // Kategori filtrelemesi
    @GET("api/products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): Response<List<Product>>
    
    // Arama
    @GET("api/products/search")
    suspend fun searchProducts(@Query("q") query: String): Response<List<Product>>
    
    // Yeni ürünler
    @GET("api/products/new")
    suspend fun getNewProducts(): Response<List<Product>>
    
    // Çok satanlar
    @GET("api/products/bestsellers")
    suspend fun getBestSellers(): Response<List<Product>>
    
    // Kullanıcı girişi
    @POST("api/auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<AuthResponse>
    
    // Kullanıcı kaydı
    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>
    
    // Kullanıcı profili bilgilerini getir (Eski endpoint)
    @GET("api/users/profile")
    suspend fun getUserProfile(): Response<User>
    
    // Kullanıcı bilgileri ve adresini getir (Yeni endpoint)
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    // Kullanıcı adresini güncelle
    @PUT("api/auth/update-profile")
    suspend fun updateUserAddress(@Body addressUpdate: Map<String, String>): Response<User>
    
    // Yeni ürün ekleme
    @POST("api/products")
    suspend fun addProduct(@Body product: Product): Response<Product>
    
    // Ürün güncelleme
    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): Response<Product>
    
    // Ürün silme
    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Void>
} 