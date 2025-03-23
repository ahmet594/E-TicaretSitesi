package com.example.mobilsmartwear.util

import com.example.mobilsmartwear.data.model.Product
import java.text.NumberFormat
import java.util.Locale

/**
 * Ürün fiyatını formatlar
 */
fun Product.formatPrice(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = 2
    
    return format.format(price).replace("₺", "TL ")
} 