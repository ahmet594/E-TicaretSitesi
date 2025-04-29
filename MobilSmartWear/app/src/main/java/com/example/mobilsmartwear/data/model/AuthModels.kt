package com.example.mobilsmartwear.data.model

import com.google.gson.annotations.SerializedName

/**
 * Giriş isteği için model
 */
data class AuthRequest(
    val email: String,
    val password: String
)

/**
 * Kayıt isteği için model
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

/**
 * Kimlik doğrulama yanıtı için model
 */
data class AuthResponse(
    val token: String,
    val user: User,
    val message: String? = null
)

/**
 * Kullanıcı modeli
 */
data class User(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val email: String,
    val role: String = "user",
    val createdAt: String? = null,
    @SerializedName("avatar")
    val avatarUrl: String? = null,
    val address: String? = null
) 