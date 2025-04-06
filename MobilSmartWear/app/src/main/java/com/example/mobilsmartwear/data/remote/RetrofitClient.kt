package com.example.mobilsmartwear.data.remote

import android.content.Context
import android.os.Build
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // Backend API adresleri
    private const val LOCALHOST = "http://localhost:3000/api/"
    private const val EMULATOR = "http://10.0.2.2:3000/api/" // Emülatör için localhost 10.0.2.2 IP'sidir
    private const val PUBLIC_API = "https://mobilsmartwear-api.onrender.com/api/" // Eğer varsa public API adresiniz
    
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
    
    // Bu metodu çağırarak hangi cihazda olduğumuzu belirleyelim
    private fun getBaseUrl(): String {
        Log.d(TAG, "Cihaz tipi belirleniyor...")
        
        // Emülatörde çalışıyor muyuz?
        val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT
        
        // Public API'yi öncelikli olarak kullanıyoruz
        // Eğer public bir API sunucunuz varsa bunu kullanabilirsiniz
        val baseUrl = PUBLIC_API
        
        // Alternatif olarak emülatör ve gerçek cihaz ayrımı yapabilirsiniz
        // val baseUrl = if (isEmulator) {
        //     Log.d(TAG, "Emülatör tespit edildi, 10.0.2.2 adresi kullanılıyor")
        //     EMULATOR
        // } else {
        //     Log.d(TAG, "Gerçek cihaz tespit edildi, localhost adresi kullanılıyor")
        //     LOCALHOST
        // }
        
        Log.d(TAG, "Kullanılan API adresi: $baseUrl")
        return baseUrl
    }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(getBaseUrl())
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val productApiService: ProductApiService = retrofit.create(ProductApiService::class.java)
    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
} 