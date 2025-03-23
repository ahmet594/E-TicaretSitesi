package com.example.mobilsmartwear.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@Entity(tableName = "products")
@TypeConverters(ProductConverters::class)
data class Product(
    @PrimaryKey
    @SerializedName("_id", alternate = ["id"])
    val id: Int,
    
    val name: String,
    val description: String,
    val price: Double,
    
    @SerializedName("imagePath", alternate = ["thumbnail", "imageUrl"])
    val imageUrl: String,
    
    val category: String,
    val subCategory: String? = null,
    
    @SerializedName("inStock", alternate = ["isAvailable"])
    val isAvailable: Boolean = true,
    
    val isNew: Boolean = false,
    val isFeatured: Boolean = false,
    val isOnSale: Boolean = false,
    
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    
    @TypeConverters(ProductConverters::class)
    val sizes: List<String>? = null,
    
    @TypeConverters(ProductConverters::class)
    val colors: List<String>? = null,
    
    val currency: String = "TL",
    val discountPercentage: Double = 0.0,
    
    // Room için JSON formatında saklanacak
    @TypeConverters(ProductConverters::class)
    @SerializedName("additionalData")
    val additionalData: Map<String, String>? = null
) {
    override fun toString(): String {
        return "Product(id=$id, name='$name', price=$price, category='$category')"
    }
}

// Type converter for Room
class ProductConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun stringListToJson(value: List<String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }
    
    @TypeConverter
    fun jsonToStringList(value: String?): List<String>? {
        if (value == null) return null
        val listType: Type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun mapToJson(value: Map<String, String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }
    
    @TypeConverter
    fun jsonToMap(value: String?): Map<String, String>? {
        if (value == null) return null
        val mapType: Type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }
} 