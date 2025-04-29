package com.example.mobilsmartwear.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Order
import com.example.mobilsmartwear.data.repository.AuthRepository
import com.example.mobilsmartwear.data.repository.OrderRepository
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class OrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository: OrderRepository = AppModule.provideOrderRepository()
    private val authRepository: AuthRepository = AppModule.provideAuthRepository(application)
    
    private val _ordersState = MutableStateFlow<List<OrderUiState>>(emptyList())
    val ordersState: StateFlow<List<OrderUiState>> = _ordersState.asStateFlow()
    
    init {
        loadOrders()
    }
    
    private fun loadOrders() {
        viewModelScope.launch {
            try {
                // Tüm siparişleri al
                val allOrders = orderRepository._orders.value
                
                // Siparişleri UI modeline dönüştür
                _ordersState.value = allOrders.map { it.toOrderUiState() }
            } catch (e: Exception) {
                // Hata durumunda boş liste göster
                _ordersState.value = emptyList()
            }
        }
    }
    
    private fun Order.toOrderUiState(): OrderUiState {
        return OrderUiState(
            id = this.id,
            orderDate = this.orderDate,
            status = this.status,
            totalAmount = this.totalAmount,
            items = this.items.map { item ->
                OrderItemUiState(
                    productName = item.product.name,
                    quantity = item.quantity,
                    price = item.price,
                    size = item.selectedSize
                )
            }
        )
    }
}

data class OrderUiState(
    val id: String,
    val orderDate: Date,
    val status: String,
    val totalAmount: Double,
    val items: List<OrderItemUiState>
)

data class OrderItemUiState(
    val productName: String,
    val quantity: Int,
    val price: Double,
    val size: String?
) 