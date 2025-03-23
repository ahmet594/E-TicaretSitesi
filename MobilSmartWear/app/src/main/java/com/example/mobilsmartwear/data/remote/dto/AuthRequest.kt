package com.example.mobilsmartwear.data.remote.dto

/**
 * Kullanıcı kaydı için istek
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

/**
 * Kullanıcı girişi için istek
 */
data class LoginRequest(
    val email: String,
    val password: String
) 