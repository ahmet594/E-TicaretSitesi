package com.example.mobilsmartwear.data.remote.dto

/**
 * Kullanıcı adresi için DTO
 */
data class Address(
    val _id: String? = null,
    val title: String,
    val address: String,
    val city: String,
    val district: String,
    val postalCode: String,
    val phone: String,
    val isDefault: Boolean = false
) 