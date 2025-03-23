package com.example.mobilsmartwear.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("_id", alternate = ["id"])
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    val email: String,
    
    val isLoggedIn: Boolean = false,
    
    // İsteğe bağlı alanlar
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val district: String? = null,
    val postalCode: String? = null,
    val profileImage: String? = null,
    val createdAt: String? = null
) 