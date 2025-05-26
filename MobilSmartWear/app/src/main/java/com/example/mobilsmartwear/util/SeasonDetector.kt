package com.example.mobilsmartwear.util

import com.example.mobilsmartwear.data.model.Product

enum class Season(val displayName: String, val emoji: String) {
    WINTER("Kış", "❄️"),
    SPRING("Bahar", "🌸"),
    SUMMER("Yaz", "☀️")
}

object SeasonDetector {
    
    // Kış kelimelerı
    private val winterKeywords = listOf(
        "bot", "mont", "kaban", "parka", "trençkot", "trenckot", 
        "palto", "coat", "kışlık"
    )
    
    // Bahar kelimeleri
    private val springKeywords = listOf(
        "hırka", "hirka", "sweatshirt", "ceket", "yelek"
    )
    
    /**
     * Ürün listesine göre mevsim belirler
     * Web sitesindeki öncelik sırasını takip eder:
     * 1. KIŞ ÖNCELİĞİ ❄️
     * 2. BAHAR ÖNCELİĞİ 🌸  
     * 3. ÖZEL KOD ÖNCELİĞİ 🏷️ (combinationCode = 5)
     * 4. KAZAK KATEGORİ ÖNCELİĞİ 🧶 (subcategory = kazak)
     * 5. VARSAYILAN ☀️ (YAZ)
     */
    fun determineSeason(products: List<Product>): Season {
        // 1️⃣ KIŞ ÖNCELİĞİ - En yüksek öncelik
        for (product in products) {
            val productName = product.name.lowercase()
            if (winterKeywords.any { keyword -> productName.contains(keyword) }) {
                return Season.WINTER
            }
        }
        
        // 2️⃣ BAHAR ÖNCELİĞİ - İkinci öncelik
        for (product in products) {
            val productName = product.name.lowercase()
            if (springKeywords.any { keyword -> productName.contains(keyword) }) {
                return Season.SPRING
            }
        }
        
        // 3️⃣ ÖZEL KOD ÖNCELİĞİ - Üçüncü öncelik
        for (product in products) {
            if (product.combinationCode == "5" || product.combinationCode == 5.toString()) {
                return Season.SPRING
            }
        }
        
        // 4️⃣ KAZAK KATEGORİ ÖNCELİĞİ - Dördüncü öncelik
        for (product in products) {
            if (product.subcategory?.lowercase() == "kazak") {
                return Season.SPRING
            }
        }
        
        // 5️⃣ VARSAYILAN - Son öncelik
        return Season.SUMMER
    }
    
    /**
     * Tek ürün için mevsim belirler
     */
    fun determineProductSeason(product: Product): Season {
        return determineSeason(listOf(product))
    }
    
    /**
     * Mevsim ikonu ve adını birlikte döndürür
     */
    fun getSeasonDisplay(season: Season): String {
        return "${season.emoji} ${season.displayName}"
    }
} 