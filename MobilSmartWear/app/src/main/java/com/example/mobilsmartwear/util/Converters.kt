package com.example.mobilsmartwear.util

import androidx.room.TypeConverter
import com.example.mobilsmartwear.data.model.OrderItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class OrderItemsConverter {
    @TypeConverter
    fun fromOrderItemList(items: List<OrderItem>?): String? {
        return Gson().toJson(items)
    }

    @TypeConverter
    fun toOrderItemList(itemsString: String?): List<OrderItem>? {
        if (itemsString.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<OrderItem>>() {}.type
        return Gson().fromJson(itemsString, listType)
    }
}

class ProductConverters {
    @TypeConverter
    fun fromAdditionalDataMap(map: Map<String, String>?): String? {
        if (map == null) return null
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toAdditionalDataMap(mapString: String?): Map<String, String>? {
        if (mapString.isNullOrEmpty()) return emptyMap()
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(mapString, mapType)
    }
} 