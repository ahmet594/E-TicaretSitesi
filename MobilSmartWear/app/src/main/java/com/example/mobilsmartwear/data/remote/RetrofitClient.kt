package com.example.mobilsmartwear.data.remote

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // API Base URLs
    private const val BASE_URL = "https://ap-southeast-1.aws.data.mongodb-api.com/app/data-rvdnw/endpoint/data/v1/" // MongoDB Data API
    private const val BASE_URL_DUMMY = "https://dummyjson.com/"
    
    // API tipi seçimi - gerçek MongoDB API kullanmak için true yapıyoruz
    private const val USE_REAL_API = true
    
    // Active base URL
    private val activeBaseUrl = if (USE_REAL_API) BASE_URL else BASE_URL_DUMMY
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // MongoDB Data API key interceptor
    class MongoDBApiKeyInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            
            // MongoDB Data API key ekleniyor
            val request = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Access-Control-Request-Headers", "*")
                .header("api-key", "VKEoOjdTTzXZGkzZfnPo3UfI9OTnMrATihCpwYXYWKGnuFb8LU15k9i35W4AkWOs")
                .method(original.method, original.body)
                .build()
            
            return chain.proceed(request)
        }
    }
    
    // SafeCall Interceptor for better error handling
    class SafeCallInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            
            Log.d(TAG, "Request: ${request.method} ${request.url}")
            try {
                val response = chain.proceed(request)
                Log.d(TAG, "Response code: ${response.code}")
                return response
            } catch (e: Exception) {
                Log.e(TAG, "Network error during API call", e)
                throw e
            }
        }
    }
    
    // Auth Interceptor for attaching token to requests
    class AuthInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            
            // Get token from TokenManager
            val token = TokenManager.getToken()
            
            // If token exists and URL is not for authentication, add Authorization header
            val request = if (token != null && !original.url.toString().contains("/auth/login")) {
                Log.d(TAG, "Adding Authorization header to: ${original.url}")
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .method(original.method, original.body)
                    .build()
            } else {
                original
            }
            
            return chain.proceed(request)
        }
    }
    
    // OkHttp Client
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(SafeCallInterceptor())
        .addInterceptor(AuthInterceptor())
        .addInterceptor(MongoDBApiKeyInterceptor()) // MongoDB için API key interceptor'ı eklendi
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    // Gson
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        try {
            Log.d(TAG, "Creating Retrofit instance with base URL: $activeBaseUrl")
            Retrofit.Builder()
                .baseUrl(activeBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Retrofit instance", e)
            throw e
        }
    }
    
    // MongoDB API Service
    val mongoDBApiService: MongoDBApiService by lazy {
        try {
            Log.d(TAG, "Creating MongoDBApiService instance")
            retrofit.create(MongoDBApiService::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating MongoDBApiService", e)
            throw e
        }
    }
    
    // General API Service
    val apiService: ApiService by lazy {
        try {
            Log.d(TAG, "Creating ApiService instance")
            retrofit.create(ApiService::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating ApiService", e)
            throw e
        }
    }
} 