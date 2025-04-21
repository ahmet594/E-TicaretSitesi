package com.example.mobilsmartwear.data.remote

import android.content.Context
import android.os.Build
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * API istekleri için Retrofit istemcisi
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // Backend API adresleri - localhost sabit 3000 portu olarak tanımlandı
    private const val LOCALHOST = "http://localhost:3000/"
    private const val EMULATOR = "http://10.0.2.2:3000/" // Emülatör için localhost 10.0.2.2 IP'sidir
    private const val PUBLIC_API = "http://192.168.43.3:3000/" // Şirket local network IP'si (gerekirse değiştirilmeli)
    
    private const val TIMEOUT_SECONDS = 30L
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    // Kullanılacak URL'yi belirler
    private fun getBaseUrl(): String {
        Log.d(TAG, "API adresi olarak PUBLIC_API kullanılıyor")
        return PUBLIC_API // Cihaz tipine göre farklı URL'ler kullanılabilir
    }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(getBaseUrl())
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    // Resim URL'lerini oluşturmak için yardımcı metod
    fun getImageUrl(imagePath: String): String {
        if (imagePath.startsWith("http")) {
            return imagePath
        }
        
        val baseUrl = getBaseUrl()
        return when {
            imagePath.startsWith("/") -> "$baseUrl${imagePath.substring(1)}"
            else -> "$baseUrl$imagePath"
        }
    }
} 