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