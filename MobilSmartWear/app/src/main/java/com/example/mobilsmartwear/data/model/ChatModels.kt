package com.example.mobilsmartwear.data.model

data class ChatRequest(
    val model: String = "openai/gpt-3.5-turbo",
    val messages: List<ChatMessageRequest>,
    val max_tokens: Int = 500,
    val temperature: Double = 0.7
)

data class ChatMessageRequest(
    val role: String, // "user", "assistant", "system"
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val content: String
) 