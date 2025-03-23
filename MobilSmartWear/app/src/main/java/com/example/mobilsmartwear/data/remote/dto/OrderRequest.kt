package com.example.mobilsmartwear.data.remote.dto

/**
 * Sipariş oluşturma isteği için DTO
 */
data class OrderRequest(
    val items: List<OrderItemRequest>,
    val shippingAddress: AddressRequest,
    val paymentMethod: String,
    val paymentDetails: Map<String, Any>? = null
)

/**
 * Sipariş item'ı için DTO
 */
data class OrderItemRequest(
    val productId: String,
    val quantity: Int,
    val size: String? = null,
    val color: String? = null
)

/**
 * Teslimat adresi için DTO
 */
data class AddressRequest(
    val title: String,
    val address: String,
    val city: String,
    val district: String,
    val postalCode: String,
    val phone: String
) 