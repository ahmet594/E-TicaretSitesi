package com.example.mobilsmartwear.util

import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.remote.api.OutfitResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Eşleşme sonucu modeli
data class OutfitMatchResult(
    val perfectMatches: List<OutfitResponse> = emptyList(),
    val partialMatches: List<Pair<OutfitResponse, Int>> = emptyList(), // Outfit ve eşleşen ürün sayısı
    val hasResults: Boolean = false,
    val selectedProductCount: Int = 0,
    val problematicProductId: String? = null, // En az eşleşen ürün ID'si
    val perfectMatchesBySeason: Map<Season, List<OutfitResponse>> = emptyMap(), // Mevsime göre gruplu kombinler
    val selectedSeason: Season? = null // Filtrelenen mevsim
)

object CombinationManager {
    private val _selectedProducts = MutableStateFlow<List<Product>>(emptyList())
    val selectedProducts: StateFlow<List<Product>> = _selectedProducts.asStateFlow()
    
    private val _matchResults = MutableStateFlow(OutfitMatchResult())
    val matchResults: StateFlow<OutfitMatchResult> = _matchResults.asStateFlow()
    
    fun addProduct(product: Product): Boolean {
        val currentList = _selectedProducts.value.toMutableList()
        
        // Aynı ID'li ürün varsa ekleme
        if (currentList.any { it.id == product.id }) {
            return false
        }
        
        // combinationCode kontrolü - aynı koddan bir ürün varsa ekleme
        if (product.combinationCode != null) {
            val existingProductWithSameCode = currentList.find { 
                it.combinationCode != null && it.combinationCode == product.combinationCode 
            }
            if (existingProductWithSameCode != null) {
                return false // Aynı kombinasyon kodundan ürün zaten var
            }
        }
        
        currentList.add(product)
        _selectedProducts.value = currentList
        return true
    }
    
    fun removeProduct(productId: String, outfits: List<OutfitResponse>? = null, allProducts: List<Product> = emptyList()) {
        val currentList = _selectedProducts.value.toMutableList()
        currentList.removeAll { it.id == productId }
        _selectedProducts.value = currentList
        
        // Eğer sonuç varsa ve outfits listesi verilmişse, otomatik güncelle
        if (_matchResults.value.hasResults && outfits != null) {
            if (currentList.isNotEmpty()) {
                val currentFilter = _matchResults.value.selectedSeason
                findMatchingOutfitsWithSeasonFilter(outfits, allProducts, currentFilter)
            } else {
                clearResults()
            }
        }
    }
    
    fun clearAll() {
        _selectedProducts.value = emptyList()
        _matchResults.value = OutfitMatchResult()
    }
    
    fun isProductSelected(productId: String): Boolean {
        return _selectedProducts.value.any { it.id == productId }
    }
    
