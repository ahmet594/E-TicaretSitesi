package com.example.mobilsmartwear.data.remote.api

import retrofit2.Response
import retrofit2.http.GET

interface OutfitApi {
    @GET("outfits")
    suspend fun getAllOutfits(): Response<List<OutfitResponse>>
}

data class OutfitResponse(
    val _id: String,
    val productIds: List<String>,
    val createdAt: String
) 