package com.example.mobilsmartwear.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * JWT token yönetimi için yardımcı sınıf
 */
object TokenManager {
    private const val TAG = "TokenManager"
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    
    private lateinit var prefs: SharedPreferences
    
    /**
     * SharedPreferences'ı başlat
     */
    fun init(context: Context) {
        Log.d(TAG, "Initializing TokenManager")
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Token kaydet
     */
    fun saveToken(token: String) {
        Log.d(TAG, "Saving token (${token.take(15)}...)")
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }
    
    /**
     * Token ve kullanıcı bilgilerini kaydet
     */
    fun saveToken(token: String, userId: String, userName: String? = null, email: String? = null) {
        Log.d(TAG, "Saving token (${token.take(15)}...) and user info")
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, userId)
            userName?.let { putString(KEY_USER_NAME, it) }
            email?.let { putString(KEY_USER_EMAIL, it) }
            apply()
        }
    }
    
    /**
     * Token'ı getir
     */
    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        Log.d(TAG, "Getting token: ${if (token != null) "Token exists" else "No token"}")
        return token
    }
    
    /**
     * Kullanıcı ID'sini getir
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Kullanıcı adını getir
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Kullanıcı e-postasını getir
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Token'ı sil
     */
    fun clearToken() {
        Log.d(TAG, "Clearing token")
        prefs.edit().remove(KEY_TOKEN).apply()
    }
    
    /**
     * Tüm oturum bilgilerini temizle
     */
    fun clearSession() {
        Log.d(TAG, "Clearing entire session")
        prefs.edit().clear().apply()
    }
    
    /**
     * Kullanıcı oturum açmış mı?
     */
    fun isLoggedIn(): Boolean {
        val isLoggedIn = !getToken().isNullOrEmpty()
        Log.d(TAG, "Checking login status: $isLoggedIn")
        return isLoggedIn
    }
} 