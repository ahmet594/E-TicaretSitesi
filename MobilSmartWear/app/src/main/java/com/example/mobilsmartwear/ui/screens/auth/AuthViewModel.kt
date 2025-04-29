package com.example.mobilsmartwear.ui.screens.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.User
import com.example.mobilsmartwear.data.repository.AuthRepository
import com.example.mobilsmartwear.data.repository.AuthResult
import com.example.mobilsmartwear.data.repository.UpdateResult
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "AuthViewModel"
    
    private val authRepository: AuthRepository = AppModule.provideAuthRepository(application)
    
    // UI State
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()
    
    // User State
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()
    
    // Address Update State
    private val _addressUpdateState = MutableStateFlow<AddressUpdateState>(AddressUpdateState.Idle)
    val addressUpdateState: StateFlow<AddressUpdateState> = _addressUpdateState.asStateFlow()
    
    init {
        Log.d(TAG, "AuthViewModel initialized")
        
        // Önce yerel bellekten kullanıcı bilgisini hemen yükle
        _userState.value = authRepository.userPreferences.getUser()
        
        // Sonra API'den güncel verileri al
        loadUserIfExists()
    }
    
    private fun loadUserIfExists() {
        viewModelScope.launch {
            authRepository.getCurrentUserWithAddress().collect { user ->
                _userState.value = user
            }
        }
    }
    
    /**
     * Kullanıcı girişi yapar
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Login attempt: $email")
            authRepository.login(email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _authState.value = AuthUiState.Loading
                    }
                    is AuthResult.Success -> {
                        _userState.value = result.user
                        _authState.value = AuthUiState.Success(result.user)
                    }
                    is AuthResult.Error -> {
                        _authState.value = AuthUiState.Error(result.message)
                    }
                }
            }
        }
    }
    
    /**
     * Yeni kullanıcı kaydı yapar
     */
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Register attempt: $email")
            authRepository.register(name, email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _authState.value = AuthUiState.Loading
                    }
                    is AuthResult.Success -> {
                        _userState.value = result.user
                        _authState.value = AuthUiState.Success(result.user)
                    }
                    is AuthResult.Error -> {
                        _authState.value = AuthUiState.Error(result.message)
                    }
                }
            }
        }
    }
    
    /**
     * Kullanıcı adres bilgilerini yeniden yükler
     */
    fun refreshUserWithAddress() {
        viewModelScope.launch {
            Log.d(TAG, "Kullanıcı bilgilerini yenileme (refreshUserWithAddress) başlatılıyor")
            
            try {
                // Önce yerel olarak kaydedilmiş kullanıcı bilgilerini kontrol et
                val cachedUser = authRepository.userPreferences.getUser()
                if (cachedUser != null) {
                    Log.d(TAG, "Önbellekte kullanıcı bilgileri bulundu: ${cachedUser.name}, ${cachedUser.email}")
                    _userState.value = cachedUser
                } else {
                    Log.w(TAG, "Önbellekte kullanıcı bilgisi bulunamadı")
                }
                
                // Token durumunu kontrol et
                val token = authRepository.userPreferences.getToken()
                if (token.isEmpty()) {
                    Log.e(TAG, "Token bulunamadı, API çağrısı yapılmayacak")
                    // Token yoksa ve userState de null ise kullanıcı oturumu açmamış demektir
                    if (_userState.value == null) {
                        Log.e(TAG, "Kullanıcı oturumu açık değil")
                    }
                    return@launch
                }
                
                Log.d(TAG, "Token mevcut, API çağrısı yapılacak: ${token.take(10)}...")
                
                // Ardından API'den güncel verileri al
                try {
                    authRepository.getCurrentUserWithAddress().collect { user ->
                        if (user != null) {
                            Log.d(TAG, "Kullanıcı bilgileri API'den başarıyla alındı: ${user.name}, ${user.email}")
                            _userState.value = user
                        } else {
                            Log.e(TAG, "API'den alınan kullanıcı bilgileri null")
                            // Eğer API'den null gelirse ama önbellekte veri varsa, önbellekteki veri korunur
                            if (_userState.value == null) {
                                Log.e(TAG, "Hem API hem de önbellekte kullanıcı bilgisi yok")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "API çağrısında hata: ${e.message}", e)
                    // API hatası durumunda önbellekteki veri korunur
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kullanıcı bilgilerini yenilerken üst seviye hata: ${e.message}", e)
            }
        }
    }
    
    /**
     * Kullanıcı adresini günceller
     */
    fun updateUserAddress(address: String) {
        viewModelScope.launch {
            Log.d(TAG, "Updating user address: $address")
            authRepository.updateUserAddress(address).collect { result ->
                when (result) {
                    is UpdateResult.Loading -> {
                        _addressUpdateState.value = AddressUpdateState.Loading
                    }
                    is UpdateResult.Success -> {
                        _userState.value = result.user
                        _addressUpdateState.value = AddressUpdateState.Success(result.user)
                    }
                    is UpdateResult.Error -> {
                        _addressUpdateState.value = AddressUpdateState.Error(result.message)
                    }
                }
            }
        }
    }
    
    /**
     * Kullanıcı çıkışı yapar
     */
    fun logout() {
        authRepository.logout()
        _userState.value = null
        _authState.value = AuthUiState.Idle
        _addressUpdateState.value = AddressUpdateState.Idle
    }
    
    /**
     * Kullanıcı giriş yapmış mı kontrol eder
     */
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
    
    /**
     * Hata durumunu temizler
     */
    fun clearError() {
        if (_authState.value is AuthUiState.Error) {
            _authState.value = AuthUiState.Idle
        }
        
        if (_addressUpdateState.value is AddressUpdateState.Error) {
            _addressUpdateState.value = AddressUpdateState.Idle
        }
    }
}

/**
 * Kimlik doğrulama UI durumları
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

/**
 * Adres güncelleme UI durumları
 */
sealed class AddressUpdateState {
    object Idle : AddressUpdateState()
    object Loading : AddressUpdateState()
    data class Success(val user: User) : AddressUpdateState()
    data class Error(val message: String) : AddressUpdateState()
} 