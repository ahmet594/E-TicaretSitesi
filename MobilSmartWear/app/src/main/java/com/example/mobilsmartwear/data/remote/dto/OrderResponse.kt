package com.example.mobilsmartwear.data.remote.dto

/**
 * Sipariş oluşturma yanıtı için DTO
 */
data class OrderResponse(
    val orderId: String,
    val message: String,
    val orderSummary: OrderSummary
)

/**
 * Sipariş özeti için DTO
 */
data class OrderSummary(
    val totalItems: Int,
    val subtotal: Double,
    val shipping: Double,
    val tax: Double,
    val total: Double
) 