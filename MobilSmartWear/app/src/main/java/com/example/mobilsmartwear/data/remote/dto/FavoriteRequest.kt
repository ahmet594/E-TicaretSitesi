package com.example.mobilsmartwear.data.remote.dto

/**
 * Favorilere ekleme isteği için DTO
 */
data class FavoriteRequest(
    val productId: String
)

/**
 * Yorum ekleme isteği için DTO
 */
data class ReviewRequest(
    val rating: Int,
    val comment: String
)

/**
 * Yorum yanıtı için DTO
 */
data class ReviewResponse(
    val message: String,
    val review: ReviewItem
)

/**
 * Kullanıcı yorumları yanıtı için DTO
 */
data class ReviewsResponse(
    val reviews: List<UserReviewItem>
)

/**
 * Kullanıcı yorumu için DTO
 */
data class UserReviewItem(
    val _id: String,
    val productId: String,
    val productName: String,
    val productImage: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
) 