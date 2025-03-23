package com.example.mobilsmartwear.data.remote

import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Order
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.remote.dto.Address
import com.example.mobilsmartwear.data.remote.dto.AuthResponse
import com.example.mobilsmartwear.data.remote.dto.CouponResponse
import com.example.mobilsmartwear.data.remote.dto.CouponVerifyRequest
import com.example.mobilsmartwear.data.remote.dto.CouponVerifyResponse
import com.example.mobilsmartwear.data.remote.dto.DummyProductsResponse
import com.example.mobilsmartwear.data.remote.dto.FavoriteRequest
import com.example.mobilsmartwear.data.remote.dto.LoginRequest
import com.example.mobilsmartwear.data.remote.dto.OrderRequest
import com.example.mobilsmartwear.data.remote.dto.OrderResponse
import com.example.mobilsmartwear.data.remote.dto.ProductDetailResponse
import com.example.mobilsmartwear.data.remote.dto.ProductsResponse
import com.example.mobilsmartwear.data.remote.dto.RegisterRequest
import com.example.mobilsmartwear.data.remote.dto.ReviewRequest
import com.example.mobilsmartwear.data.remote.dto.ReviewResponse
import com.example.mobilsmartwear.data.remote.dto.ReviewsResponse
import com.example.mobilsmartwear.data.remote.dto.UserInfoResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    // -------------------- Auth Endpoints --------------------
    
    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): AuthResponse
    
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): AuthResponse
    
    @GET("api/auth/me")
    suspend fun getUserInfo(): UserInfoResponse
    
    // -------------------- Product Endpoints --------------------
    
    @GET("api/products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1,
        @Query("sort") sort: String? = null
    ): ProductsResponse
    
    @GET("api/products/search")
    suspend fun searchProducts(@Query("query") query: String): List<Product>
    
    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductDetailResponse
    
    // -------------------- Orders Endpoints --------------------
    
    @POST("api/orders")
    suspend fun createOrder(@Body orderRequest: OrderRequest): OrderResponse
    
    @GET("api/orders")
    suspend fun getUserOrders(): List<Order>
    
    @GET("api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): Order
    
    // -------------------- Address Endpoints --------------------
    
    @GET("api/addresses")
    suspend fun getUserAddresses(): List<Address>
    
    @POST("api/addresses")
    suspend fun addAddress(@Body address: Address): Address
    
    @DELETE("api/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String)
    
    // -------------------- Favorites Endpoints --------------------
    
    @GET("api/favorites")
    suspend fun getFavorites(): List<Product>
    
    @POST("api/favorites")
    suspend fun addToFavorites(@Body favoriteRequest: FavoriteRequest)
    
    @DELETE("api/favorites/{productId}")
    suspend fun removeFromFavorites(@Path("productId") productId: String)
    
    // -------------------- Reviews Endpoints --------------------
    
    @POST("api/products/{id}/reviews")
    suspend fun addReview(
        @Path("id") productId: String,
        @Body reviewRequest: ReviewRequest
    ): ReviewResponse
    
    @GET("api/reviews")
    suspend fun getUserReviews(): ReviewsResponse
    
    // -------------------- Coupons Endpoints --------------------
    
    @GET("api/coupons")
    suspend fun getUserCoupons(): CouponResponse
    
    @POST("api/coupons/verify")
    suspend fun verifyCoupon(@Body request: CouponVerifyRequest): CouponVerifyResponse
    
    // -------------------- DummyJSON API --------------------
    
    @GET("https://dummyjson.com/products")
    suspend fun getDummyProducts(
        @Query("limit") limit: Int = 10,
        @Query("skip") skip: Int = 0
    ): DummyProductsResponse
    
    @GET("https://dummyjson.com/products/category/{category}")
    suspend fun getDummyProductsByCategory(
        @Path("category") category: String
    ): DummyProductsResponse
    
    @GET("https://dummyjson.com/products/search")
    suspend fun searchDummyProducts(
        @Query("q") query: String
    ): DummyProductsResponse
    
    @GET("https://dummyjson.com/products/{id}")
    suspend fun getDummyProductById(
        @Path("id") id: Int
    ): Product
} 