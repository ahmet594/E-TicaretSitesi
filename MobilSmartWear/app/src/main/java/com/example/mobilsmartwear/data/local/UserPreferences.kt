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
        val token = sharedPreferences.getString(KEY_TOKEN, "") ?: ""
        if (token.isEmpty()) {
            android.util.Log.w("UserPreferences", "Kaydedilmiş token bulunamadı, kullanıcı giriş yapmamış olabilir.")
        } else {
            android.util.Log.d("UserPreferences", "Token bulundu (${token.take(10)}...)")
        }
        return token
    }
    
    /**
     * Kullanıcı bilgilerini JSON formatında kaydeder
     */
    fun saveUser(user: User) {
        val userJson = Gson().toJson(user)
        sharedPreferences.edit().putString(KEY_USER, userJson).apply()
        android.util.Log.d("UserPreferences", "Kullanıcı bilgileri kaydedildi: ID=${user.id}, Name=${user.name}")
    }
    
    /**
     * Kaydedilmiş kullanıcı bilgilerini getirir
     */
    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        if (userJson == null) {
            android.util.Log.w("UserPreferences", "Kaydedilmiş kullanıcı bilgisi bulunamadı")
            return null
        }
        
        return try {
            val user = Gson().fromJson(userJson, User::class.java)
            android.util.Log.d("UserPreferences", "Kullanıcı bilgileri alındı: ID=${user.id}, Name=${user.name}")
            user
        } catch (e: Exception) {
            android.util.Log.e("UserPreferences", "Kullanıcı bilgileri JSON formatından çözülemedi", e)
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