package com.example.mobilsmartwear.data.remote.dto

import com.example.mobilsmartwear.data.model.User

/**
 * Kimlik doğrulama yanıtı için DTO
 */
data class AuthResponse(
    val token: String,
    val user: User
)

/**
 * Kullanıcı bilgileri yanıtı için DTO
 */
data class UserInfoResponse(
    val user: UserInfo
)

/**
 * Kullanıcı bilgileri için DTO
 */
data class UserInfo(
    val _id: String,
    val name: String,
    val email: String,
    val createdAt: String? = null
) 