package com.example.mobilsmartwear.model

data class Outfit(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val items: List<String>
) 