package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class ProductRepository {
    private val apiService = RetrofitClient.apiService
    
    companion object {
        private const val TAG = "ProductRepository"
    }
    
    fun getAllProducts(): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "API çağrısı yapılıyor: getAllProducts")
            val response = apiService.getProducts()
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Log.d(TAG, "Ürünler API'den alındı: ${products.size} adet ürün")
                emit(products)
            } else {
                Log.e(TAG, "Ürünler alınamadı: ${response.code()}")
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Hata detayı: $errorBody")
                } catch (e: Exception) {
                    Log.e(TAG, "Hata detayı alınamadı", e)
                }
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ürünler alınırken hata oluştu", e)
            emit(getBackupProducts()) // Bağlantı hatası durumunda örnek veriler kullanılır
        }
    }
    
    fun getFeaturedProducts(): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "API çağrısı yapılıyor: getFeaturedProducts")
            val response = apiService.getFeaturedProducts()
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Log.d(TAG, "Öne çıkan ürünler API'den alındı: ${products.size} adet ürün")
                emit(products)
            } else {
                Log.e(TAG, "Öne çıkan ürünler alınamadı: ${response.code()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Öne çıkan ürünler alınırken hata oluştu", e)
            emit(getBackupProducts().filter { it.featured }) // Bağlantı hatası durumunda örnek veriler kullanılır
        }
    }
    
    fun searchProducts(query: String): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "API çağrısı yapılıyor: searchProducts")
            val response = apiService.searchProducts(query)
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Log.d(TAG, "Arama sonuçları API'den alındı: ${products.size} adet ürün")
                emit(products)
            } else {
                Log.e(TAG, "Arama sonuçları alınamadı: ${response.code()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Arama yapılırken hata oluştu", e)
            // Bağlantı hatası durumunda örnek veriler içinde arama yap
            val results = getBackupProducts().filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true)
            }
            emit(results)
        }
    }
    
    fun getProductsByCategory(category: String): Flow<List<Product>> = flow {
        try {
            // Anasayfa seçiliyse tüm ürünleri getir
            if (category == "Anasayfa") {
                getAllProducts().collect { emit(it) }
                return@flow
            }
            
            Log.d(TAG, "API çağrısı yapılıyor: getProductsByCategory")
            val response = apiService.getProductsByCategory(category)
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Log.d(TAG, "Kategoriye göre ürünler API'den alındı: ${products.size} adet ürün")
                emit(products)
            } else {
                Log.e(TAG, "Kategoriye göre ürünler alınamadı: ${response.code()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kategoriye göre ürünler alınırken hata oluştu", e)
            // Bağlantı hatası durumunda örnek veriler içinde kategori filtrele
            if (category == "Anasayfa") {
                emit(getBackupProducts())
            } else {
                emit(getBackupProducts().filter { it.category == category })
            }
        }
    }
    
    suspend fun getProductById(id: String): Product? {
        try {
            Log.d(TAG, "API çağrısı yapılıyor: getProductById($id)")
            val response = apiService.getProductById(id)
            
            if (response.isSuccessful) {
                val product = response.body()
                Log.d(TAG, "Ürün detayı başarıyla alındı: $id")
                return product
            } else {
                Log.e(TAG, "Ürün detayı alınamadı: ${response.code()}")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ürün detayı alınırken hata oluştu", e)
            // Bağlantı hatası durumunda örnek veriler içinde ürün ara
            return getBackupProducts().find { it.id == id }
        }
    }
    
    // Bağlantı hatası durumunda kullanılacak örnek ürünler
    private fun getBackupProducts(): List<Product> {
        return listOf(
            Product(
                id = "1",
                name = "Siyah T-Shirt",
                description = "Pamuklu rahat siyah t-shirt",
                price = 199.99,
                category = "Erkek",
                brand = "SmartWear",
                _imageUrl = "https://fastly.picsum.photos/id/192/300/400.jpg",
                stockCount = 25,
                featured = true
            ),
            Product(
                id = "2",
                name = "Mavi Kot Pantolon",
                description = "Slim fit mavi kot pantolon",
                price = 399.99,
                category = "Erkek",
                brand = "SmartWear",
                _imageUrl = "https://fastly.picsum.photos/id/173/300/400.jpg",
                stockCount = 15,
                featured = false
            ),
            Product(
                id = "3",
                name = "Kırmızı Elbise",
                description = "Şık kırmızı elbise",
                price = 499.99,
                category = "Kadın",
                brand = "SmartWear",
                _imageUrl = "https://fastly.picsum.photos/id/177/300/400.jpg",
                stockCount = 10,
                featured = true
            ),
            Product(
                id = "4",
                name = "Beyaz Gömlek",
                description = "Klasik beyaz gömlek",
                price = 299.99,
                category = "Erkek",
                brand = "SmartWear",
                _imageUrl = "https://fastly.picsum.photos/id/175/300/400.jpg",
                stockCount = 20,
                featured = false
            ),
            Product(
                id = "5",
                name = "Siyah Ceket",
                description = "Deri siyah ceket",
                price = 899.99,
                category = "Erkek",
                brand = "SmartWear",
                _imageUrl = "https://fastly.picsum.photos/id/176/300/400.jpg",
                stockCount = 5,
                featured = true
            )
        )
    }

    /**
     * Yeni ürün ekler
     * @param product Eklenecek ürün
     * @return İşlem sonucu başarılı ise eklenen ürün, değilse null
     */
    suspend fun addProduct(product: Product): Product? {
        return try {
            val response = apiService.addProduct(product)
            if (response.isSuccessful) {
                val addedProduct = response.body()
                Log.d(TAG, "Ürün başarıyla eklendi: ${addedProduct?.name}")
                addedProduct
            } else {
                Log.e(TAG, "Ürün ekleme başarısız: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ürün eklerken hata oluştu", e)
            null
        }
    }

    /**
     * Mevcut ürünü günceller
     * @param id Güncellenecek ürünün ID'si
     * @param product Güncellenmiş ürün bilgileri
     * @return İşlem sonucu başarılı ise güncellenen ürün, değilse null
     */
    suspend fun updateProduct(id: String, product: Product): Product? {
        return try {
            val response = apiService.updateProduct(id, product)
            if (response.isSuccessful) {
                val updatedProduct = response.body()
                Log.d(TAG, "Ürün başarıyla güncellendi: ${updatedProduct?.name}")
                updatedProduct
            } else {
                Log.e(TAG, "Ürün güncelleme başarısız: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ürün güncellerken hata oluştu", e)
            null
        }
    }

    /**
     * Ürünü ID'sine göre siler
     * @param id Silinecek ürünün ID'si
     * @return İşlem başarılıysa true, değilse false
     */
    suspend fun deleteProductById(id: String): Boolean {
        return try {
            Log.d(TAG, "API çağrısı yapılıyor: deleteProductById($id)")
            val response = apiService.deleteProduct(id)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Ürün başarıyla silindi: $id")
                true
            } else {
                Log.e(TAG, "Ürün silinemedi: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ürün silinirken hata oluştu", e)
            false
        }
    }

    /**
     * Filters products to get products that are in stock
     */
    fun getProductsInStock(): Flow<List<Product>> = getAllProducts()
        .map { products ->
            products.filter { it.hasStock() }
        }

    /**
     * Retrieves favorite products
     */
    fun getFavoriteProducts(): Flow<List<Product>> = getAllProducts()
        .map { products ->
            products.filter { it.isFavorite }
        }

    /**
     * Toggles favorite status of a product
     * @param productId The ID of the product to toggle favorite status
     * @return The updated favorite status
     */
    suspend fun toggleFavoriteProduct(productId: String): Boolean {
        val products = getAllProducts().first()
        val product = products.find { it.id == productId } ?: run {
            Log.e(TAG, "Product with ID: $productId not found for favorite toggle")
            return false
        }
        
        // Toggle favorite status
        product.isFavorite = !product.isFavorite
        
        // Update cache with the updated product list
        // Şimdilik cache güncelleme kaldırıldı - repository Flow ile çalışıyor
        
        Log.d(TAG, "Product favorite status toggled: $productId, isFavorite: ${product.isFavorite}")
        return product.isFavorite
    }
} 