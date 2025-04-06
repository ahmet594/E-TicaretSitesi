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
    
    private val productRepository: ProductRepository = AppModule.provideProductRepository(application)
    
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
    private val _selectedCategory = MutableStateFlow("Anasayfa")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    // Kategoriler
    val categories = listOf("Anasayfa", "Erkek", "Kadın", "Koleksiyon")
    
    // IsLoading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        Log.d(TAG, "HomeViewModel initialized")
        loadProducts()
        loadFeaturedProducts()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                productRepository.getAllProducts().collect { products ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            products = products,
                            featuredProducts = selectFeaturedProducts(products),
                            error = null,
                            isFromCache = false
                        )
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading products", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        products = emptyList(),
                        error = "Ürünler yüklenirken bir hata oluştu: ${e.message}",
                        isFromCache = false
                    )
                }
                _isLoading.value = false
            }
        }
    }
    
    fun loadFeaturedProducts() {
        viewModelScope.launch {
            try {
                productRepository.getFeaturedProducts().collect { products ->
                    _featuredProducts.value = products
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading featured products", e)
            }
        }
    }
    
    fun selectCategory(category: String) {
        Log.d(TAG, "Selected category: $category")
        
        // Seçilen kategoriyi güncelle ve o kategorideki ürünleri yükle
        _uiState.update { it.copy(selectedCategory = category) }
        loadProductsByCategory(category)
    }
    
    fun loadProductsByCategory(category: String) {
        viewModelScope.launch {
            Log.d(TAG, "Loading products for category: $category")
            
            // Eğer "Anasayfa" seçilmişse, tüm ürünleri yükle
            if (category == "Anasayfa") {
                loadProducts()
                return@launch
            }
            
            // UI'ı yükleniyor durumuna güncelle
            _isLoading.value = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                productRepository.getProductsByCategory(category).collect { products ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            products = products,
                            error = null,
                            isFromCache = false
                        )
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading products for category $category", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        products = emptyList(),
                        error = "Kategori için ürünler yüklenirken bir hata oluştu: ${e.message}",
                        isFromCache = false
                    )
                }
                _isLoading.value = false
            }
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            Log.d(TAG, "Searching for: $query")
            
            _isLoading.value = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                productRepository.searchProducts(query).collect { products ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            products = products,
                            error = null,
                            isFromCache = false
                        )
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while searching products", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        products = emptyList(),
                        error = "Ürünler aranırken bir hata oluştu: ${e.message}",
                        isFromCache = false
                    )
                }
                _isLoading.value = false
            }
        }
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
     * Belirli bir ürünü siler
     */
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Deleting product with id: $productId")
            
            try {
                // UI'dan ürünü kaldır
                _uiState.update { currentState ->
                    val updatedProducts = currentState.products.filter { it.id != productId }
                    val updatedFeaturedProducts = currentState.featuredProducts.filter { it.id != productId }
                    
                    currentState.copy(
                        products = updatedProducts,
                        featuredProducts = updatedFeaturedProducts
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting product: ${e.message}", e)
            }
        }
    }
    
    /**
     * Tüm ürünleri UI'dan temizler
     */
    fun deleteAllProducts() {
        viewModelScope.launch {
            Log.d(TAG, "Deleting all products")
            
            try {
                // UI'ı temizle
                _uiState.update { currentState ->
                    currentState.copy(
                        products = emptyList(),
                        featuredProducts = emptyList()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting all products: ${e.message}", e)
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val selectedCategory: String = "Anasayfa",
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