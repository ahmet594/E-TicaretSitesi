package com.example.mobilsmartwear.ui.screens.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.User
import com.example.mobilsmartwear.data.repository.AuthRepository
import com.example.mobilsmartwear.data.repository.AuthResult
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
    
    init {
        Log.d(TAG, "AuthViewModel initialized")
        // Kayıtlı kullanıcı varsa yükle
        loadUserIfExists()
    }
    
    private fun loadUserIfExists() {
        viewModelScope.launch {
            if (authRepository.isUserLoggedIn()) {
                authRepository.getUserProfile().collect { user ->
                    _userState.value = user
                }
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
     * Kullanıcı çıkışı yapar
     */
    fun logout() {
        authRepository.logout()
        _userState.value = null
        _authState.value = AuthUiState.Idle
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