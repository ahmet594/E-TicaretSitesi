package com.example.mobilsmartwear.ui.screens.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.ProductRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "HomeViewModel"
    
    private val productRepository: ProductRepository = AppModule.provideProductRepository(application)
    
    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Featured Products
    private val _featuredProducts = MutableStateFlow<List<Product>>(emptyList())
    val featuredProducts: StateFlow<List<Product>> = _featuredProducts.asStateFlow()
    
    // Selected Category
    private val _selectedCategory = MutableStateFlow("Anasayfa")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    // Kategoriler
    val categories = listOf("Anasayfa", "Erkek", "Kadın", "Koleksiyon")
    
    // IsLoading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Otomatik yenileme için Job
    private var autoRefreshJob: Job? = null
    
    init {
        Log.d(TAG, "HomeViewModel initialized")
        // Başlangıçta ürünleri yükle
        loadProducts()
        
        // API'den düzenli olarak yenileme yapmak için otomatik yenileme başlat
        startAutoRefresh()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
    
    // API'den düzenli olarak veri almak için otomatik yenileme
    private fun startAutoRefresh() {
        stopAutoRefresh()
        
        autoRefreshJob = viewModelScope.launch {
            while(true) {
                delay(30000) // 30 saniye bekle
                Log.d(TAG, "Ürünler API'den otomatik yenileniyor")
                loadProducts()
            }
        }
    }
    
    private fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                productRepository.getAllProducts().collect { products ->
                    Log.d(TAG, "Ürünler başarıyla yüklendi: ${products.size} ürün")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            products = products,
                            featuredProducts = getFeaturedProductsFromList(products),
                            error = null
                        )
                    }
                    _isLoading.value = false
                    
                    // Öne çıkan ürünleri de yükle
                    loadFeaturedProducts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ürünler yüklenirken hata oluştu", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ürünler yüklenirken bir hata oluştu: ${e.message}"
                    )
                }
                _isLoading.value = false
            }
        }
    }
    
    fun refreshProducts() {
        loadProducts()
    }
    
    private fun loadFeaturedProducts() {
        viewModelScope.launch {
            try {
                productRepository.getFeaturedProducts().collect { featured ->
                    _featuredProducts.value = featured
                    Log.d(TAG, "Öne çıkan ürünler yüklendi: ${featured.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Öne çıkan ürünler yüklenirken hata oluştu", e)
            }
        }
    }
    
    fun selectCategory(category: String) {
        Log.d(TAG, "Seçilen kategori: $category")
        
        _uiState.update { it.copy(selectedCategory = category) }
        loadProductsByCategory(category)
    }
    
    fun loadProductsByCategory(category: String) {
        viewModelScope.launch {
            Log.d(TAG, "Kategori için ürünler yükleniyor: $category")
            
            _isLoading.value = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                productRepository.getProductsByCategory(category).collect { products ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            products = products,
                            error = if (products.isEmpty()) "Bu kategoride ürün bulunamadı" else null
                        )
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "$category kategorisi için ürünler yüklenirken hata oluştu", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Kategori için ürünler yüklenirken bir hata oluştu: ${e.message}"
                    )
                }
                _isLoading.value = false
            }
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            Log.d(TAG, "Ürün araması: $query")
            
            _isLoading.value = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                productRepository.searchProducts(query).collect { products ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            products = products,
                            error = if (products.isEmpty()) "Aramanızla eşleşen ürün bulunamadı" else null
                        )
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ürün araması yapılırken hata oluştu", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ürünler aranırken bir hata oluştu: ${e.message}"
                    )
                }
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun getFeaturedProductsFromList(products: List<Product>): List<Product> {
        return products.filter { it.featured }
    }
    
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Ürün siliniyor: $productId")
            
            try {
                _uiState.update { currentState ->
                    val updatedProducts = currentState.products.filter { it.id != productId }
                    val updatedFeaturedProducts = currentState.featuredProducts.filter { it.id != productId }
                    
                    currentState.copy(
                        products = updatedProducts,
                        featuredProducts = updatedFeaturedProducts
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ürün silinirken hata oluştu: ${e.message}", e)
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val selectedCategory: String = "Anasayfa",
    val error: String? = null
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