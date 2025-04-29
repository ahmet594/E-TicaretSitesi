package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class CartRepository(private val productRepository: ProductRepository) {
    private val TAG = "CartRepository"
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val cartItems = _cartItems.asStateFlow()
    
    fun getCartItems(): Flow<List<CartItem>> = cartItems
    
    suspend fun addToCart(product: Product, quantity: Int = 1, selectedSize: String? = null) {
        val existingItem = _cartItems.value.find { 
            it.product.id == product.id && it.selectedSize == selectedSize 
        }
        if (existingItem != null) {
            val index = _cartItems.value.indexOf(existingItem)
            val updatedItems = _cartItems.value.toMutableList().apply {
                set(index, existingItem.copy(quantity = existingItem.quantity + quantity))
            }
            _cartItems.value = updatedItems
        } else {
            _cartItems.value = _cartItems.value + CartItem(product, quantity, selectedSize)
        }
        Log.d(TAG, "Ürün sepete eklendi: ${product.name}, Miktar: $quantity, Beden: $selectedSize")
    }
    
    suspend fun increaseQuantity(productId: String) {
        val item = _cartItems.value.find { it.product.id == productId }
        if (item != null) {
            val index = _cartItems.value.indexOf(item)
            val updatedItems = _cartItems.value.toMutableList().apply {
                set(index, item.copy(quantity = item.quantity + 1))
            }
            _cartItems.value = updatedItems
            Log.d(TAG, "Ürün miktarı artırıldı: ${item.product.name}")
        }
    }
    
    suspend fun decreaseQuantity(productId: String) {
        val item = _cartItems.value.find { it.product.id == productId }
        if (item != null && item.quantity > 1) {
            val index = _cartItems.value.indexOf(item)
            val updatedItems = _cartItems.value.toMutableList().apply {
                set(index, item.copy(quantity = item.quantity - 1))
            }
            _cartItems.value = updatedItems
            Log.d(TAG, "Ürün miktarı azaltıldı: ${item.product.name}")
        }
    }
    
    suspend fun removeFromCart(productId: String) {
        val item = _cartItems.value.find { it.product.id == productId }
        if (item != null) {
            _cartItems.value = _cartItems.value.filter { it.product.id != productId }
            Log.d(TAG, "Ürün sepetten kaldırıldı: ${item.product.name}")
        }
    }
    
    suspend fun placeOrder() {
        // TODO: Sipariş verme işlemleri burada yapılacak
        _cartItems.value = emptyList()
        Log.d(TAG, "Sipariş verildi ve sepet temizlendi")
    }
    
    fun getCartItemCount(): Int {
        return _cartItems.value.size
    }
    
    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf { it.product.price * it.quantity }
    }
} 