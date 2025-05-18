package com.example.mobilsmartwear.repository

import android.content.Context
import com.example.mobilsmartwear.model.Outfit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class OutfitRepository(private val context: Context) {
    private val gson = Gson()

    suspend fun getOutfits(): List<Outfit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("outfits.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, List<Outfit>>>() {}.type
            val response: Map<String, List<Outfit>> = gson.fromJson(jsonString, type)
            response["outfits"] ?: emptyList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
} 