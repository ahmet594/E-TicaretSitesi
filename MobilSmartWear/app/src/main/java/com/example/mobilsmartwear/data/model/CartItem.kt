package com.example.mobilsmartwear.data.model

data class CartItem(
    val product: Product,
    val quantity: Int,
    val selectedSize: String? = null
) 