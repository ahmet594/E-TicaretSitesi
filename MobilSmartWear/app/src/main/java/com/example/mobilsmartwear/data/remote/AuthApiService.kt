package com.example.mobilsmartwear.data.remote

import com.example.mobilsmartwear.data.model.AuthResponse
import com.example.mobilsmartwear.data.model.LoginRequest
import com.example.mobilsmartwear.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<AuthResponse>
} 