package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.local.ProductDao
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.remote.ApiService
import com.example.mobilsmartwear.data.remote.MongoDBApiService
import com.example.mobilsmartwear.data.remote.NetworkResult
import com.example.mobilsmartwear.data.remote.RetrofitClient
import com.example.mobilsmartwear.data.remote.dto.MongoDBFindRequest
import com.example.mobilsmartwear.data.remote.dto.MongoDBInsertRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException

class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ApiService
) {
    
    companion object {
        private const val TAG = "ProductRepository"
    }
    
    // MongoDB API Service
    private val mongoDBApiService = RetrofitClient.mongoDBApiService
    
    // Local database operations
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
            .catch { e -> 
                Log.e(TAG, "Error fetching all products", e) 
                emit(emptyList())
            }
    }
    
    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category)
            .catch { e -> 
                Log.e(TAG, "Error fetching products by category", e) 
                emit(emptyList())
            }
    }
    
    fun searchProducts(query: String): Flow<List<Product>> {
        return flow {
            try {
                // MongoDB'den tüm ürünleri al ve arama yap
                val mongoResult = getMongoDBProducts()
                
                if (mongoResult is NetworkResult.Success && mongoResult.data.isNotEmpty()) {
                    // MongoDB'den gelen ürünleri arama terimine göre filtrele
                    val filteredProducts = mongoResult.data.filter { product ->
                        product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true)
                    }
                    emit(filteredProducts)
                } else {
                    emit(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching products from MongoDB", e)
                emit(emptyList())
            }
        }
    }
    
    suspend fun getProductById(id: Int): Product? {
        return try {
            // Önce yerel veritabanından bul
            val localProduct = productDao.getProductById(id)
            
            if (localProduct != null) {
                localProduct
            } else {
                // Yerel veritabanında yoksa API'den getir
                try {
                    // Önce dummy API'den getirmeyi dene
                    val dummyProduct = apiService.getDummyProductById(id)
                    insertProductsSafely(listOf(dummyProduct))
                    dummyProduct
                } catch (e: Exception) {
                    // Dummy API'de bulunamazsa, ana API'den getir
                    val detailResponse = apiService.getProductById(id)
                    val product = Product(
                        id = id,
                        name = detailResponse.name,
                        description = detailResponse.description,
                        price = detailResponse.price,
                        imageUrl = detailResponse.imagePath,
                        category = detailResponse.category,
                        subCategory = detailResponse.subCategory,
                        isAvailable = detailResponse.inStock,
                        rating = detailResponse.rating,
                        reviewCount = detailResponse.reviewCount,
                        sizes = detailResponse.sizes,
                        colors = detailResponse.colors
                    )
                    
                    // Veritabanına kaydet
                    insertProductsSafely(listOf(product))
                    
                    product
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product by id: $id", e)
            null
        }
    }
    
    // MongoDB'den ürünleri getir
    suspend fun getMongoDBProducts(): NetworkResult<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching products from MongoDB...")
                
                val request = MongoDBFindRequest()
                val response = mongoDBApiService.findProducts(request)
                
                Log.d(TAG, "MongoDB products fetched: ${response.products.size}")
                
                if (response.products.isEmpty()) {
                    Log.d(TAG, "No products returned from MongoDB")
                    return@withContext NetworkResult.Error("Ürünler yüklenirken bir sorun oluştu. Lütfen daha sonra tekrar deneyin.")
                }
                
                // MongoDB'den alınan ürünleri local DB'ye kaydet
                insertProductsSafely(response.products)
                
                NetworkResult.Success(response.products)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching products from MongoDB: ${e.message}", e)
                return@withContext NetworkResult.Error("Ürünler yüklenirken bir sorun oluştu. Lütfen daha sonra tekrar deneyin.")
            }
        }
    }
    
    // MongoDB'ye yeni ürün ekle
    suspend fun addProductToMongoDB(product: Product): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Adding product to MongoDB: ${product.name}")
                
                val request = MongoDBInsertRequest(document = product)
                val response = mongoDBApiService.insertProduct(request)
                
                // Yerel veritabanına da ekleyelim
                insertProductsSafely(listOf(product))
                
                Log.d(TAG, "Product added to MongoDB successfully: ${response["insertedId"]}")
                NetworkResult.Success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding product to MongoDB: ${e.message}", e)
                NetworkResult.Error("Ürün eklenirken hata oluştu: ${e.message}")
            }
        }
    }
    
    // Remote API operations with local cache
    suspend fun refreshProducts(): NetworkResult<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                // Önce MongoDB'den verileri almayı deneyelim
                val mongoResult = getMongoDBProducts()
                
                if (mongoResult is NetworkResult.Success && mongoResult.data.isNotEmpty()) {
                    Log.d(TAG, "Using MongoDB products")
                    return@withContext mongoResult
                }
                
                // MongoDB başarısız olursa hata döndür
                return@withContext NetworkResult.Error("Ürünler yüklenirken bir sorun oluştu. Lütfen daha sonra tekrar deneyin.")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing products from MongoDB: ${e.message}", e)
                return@withContext NetworkResult.Error("Ürünler yüklenirken bir sorun oluştu. Lütfen daha sonra tekrar deneyin.")
            }
        }
    }
    
    private suspend fun tryFallbackToCache(): NetworkResult<List<Product>> {
        // Veritabanında veri var mı kontrol et
        Log.d(TAG, "Checking local database for products...")
        val localProducts = try {
            productDao.getAllProducts().firstOrNull() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading from database", e)
            emptyList()
        }
        
        Log.d(TAG, "Found ${localProducts.size} products in local database")
        
        if (localProducts.isNotEmpty()) {
            Log.d(TAG, "Returning cached products")
            return NetworkResult.Success(localProducts, isFromCache = true)
        }
        
        // Veritabanında ürün yoksa, bilgi mesajı dön
        Log.d(TAG, "No products found in database and API request failed")
        return NetworkResult.Error("Veritabanına ve API'ye bağlanılamıyor. Lütfen internet bağlantınızı kontrol edin.")
    }
    
    suspend fun refreshProductsByCategory(category: String): NetworkResult<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                // Önce MongoDB'den verileri almayı deneyelim
                val mongoResult = getMongoDBProducts()
                
                if (mongoResult is NetworkResult.Success && mongoResult.data.isNotEmpty()) {
                    // MongoDB'den gelen verileri kategoriye göre filtreleyelim
                    val filteredProducts = mongoResult.data.filter { 
                        it.category.equals(category, ignoreCase = true) 
                    }
                    
                    if (filteredProducts.isNotEmpty()) {
                        return@withContext NetworkResult.Success(filteredProducts)
                    } else {
                        return@withContext NetworkResult.Error("Bu kategoride ürün bulunamadı.")
                    }
                }
                
                // MongoDB başarısız olursa hata döndür
                return@withContext NetworkResult.Error("Ürünler yüklenirken bir sorun oluştu. Lütfen daha sonra tekrar deneyin.")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching products by category from MongoDB: ${e.message}", e)
                return@withContext NetworkResult.Error("Ürünler yüklenirken bir sorun oluştu. Lütfen daha sonra tekrar deneyin.")
            }
        }
    }
    
    /**
     * Uygulama kategorilerini DummyJSON formatına dönüştürür
     */
    private fun mapCategoryToDummyJSON(category: String): String? {
        return when (category.lowercase()) {
            "erkek" -> "mens-shirts"
            "kadın" -> "womens-dresses"
            "yeni sezon" -> "sunglasses"
            "koleksiyon" -> "fragrances"
            "tümü" -> null // Tüm kategoriler
            else -> null
        }
    }
    
    private suspend fun insertProductsSafely(products: List<Product>) {
        try {
            if (products.isNotEmpty()) {
                productDao.insertAll(products)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting products to database", e)
        }
    }
} 