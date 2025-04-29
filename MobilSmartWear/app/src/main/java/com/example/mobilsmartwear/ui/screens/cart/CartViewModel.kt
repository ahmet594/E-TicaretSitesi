package com.example.mobilsmartwear.ui.screens.cart

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Order
import com.example.mobilsmartwear.data.repository.AuthRepository
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.OrderRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CartViewModel"
    private val cartRepository: CartRepository = AppModule.provideCartRepository(application)
    private val authRepository: AuthRepository = AppModule.provideAuthRepository(application)
    private val orderRepository: OrderRepository = AppModule.provideOrderRepository()
    
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()
    
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()
    
    private val _orderPlaced = MutableStateFlow<Order?>(null)
    val orderPlaced: StateFlow<Order?> = _orderPlaced.asStateFlow()
    
    init {
        loadCartItems()
        checkUserLoginStatus()
    }
    
    private fun loadCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems().collect { items ->
                _cartState.value = CartState(
                    items = items,
                    totalPrice = calculateTotalPrice(items)
                )
            }
        }
    }
    
    private fun checkUserLoginStatus() {
        _isUserLoggedIn.value = authRepository.isUserLoggedIn()
        Log.d(TAG, "Kullanıcı giriş durumu: ${_isUserLoggedIn.value}")
    }
    
    fun increaseQuantity(productId: String) {
        viewModelScope.launch {
            cartRepository.increaseQuantity(productId)
        }
    }
    
    fun decreaseQuantity(productId: String) {
        viewModelScope.launch {
            cartRepository.decreaseQuantity(productId)
        }
    }
    
    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
        }
    }
    
    fun placeOrder() {
        try {
            if (cartState.value.items.isEmpty()) {
                Toast.makeText(getApplication(), "Sepetinizde ürün bulunmuyor", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Kullanıcı giriş yapmış mı kontrol et
            val user = authRepository.userPreferences.getUser()
            
            // Kullanıcı yoksa veya name değeri yoksa
            if (user == null || user.name.isNullOrEmpty()) {
                Log.d(TAG, "Sipariş işlemi başarısız: Kullanıcı giriş yapmamış veya isim bilgisi yok")
                _isUserLoggedIn.value = false
                Toast.makeText(getApplication(), "Sipariş vermek için giriş yapmanız gerekiyor", Toast.LENGTH_LONG).show()
                return
            }
            
            viewModelScope.launch {
                try {
                    // Basit bir sipariş oluşturalım
                    val cartItems = cartState.value.items
                    
                    // Kullanıcı ID (null kontrolü yapalım)
                    val userId = user.id ?: "user_${System.currentTimeMillis()}"
                    
                    // Sipariş oluşturma
                    val order = Order(
                        id = java.util.UUID.randomUUID().toString(),
                        userId = userId,
                        items = cartItems.map { cartItem ->
                            com.example.mobilsmartwear.data.model.OrderItem(
                                product = cartItem.product,
                                quantity = cartItem.quantity,
                                selectedSize = cartItem.selectedSize,
                                price = cartItem.product.price
                            )
                        },
                        totalAmount = cartState.value.totalPrice,
                        status = "Hazırlanıyor",
                        orderDate = java.util.Date(),
                        shippingAddress = "Test Adresi",
                        paymentMethod = "Kapıda Ödeme"
                    )
                    
                    // Siparişi direkt olarak yerleştiriyoruz
                    try {
                        val currentOrders = orderRepository._orders.value.toMutableList()
                        currentOrders.add(order)
                        orderRepository._orders.value = currentOrders
                        
                        // Sepeti temizle
                        cartRepository.placeOrder()
                        
                        // Siparişlerim sayfasına yönlendirmek için ayarla
                        _orderPlaced.value = order
                        
                        Toast.makeText(getApplication(), "Siparişiniz alındı!", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Sipariş oluşturuldu: ${order.id}, Müşteri: ${user.name}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Sipariş kaydedilirken hata: ${e.message}", e)
                        Toast.makeText(getApplication(), "Sipariş işlemi sırasında bir hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Sipariş oluşturma hatası: ${e.message}", e)
                    Toast.makeText(getApplication(), "Sipariş işlemi sırasında bir hata oluştu", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Beklenmeyen hata: ${e.message}", e)
            Toast.makeText(getApplication(), "Beklenmeyen bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun resetOrderPlaced() {
        _orderPlaced.value = null
    }
    
    private fun calculateTotalPrice(items: List<CartItem>): Double {
        return items.sumOf { it.product.price * it.quantity }
    }
}

data class CartState(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0
) 