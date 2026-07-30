package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.repository.LiveChatData
import com.example.data.repository.LiveChatReaderRepository

class ThenuxChatAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        LiveChatReaderRepository.setServiceActive(true)
        Log.d("ThenuxChatAccessService", "THENUX Context AI Background Chat Reader Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!LiveChatReaderRepository.isAutoReadAllowed.value) return

        val packageName = event.packageName?.toString() ?: return
        if (!isSupportedChatApp(packageName)) return

        val rootNode = rootInActiveWindow ?: return
        try {
            extractAndProcessChatNodes(rootNode, packageName)
        } catch (e: Exception) {
            Log.e("ThenuxChatAccessService", "Error parsing chat node window", e)
        } finally {
            @Suppress("DEPRECATION")
            rootNode.recycle()
        }
    }

    private fun isSupportedChatApp(pkg: String): Boolean {
        return pkg.contains("whatsapp") ||
                pkg.contains("telegram") ||
                pkg.contains("messaging") ||
                pkg.contains("orca") ||
                pkg.contains("gm") ||
                pkg.contains("android.mms")
    }

    private fun getReadableAppName(pkg: String): String {
        return when {
            pkg.contains("whatsapp") -> "WhatsApp"
            pkg.contains("telegram") -> "Telegram"
            pkg.contains("messaging") || pkg.contains("mms") -> "SMS / Messages"
            pkg.contains("orca") -> "Messenger"
            pkg.contains("gm") -> "Gmail"
            else -> "Chat App"
        }
    }

    private fun extractAndProcessChatNodes(rootNode: AccessibilityNodeInfo, packageName: String) {
        val collectedTextNodes = mutableListOf<String>()
        var inputDraftText: String? = null

        fun traverseNode(node: AccessibilityNodeInfo?) {
            if (node == null) return

            // Check for edit text / draft input field
            if (node.isEditable || node.className?.toString()?.contains("EditText", ignoreCase = true) == true) {
                node.text?.toString()?.takeIf { it.isNotBlank() }?.let {
                    inputDraftText = it
                }
            }

            // Collect text
            val text = node.text?.toString()?.trim()
            if (!text.isNullOrBlank() && text.length > 1 && !isSystemOrUiLabel(text)) {
                collectedTextNodes.add(text)
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                traverseNode(child)
                @Suppress("DEPRECATION")
                child?.recycle()
            }
        }

        traverseNode(rootNode)

        if (collectedTextNodes.isEmpty()) return

        val appName = getReadableAppName(packageName)
        val senderName = collectedTextNodes.firstOrNull { it.length in 3..25 && !it.contains(":") } ?: "Chat Contact"

        // Find candidate message strings
        val messageCandidates = collectedTextNodes.filter {
            it.length > 5 && it != senderName && !it.contains("Online", ignoreCase = true) && !it.contains("Type a message", ignoreCase = true)
        }

        val lastReceived = messageCandidates.lastOrNull() ?: collectedTextNodes.lastOrNull() ?: "Conversation active"

        val threadSnippet = messageCandidates.takeLast(6).joinToString("\n")

        val liveChat = LiveChatData(
            packageName = packageName,
            appName = appName,
            senderName = senderName,
            lastMessageReceived = lastReceived,
            isWaitingForReply = inputDraftText.isNullOrBlank(),
            inputDraft = inputDraftText,
            fullThreadSnippet = threadSnippet,
            timestamp = System.currentTimeMillis()
        )

        LiveChatReaderRepository.updateLiveChat(liveChat)
    }

    private fun isSystemOrUiLabel(text: String): Boolean {
        val lower = text.lowercase()
        return lower in listOf("online", "typing...", "yesterday", "today", "whatsapp", "telegram", "chats", "calls", "status", "settings", "search", "send", "message")
    }

    override fun onInterrupt() {
        Log.d("ThenuxChatAccessService", "THENUX Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        LiveChatReaderRepository.setServiceActive(false)
        super.onDestroy()
    }
}
