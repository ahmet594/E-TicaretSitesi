package com.example.mobilsmartwear.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room veritabanı için tip dönüştürücüler
 */
class Converters {
    private val gson = Gson()
    
    /**
     * String listesini JSON'a dönüştürür
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) {
            ""
        } else {
            gson.toJson(value)
        }
    }
    
    /**
     * JSON'ı String listesine dönüştürür
     */
    @TypeConverter
    fun toStringList(value: String): List<String>? {
        if (value.isBlank()) {
            return null
        }
        
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
} 