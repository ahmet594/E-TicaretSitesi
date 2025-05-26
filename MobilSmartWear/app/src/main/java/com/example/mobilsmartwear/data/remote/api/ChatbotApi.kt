package com.example.mobilsmartwear.data.remote.api

import com.example.mobilsmartwear.data.model.ChatRequest
import com.example.mobilsmartwear.data.model.ChatResponse
import retrofit2.Response
import retrofit2.http.*

interface ChatbotApi {
    @POST("chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatRequest
    ): Response<ChatResponse>
} 