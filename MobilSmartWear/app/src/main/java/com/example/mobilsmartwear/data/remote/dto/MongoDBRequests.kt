package com.example.mobilsmartwear.data.remote.dto

import com.example.mobilsmartwear.data.model.Product
import com.google.gson.annotations.SerializedName

/**
 * MongoDB API için sorgu isteği
 */
data class MongoDBFindRequest(
    val dataSource: String = "Cluster0",
    val database: String = "test",
    val collection: String = "products",
    val filter: Map<String, Any> = emptyMap(),
    val projection: Map<String, Any>? = null,
    val sort: Map<String, Int>? = null,
    val limit: Int? = null
)

/**
 * MongoDB API için veri ekleme isteği
 */
data class MongoDBInsertRequest(
    val dataSource: String = "Cluster0",
    val database: String = "test",
    val collection: String = "products",
    val document: Product
)

/**
 * MongoDB API'den gelen ürün listesi yanıtı
 */
data class MongoDBProductResponse(
    @SerializedName("documents")
    val products: List<Product> = emptyList()
) 