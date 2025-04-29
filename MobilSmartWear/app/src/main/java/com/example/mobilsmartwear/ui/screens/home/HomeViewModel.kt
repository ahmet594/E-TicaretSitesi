package com.example.mobilsmartwear.ui.screens.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.ProductRepository
import com.example.mobilsmartwear.data.repository.FavoriteRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val featuredProducts: List<Product> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val TAG = "HomeViewModel"
    
    private val repository = AppModule.provideProductRepository(application)
    private val favoriteRepository = AppModule.provideFavoriteRepository(application)
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _favoriteProductIds = MutableStateFlow<Set<String>>(emptySet())
    
    init {
        loadFeaturedProducts()
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepository.favoriteProductIds.collect { favoriteIds ->
                _favoriteProductIds.value = favoriteIds
            }
        }
    }
    
    fun loadFeaturedProducts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getFeaturedProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(
                        featuredProducts = products
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Öne çıkan ürünler yüklenirken hata oluştu: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            if (_favoriteProductIds.value.contains(productId)) {
                favoriteRepository.removeFromFavorites(productId)
            } else {
                favoriteRepository.addToFavorites(productId)
            }
        }
    }
    
    fun isFavorite(productId: String): Boolean {
        return _favoriteProductIds.value.contains(productId)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshData() {
        loadFeaturedProducts()
    }

    companion object {
        fun provideFactory(
            application: Application
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>
            ): T {
                return HomeViewModel(
                    application = application
                ) as T
            }
        }
    }
} 