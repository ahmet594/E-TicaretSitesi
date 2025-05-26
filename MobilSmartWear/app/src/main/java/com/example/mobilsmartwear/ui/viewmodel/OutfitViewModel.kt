package com.example.mobilsmartwear.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.remote.api.OutfitResponse
import com.example.mobilsmartwear.data.repository.OutfitRepository
import com.example.mobilsmartwear.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OutfitViewModel : ViewModel() {
    private val TAG = "OutfitViewModel"
    private val outfitRepository = OutfitRepository()
    private val productRepository = ProductRepository()

    private val _outfits = MutableStateFlow<List<OutfitResponse>>(emptyList())
    val outfits: StateFlow<List<OutfitResponse>> = _outfits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Ürün önbelleği
    private val productCache = mutableMapOf<String, Product>()

    init {
        loadOutfits()
    }

    fun loadOutfits() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d(TAG, "Kombinler yükleniyor...")
                outfitRepository.getAllOutfits()
                    .onSuccess { outfitList ->
                        Log.d(TAG, "Kombinler başarıyla yüklendi: ${outfitList.size} adet")
                        _outfits.value = outfitList
                        // Kombinlerdeki ürünleri önbelleğe al
                        preloadProducts(outfitList)
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Kombinler yüklenirken hata:", exception)
                        _error.value = exception.message ?: "Kombinler yüklenirken bir hata oluştu"
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun preloadProducts(outfits: List<OutfitResponse>) {
        viewModelScope.launch {
            val productIds = outfits.flatMap { it.productIds }.distinct()
            Log.d(TAG, "Ürünler önbelleğe alınıyor: ${productIds.size} adet")
            
            productIds.forEach { productId ->
                try {
                    productRepository.getProductById(productId)?.let { product ->
                        productCache[productId] = product
                        Log.d(TAG, "Ürün önbelleğe alındı: ${product.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ürün önbelleğe alınırken hata: $productId", e)
                }
            }
        }
    }

    fun getProductsForOutfit(outfit: OutfitResponse): List<Product> {
        return outfit.productIds.mapNotNull { productId ->
            productCache[productId] ?: run {
                Log.w(TAG, "Ürün önbellekte bulunamadı: $productId")
                null
            }
        }
    }
    
    fun getAllCachedProducts(): List<Product> {
        return productCache.values.toList()
    }
} 