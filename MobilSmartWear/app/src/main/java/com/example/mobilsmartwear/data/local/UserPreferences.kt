package com.example.mobilsmartwear.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.mobilsmartwear.data.model.User
import com.google.gson.Gson

/**
 * Kullanıcı oturum bilgilerini yerel depolamak için SharedPreferences wrapper sınıfı
 */
class UserPreferences(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "user_data"
    }
    
    /**
     * Kimlik doğrulama token'ını kaydeder
     */
    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }
    
    /**
     * Kaydedilmiş token'ı getirir
     */
    fun getToken(): String {
        return sharedPreferences.getString(KEY_TOKEN, "") ?: ""
    }
    
    /**
     * Kullanıcı bilgilerini JSON formatında kaydeder
     */
    fun saveUser(user: User) {
        val userJson = Gson().toJson(user)
        sharedPreferences.edit().putString(KEY_USER, userJson).apply()
    }
    
    /**
     * Kaydedilmiş kullanıcı bilgilerini getirir
     */
    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                Gson().fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Tüm kullanıcı verilerini temizler (çıkış yapma)
     */
    fun clearUserData() {
        sharedPreferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER)
            .apply()
    }
} 