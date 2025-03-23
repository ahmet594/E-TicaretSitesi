package com.example.mobilsmartwear.data.remote

import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.remote.dto.MongoDBFindRequest
import com.example.mobilsmartwear.data.remote.dto.MongoDBInsertRequest
import com.example.mobilsmartwear.data.remote.dto.MongoDBProductResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * MongoDB Data API servisi
 * https://docs.mongodb.com/realm/web/mongodb-remote-access/
 */
interface MongoDBApiService {
    
    @POST("action/find")
    suspend fun findProducts(
        @Body request: MongoDBFindRequest
    ): MongoDBProductResponse
    
    @POST("action/insertOne")
    suspend fun insertProduct(
        @Body request: MongoDBInsertRequest
    ): Map<String, Any>
    
    @POST("action/findOne")
    suspend fun findProductById(
        @Body request: MongoDBFindRequest
    ): Map<String, Product>
} 