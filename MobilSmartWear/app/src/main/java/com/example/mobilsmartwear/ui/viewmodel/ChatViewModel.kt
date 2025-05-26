package com.example.mobilsmartwear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilsmartwear.data.model.ChatMessage
import com.example.mobilsmartwear.data.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    
    private val chatbotRepository = ChatbotRepository()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Hoş geldin mesajı
        addBotMessage("Merhaba! 😊 SmartWear asistanıyım. Size nasıl yardımcı olabilirim?\n\n🛍️ Ürün önerileri\n👔 Kombin tavsiyeleri\n📏 Beden rehberi\n✨ Stil ipuçları")
    }
    
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return
        
        // Kullanıcı mesajını ekle
        addUserMessage(userMessage.trim())
        
        // Bot cevabını al
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = chatbotRepository.sendMessage(userMessage.trim())
                result.onSuccess { botResponse ->
                    addBotMessage(botResponse)
                }.onFailure { exception ->
                    _error.value = exception.message
                    addBotMessage("Üzgünüm, şu anda bir sorun yaşıyorum. 😔 Lütfen tekrar deneyin.")
                }
            } catch (e: Exception) {
                _error.value = e.message
                addBotMessage("Bağlantı sorunu yaşıyorum. İnternet bağlantınızı kontrol edin. 📡")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun addUserMessage(message: String) {
        val userMessage = ChatMessage(
            message = message,
            isUser = true
        )
        _messages.value = _messages.value + userMessage
    }
    
    private fun addBotMessage(message: String) {
        val botMessage = ChatMessage(
            message = message,
            isUser = false
        )
        _messages.value = _messages.value + botMessage
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearChat() {
        _messages.value = emptyList()
        // Hoş geldin mesajını tekrar ekle
        addBotMessage("Merhaba! 😊 SmartWear asistanıyım. Size nasıl yardımcı olabilirim?\n\n🛍️ Ürün önerileri\n👔 Kombin tavsiyeleri\n📏 Beden rehberi\n✨ Stil ipuçları")
    }
} 