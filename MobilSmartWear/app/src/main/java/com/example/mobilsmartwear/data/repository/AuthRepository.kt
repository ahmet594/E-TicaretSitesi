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
    val userPreferences = UserPreferences(context)
    
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
        // Token kontrolü
        if (userPreferences.getToken().isEmpty()) {
            emit(null)
            return@flow
        }
        
        // Önce cache'den kullanıcı bilgilerini getir
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
                // Token geçersiz olabilir, temizle
                if (response.code() == 401) {
                    userPreferences.clearUserData()
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kullanıcı profili yüklenirken hata oluştu", e)
        }
    }
    
    /**
     * Adres bilgisi içeren kullanıcı profilini yükler
     * @return Flow<User?> kullanıcı bilgileri ve adresi
     */
    fun getCurrentUserWithAddress(): Flow<User?> = flow {
        // Token kontrolü
        val token = userPreferences.getToken()
        if (token.isEmpty()) {
            Log.e(TAG, "Token bulunamadı, kullanıcı giriş yapmamış")
            emit(null)
            return@flow
        } else {
            Log.d(TAG, "Token bulundu: ${token.take(10)}...")
        }
        
        // Önce cache'den kullanıcı bilgilerini getir
        val cachedUser = userPreferences.getUser()
        if (cachedUser != null) {
            Log.d(TAG, "Önbellekten kullanıcı yüklendi: ${cachedUser.name}, E-posta: ${cachedUser.email}, Adres: ${cachedUser.address ?: "Yok"}")
            emit(cachedUser)
        } else {
            Log.w(TAG, "Önbellekte kullanıcı bilgisi bulunamadı")
        }
        
        try {
            Log.d(TAG, "API çağrısı yapılıyor: getCurrentUser")
            val response = apiService.getCurrentUser()
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Log.d(TAG, "Kullanıcı profili API'den yüklendi: ${user.name}, E-posta: ${user.email}, Adres: ${user.address ?: "Yok"}")
                    userPreferences.saveUser(user)
                    emit(user)
                } else {
                    Log.e(TAG, "API yanıtı başarılı ancak kullanıcı bilgisi null")
                    emit(null)
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Bilinmeyen hata"
                Log.e(TAG, "Kullanıcı profili yüklenemedi: ${response.code()}, Hata: $errorBody")
                // Token geçersiz olabilir, temizle
                if (response.code() == 401) {
                    Log.e(TAG, "Yetkilendirme hatası (401): Token geçersiz olabilir, temizleniyor")
                    userPreferences.clearUserData()
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kullanıcı profili yüklenirken hata oluştu: ${e.message}", e)
            emit(null)
        }
    }
    
    /**
     * Kullanıcı adresini günceller
     * @param address yeni adres bilgisi
     * @return Flow<UpdateResult> güncelleme işlemi sonucu
     */
    fun updateUserAddress(address: String): Flow<UpdateResult> = flow {
        emit(UpdateResult.Loading)
        
        // Token kontrolü
        val token = userPreferences.getToken()
        if (token.isEmpty()) {
            Log.e(TAG, "Token bulunamadı, adres güncellenemiyor")
            emit(UpdateResult.Error("Oturum süresi dolmuş. Lütfen tekrar giriş yapın."))
            return@flow
        }
        
        try {
            Log.d(TAG, "API çağrısı yapılıyor: updateUserAddress - Adres: $address")
            
            val addressMap = mapOf("address" to address)
            val response = apiService.updateUserAddress(addressMap)
            
            if (response.isSuccessful) {
                val updatedUser = response.body()
                if (updatedUser != null) {
                    Log.d(TAG, "Adres güncelleme başarılı: ${updatedUser.address}")
                    userPreferences.saveUser(updatedUser)
                    emit(UpdateResult.Success(updatedUser))
                } else {
                    Log.e(TAG, "Adres güncelleme başarılı ancak yanıt boş")
                    emit(UpdateResult.Error("Adres güncelleme başarılı ancak kullanıcı bilgileri alınamadı"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Adres güncelleme başarısız"
                Log.e(TAG, "Adres güncelleme başarısız: Kod ${response.code()}, Hata: $errorMessage")
                emit(UpdateResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Adres güncelleme sırasında hata oluştu: ${e.message}", e)
            emit(UpdateResult.Error("Adres güncelleme sırasında bir hata oluştu: ${e.message}"))
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
        val user = userPreferences.getUser()
        Log.d(TAG, "getCurrentUserId: User = ${user?.name}, ID = ${user?.id}")
        
        if (user == null) {
            Log.e(TAG, "HATA: Kullanıcı bilgisi bulunamadı")
            return null
        }
        
        if (user.name.isNullOrEmpty()) {
            Log.e(TAG, "HATA: Kullanıcının ismi boş")
            return null
        }
        
        // Token kontrolü de yapalım
        val token = userPreferences.getToken()
        if (token.isEmpty()) {
            Log.e(TAG, "HATA: Token bulunamadı, oturum sonlanmış olabilir")
            return null
        }
        
        Log.d(TAG, "Geçerli kullanıcı ID'si başarıyla alındı: ${user.id}, İsim: ${user.name}")
        return user.id
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

/**
 * Kullanıcı bilgisi güncelleme işlemlerinin sonuçları için sealed class
 */
sealed class UpdateResult {
    object Loading : UpdateResult()
    data class Success(val user: User) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
} 