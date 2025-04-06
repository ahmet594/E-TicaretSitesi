package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CartRepository(private val productRepository: ProductRepository) {
    
    companion object {
        private const val TAG = "CartRepository"
    }
    
    // Sepet öğelerini bellekte tutuyoruz
    private val cartItemsFlow = MutableStateFlow<List<CartItem>>(emptyList())
    
    // Sepet işlemleri
    fun getAllCartItems(): Flow<List<CartItem>> = cartItemsFlow
    
    fun getCartWithProducts(): Flow<List<Pair<CartItem, Product?>>> = cartItemsFlow.map { cartItems ->
        cartItems.map { cartItem ->
            // Her sepet öğesi için ürün detayını al
            val product = getProductById(cartItem.productId)
            Pair(cartItem, product)
        }
    }
    
    suspend fun addToCart(cartItem: CartItem): Boolean {
        try {
            // Sepette bu ürün varsa miktarını güncelle, yoksa ekle
            val currentItems = cartItemsFlow.value
            val existingItemIndex = currentItems.indexOfFirst { it.productId == cartItem.productId }
            
            val updatedItems = if (existingItemIndex >= 0) {
                val existingItem = currentItems[existingItemIndex]
                val updatedItem = existingItem.copy(quantity = existingItem.quantity + cartItem.quantity)
                currentItems.toMutableList().apply {
                    set(existingItemIndex, updatedItem)
                }
            } else {
                currentItems + cartItem
            }
            
            cartItemsFlow.value = updatedItems
            Log.d(TAG, "Ürün sepete eklendi: ${cartItem.productId}, miktar: ${cartItem.quantity}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Ürün sepete eklenirken hata oluştu", e)
            return false
        }
    }
    
    suspend fun updateCartItem(cartItem: CartItem): Boolean {
        try {
            val currentItems = cartItemsFlow.value
            val existingItemIndex = currentItems.indexOfFirst { it.productId == cartItem.productId }
            
            if (existingItemIndex >= 0) {
                val updatedItems = currentItems.toMutableList().apply {
                    set(existingItemIndex, cartItem)
                }
                cartItemsFlow.value = updatedItems
                Log.d(TAG, "Sepet öğesi güncellendi: ${cartItem.productId}, miktar: ${cartItem.quantity}")
                return true
            }
            
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Sepet öğesi güncellenirken hata oluştu", e)
            return false
        }
    }
    
    suspend fun removeFromCart(cartItem: CartItem): Boolean {
        try {
            val currentItems = cartItemsFlow.value
            val updatedItems = currentItems.filter { it.productId != cartItem.productId }
            
            cartItemsFlow.value = updatedItems
            Log.d(TAG, "Ürün sepetten çıkarıldı: ${cartItem.productId}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Ürün sepetten çıkarılırken hata oluştu", e)
            return false
        }
    }
    
    suspend fun clearCart(): Boolean {
        try {
            cartItemsFlow.value = emptyList()
            Log.d(TAG, "Sepet temizlendi")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Sepet temizlenirken hata oluştu", e)
            return false
        }
    }
    
    private suspend fun getProductById(productId: String): Product? {
        return productRepository.getProductById(productId)
    }
} 