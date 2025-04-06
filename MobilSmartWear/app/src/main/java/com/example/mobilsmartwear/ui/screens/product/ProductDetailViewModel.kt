package com.example.mobilsmartwear.ui.screens.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.FavoriteRepository
import com.example.mobilsmartwear.data.repository.ProductRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val productRepository: ProductRepository = AppModule.provideProductRepository(application)
    private val cartRepository: CartRepository = AppModule.provideCartRepository(application)
    private val favoriteRepository: FavoriteRepository = AppModule.provideFavoriteRepository(application)
    
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite
    
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Ürün bilgilerini yükle
            _product.value = productRepository.getProductById(productId)
            
            // Favori durumunu kontrol et
            favoriteRepository.isFavorite(productId).collect { isFav ->
                _isFavorite.value = isFav
            }
            
            _isLoading.value = false
        }
    }
    
    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.id,
                quantity = quantity
            )
            cartRepository.addToCart(cartItem)
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            _product.value?.let { product ->
                favoriteRepository.toggleFavorite(product.id)
                // Favori durumunu güncelliyoruz
                _isFavorite.value = !_isFavorite.value
            }
        }
    }
    
    fun addToFavorites() {
        viewModelScope.launch {
            _product.value?.let { product ->
                favoriteRepository.addToFavorites(product.id)
                _isFavorite.value = true
            }
        }
    }
    
    fun removeFromFavorites() {
        viewModelScope.launch {
            _product.value?.let { product ->
                favoriteRepository.removeFromFavorites(product.id)
                _isFavorite.value = false
            }
        }
    }
} 