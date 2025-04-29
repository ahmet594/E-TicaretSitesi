package com.example.mobilsmartwear.ui.screens.product

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.FavoriteRepository
import com.example.mobilsmartwear.data.repository.ProductRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val error: String? = null,
    val selectedSize: String? = null
)

class ProductDetailViewModel(
    application: Application,
    private val productId: String
) : AndroidViewModel(application) {
    
    private val productRepository: ProductRepository = AppModule.provideProductRepository(application)
    private val cartRepository: CartRepository = AppModule.provideCartRepository(application)
    private val favoriteRepository: FavoriteRepository = AppModule.provideFavoriteRepository(application)
    
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    init {
        loadProduct()
        checkFavoriteStatus()
    }
    
    private fun loadProduct() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val product = productRepository.getProductById(productId)
                product?.let {
                    _uiState.value = _uiState.value.copy(
                        product = it,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ürün yüklenirken bir hata oluştu: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkFavoriteStatus() {
        viewModelScope.launch {
            _isFavorite.value = favoriteRepository.isFavorite(productId)
        }
    }
    
    fun selectSize(size: String) {
        _uiState.value = _uiState.value.copy(
            selectedSize = size
        )
    }
    
    fun addToCart() {
        viewModelScope.launch {
            try {
                uiState.value.product?.let { product ->
                    cartRepository.addToCart(
                        product = product,
                        selectedSize = uiState.value.selectedSize
                    )
                    Toast.makeText(getApplication(), "Ürün sepete eklendi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ürün sepete eklenirken bir hata oluştu: ${e.message}"
                )
                Toast.makeText(getApplication(), "Ürün sepete eklenemedi", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            _uiState.value.product?.let { product ->
                if (_isFavorite.value) {
                    favoriteRepository.removeFromFavorites(product.id)
                } else {
                    favoriteRepository.addToFavorites(product.id)
                }
                _isFavorite.value = !_isFavorite.value
            }
        }
    }
    
    companion object {
        fun provideFactory(
            application: Application,
            productId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                return ProductDetailViewModel(
                    application = application,
                    productId = productId
                ) as T
            }
        }
    }
} 