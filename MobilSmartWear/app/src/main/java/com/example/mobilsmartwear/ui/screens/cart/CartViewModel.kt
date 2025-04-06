package com.example.mobilsmartwear.ui.screens.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val cartRepository: CartRepository = AppModule.provideCartRepository(application)
    
    private val _cartItems = MutableStateFlow<List<Pair<CartItem, Product?>>>(emptyList())
    val cartItems: StateFlow<List<Pair<CartItem, Product?>>> = _cartItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadCartItems()
    }
    
    private fun loadCartItems() {
        viewModelScope.launch {
            _isLoading.value = true
            cartRepository.getCartWithProducts().collect { items ->
                _cartItems.value = items
                _isLoading.value = false
            }
        }
    }
    
    fun increaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            cartRepository.updateCartItem(cartItem.copy(quantity = cartItem.quantity + 1))
        }
    }
    
    fun decreaseQuantity(cartItem: CartItem) {
        if (cartItem.quantity > 1) {
            viewModelScope.launch {
                cartRepository.updateCartItem(cartItem.copy(quantity = cartItem.quantity - 1))
            }
        }
    }
    
    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            cartRepository.removeFromCart(cartItem)
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
} 