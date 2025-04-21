package com.example.mobilsmartwear.data.repository

import android.content.Context
import android.util.Log
import com.example.mobilsmartwear.data.local.UserPreferences
import com.example.mobilsmartwear.data.model.AuthRequest
import com.example.mobilsmartwear.data.model.AuthResponse
import com.example.mobilsmartwear.data.model.RegisterRequest
import com.example.mobilsmartwear.data.model.User
import com.example.mobilsmartwear.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Kimlik doğrulama işlemleri için repository
 */
class AuthRepository(private val context: Context) {
    
    private val apiService = RetrofitClient.apiService
    private val userPreferences = UserPreferences(context)
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    /**
     * Kullanıcı girişi yapar
     * @param email kullanıcı email adresi
     * @param password kullanıcı şifresi
     * @return Flow<AuthResult> giriş işleminin sonucu
     */
    fun login(email: String, password: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        
        try {
            Log.d(TAG, "API çağrısı yapılıyor: login")
            
            val authRequest = AuthRequest(email, password)
            val response = apiService.login(authRequest)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d(TAG, "Giriş başarılı: ${authResponse.user.name}")
                    
                    // Token ve kullanıcı bilgilerini kaydet
                    userPreferences.saveToken(authResponse.token)
                    userPreferences.saveUser(authResponse.user)
                    
                    emit(AuthResult.Success(authResponse.user))
                } else {
                    Log.e(TAG, "Giriş başarılı ancak yanıt boş")
                    emit(AuthResult.Error("Giriş başarılı ancak kullanıcı bilgileri alınamadı"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Giriş başarısız"
                Log.e(TAG, "Giriş başarısız: $errorMessage")
                emit(AuthResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Giriş sırasında hata oluştu", e)
            emit(AuthResult.Error("Giriş sırasında bir hata oluştu: ${e.message}"))
        }
    }
    
    /**
     * Yeni kullanıcı kaydı yapar
     * @param name kullanıcı adı
     * @param email kullanıcı email adresi
     * @param password kullanıcı şifresi
     * @return Flow<AuthResult> kayıt işleminin sonucu
     */
    fun register(name: String, email: String, password: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        
        try {
            Log.d(TAG, "API çağrısı yapılıyor: register")
            
            val registerRequest = RegisterRequest(name, email, password)
            val response = apiService.register(registerRequest)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d(TAG, "Kayıt başarılı: ${authResponse.user.name}")
                    
                    // Token ve kullanıcı bilgilerini kaydet
                    userPreferences.saveToken(authResponse.token)
                    userPreferences.saveUser(authResponse.user)
                    
                    emit(AuthResult.Success(authResponse.user))
                } else {
                    Log.e(TAG, "Kayıt başarılı ancak yanıt boş")
                    emit(AuthResult.Error("Kayıt başarılı ancak kullanıcı bilgileri alınamadı"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Kayıt başarısız"
                Log.e(TAG, "Kayıt başarısız: $errorMessage")
                emit(AuthResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kayıt sırasında hata oluştu", e)
            emit(AuthResult.Error("Kayıt sırasında bir hata oluştu: ${e.message}"))
        }
    }
    
    /**
     * Kullanıcının oturum açık mı kontrol eder
     * @return Boolean - kullanıcı giriş yapmışsa true, aksi halde false
     */
    fun isUserLoggedIn(): Boolean {
        return userPreferences.getToken().isNotEmpty()
    }
    
    /**
     * Kullanıcı profilini yükler
     * @return Flow<User?> kullanıcı bilgileri
     */
    fun getUserProfile(): Flow<User?> = flow {
        val cachedUser = userPreferences.getUser()
        if (cachedUser != null) {
            emit(cachedUser)
        }
        
        try {
            Log.d(TAG, "API çağrısı yapılıyor: getUserProfile")
            val response = apiService.getUserProfile()
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Log.d(TAG, "Kullanıcı profili yüklendi: ${user.name}")
                    userPreferences.saveUser(user)
                    emit(user)
                }
            } else {
                Log.e(TAG, "Kullanıcı profili yüklenemedi: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kullanıcı profili yüklenirken hata oluştu", e)
        }
    }
    
    /**
     * Kullanıcı çıkışı yapar
     */
    fun logout() {
        userPreferences.clearUserData()
    }
    
    /**
     * Geçerli kullanıcı kimliğini döndürür
     * @return String? - kullanıcı ID'si, yoksa null
     */
    fun getCurrentUserId(): String? {
        return userPreferences.getUser()?.id
    }
}

/**
 * Kimlik doğrulama işlemlerinin sonuçları için sealed class
 */
sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
} 