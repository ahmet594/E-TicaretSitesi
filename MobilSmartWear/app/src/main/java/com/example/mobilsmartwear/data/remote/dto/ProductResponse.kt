package com.example.mobilsmartwear.data.remote.dto

import com.example.mobilsmartwear.data.model.Product

/**
 * Ürün listesi yanıtı için DTO
 */
data class ProductsResponse(
    val products: List<Product>,
    val totalProducts: Int,
    val totalPages: Int,
    val currentPage: Int
)

/**
 * Ürün detayı yanıtı için DTO
 */
data class ProductDetailResponse(
    val _id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val subCategory: String,
    val imagePath: String,
    val images: List<String>,
    val inStock: Boolean,
    val sizes: List<String>,
    val colors: List<String>,
    val rating: Float,
    val reviewCount: Int,
    val reviews: List<ReviewItem>
)

/**
 * Değerlendirme için DTO
 */
data class ReviewItem(
    val _id: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
) 