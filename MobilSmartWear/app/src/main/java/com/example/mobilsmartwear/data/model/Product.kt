package com.example.mobilsmartwear.data.model

import com.example.mobilsmartwear.data.remote.RetrofitClient
import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    @SerializedName("image")
    private val _imageUrl: String,
    val category: String,
    val sizes: List<String> = listOf("S", "M", "L", "XL"),
    @SerializedName("stock")
    val stockCount: Int = 100,
    val subcategory: String? = null,
    val brand: String,
    @SerializedName("featured")
    val featured: Boolean = false,
    @SerializedName("salesCount")
    val salesCount: Int = 0,
    val color: String? = null,
    val size: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    var isFavorite: Boolean = false,
    @SerializedName("rating")
    val rating: Float = 0f,
    val tags: List<String>? = null,
    @SerializedName("combinationCode")
    val combinationCode: String? = null
) {
    val imageUrl: String
        get() = RetrofitClient.getImageUrl(_imageUrl)

    fun hasStock(): Boolean {
        return stockCount > 0
    }
} 