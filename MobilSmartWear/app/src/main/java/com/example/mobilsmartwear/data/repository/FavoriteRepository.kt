package com.example.mobilsmartwear.data.repository

import android.util.Log
import com.example.mobilsmartwear.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Favori ürünleri yöneten repository sınıfı
 */
class FavoriteRepository(private val productRepository: ProductRepository) {
    
    companion object {
        private const val TAG = "FavoriteRepository"
    }
    
    // Favori ürün ID'lerini bellekte tutuyoruz
    private val favoriteIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    
    /**
     * Tüm favori ürünleri getir
     */
    fun getFavoriteProducts(): Flow<List<Product>> = favoriteIdsFlow.map { favoriteIds ->
        favoriteIds.mapNotNull { productId ->
            productRepository.getProductById(productId)
        }
    }
    
    /**
     * Bir ürünün favori olup olmadığını kontrol et
     */
    fun isFavorite(productId: String): Flow<Boolean> = favoriteIdsFlow.map { favoriteIds ->
        favoriteIds.contains(productId)
    }
    
    /**
     * Ürünü favorilere ekle/çıkar
     */
    suspend fun toggleFavorite(productId: String): Boolean {
        try {
            val currentFavorites = favoriteIdsFlow.value
            val updatedFavorites = if (currentFavorites.contains(productId)) {
                // Favorilerden çıkar
                currentFavorites.minus(productId)
            } else {
                // Favorilere ekle
                currentFavorites.plus(productId)
            }
            
            favoriteIdsFlow.value = updatedFavorites
            
            val action = if (updatedFavorites.contains(productId)) "eklendi" else "çıkarıldı"
            Log.d(TAG, "Ürün favorilerden $action: $productId")
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Favori durumu değiştirilirken hata oluştu", e)
            return false
        }
    }
    
    /**
     * Ürünü favorilere ekle
     */
    suspend fun addToFavorites(productId: String): Boolean {
        try {
            val currentFavorites = favoriteIdsFlow.value
            if (!currentFavorites.contains(productId)) {
                favoriteIdsFlow.value = currentFavorites.plus(productId)
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
            val currentFavorites = favoriteIdsFlow.value
            if (currentFavorites.contains(productId)) {
                favoriteIdsFlow.value = currentFavorites.minus(productId)
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
            favoriteIdsFlow.value = emptySet()
            Log.d(TAG, "Tüm favoriler temizlendi")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Favoriler temizlenirken hata oluştu", e)
            return false
        }
    }
} 