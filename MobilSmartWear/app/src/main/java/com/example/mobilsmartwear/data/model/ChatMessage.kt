package com.example.mobilsmartwear.data.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) 