package com.example.domain.model

enum class AiModel(
    val id: String,
    val displayName: String,
    val description: String,
    val speed: String,
    val quality: String,
    val languageSupport: String,
    val isDefault: Boolean = false
) {
    T_NEX(
        id = "t-nex",
        displayName = "T-Nex",
        description = "Official THENUX Worker model. Superfast, context-aware AI.",
        speed = "⚡⚡⚡ Super Fast",
        quality = "9.9 / 10",
        languageSupport = "English, Sinhala, Tamil & 50+ languages",
        isDefault = true
    ),
    GPT_4(
        id = "gpt4",
        displayName = "GPT-4",
        description = "High precision GPT-4 API endpoint for complex replies.",
        speed = "⚡⚡ Fast",
        quality = "9.8 / 10",
        languageSupport = "Global Multilingual"
    ),
    GPT_3(
        id = "gpt3",
        displayName = "GPT-3",
        description = "Lightweight & responsive model for quick chat replies.",
        speed = "⚡⚡⚡ Ultra Fast",
        quality = "9.2 / 10",
        languageSupport = "Global Multilingual"
    ),
    GEMINI_SINHALA(
        id = "gemini-sinhala",
        displayName = "Gemini Sinhala",
        description = "Deeply tuned for native Sinhala, Singlish & localized tone.",
        speed = "⚡⚡⚡ Instant",
        quality = "9.7 / 10",
        languageSupport = "Sinhala, Roman Sinhala (Singlish), English"
    );

    companion object {
        fun fromId(id: String): AiModel {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: T_NEX
        }
    }
}

enum class GenerationType(val displayName: String, val description: String) {
    REPLY("Reply", "Generate an intelligent response"),
    TRANSLATE("Translate", "Translate text to target language"),
    REWRITE("Rewrite", "Rephrase and improve clarity"),
    SUMMARIZE("Summarize", "Condense text into key takeaways"),
    GRAMMAR("Grammar Fix", "Fix syntax, spelling, and punctuation"),
    EXPLAIN("Explain", "Explain complex concepts clearly")
}

enum class ToneOption(val displayName: String, val iconName: String) {
    PROFESSIONAL("Professional", "Work"),
    FRIENDLY("Friendly", "Smile"),
    FUNNY("Funny", "Laugh"),
    CASUAL("Casual", "Chat"),
    FORMAL("Formal", "Badge"),
    SHORT("Short", "Lightning"),
    LONG("Long", "Document")
}

enum class SinhalaTone(val displayName: String, val description: String, val promptPrefix: String) {
    FORMAL("Formal Sinhala", "රාජකාරි සහ නිල සිංහල", "Reply in formal, official Sinhala language: "),
    OFFICE("Office & Corporate Sinhala", "කාර්යාලීය සහ නිල සිංහල", "Reply in official Sri Lankan corporate office Sinhala: "),
    FRIENDLY("Friendly Sinhala", "හිතකාමී සහ සරල සිංහල", "Reply in friendly, polite Sinhala language: "),
    GEN_Z_SINGLISH("Gen-Z Singlish", "Gen-Z Singlish (e.g. Bro eka elakiri)", "Reply in ultra-modern Gen-Z Singlish with current Sri Lankan youth slang: "),
    ELDER_RESPECT("Respectful Elder Communication", "වැඩිහිටියන්ට ගෞරවනීය සිංහල", "Reply in deeply respectful, honorific Sinhala suitable for elders and superiors: "),
    BUSINESS("Business Sinhala", "ව්‍යාපාරික සිංහල", "Reply in professional business Sinhala: ")
}

data class WritingStyleProfile(
    val length: String = "Medium", // Concise, Medium, Detailed
    val emojiUsage: String = "Subtle", // None, Subtle, Expressive
    val formality: String = "Balanced", // Casual, Balanced, Strictly Formal
    val sinhalaPreference: String = "Native Sinhala", // Native Sinhala, Singlish, Mixed
    val customPhrases: String = "" // e.g. "Best regards, Thenula"
)

data class CommunicationQualityReport(
    val professionalScore: Int,
    val friendlinessScore: Int,
    val clarityScore: Int,
    val grammarScore: Int,
    val overallGrade: String
)

data class CapturedNotification(
    val id: Long = System.currentTimeMillis(),
    val packageName: String,
    val appName: String,
    val sender: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ScreenshotAnalysisResult(
    val extractedText: String,
    val detectedContext: String,
    val summary: String,
    val suggestedReply: String
)

data class ChatSuggestedReply(
    val toneLabel: String,
    val replyText: String,
    val iconTag: String = "✨"
)

data class ChatContextAnalysisResult(
    val conversationText: String,
    val detectedApp: String = "WhatsApp / Messaging",
    val summary: String,
    val keyPoints: List<String>,
    val detectedIntent: String,
    val detectedTone: String,
    val pendingQuestions: List<String>,
    val suggestedReplies: List<ChatSuggestedReply>
)

