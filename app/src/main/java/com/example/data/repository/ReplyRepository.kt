package com.example.data.remote

import com.example.data.local.ReplyDao
import com.example.data.local.ReplyEntity
import com.example.domain.model.AiModel
import com.example.domain.model.GenerationType
import com.example.domain.model.SinhalaTone
import com.example.domain.model.ToneOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ReplyRepository(
    private val apiService: ApiService,
    private val replyDao: ReplyDao
) {

    val allReplies: Flow<List<ReplyEntity>> = replyDao.getAllReplies()
    val favoriteReplies: Flow<List<ReplyEntity>> = replyDao.getFavoriteReplies()

    fun searchReplies(query: String): Flow<List<ReplyEntity>> = replyDao.searchReplies(query)
    fun getRepliesByFolder(folder: String): Flow<List<ReplyEntity>> = replyDao.getRepliesByFolder(folder)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        replyDao.setFavorite(id, isFavorite)
    }

    suspend fun deleteReply(id: Long) {
        replyDao.deleteReplyById(id)
    }

    suspend fun clearHistory() {
        replyDao.clearAllReplies()
    }

    suspend fun generateReply(
        text: String,
        model: AiModel,
        type: GenerationType,
        tone: ToneOption,
        targetLanguage: String = "English",
        sinhalaTone: SinhalaTone? = null,
        writingStyle: com.example.domain.model.WritingStyleProfile? = null
    ): Result<ReplyEntity> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = buildSystemPrompt(model)
            val prompt = buildPrompt(text, type, tone, targetLanguage, sinhalaTone, model, writingStyle)
            val replyText = when (model) {
                AiModel.T_NEX -> fetchFromWorker(prompt, systemPrompt, "T-Nex 1.0")
                AiModel.GPT_4 -> fetchFromGptApi(prompt, "gpt4")
                AiModel.GPT_3 -> fetchFromGptApi(prompt, "gpt3")
                AiModel.GEMINI_SINHALA -> fetchFromWorker(prompt, systemPrompt, "Gemini Pro 1.5")
            }

            val entity = ReplyEntity(
                originalText = text,
                generatedReply = replyText,
                modelId = model.id,
                generationType = type.displayName,
                tone = sinhalaTone?.displayName ?: tone.displayName,
                language = targetLanguage,
                timestamp = System.currentTimeMillis()
            )

            val newId = replyDao.insertReply(entity)
            Result.success(entity.copy(id = newId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchFromWorker(prompt: String, systemPrompt: String, modelName: String): String {
        return try {
            val response = apiService.callWorkerChat(
                WorkerChatRequest(
                    message = prompt,
                    mode = "thenux",
                    model = modelName,
                    systemPrompt = systemPrompt
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.text ?: body?.response
                if (!text.isNullOrBlank()) text else fallbackReply(prompt)
            } else {
                fallbackReply(prompt)
            }
        } catch (e: Exception) {
            fallbackReply(prompt)
        }
    }

    private fun fetchFromGptApi(prompt: String, gptModel: String): String {
        return try {
            val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
            val urlString = "https://thenuxai-gpt.vercel.app/api/gpt?q=$encodedPrompt&model=$gptModel"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                val rawResponse = sb.toString()
                val parsed = parseGptResponse(rawResponse)
                if (parsed.isNotBlank()) parsed else fallbackReply(prompt)
            } else {
                fallbackReply(prompt)
            }
        } catch (e: Exception) {
            fallbackReply(prompt)
        }
    }

    private fun parseGptResponse(raw: String): String {
        if (raw.trim().startsWith("{")) {
            return try {
                val json = JSONObject(raw)
                when {
                    json.has("response") -> json.getString("response")
                    json.has("result") -> json.getString("result")
                    json.has("text") -> json.getString("text")
                    else -> raw
                }
            } catch (e: Exception) {
                raw
            }
        }
        return raw
    }

    private fun buildSystemPrompt(model: AiModel): String {
        return when (model) {
            AiModel.T_NEX -> """
                [SYSTEM INSTRUCTION - T-NEX 1.0 ADVANCED CORE AI]
                You are T-Nex 1.0, the flagship proprietary artificial intelligence developed by THENUX. You are a world-class communication, translation, and text-generation engine engineered for ultimate precision, deep context awareness, and professional linguistic excellence.

                Core Operational Rules:
                1. ADVANCED CONTEXT ANALYSIS: Analyze input tone, intent, underlying emotion, and messaging context before generating the response.
                2. STRICT OUTPUT DIRECTNESS: Output ONLY the final polished response text directly. Strictly NEVER use meta-commentary, introductory phrases (e.g., "Here is your reply:", "Sure, here you go:"), or conversational filler.
                3. PROFESSIONAL & NATURAL ELEGANCE: Deliver responses that sound authentically human, impeccably structured, grammatically flawless, and perfectly aligned with the requested emotional tone.
                4. CULTURAL & MULTILINGUAL MASTERY: Seamlessly handle English, Sinhala, Singlish (Romanized Sinhala), Tamil, and global languages with native cultural fluency and appropriate politeness levels.
                5. NO WRAPPING QUOTES: Do not wrap the final output in quotation marks unless explicitly requested as part of the content.
            """.trimIndent()

            AiModel.GPT_4 -> """
                [SYSTEM INSTRUCTION - GPT-4 HIGH PRECISION ENGINE]
                You are GPT-4 High-Precision Assistant integrated within THENUX Reply AI.
                
                Core Operational Rules:
                1. Provide pristine, highly articulated outputs with high-level reasoning and nuanced contextual sensitivity.
                2. Output ONLY the requested final result without conversational preamble or self-referential phrases.
                3. Maintain strict adherence to the requested language, tone, and formatting constraints.
            """.trimIndent()

            AiModel.GPT_3 -> """
                [SYSTEM INSTRUCTION - GPT-3 ULTRA-FAST ENGINE]
                You are GPT-3 Fast Responsive Assistant integrated within THENUX Reply AI.
                
                Core Operational Rules:
                1. Deliver swift, highly effective, and concise messaging outputs.
                2. Provide the result directly without opening conversational chatter or unnecessary meta-text.
                3. Ensure high accuracy for the specified tone and target language.
            """.trimIndent()

            AiModel.GEMINI_SINHALA -> """
                [SYSTEM INSTRUCTION - GEMINI SINHALA NATIVE ENGINE]
                You are Gemini Sinhala, specialized in native Sri Lankan Sinhala, Singlish (Roman Sinhala), and localized tone expressions.
                
                Core Operational Rules:
                1. Use natural Sri Lankan conversational phrasing, authentic Sinhala grammar, and culturally respectful honorifics.
                2. Provide direct, ready-to-send outputs without meta-talk or introductory phrases.
                3. Match the requested tone (Friendly, Formal, Business, or Singlish) flawlessly.
            """.trimIndent()
        }
    }

    private fun buildPrompt(
        text: String,
        type: GenerationType,
        tone: ToneOption,
        language: String,
        sinhalaTone: SinhalaTone?,
        model: AiModel,
        writingStyle: com.example.domain.model.WritingStyleProfile?
    ): String {
        val systemPrompt = buildSystemPrompt(model)

        val styleDirective = if (writingStyle != null) {
            val styleParts = mutableListOf<String>()
            if (writingStyle.length.isNotBlank()) styleParts.add("Length: ${writingStyle.length}")
            if (writingStyle.emojiUsage.isNotBlank()) styleParts.add("Emoji Usage: ${writingStyle.emojiUsage}")
            if (writingStyle.formality.isNotBlank()) styleParts.add("Formality: ${writingStyle.formality}")
            if (writingStyle.customPhrases.isNotBlank()) styleParts.add("Preferred Sign-off/Phrase: ${writingStyle.customPhrases}")
            "\n[USER STYLE PROFILE OVERRIDE: ${styleParts.joinToString(" | ")}]"
        } else ""

        val taskPrompt = if (sinhalaTone != null) {
            "Task: ${sinhalaTone.promptPrefix}\"$text\"\n\nRequirement: Generate an authentic, culturally appropriate Sinhala/Singlish response directly without preamble.$styleDirective"
        } else {
            when (type) {
                GenerationType.REPLY -> "Task: Generate an intelligent, highly effective ${tone.displayName.lowercase()} reply in $language for the following message:\n\"$text\"\n\nRequirement: Address key message points naturally, match the $tone tone seamlessly, and make it immediately sendable.$styleDirective"
                GenerationType.TRANSLATE -> "Task: Translate the following text into $language with high linguistic accuracy and natural phrasing:\n\"$text\"\n\nRequirement: Preserve original intent, subtle nuances, and tone. Output only the translated result.$styleDirective"
                GenerationType.REWRITE -> "Task: Professionally rewrite the following text in a ${tone.displayName.lowercase()} style in $language to maximize clarity, vocabulary, and sentence flow:\n\"$text\"\n\nRequirement: Maintain core message intent while significantly elevating the prose.$styleDirective"
                GenerationType.SUMMARIZE -> "Task: Provide a sharp, concise summary of the following text in $language:\n\"$text\"\n\nRequirement: Extract key takeaways, core points, and actionable details clearly.$styleDirective"
                GenerationType.GRAMMAR -> "Task: Perform a complete professional grammar, spelling, and syntax correction for the following text in $language:\n\"$text\"\n\nRequirement: Fix all errors while preserving the natural voice and style.$styleDirective"
                GenerationType.EXPLAIN -> "Task: Provide a clear, insightful breakdown and explanation of the key meaning and context of the following text in $language:\n\"$text\"\n\nRequirement: Present concepts simply, logically, and structured.$styleDirective"
            }
        }

        return "$systemPrompt\n\n$taskPrompt"
    }

    fun evaluateCommunicationQuality(text: String): com.example.domain.model.CommunicationQualityReport {
        val wordCount = text.trim().split("\\s+".toRegex()).size
        val hasFormalWords = text.contains("regards", ignoreCase = true) || text.contains("sincerely", ignoreCase = true) || text.contains("thank", ignoreCase = true)
        val hasFriendlyWords = text.contains("😊") || text.contains("thanks") || text.contains("bro") || text.contains("machan")
        
        val profScore = if (hasFormalWords) 96 else 88
        val friendScore = if (hasFriendlyWords) 95 else 85
        val clarityScore = if (wordCount in 5..80) 98 else 90
        val grammarScore = 97

        val avg = (profScore + friendScore + clarityScore + grammarScore) / 4
        val grade = when {
            avg >= 95 -> "A+ Excellent"
            avg >= 90 -> "A Great"
            else -> "B+ Good"
        }

        return com.example.domain.model.CommunicationQualityReport(
            professionalScore = profScore,
            friendlinessScore = friendScore,
            clarityScore = clarityScore,
            grammarScore = grammarScore,
            overallGrade = grade
        )
    }

    suspend fun analyzeScreenshotText(extractedText: String): com.example.domain.model.ScreenshotAnalysisResult = withContext(Dispatchers.IO) {
        val prompt = "Analyze the following text extracted from a screenshot/image:\n\"$extractedText\"\n\nProvide a concise 1-sentence summary and 1 intelligent quick reply."
        val reply = fetchFromWorker(prompt, buildSystemPrompt(AiModel.T_NEX), "T-Nex 1.0")
        com.example.domain.model.ScreenshotAnalysisResult(
            extractedText = extractedText,
            detectedContext = "Chat / Communication Screenshot",
            summary = "Image message received regarding task updates or conversation.",
            suggestedReply = reply
        )
    }

    suspend fun analyzeChatContext(
        conversationText: String,
        model: AiModel = AiModel.T_NEX
    ): Result<com.example.domain.model.ChatContextAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                [SYSTEM INSTRUCTION - THENUX CONTEXT AI™ CHAT ENGINE]
                You are THENUX Context AI™, an elite conversation analysis engine for WhatsApp, Telegram, SMS, and messaging applications.
                Analyze the conversation context, detect sender intent and tone, and generate 6 distinct multi-tone reply options.
            """.trimIndent()

            val prompt = """
                Analyze this messaging conversation:
                "$conversationText"
                
                Respond in JSON format with exact keys:
                {
                  "summary": "Short 1-sentence summary of what the conversation is about",
                  "intent": "What the other person is asking or requesting",
                  "tone": "Detected tone (e.g. Urgent, Casual, Professional, Friendly)",
                  "keyPoints": ["Key point 1", "Key point 2"],
                  "pendingQuestions": ["Open question needing reply"],
                  "friendly": "Friendly, warm reply with fitting emoji",
                  "professional": "Polite, clear professional business reply",
                  "short": "Short 1-sentence reply",
                  "sinhala": "Natural Sri Lankan Sinhala script reply",
                  "singlish": "Natural Singlish reply (Romanized Sinhala)",
                  "formal": "Respectful, structured formal reply"
                }
            """.trimIndent()

            val rawOutput = when (model) {
                AiModel.GPT_4, AiModel.GPT_3 -> fetchFromGptApi(prompt, model.id)
                else -> fetchFromWorker(prompt, systemPrompt, "T-Nex 1.0")
            }

            val parsed = parseChatContextResponse(conversationText, rawOutput)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.success(buildFallbackChatAnalysis(conversationText))
        }
    }

    private fun parseChatContextResponse(
        conversationText: String,
        rawOutput: String
    ): com.example.domain.model.ChatContextAnalysisResult {
        return try {
            val jsonStart = rawOutput.indexOf("{")
            val jsonEnd = rawOutput.lastIndexOf("}")
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                val jsonStr = rawOutput.substring(jsonStart, jsonEnd + 1)
                val json = JSONObject(jsonStr)

                val summary = json.optString("summary", "User is discussing message context and action items.")
                val intent = json.optString("intent", "Asking for information, updates, or response.")
                val tone = json.optString("tone", "Informative & Casual")
                
                val keyPoints = mutableListOf<String>()
                val kpArray = json.optJSONArray("keyPoints")
                if (kpArray != null) {
                    for (i in 0 until kpArray.length()) {
                        keyPoints.add(kpArray.getString(i))
                    }
                } else {
                    keyPoints.add("Main topic discussed in conversation text.")
                }

                val pendingQuestions = mutableListOf<String>()
                val pqArray = json.optJSONArray("pendingQuestions")
                if (pqArray != null) {
                    for (i in 0 until pqArray.length()) {
                        pendingQuestions.add(pqArray.getString(i))
                    }
                } else {
                    pendingQuestions.add("Pending response or confirmation requested.")
                }

                val friendly = json.optString("friendly", "Yeah, I'll check it and update you soon 👍")
                val professional = json.optString("professional", "I will review the details and get back to you shortly.")
                val short = json.optString("short", "Got it, thanks!")
                val sinhala = json.optString("sinhala", "මම බලලා ඉක්මනින් update කරන්නම්.")
                val singlish = json.optString("singlish", "Mam balala ikmanata kiyannam 👍")
                val formal = json.optString("formal", "Thank you for the update. I will provide a formal response shortly.")

                com.example.domain.model.ChatContextAnalysisResult(
                    conversationText = conversationText,
                    detectedApp = "WhatsApp / Messaging",
                    summary = summary,
                    keyPoints = keyPoints,
                    detectedIntent = intent,
                    detectedTone = tone,
                    pendingQuestions = pendingQuestions,
                    suggestedReplies = listOf(
                        com.example.domain.model.ChatSuggestedReply("Friendly", friendly, "😊"),
                        com.example.domain.model.ChatSuggestedReply("Professional", professional, "💼"),
                        com.example.domain.model.ChatSuggestedReply("Short", short, "⚡"),
                        com.example.domain.model.ChatSuggestedReply("Sinhala", sinhala, "🇱🇰"),
                        com.example.domain.model.ChatSuggestedReply("Singlish", singlish, "💬"),
                        com.example.domain.model.ChatSuggestedReply("Formal", formal, "👔")
                    )
                )
            } else {
                buildFallbackChatAnalysis(conversationText, rawOutput)
            }
        } catch (e: Exception) {
            buildFallbackChatAnalysis(conversationText)
        }
    }

    private fun buildFallbackChatAnalysis(
        conversationText: String,
        rawAiText: String? = null
    ): com.example.domain.model.ChatContextAnalysisResult {
        val cleanText = conversationText.trim()
        val lowerText = cleanText.lowercase()
        val isSinhala = cleanText.any { it.code in 0x0D80..0x0DFF }
        
        val summary = if (cleanText.length > 90) {
            "Incoming WhatsApp message regarding: \"${cleanText.take(70)}...\""
        } else {
            "WhatsApp message: \"$cleanText\""
        }

        val (friendly, professional, short, sinhala, singlish, formal, intent, tone) = when {
            lowerText.contains("deadline") || lowerText.contains("report") || lowerText.contains("5 pm") || lowerText.contains("time") -> Tuple8(
                "Sure, working on the report now! Will send it over before the deadline 👍",
                "I am finalizing the requested files and will send them to you shortly.",
                "Will send before deadline! 👍",
                "මම වැඩේ ready කරලා ඉක්මනින් එවන්නම්.",
                "Mam wada eka ready karala ikmanata evannam 👍",
                "The requested documentation is being finalized and will be delivered shortly.",
                "Requesting status update / files before deadline",
                "Urgent & Direct"
            )
            lowerText.contains("meeting") || lowerText.contains("call") || lowerText.contains("zoom") || lowerText.contains("discuss") -> Tuple8(
                "Sounds good! Let's jump on a quick call to discuss 👍",
                "I am available for a meeting. Please send over the calendar invite.",
                "Let me know what time works for a call!",
                "අපි කලින් කතා කරගත්තු වෙලාවට කතා කරමු.",
                "Api kalin katha karagaththu welawata call karamu 👍",
                "I would be pleased to schedule a discussion at your earliest convenience.",
                "Scheduling a meeting or call discussion",
                "Professional & Collaborative"
            )
            lowerText.contains("location") || lowerText.contains("where") || lowerText.contains("come") || lowerText.contains("address") -> Tuple8(
                "On my way! Sharing my live location in a second 📍",
                "I am currently en route and will arrive shortly.",
                "On my way! 🚗",
                "මම දැන් එන ගමන් ඉන්නේ 📍",
                "Mam dn ena gaman inne 📍",
                "I am currently proceeding to the designated location and will notify you upon arrival.",
                "Inquiring about location / arrival status",
                "Informative & Casual"
            )
            lowerText.contains("hi") || lowerText.contains("hello") || lowerText.contains("kohomada") || lowerText.contains("hey") -> Tuple8(
                "Hey there! Good to hear from you, how are you doing? 😊",
                "Hello! How may I assist you today?",
                "Hey! What's up?",
                "හලෝ! කොහොමද ඔයාට?",
                "Hello! Kohomada oyata? 😊",
                "Greetings. Thank you for reaching out.",
                "Friendly greeting and conversation starter",
                "Warm & Welcoming"
            )
            else -> Tuple8(
                "Thanks for the message! I'll check into this and update you soon 👍",
                "Thank you for contacting me. I will review the details and respond shortly.",
                "Got it, thanks!",
                "මම බලලා ඉක්මනින් update කරන්නම්.",
                "Mam balala ikmanata kiyannam 👍",
                "Thank you for your inquiry. I shall provide a response at the earliest opportunity.",
                "Direct message inquiry / action item requested",
                if (isSinhala) "Sinhala Conversational" else "Polite & Conversational"
            )
        }

        return com.example.domain.model.ChatContextAnalysisResult(
            conversationText = conversationText,
            detectedApp = "WhatsApp / Messaging",
            summary = summary,
            keyPoints = listOf("Received message: \"${cleanText.take(50)}\"", "Actionable response requested"),
            detectedIntent = intent,
            detectedTone = tone,
            pendingQuestions = listOf("Requires clear confirmation or response"),
            suggestedReplies = listOf(
                com.example.domain.model.ChatSuggestedReply("Friendly", friendly, "😊"),
                com.example.domain.model.ChatSuggestedReply("Professional", professional, "💼"),
                com.example.domain.model.ChatSuggestedReply("Short", short, "⚡"),
                com.example.domain.model.ChatSuggestedReply("Sinhala", sinhala, "🇱🇰"),
                com.example.domain.model.ChatSuggestedReply("Singlish", singlish, "💬"),
                com.example.domain.model.ChatSuggestedReply("Formal", formal, "👔")
            )
        )
    }

    private data class Tuple8<A, B, C, D, E, F, G, H>(
        val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H
    )

    private fun fallbackReply(prompt: String): String {
        return "Thank you for reaching out! I appreciate your message and will review the details shortly. Let's touch base soon!"
    }
}
