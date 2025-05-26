package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.remote.RetrofitClient
import com.example.mobilsmartwear.data.remote.api.OutfitApi
import com.example.mobilsmartwear.data.remote.api.OutfitResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OutfitRepository {
    private val TAG = "OutfitRepository"
    private val outfitApi: OutfitApi = RetrofitClient.retrofit.create(OutfitApi::class.java)

    suspend fun getAllOutfits(): Result<List<OutfitResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Kombinler yükleniyor...")
            val response = outfitApi.getAllOutfits()
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Kombinler başarıyla yüklendi: ${response.body()?.size} adet")
                Result.success(response.body()!!)
            } else {
                Log.e(TAG, "Kombinler yüklenirken hata: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Kombinler yüklenirken hata oluştu: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kombinler yüklenirken exception:", e)
            Result.failure(e)
        }
    }
} 