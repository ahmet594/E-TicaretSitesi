package com.example.mobilsmartwear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    
    private val productRepository = ProductRepository()
    
    private val _favoriteProducts = MutableStateFlow<List<Product>>(emptyList())
    val favoriteProducts: StateFlow<List<Product>> = _favoriteProducts.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadFavoriteProducts()
    }
    
    private fun loadFavoriteProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.getFavoriteProducts().collect { favorites ->
                    _favoriteProducts.value = favorites
                }
            } catch (e: Exception) {
                // Hata durumunda loglama yapılabilir
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.toggleFavoriteProduct(product.id)
                // Favori listesini güncelle
                loadFavoriteProducts()
            } catch (e: Exception) {
                // Hata durumunda loglama yapılabilir
            }
        }
    }
} 