package com.example.mobilsmartwear.ui.screens.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.remote.NetworkResult
import com.example.mobilsmartwear.data.repository.ProductRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "HomeViewModel"
    
    private val productRepository = AppModule.provideProductRepository(application)
    
    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Featured Products
    private val _featuredProducts = MutableStateFlow<List<Product>>(emptyList())
    val featuredProducts: StateFlow<List<Product>> = _featuredProducts.asStateFlow()
    
    // New Arrivals
    private val _newArrivals = MutableStateFlow<List<Product>>(emptyList())
    val newArrivals: StateFlow<List<Product>> = _newArrivals.asStateFlow()
    
    // Selected Category
    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    // Kategoriler
    val categories = listOf("Tümü", "Erkek", "Kadın", "Yeni Sezon", "Koleksiyon")
    
    init {
        Log.d(TAG, "HomeViewModel initialized")
        loadProducts()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            Log.d(TAG, "Loading products")
            
            // UI'ı yükleniyor durumuna güncelle
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Önce MongoDB'den verileri çekmeye çalış
                val mongoResult = productRepository.getMongoDBProducts()
                
                when (mongoResult) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Products loaded from MongoDB successfully: ${mongoResult.data.size}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                products = mongoResult.data,
                                featuredProducts = selectFeaturedProducts(mongoResult.data),
                                error = null,
                                isFromCache = mongoResult.isFromCache
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        // MongoDB'den veri çekilemediyse, diğer API'den dene
                        Log.d(TAG, "Failed to load products from MongoDB: ${mongoResult.message}, trying API...")
                        
                        val result = productRepository.refreshProducts()
                        
                        when (result) {
                            is NetworkResult.Success -> {
                                Log.d(TAG, "Products loaded from API successfully: ${result.data.size}")
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        products = result.data,
                                        featuredProducts = selectFeaturedProducts(result.data),
                                        error = null,
                                        isFromCache = result.isFromCache
                                    )
                                }
                            }
                            is NetworkResult.Error -> {
                                Log.e(TAG, "Error loading products: ${result.message}")
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        error = result.message
                                    )
                                }
                            }
                            else -> {
                                // Handle other cases if needed
                            }
                        }
                    }
                    else -> {
                        // Handle other cases if needed
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading products", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ürünler yüklenirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun loadProductsByCategory(category: String) {
        viewModelScope.launch {
            Log.d(TAG, "Loading products for category: $category")
            
            // Eğer "Tümü" seçilmişse, tüm ürünleri yükle
            if (category == "Tümü") {
                loadProducts()
                return@launch
            }
            
            // UI'ı yükleniyor durumuna güncelle
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = productRepository.refreshProductsByCategory(category)
                
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Products loaded for category $category: ${result.data.size}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                products = result.data,
                                error = null,
                                isFromCache = result.isFromCache
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error loading products for category $category: ${result.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    else -> {
                        // Handle other cases if needed
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading products for category $category", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Kategori için ürünler yüklenirken bir hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun selectCategory(category: String) {
        Log.d(TAG, "Selected category: $category")
        
        // Seçilen kategoriyi güncelle ve o kategorideki ürünleri yükle
        _uiState.update { it.copy(selectedCategory = category) }
        loadProductsByCategory(category)
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun selectFeaturedProducts(products: List<Product>): List<Product> {
        // Ürünlerden bazılarını öne çıkarılanlar olarak seç
        return if (products.size <= 5) {
            products
        } else {
            products.shuffled().take(5)
        }
    }
    
    /**
     * Ürünleri arar
     */
    fun searchProducts(query: String): Flow<List<Product>> {
        Log.d(TAG, "Searching for: $query")
        return productRepository.searchProducts(query)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val selectedCategory: String = "Tümü",
    val error: String? = null,
    val isFromCache: Boolean = false
)

/**
 * HomeScreen için UI durumu
 */
sealed class HomeScreenState {
    object Loading : HomeScreenState()
    data class Success(val isFromCache: Boolean = false) : HomeScreenState()
    data class Error(val message: String) : HomeScreenState()
    data class Empty(val message: String) : HomeScreenState()
} 