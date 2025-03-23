package com.example.mobilsmartwear.ui.screens.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.CartWithProduct
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {
    
    private val cartRepository = AppModule.provideCartRepository(application)
    
    val cartItems: Flow<List<CartWithProduct>> = cartRepository.getCartWithProducts()
    
    val totalPrice: Flow<Double> = cartItems.map { items ->
        items.sumOf { it.product.price * it.cartItem.quantity }
    }
    
    fun increaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            cartRepository.updateCartItemQuantity(cartItem, cartItem.quantity + 1)
        }
    }
    
    fun decreaseQuantity(cartItem: CartItem) {
        if (cartItem.quantity > 1) {
            viewModelScope.launch {
                cartRepository.updateCartItemQuantity(cartItem, cartItem.quantity - 1)
            }
        } else {
            removeCartItem(cartItem)
        }
    }
    
    fun removeCartItem(cartItem: CartItem) {
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