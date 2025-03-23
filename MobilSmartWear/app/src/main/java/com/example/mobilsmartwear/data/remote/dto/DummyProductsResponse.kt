package com.example.mobilsmartwear.data.remote.dto

import com.example.mobilsmartwear.data.model.Product
import com.google.gson.annotations.SerializedName

/**
 * DummyJSON API'den gelen ürün listesi yanıtı için DTO
 */
data class DummyProductsResponse(
    val products: List<DummyProductItem>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

/**
 * DummyJSON Ürün modeli
 */
data class DummyProductItem(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Float,
    val stock: Int,
    val brand: String,
    val category: String,
    val thumbnail: String,
    val images: List<String>
) {
    /**
     * Uygulama Ürün modeline dönüştürür
     */
    fun toProduct(): Product {
        return Product(
            id = id,
            name = title,
            description = description,
            price = price,
            imageUrl = thumbnail,
            category = mapCategory(category),
            rating = rating,
            reviewCount = (10..100).random(),
            isAvailable = stock > 0,
            isNew = (0..1).random() == 1,
            currency = "TL",
            discountPercentage = discountPercentage
        )
    }
    
    /**
     * DummyJSON kategorilerini uygulama kategorilerine eşler
     */
    private fun mapCategory(dummyCategory: String): String {
        return when (dummyCategory.lowercase()) {
            "smartphones" -> "Erkek"
            "laptops" -> "Erkek"
            "fragrances" -> "Koleksiyon"
            "skincare" -> "Koleksiyon"
            "groceries" -> "Yeni Sezon"
            "home-decoration" -> "Yeni Sezon"
            "furniture" -> "Erkek"
            "tops" -> "Kadın"
            "womens-dresses" -> "Kadın"
            "womens-shoes" -> "Kadın"
            "mens-shirts" -> "Erkek"
            "mens-shoes" -> "Erkek"
            "mens-watches" -> "Erkek"
            "womens-watches" -> "Kadın"
            "womens-bags" -> "Kadın"
            "womens-jewellery" -> "Koleksiyon"
            "sunglasses" -> "Yeni Sezon"
            "automotive" -> "Erkek"
            "motorcycle" -> "Erkek"
            "lighting" -> "Yeni Sezon"
            else -> "Diğer"
        }
    }
} 