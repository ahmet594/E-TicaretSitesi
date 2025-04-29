package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Order
import com.example.mobilsmartwear.data.model.OrderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID

class OrderRepository {
    private val TAG = "OrderRepository"
    
    // Kullanıcı siparişlerini bellekte tutuyoruz
    val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders = _orders.asStateFlow()
    
    /**
     * Kullanıcının siparişlerini döndürür
     * @param userId Kullanıcı ID'si
     * @return Flow<List<Order>> Kullanıcının siparişleri
     */
    fun getUserOrders(userId: String): Flow<List<Order>> {
        return MutableStateFlow(_orders.value.filter { it.userId == userId })
    }
    
    /**
     * Sepetteki ürünlerden sipariş oluşturur
     * @param userId Kullanıcı ID'si
     * @param cartItems Sepetteki ürünler
     * @param address Teslimat adresi (opsiyonel)
     * @param paymentMethod Ödeme yöntemi (opsiyonel)
     * @return Oluşturulan sipariş
     */
    fun createOrder(
        userId: String,
        cartItems: List<CartItem>,
        address: String? = null,
        paymentMethod: String = "Kapıda Ödeme"
    ): Order {
        Log.d(TAG, "Sipariş oluşturuluyor: UserId=$userId, Ürün sayısı=${cartItems.size}")
        
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                product = cartItem.product,
                quantity = cartItem.quantity,
                selectedSize = cartItem.selectedSize,
                price = cartItem.product.price
            )
        }
        
        val totalAmount = cartItems.sumOf { it.product.price * it.quantity }
        
        val order = Order(
            id = UUID.randomUUID().toString(),
            userId = userId,
            items = orderItems,
            totalAmount = totalAmount,
            status = "Hazırlanıyor",
            orderDate = Date(),
            shippingAddress = address,
            paymentMethod = paymentMethod
        )
        
        val currentOrders = _orders.value.toMutableList()
        currentOrders.add(order)
        _orders.value = currentOrders
        
        Log.d(TAG, "Sipariş oluşturuldu: ${order.id}")
        
        return order
    }
    
    /**
     * Sipariş detaylarını getirir
     * @param orderId Sipariş ID'si
     * @return Sipariş detayları
     */
    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }
    
    /**
     * Sipariş durumunu günceller
     * @param orderId Sipariş ID'si
     * @param status Yeni durum
     * @return İşlem başarılı mı
     */
    fun updateOrderStatus(orderId: String, status: String): Boolean {
        val orderIndex = _orders.value.indexOfFirst { it.id == orderId }
        
        if (orderIndex == -1) {
            Log.e(TAG, "Sipariş bulunamadı: $orderId")
            return false
        }
        
        val updatedOrders = _orders.value.toMutableList()
        val order = updatedOrders[orderIndex]
        
        updatedOrders[orderIndex] = order.copy(status = status)
        _orders.value = updatedOrders
        
        Log.d(TAG, "Sipariş durumu güncellendi: $orderId, Durum: $status")
        
        return true
    }
} 