package com.example.mobilsmartwear.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryUiState(
    val products: List<Product> = emptyList(),
    val originalProducts: List<Product> = emptyList(),
    val error: String? = null
)

class CategoryViewModel(
    private val category: String,
    private val repository: ProductRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadCategoryProducts()
    }
    
    private fun loadCategoryProducts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getProductsByCategory(category).collect { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        originalProducts = products,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ürünler yüklenirken bir hata oluştu: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshProducts() {
        loadCategoryProducts()
    }
    
    fun applyFilters(
        selectedFilters: List<String>, 
        priceRange: PriceRange?, 
        sortOption: SortOption
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Orijinal ürün listesi üzerinden filtreleme yapılır
                var filteredProducts = _uiState.value.originalProducts
                
                // Tür filtresi uygula
                if (selectedFilters.isNotEmpty()) {
                    filteredProducts = filteredProducts.filter { product ->
                        // Ürünün subcategory alanında filtreye uygun değer var mı kontrol et
                        selectedFilters.any { filter ->
                            product.subcategory?.equals(filter, ignoreCase = true) == true ||
                            // Eğer subcategory yoksa name veya etiketlerde de kontrol edelim
                            product.name.contains(filter, ignoreCase = true) || 
                            (product.tags?.any { it.contains(filter, ignoreCase = true) } ?: false)
                        }
                    }
                }
                
                // Fiyat aralığı filtresi uygula
                if (priceRange != null) {
                    filteredProducts = filteredProducts.filter { product ->
                        product.price >= priceRange.min && 
                        (priceRange.max == Int.MAX_VALUE || product.price <= priceRange.max)
                    }
                }
                
                // Sıralama uygula
                filteredProducts = when (sortOption) {
                    SortOption.PRICE_LOW_TO_HIGH -> filteredProducts.sortedBy { it.price }
                    SortOption.PRICE_HIGH_TO_LOW -> filteredProducts.sortedByDescending { it.price }
                    SortOption.NEWEST -> {
                        if (filteredProducts.all { it.createdAt != null }) {
                            filteredProducts.sortedByDescending { it.createdAt }
                        } else {
                            filteredProducts
                        }
                    }
                    SortOption.RECOMMENDED -> filteredProducts // Varsayılan sıralama
                }
                
                _uiState.value = _uiState.value.copy(products = filteredProducts)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Filtreleme sırasında bir hata oluştu: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    companion object {
        fun provideFactory(
            category: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                return CategoryViewModel(
                    category = category,
                    repository = ProductRepository()
                ) as T
            }
        }
    }
} 