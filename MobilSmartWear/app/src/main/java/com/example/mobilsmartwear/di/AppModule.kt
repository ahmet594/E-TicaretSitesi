package com.example.mobilsmartwear.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.mobilsmartwear.data.local.AppDatabase
import com.example.mobilsmartwear.data.local.CartDao
import com.example.mobilsmartwear.data.local.OrderDao
import com.example.mobilsmartwear.data.local.ProductDao
import com.example.mobilsmartwear.data.local.UserDao
import com.example.mobilsmartwear.data.model.Order
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.model.User
import com.example.mobilsmartwear.data.remote.ApiService
import com.example.mobilsmartwear.data.remote.RetrofitClient
import com.example.mobilsmartwear.data.remote.dto.AddressRequest
import com.example.mobilsmartwear.data.remote.dto.Address
import com.example.mobilsmartwear.data.remote.dto.AuthResponse
import com.example.mobilsmartwear.data.remote.dto.Coupon
import com.example.mobilsmartwear.data.remote.dto.CouponDetails
import com.example.mobilsmartwear.data.remote.dto.CouponResponse
import com.example.mobilsmartwear.data.remote.dto.CouponVerifyRequest
import com.example.mobilsmartwear.data.remote.dto.CouponVerifyResponse
import com.example.mobilsmartwear.data.remote.dto.DummyProductItem
import com.example.mobilsmartwear.data.remote.dto.DummyProductsResponse
import com.example.mobilsmartwear.data.remote.dto.FavoriteRequest
import com.example.mobilsmartwear.data.remote.dto.LoginRequest
import com.example.mobilsmartwear.data.remote.dto.OrderItemRequest
import com.example.mobilsmartwear.data.remote.dto.OrderRequest
import com.example.mobilsmartwear.data.remote.dto.OrderResponse
import com.example.mobilsmartwear.data.remote.dto.OrderSummary
import com.example.mobilsmartwear.data.remote.dto.ProductDetailResponse
import com.example.mobilsmartwear.data.remote.dto.ProductsResponse
import com.example.mobilsmartwear.data.remote.dto.RegisterRequest
import com.example.mobilsmartwear.data.remote.dto.ReviewItem
import com.example.mobilsmartwear.data.remote.dto.ReviewRequest
import com.example.mobilsmartwear.data.remote.dto.ReviewResponse
import com.example.mobilsmartwear.data.remote.dto.ReviewsResponse
import com.example.mobilsmartwear.data.remote.dto.UserInfo
import com.example.mobilsmartwear.data.remote.dto.UserInfoResponse
import com.example.mobilsmartwear.data.remote.dto.UserReviewItem
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bağımlılık enjeksiyonu için API modülü
 */
object AppModule {
    
    private var database: AppDatabase? = null
    
