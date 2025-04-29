package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Favori ürünleri yöneten repository sınıfı
 */
class FavoriteRepository(private val productRepository: ProductRepository) {
    
    companion object {
        private const val TAG = "FavoriteRepository"
    }
    
    // Favori ürün ID'lerini bellekte tutuyoruz
    private val _favoriteProductIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteProductIds: StateFlow<Set<String>> = _favoriteProductIds.asStateFlow()
    
    /**
     * Tüm favori ürünleri getir
     */
    fun getFavoriteProducts(): Flow<List<Product>> = favoriteProductIds.map { favoriteIds ->
        favoriteIds.mapNotNull { productId ->
            productRepository.getProductById(productId)
        }
    }
    
    /**
     * Bir ürünün favori olup olmadığını kontrol et
     */
    fun isFavorite(productId: String): Boolean {
        return _favoriteProductIds.value.contains(productId)
    }
    
    /**
     * Ürünü favorilere ekle
     */
    suspend fun addToFavorites(productId: String): Boolean {
        try {
            val currentFavorites = _favoriteProductIds.value.toMutableSet()
            if (!currentFavorites.contains(productId)) {
                currentFavorites.add(productId)
                _favoriteProductIds.value = currentFavorites
                Log.d(TAG, "Ürün favorilere eklendi: $productId")
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Ürün favorilere eklenirken hata oluştu", e)
            return false
        }
    }
    
    /**
     * Ürünü favorilerden çıkar
     */
    suspend fun removeFromFavorites(productId: String): Boolean {
        try {
            val currentFavorites = _favoriteProductIds.value.toMutableSet()
            if (currentFavorites.contains(productId)) {
                currentFavorites.remove(productId)
                _favoriteProductIds.value = currentFavorites
                Log.d(TAG, "Ürün favorilerden çıkarıldı: $productId")
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Ürün favorilerden çıkarılırken hata oluştu", e)
            return false
        }
    }
    
    /**
     * Tüm favorileri temizle
     */
    suspend fun clearFavorites(): Boolean {
        try {
            _favoriteProductIds.value = emptySet()
            Log.d(TAG, "Tüm favoriler temizlendi")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Favoriler temizlenirken hata oluştu", e)
            return false
        }
    }

    fun getFavoriteCount(): StateFlow<Int> {
        return MutableStateFlow(_favoriteProductIds.value.size).asStateFlow()
    }
} 