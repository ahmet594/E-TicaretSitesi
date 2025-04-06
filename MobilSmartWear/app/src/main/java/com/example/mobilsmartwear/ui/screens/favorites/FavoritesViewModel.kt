package com.example.mobilsmartwear.ui.screens.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.FavoriteRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val favoriteRepository: FavoriteRepository = AppModule.provideFavoriteRepository(application)
    
    private val _favorites = MutableStateFlow<List<Product>>(emptyList())
    val favorites: StateFlow<List<Product>> = _favorites.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            favoriteRepository.getFavoriteProducts().collect { products ->
                _favorites.value = products
                _isLoading.value = false
            }
        }
    }
    
    fun removeFromFavorites(productId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFromFavorites(productId)
            // loadFavorites() aktif Flow ile otomatik güncelleniyor
        }
    }
    
    fun clearAllFavorites() {
        viewModelScope.launch {
            favoriteRepository.clearFavorites()
            // loadFavorites() aktif Flow ile otomatik güncelleniyor
        }
    }
} 