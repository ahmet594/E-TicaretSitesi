package com.example.mobilsmartwear.data.model

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
    val image: String,
    val stock: Int,
    val featured: Boolean = false,
    @SerializedName("salesCount")
    val salesCount: Int = 0
) 