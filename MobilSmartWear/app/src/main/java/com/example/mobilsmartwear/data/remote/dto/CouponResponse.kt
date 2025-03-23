package com.example.mobilsmartwear.data.remote.dto

/**
 * Kullanıcının kuponlarını listeleme yanıtı için DTO
 */
data class CouponResponse(
    val coupons: List<Coupon>
)

/**
 * Kupon modeli için DTO
 */
data class Coupon(
    val _id: String,
    val code: String,
    val description: String,
    val discountType: String,
    val discountValue: Int,
    val minimumPurchase: Int,
    val expiryDate: String,
    val isUsed: Boolean
)

/**
 * Kupon doğrulama isteği için DTO
 */
data class CouponVerifyRequest(
    val code: String,
    val cartTotal: Double
)

/**
 * Kupon doğrulama yanıtı için DTO
 */
data class CouponVerifyResponse(
    val valid: Boolean,
    val coupon: CouponDetails? = null,
    val message: String? = null
)

/**
 * Kupon detayları için DTO
 */
data class CouponDetails(
    val code: String,
    val description: String,
    val discountType: String,
    val discountValue: Int,
    val discountAmount: Double
) 