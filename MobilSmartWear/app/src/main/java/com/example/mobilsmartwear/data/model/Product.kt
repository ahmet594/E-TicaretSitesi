package com.example.mobilsmartwear.data.model

import com.example.mobilsmartwear.data.remote.RetrofitClient
import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val subcategory: String? = null,
    val brand: String,
    @SerializedName("image")
    private val _imageUrl: String,
    val stock: Int,
    @SerializedName("featured")
    val featured: Boolean = false,
    @SerializedName("salesCount")
    val salesCount: Int = 0,
    val color: String? = null,
    val size: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    var isFavorite: Boolean = false
) {
    val imageUrl: String
        get() = RetrofitClient.getImageUrl(_imageUrl)

    fun hasStock(): Boolean {
        return stock > 0
    }
} 