package com.example.mobilsmartwear.ui.screens.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.ProductRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.launch

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val productRepository = AppModule.provideProductRepository(application)
    private val cartRepository = AppModule.provideCartRepository(application)
    
    suspend fun getProductById(id: Int): Product? {
        return productRepository.getProductById(id)
    }
    
    fun addToCart(productId: Int, quantity: Int) {
        viewModelScope.launch {
            cartRepository.addToCart(productId, quantity)
        }
    }
} 