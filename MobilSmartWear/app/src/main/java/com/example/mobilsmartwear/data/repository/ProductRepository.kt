package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepository {
    private val productApiService = RetrofitClient.productApiService
    
    companion object {
        private const val TAG = "ProductRepository"
        
        // Örnek ürünler - API çağrıları başarısız olduğunda kullanılacak
        private val sampleProducts = listOf(
            // Erkek Kategorisi - Giyim
            Product(
                id = "e1",
                name = "Erkek Slim Fit Kot Pantolon",
                description = "Mavi, slim fit kesim, rahat kot pantolon",
                price = 399.99,
                category = "Erkek",
                subcategory = "Kot Pantolon",
                brand = "DenimCo",
                image = "https://picsum.photos/seed/erkek1/300/400",
                stock = 25,
                featured = true,
                salesCount = 45
            ),
            Product(
                id = "e2",
                name = "Erkek Gömlek",
                description = "Beyaz, uzun kollu, pamuklu gömlek",
                price = 299.99,
                category = "Erkek",
                subcategory = "Gömlek",
                brand = "FormalWear",
                image = "https://picsum.photos/seed/erkek2/300/400",
                stock = 20,
                featured = true,
                salesCount = 38
            ),
            Product(
                id = "e3",
                name = "Erkek Kışlık Mont",
                description = "Su geçirmez, siyah, kapüşonlu mont",
                price = 899.99,
                category = "Erkek",
                subcategory = "Mont",
                brand = "WinterStyle",
                image = "https://picsum.photos/seed/erkek3/300/400",
                stock = 15,
                featured = false,
                salesCount = 25
            ),
            Product(
                id = "e4",
                name = "Erkek Spor Ayakkabı",
                description = "Konforlu ve şık spor ayakkabı",
                price = 599.99,
                category = "Erkek",
                subcategory = "Spor Ayakkabı",
                brand = "SportMax",
                image = "https://picsum.photos/seed/erkek4/300/400",
                stock = 30,
                featured = false,
                salesCount = 55
            ),
            Product(
                id = "e5",
                name = "Erkek Klasik Kemer",
                description = "Hakiki deri, kahverengi kemer",
                price = 199.99,
                category = "Erkek",
                subcategory = "Kemer",
                brand = "LeatherCraft",
                image = "https://picsum.photos/seed/erkek5/300/400",
                stock = 40,
                featured = false,
                salesCount = 30
            ),
            
            // Kadın Kategorisi - Giyim
            Product(
                id = "k1",
                name = "Kadın Bluz",
                description = "Şık ve rahat, beyaz bluz",
                price = 249.99,
                category = "Kadın",
                subcategory = "Bluz",
                brand = "ElegantStyles",
                image = "https://picsum.photos/seed/kadin1/300/400",
                stock = 18,
                featured = true,
                salesCount = 42
            ),
            Product(
                id = "k2",
                name = "Kadın Yüksek Bel Tayt",
                description = "Spor ve günlük kullanıma uygun siyah tayt",
                price = 199.99,
                category = "Kadın",
                subcategory = "Tayt",
                brand = "ActiveFit",
                image = "https://picsum.photos/seed/kadin2/300/400",
                stock = 22,
                featured = true,
                salesCount = 50
            ),
            Product(
                id = "k3",
                name = "Kadın Deri Ceket",
                description = "Siyah, deri ceket",
                price = 999.99,
                category = "Kadın",
                subcategory = "Ceket",
                brand = "LeatherWear",
                image = "https://picsum.photos/seed/kadin3/300/400",
                stock = 10,
                featured = false,
                salesCount = 20
            ),
            Product(
                id = "k4",
                name = "Kadın Topuklu Ayakkabı",
                description = "Kırmızı, stiletto topuklu ayakkabı",
                price = 499.99,
                category = "Kadın",
                subcategory = "Topuklu Ayakkabı",
                brand = "HeelMaster",
                image = "https://picsum.photos/seed/kadin4/300/400",
                stock = 12,
                featured = false,
                salesCount = 35
            ),
            Product(
                id = "k5",
                name = "Kadın El Çantası",
                description = "Siyah, deri el çantası",
                price = 699.99,
                category = "Kadın",
                subcategory = "Çanta",
                brand = "BagQueen",
                image = "https://picsum.photos/seed/kadin5/300/400",
                stock = 8,
                featured = true,
                salesCount = 40
            ),
            
            // Koleksiyon Kategorisi - Özel Ürünler
            Product(
                id = "c1",
                name = "Limited Edition Akıllı Saat",
                description = "Fitness takibi ve bildirimler için sınırlı üretim akıllı saat",
                price = 1899.99,
                category = "Koleksiyon",
                subcategory = "Akıllı Saat",
                brand = "TechWear",
                image = "https://picsum.photos/seed/koleksiyon1/300/400",
                stock = 5,
                featured = true,
                salesCount = 18
            ),
            Product(
                id = "c2",
                name = "Premium Deri Cüzdan",
                description = "El yapımı, sınırlı üretim deri cüzdan",
                price = 899.99,
                category = "Koleksiyon",
                subcategory = "Cüzdan",
                brand = "LuxuryLeather",
                image = "https://picsum.photos/seed/koleksiyon2/300/400",
                stock = 7,
                featured = true,
                salesCount = 15
            ),
            Product(
                id = "c3",
                name = "Tasarım Takı Seti",
                description = "Özel tasarım, el yapımı takı seti",
                price = 1299.99,
                category = "Koleksiyon",
                subcategory = "Takı",
                brand = "ArtisanJewels",
                image = "https://picsum.photos/seed/koleksiyon3/300/400",
                stock = 3,
                featured = true,
                salesCount = 12
            )
        )
    }
    
    fun getAllProducts(): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "API çağrısı yapılıyor: getAllProducts")
            val response = productApiService.getAllProducts()
            Log.d(TAG, "API yanıtı alındı: ${response.isSuccessful}, kod: ${response.code()}")
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Log.d(TAG, "Ürünler başarıyla alındı: ${products.size} adet ürün")
                emit(products)
            } else {
                Log.e(TAG, "Ürünler alınamadı: Kod: ${response.code()}, Mesaj: ${response.message()}")
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Hata detayı: $errorBody")
                } catch (e: Exception) {
                    Log.e(TAG, "Hata detayı alınamadı", e)
                }
                Log.d(TAG, "API çağrısı başarısız oldu, örnek ürünler kullanılıyor")
                emit(sampleProducts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ürünler alınırken hata oluştu", e)
            Log.d(TAG, "Hata oluştu, örnek ürünler kullanılıyor")
            emit(sampleProducts)
        }
    }
    
    fun getFeaturedProducts(): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "API çağrısı yapılıyor: getFeaturedProducts")
            val response = productApiService.getFeaturedProducts()
            Log.d(TAG, "API yanıtı alındı: ${response.isSuccessful}, kod: ${response.code()}")
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Log.d(TAG, "Öne çıkan ürünler başarıyla alındı: ${products.size} adet ürün")
                emit(products)
            } else {
                Log.e(TAG, "Öne çıkan ürünler alınamadı: ${response.code()}")
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Hata detayı: $errorBody")
                } catch (e: Exception) {
                    Log.e(TAG, "Hata detayı alınamadı", e)
                }
                Log.d(TAG, "API çağrısı başarısız oldu, örnek öne çıkan ürünler kullanılıyor")
                emit(sampleProducts.filter { it.featured })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Öne çıkan ürünler alınırken hata oluştu", e)
            Log.d(TAG, "Hata oluştu, örnek öne çıkan ürünler kullanılıyor")
            emit(sampleProducts.filter { it.featured })
        }
    }
    
    fun searchProducts(query: String): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "API çağrısı yapılıyor: searchProducts, sorgu: $query")
            val response = productApiService.searchProducts(query)
            Log.d(TAG, "API yanıtı alındı: ${response.isSuccessful}, kod: ${response.code()}")
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Log.d(TAG, "Arama sonuçları başarıyla alındı: ${products.size} adet ürün")
                emit(products)
            } else {
                Log.e(TAG, "Arama sonuçları alınamadı: ${response.code()}")
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Hata detayı: $errorBody")
                } catch (e: Exception) {
                    Log.e(TAG, "Hata detayı alınamadı", e)
                }
                
                // Örnek ürünlerde arama yap
                val filteredProducts = sampleProducts.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.subcategory?.contains(query, ignoreCase = true) ?: false ||
                    it.brand.contains(query, ignoreCase = true)
                }
                Log.d(TAG, "API çağrısı başarısız oldu, örnek ürünlerde arama yapılıyor: $query")
                emit(filteredProducts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Arama yapılırken hata oluştu", e)
            
            // Örnek ürünlerde arama yap
            val filteredProducts = sampleProducts.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.subcategory?.contains(query, ignoreCase = true) ?: false ||
                it.brand.contains(query, ignoreCase = true)
            }
            Log.d(TAG, "Hata oluştu, örnek ürünlerde arama yapılıyor: $query")
            emit(filteredProducts)
        }
    }
    
    fun getProductsByCategory(category: String): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "API çağrısı yapılıyor: getProductsByCategory, kategori: $category")
            val response = productApiService.getProductsByCategory(category)
            Log.d(TAG, "API yanıtı alındı: ${response.isSuccessful}, kod: ${response.code()}")
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Log.d(TAG, "Kategori ürünleri başarıyla alındı: ${products.size} adet ürün")
                emit(products)
            } else {
                Log.e(TAG, "Kategori ürünleri alınamadı: ${response.code()}")
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Hata detayı: $errorBody")
                } catch (e: Exception) {
                    Log.e(TAG, "Hata detayı alınamadı", e)
                }
                
                // Örnek ürünleri kategoriye göre filtrele
                val filteredProducts = if (category == "Anasayfa") {
                    sampleProducts
                } else {
                    sampleProducts.filter { 
                        it.category.equals(category, ignoreCase = true) 
                    }
                }
                Log.d(TAG, "API çağrısı başarısız oldu, örnek ürünlerde kategori filtrelemesi yapılıyor: $category")
                emit(filteredProducts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kategori ürünleri alınırken hata oluştu", e)
            
            // Örnek ürünleri kategoriye göre filtrele
            val filteredProducts = if (category == "Anasayfa") {
                sampleProducts
            } else {
                sampleProducts.filter { 
                    it.category.equals(category, ignoreCase = true) 
                }
            }
            Log.d(TAG, "Hata oluştu, örnek ürünlerde kategori filtrelemesi yapılıyor: $category")
            emit(filteredProducts)
        }
    }
    
    suspend fun getProductById(id: String): Product? {
        return try {
            Log.d(TAG, "API çağrısı yapılıyor: getProductById, id: $id")
            val response = productApiService.getProductById(id)
            Log.d(TAG, "API yanıtı alındı: ${response.isSuccessful}, kod: ${response.code()}")
            
            if (response.isSuccessful) {
                val product = response.body()
                Log.d(TAG, "Ürün detayı başarıyla alındı: ${product?.name}")
                product
            } else {
                Log.e(TAG, "Ürün detayı alınamadı: ${response.code()}")
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Hata detayı: $errorBody")
                } catch (e: Exception) {
                    Log.e(TAG, "Hata detayı alınamadı", e)
                }
                
                // ID'ye göre örnek ürün bul
                val product = sampleProducts.find { it.id == id }
                Log.d(TAG, "API çağrısı başarısız oldu, örnek ürünlerde ID araması yapılıyor: $id")
                product
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ürün detayı alınırken hata oluştu", e)
            
            // ID'ye göre örnek ürün bul
            val product = sampleProducts.find { it.id == id }
            Log.d(TAG, "Hata oluştu, örnek ürünlerde ID araması yapılıyor: $id")
            product
        }
    }
} 