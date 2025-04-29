package com.example.mobilsmartwear.data.model

import java.util.Date

data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: String,
    val orderDate: Date,
    val shippingAddress: String? = null,
    val paymentMethod: String = "Kapıda Ödeme"
)

data class OrderItem(
    val product: Product,
    val quantity: Int,
    val selectedSize: String? = null,
    val price: Double
) 