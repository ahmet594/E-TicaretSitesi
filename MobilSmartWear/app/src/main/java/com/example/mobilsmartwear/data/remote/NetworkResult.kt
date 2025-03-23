package com.example.mobilsmartwear.data.remote

/**
 * API sonuçları için sealed sınıf
 */
sealed class NetworkResult<out T> {
    /**
     * Başarılı sonuç
     * @param data Sonuç verisi
     * @param isFromCache Cache'den mi geldi
     */
    data class Success<T>(val data: T, val isFromCache: Boolean = false) : NetworkResult<T>()
    
    /**
     * Hata durumu
     * @param message Hata mesajı
     */
    data class Error(val message: String) : NetworkResult<Nothing>()
    
    /**
     * Yükleniyor durumu
     */
    object Loading : NetworkResult<Nothing>()
    
    /**
     * Başarılı sonucun cache'den gelip gelmediğini kontrol eder
     */
    fun isCachedResult(): Boolean {
        return when(this) {
            is Success -> this.isFromCache
            else -> false
        }
    }
    
    /**
     * Başarılı durumda sonuç verisini döndürür
     */
    fun getDataOrNull(): T? {
        return when(this) {
            is Success -> this.data
            else -> null
        }
    }
    
    /**
     * Başarılı durumda sonuç verisini döndürür, hata durumunda null
     */
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Hata durumunda hata mesajını döndürür, başarılı durumda null
     */
    inline fun onError(action: (String) -> Unit): NetworkResult<T> {
        if (this is Error) action(message)
        return this
    }
} 