    // Kombinlerin mevsimlerini hesapla ve filtrele
    fun findMatchingOutfitsWithSeasonFilter(allOutfits: List<OutfitResponse>, allProducts: List<Product>, filterSeason: Season? = null) {
        val selectedIds = _selectedProducts.value.map { it.id }
        
        if (selectedIds.isEmpty()) {
            _matchResults.value = OutfitMatchResult()
            return
        }
        
        val perfectMatches = mutableListOf<OutfitResponse>()
        val partialMatches = mutableListOf<Pair<OutfitResponse, Int>>()
        
        // Her ürünün kaç kombinde geçtiğini say
        val productMatchCounts = mutableMapOf<String, Int>()
        selectedIds.forEach { productId ->
            productMatchCounts[productId] = 0
        }
        
        allOutfits.forEach { outfit ->
            val matchCount = outfit.productIds.count { it in selectedIds }
            
            // Bu kombinde hangi seçili ürünler var, onları say
            selectedIds.forEach { selectedId ->
                if (selectedId in outfit.productIds) {
                    productMatchCounts[selectedId] = productMatchCounts[selectedId]!! + 1
                }
            }
            
            when {
                // Tam eşleşme: Seçili ürünlerin hepsi kombinde var
                matchCount == selectedIds.size && selectedIds.all { it in outfit.productIds } -> {
                    perfectMatches.add(outfit)
                }
                // Kısmi eşleşme: En az 1 ürün eşleşiyor
                matchCount > 0 -> {
                    partialMatches.add(outfit to matchCount)
                }
            }
        }
        
        // Kısmi eşleşmeleri eşleşen ürün sayısına göre sırala
        partialMatches.sortByDescending { it.second }
        
        // Problematik ürünü tespit et (tam eşleşme yoksa)
        var problematicProductId: String? = null
        if (perfectMatches.isEmpty() && selectedIds.size > 1) {
            problematicProductId = productMatchCounts.minByOrNull { it.value }?.key
        }
        
        // Kombinleri mevsimlerine göre grupla
        val perfectMatchesBySeason = perfectMatches.groupBy { outfit ->
            val outfitProducts = allProducts.filter { it.id in outfit.productIds }
            SeasonDetector.determineSeason(outfitProducts)
        }
        
        // Mevsim filtreleme varsa uygula
        val filteredPerfectMatches = if (filterSeason != null) {
            perfectMatchesBySeason[filterSeason] ?: emptyList()
        } else {
            perfectMatches
        }
        
        val filteredPerfectMatchesBySeason = if (filterSeason != null) {
            if (perfectMatchesBySeason.containsKey(filterSeason)) {
                mapOf(filterSeason to perfectMatchesBySeason[filterSeason]!!)
            } else {
                emptyMap()
            }
        } else {
            perfectMatchesBySeason
        }
        
        _matchResults.value = OutfitMatchResult(
            perfectMatches = filteredPerfectMatches,
            partialMatches = partialMatches,
            hasResults = filteredPerfectMatches.isNotEmpty() || partialMatches.isNotEmpty(),
            selectedProductCount = selectedIds.size,
            problematicProductId = problematicProductId,
            perfectMatchesBySeason = filteredPerfectMatchesBySeason,
            selectedSeason = filterSeason
        )
    }
    
    // Eski fonksiyonu koruyup yeni fonksiyonu çağırmasını sağla
    fun findMatchingOutfits(allOutfits: List<OutfitResponse>, allProducts: List<Product> = emptyList()) {
        // Eğer allProducts boşsa, sadece outfit'ları geçir ama mevsim filtrelemesi yapmaya çalışma
        if (allProducts.isEmpty()) {
            // Eski sistem için fallback - sadece basit arama
            val selectedIds = _selectedProducts.value.map { it.id }
            
            if (selectedIds.isEmpty()) {
                _matchResults.value = OutfitMatchResult()
                return
            }
            
            val perfectMatches = mutableListOf<OutfitResponse>()
            val partialMatches = mutableListOf<Pair<OutfitResponse, Int>>()
            
            allOutfits.forEach { outfit ->
                val matchCount = outfit.productIds.count { it in selectedIds }
                
                when {
                    matchCount == selectedIds.size && selectedIds.all { it in outfit.productIds } -> {
                        perfectMatches.add(outfit)
                    }
                    matchCount > 0 -> {
                        partialMatches.add(outfit to matchCount)
                    }
                }
            }
            
            _matchResults.value = OutfitMatchResult(
                perfectMatches = perfectMatches,
                partialMatches = partialMatches,
                hasResults = perfectMatches.isNotEmpty() || partialMatches.isNotEmpty(),
                selectedProductCount = selectedIds.size
            )
        } else {
            // Yeni sistem - mevsim filtreleme ile
            findMatchingOutfitsWithSeasonFilter(allOutfits, allProducts, null)
        }
    }
    
    fun clearResults() {
        _matchResults.value = OutfitMatchResult()
    }
} 