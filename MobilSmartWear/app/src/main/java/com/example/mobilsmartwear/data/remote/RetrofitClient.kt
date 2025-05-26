package com.example.mobilsmartwear.data.remote

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.mobilsmartwear.data.local.UserPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * API istekleri için Retrofit istemcisi
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // Backend API adresleri
    private const val BASE_URL = "http://192.168.43.3:3000/api/"
    private const val BASE_IMAGE_URL = "http://192.168.43.3:3000"
    private const val TIMEOUT_SECONDS = 30L
    
    private var userPreferences: UserPreferences? = null
    
    /**
     * RetrofitClient başlatma - uygulama başlangıcında çağrılmalı
     */
    fun init(context: Context) {
        userPreferences = UserPreferences(context)
        Log.d(TAG, "RetrofitClient başlatıldı, token durumu: ${userPreferences?.getToken()?.isNotEmpty() ?: false}")
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Token ve yetkilendirme başlığı ekleyen interceptor
    private class AuthInterceptor(private val userPrefs: UserPreferences?) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            
            // Önce UserPreferences'den token almaya çalış
            var token = userPrefs?.getToken() ?: ""
            
            // Token boşsa, TokenManager'dan almaya çalış (yedek plan)
            if (token.isEmpty()) {
                token = TokenManager.getToken() ?: ""
            }
            
            return if (token.isNotEmpty()) {
                Log.d("RetrofitClient", "Requests Headers'a Authorization token ekleniyor")
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .method(original.method, original.body)
                
                val request = requestBuilder.build()
                chain.proceed(request)
            } else {
                Log.d("RetrofitClient", "Token bulunamadı, Authorization header'ı eklenmedi")
                chain.proceed(original)
            }
        }
    }
    
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(userPreferences)) // Auth interceptor ekle
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
    
    // Resim URL'lerini oluşturmak için yardımcı metod
    fun getImageUrl(imagePath: String): String {
        Log.d(TAG, "Converting image path: $imagePath")
        return when {
            imagePath.startsWith("http") -> imagePath
            imagePath.startsWith("/") -> "$BASE_IMAGE_URL$imagePath"
            else -> "$BASE_IMAGE_URL/$imagePath"
        }.also { 
            Log.d(TAG, "Converted to: $it")
        }
    }
} 