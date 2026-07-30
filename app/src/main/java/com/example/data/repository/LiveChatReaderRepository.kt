package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveChatData(
    val packageName: String,
    val appName: String,
    val senderName: String,
    val lastMessageReceived: String,
    val isWaitingForReply: Boolean = true,
    val inputDraft: String? = null,
    val fullThreadSnippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

object LiveChatReaderRepository {

    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    private val _isAutoReadAllowed = MutableStateFlow(true)
    val isAutoReadAllowed: StateFlow<Boolean> = _isAutoReadAllowed.asStateFlow()

    private val _liveCapturedChat = MutableStateFlow<LiveChatData?>(null)
    val liveCapturedChat: StateFlow<LiveChatData?> = _liveCapturedChat.asStateFlow()

    private val _liveAnalysisResult = MutableStateFlow<com.example.domain.model.ChatContextAnalysisResult?>(null)
    val liveAnalysisResult: StateFlow<com.example.domain.model.ChatContextAnalysisResult?> = _liveAnalysisResult.asStateFlow()

    fun setServiceActive(active: Boolean) {
        _isServiceActive.value = active
    }

    fun setAutoReadAllowed(allowed: Boolean) {
        _isAutoReadAllowed.value = allowed
    }

    fun setAnalysisResult(result: com.example.domain.model.ChatContextAnalysisResult?) {
        _liveAnalysisResult.value = result
    }

    fun updateLiveChat(chat: LiveChatData) {
        if (_isAutoReadAllowed.value) {
            _liveCapturedChat.value = chat
        }
    }

    fun clearLiveChat() {
        _liveCapturedChat.value = null
        _liveAnalysisResult.value = null
    }

    fun injectSampleWhatsAppChat() {
        val sample = LiveChatData(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            senderName = "Sahan Perera",
            lastMessageReceived = "Hi, can you send me the updated project deadline and report before 5 PM?",
            isWaitingForReply = true,
            inputDraft = "",
            fullThreadSnippet = "Sahan Perera: Hi, can you send me the updated project deadline and report before 5 PM?\nYou: Checking with the team now.\nSahan Perera: Great! Let me know if you need the draft design file as well."
        )
        _liveCapturedChat.value = sample
    }
}