    private fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mobilsmartwear_db"
            ).build()
            database = instance
            instance
        }
    }
    
    // DAO Providers
    fun provideProductDao(context: Context): ProductDao {
        return getDatabase(context).productDao()
    }
    
    fun provideUserDao(context: Context): UserDao {
        return getDatabase(context).userDao()
    }
    
    fun provideCartDao(context: Context): CartDao {
        return getDatabase(context).cartDao()
    }
    
    fun provideOrderDao(context: Context): OrderDao {
        return getDatabase(context).orderDao()
    }
    
    // Repository Providers
    fun provideProductRepository(context: Context): ProductRepository {
        val productDao = provideProductDao(context)
        return ProductRepository(productDao, RetrofitClient.apiService)
    }
    
    fun provideCartRepository(context: Context): CartRepository {
        val cartDao = provideCartDao(context)
        val productDao = provideProductDao(context)
        return CartRepository(cartDao, productDao)
    }
    
    // Mock API Service
    private fun createMockApiService(): ApiService {
        return object : ApiService {
            // -------------------- Auth Endpoints --------------------
            
            override suspend fun register(registerRequest: RegisterRequest): AuthResponse {
                Log.d("MockApiService", "Register mock call")
                return AuthResponse(
                    token = "mock_token_${System.currentTimeMillis()}",
                    user = User(
                        id = 1,
                        name = registerRequest.name,
                        email = registerRequest.email,
                        isLoggedIn = true
                    )
                )
            }
            
            override suspend fun login(loginRequest: LoginRequest): AuthResponse {
                Log.d("MockApiService", "Login mock call")
                return AuthResponse(
                    token = "mock_token_${System.currentTimeMillis()}",
                    user = User(
                        id = 1,
                        name = "Test User",
                        email = loginRequest.email,
                        isLoggedIn = true
                    )
                )
            }
            
            override suspend fun getUserInfo(): UserInfoResponse {
                Log.d("MockApiService", "GetUserInfo mock call")
                return UserInfoResponse(
                    user = UserInfo(
                        _id = "1",
                        name = "Test User",
                        email = "test@example.com",
                        createdAt = "2023-01-01T00:00:00.000Z"
                    )
                )
            }
            
            // -------------------- Product Endpoints --------------------
            
            override suspend fun getProducts(
                category: String?,
                limit: Int,
                page: Int,
                sort: String?
            ): ProductsResponse {
                Log.d("MockApiService", "GetProducts mock call")
                val products = createSampleProducts(10)
                return ProductsResponse(
                    products = products,
                    totalProducts = 100,
                    totalPages = 10,
                    currentPage = page
                )
            }
            
            override suspend fun searchProducts(query: String): List<Product> {
                Log.d("MockApiService", "SearchProducts mock call: $query")
                val allProducts = createSampleProducts(20)
                return allProducts.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.description.contains(query, ignoreCase = true) 
                }
            }
            
            override suspend fun getProductById(id: Int): ProductDetailResponse {
                Log.d("MockApiService", "GetProductById mock call: $id")
                return ProductDetailResponse(
                    _id = id.toString(),
                    name = "Sample Product $id",
                    description = "This is a sample product description",
                    price = 199.99,
                    category = "Erkek",
                    subCategory = "T-shirt",
                    imagePath = "/images/products/sample-$id.jpg",
                    images = listOf(
                        "/images/products/sample-$id-1.jpg",
                        "/images/products/sample-$id-2.jpg"
                    ),
                    inStock = true,
                    sizes = listOf("S", "M", "L", "XL"),
                    colors = listOf("Black", "White", "Blue"),
                    rating = 4.5f,
                    reviewCount = 12,
                    reviews = listOf(
                        ReviewItem(
                            _id = "rev1",
                            userId = "u1",
                            userName = "John Doe",
                            rating = 5,
                            comment = "Great product!",
                            createdAt = "2023-01-01T00:00:00.000Z"
                        )
                    )
                )
            }
            
            // -------------------- Orders Endpoints --------------------
            
            override suspend fun createOrder(orderRequest: OrderRequest): OrderResponse {
                Log.d("MockApiService", "CreateOrder mock call")
                return OrderResponse(
                    orderId = "order_${System.currentTimeMillis()}",
                    message = "Order created successfully",
                    orderSummary = OrderSummary(
                        totalItems = orderRequest.items.sumOf { it.quantity },
                        subtotal = 399.99,
                        shipping = 0.0,
                        tax = 32.0,
                        total = 431.99
                    )
                )
            }
            
            override suspend fun getUserOrders(): List<Order> {
                Log.d("MockApiService", "GetUserOrders mock call")
                return emptyList() // Mock implementation
            }
            
            override suspend fun getOrderById(id: String): Order {
                Log.d("MockApiService", "GetOrderById mock call: $id")
                return Order(
                    id = id.toInt(),
                    orderNumber = "ORD123456",
                    userId = 1,
                    items = emptyList(),
                    totalAmount = 399.99,
                    status = "Processing",
                    date = System.currentTimeMillis()
                )
            }
            
            // -------------------- Address Endpoints --------------------
            
            override suspend fun getUserAddresses(): List<Address> {
                Log.d("MockApiService", "GetUserAddresses mock call")
                return listOf(
                    Address(
                        _id = "addr1",
                        title = "Home",
                        address = "123 Main St",
                        city = "Istanbul",
                        district = "Kadikoy",
                        postalCode = "34000",
                        phone = "5551234567",
                        isDefault = true
                    )
                )
            }
            
            override suspend fun addAddress(address: Address): Address {
                Log.d("MockApiService", "AddAddress mock call")
                return address.copy(_id = "addr_${System.currentTimeMillis()}")
            }
            
            override suspend fun deleteAddress(id: String) {
                Log.d("MockApiService", "DeleteAddress mock call: $id")
                // Mock implementation
            }
            
            // -------------------- Favorites Endpoints --------------------
            
            override suspend fun getFavorites(): List<Product> {
                Log.d("MockApiService", "GetFavorites mock call")
                return createSampleProducts(5)
            }
            
            override suspend fun addToFavorites(favoriteRequest: FavoriteRequest) {
                Log.d("MockApiService", "AddToFavorites mock call: ${favoriteRequest.productId}")
                // Mock implementation
            }
            
            override suspend fun removeFromFavorites(productId: String) {
                Log.d("MockApiService", "RemoveFromFavorites mock call: $productId")
                // Mock implementation
            }
            
            // -------------------- Reviews Endpoints --------------------
            
            override suspend fun addReview(productId: String, reviewRequest: ReviewRequest): ReviewResponse {
                Log.d("MockApiService", "AddReview mock call for product: $productId")
                return ReviewResponse(
                    message = "Review added successfully",
                    review = ReviewItem(
                        _id = "rev_${System.currentTimeMillis()}",
                        userId = "u1",
                        userName = "Test User",
                        rating = reviewRequest.rating,
                        comment = reviewRequest.comment,
                        createdAt = "2023-01-01T00:00:00.000Z"
                    )
                )
            }
            
            override suspend fun getUserReviews(): ReviewsResponse {
                Log.d("MockApiService", "GetUserReviews mock call")
                return ReviewsResponse(
                    reviews = listOf(
                        UserReviewItem(
                            _id = "rev1",
                            productId = "1",
                            productName = "Sample Product",
                            productImage = "/images/products/sample.jpg",
                            rating = 5,
                            comment = "Great product!",
                            createdAt = "2023-01-01T00:00:00.000Z"
                        )
                    )
                )
            }
            
            // -------------------- Coupons Endpoints --------------------
            
            override suspend fun getUserCoupons(): CouponResponse {
                Log.d("MockApiService", "GetUserCoupons mock call")
                return CouponResponse(
                    coupons = listOf(
                        Coupon(
                            _id = "c1",
                            code = "WELCOME20",
                            description = "20% off on your first order",
                            discountType = "percentage",
                            discountValue = 20,
                            minimumPurchase = 100,
                            expiryDate = "2023-12-31T23:59:59.999Z",
                            isUsed = false
                        )
                    )
                )
            }
            
            override suspend fun verifyCoupon(request: CouponVerifyRequest): CouponVerifyResponse {
                Log.d("MockApiService", "VerifyCoupon mock call: ${request.code}")
                return if (request.code == "WELCOME20") {
                    CouponVerifyResponse(
                        valid = true,
                        coupon = CouponDetails(
                            code = "WELCOME20",
                            description = "20% off on your first order",
                            discountType = "percentage",
                            discountValue = 20,
                            discountAmount = request.cartTotal * 0.2
                        )
                    )
                } else {
                    CouponVerifyResponse(
                        valid = false,
                        message = "Invalid coupon code"
                    )
                }
            }
            
            // -------------------- DummyJSON API --------------------
            
            override suspend fun getDummyProducts(limit: Int, skip: Int): DummyProductsResponse {
                Log.d("MockApiService", "GetDummyProducts mock call")
                return RetrofitClient.apiService.getDummyProducts(limit, skip)
            }
            
            override suspend fun getDummyProductsByCategory(category: String): DummyProductsResponse {
                Log.d("MockApiService", "GetDummyProductsByCategory mock call: $category")
                return RetrofitClient.apiService.getDummyProductsByCategory(category)
            }
            
            override suspend fun searchDummyProducts(query: String): DummyProductsResponse {
                Log.d("MockApiService", "SearchDummyProducts mock call: $query")
                return RetrofitClient.apiService.searchDummyProducts(query)
            }
            
            override suspend fun getDummyProductById(id: Int): Product {
                Log.d("MockApiService", "GetDummyProductById mock call: $id")
                return RetrofitClient.apiService.getDummyProductById(id)
            }
        }
    }
    
    private fun createSampleProducts(count: Int): List<Product> {
        return (1..count).map { i ->
            Product(
                id = i,
                name = "Sample Product $i",
                description = "This is a sample product description for product $i",
                price = 99.99 + (i * 10.0),
                imageUrl = "/images/products/sample-$i.jpg",
                category = if (i % 2 == 0) "Erkek" else "Kadın",
                rating = 4.0f + (i % 10) / 10.0f,
                reviewCount = i * 5,
                isAvailable = true
            )
        }
    }
} 