package com.example.mobilsmartwear.data.repository

import com.example.mobilsmartwear.data.local.CartDao
import com.example.mobilsmartwear.data.local.ProductDao
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepository(
    private val cartDao: CartDao,
    private val productDao: ProductDao
) {
    
    fun getAllCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems()
    }
    
    // Getting cart items with their product details
    fun getCartWithProducts(): Flow<List<CartWithProduct>> {
        return cartDao.getAllCartItems().map { cartItems ->
            cartItems.mapNotNull { cartItem ->
                val product = productDao.getProductById(cartItem.productId)
                product?.let {
                    CartWithProduct(cartItem, it)
                }
            }
        }
    }
    
    suspend fun addToCart(productId: Int, quantity: Int) {
        cartDao.addOrUpdateCartItem(productId, quantity)
    }
    
    suspend fun updateCartItemQuantity(cartItem: CartItem, quantity: Int) {
        val updatedItem = cartItem.copy(quantity = quantity)
        cartDao.updateCartItem(updatedItem)
    }
    
    suspend fun removeFromCart(cartItem: CartItem) {
        cartDao.deleteCartItem(cartItem)
    }
    
    suspend fun clearCart() {
        cartDao.clearCart()
    }
}

// A data class to represent cart item with its product details
data class CartWithProduct(
    val cartItem: CartItem,
    val product: Product
) 