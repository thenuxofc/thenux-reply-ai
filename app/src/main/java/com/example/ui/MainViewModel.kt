package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ReplyEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.ApiClient
import com.example.data.remote.ReplyRepository
import com.example.domain.model.AiModel
import com.example.domain.model.GenerationType
import com.example.domain.model.SinhalaTone
import com.example.domain.model.ToneOption
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GenerationUiState {
    object Idle : GenerationUiState
    object Loading : GenerationUiState
    data class Success(val reply: ReplyEntity) : GenerationUiState
    data class Error(val message: String) : GenerationUiState
}

sealed interface ChatContextUiState {
    object Idle : ChatContextUiState
    object Loading : ChatContextUiState
    data class Success(val result: com.example.domain.model.ChatContextAnalysisResult) : ChatContextUiState
    data class Error(val message: String) : ChatContextUiState
}

data class AppUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultModel: AiModel = AiModel.T_NEX,
    val defaultLanguage: String = "English",
    val onboardingCompleted: Boolean = false,
    val setupWizardCompleted: Boolean = false,
    val userName: String = "Thenux User",
    val selectedModel: AiModel = AiModel.T_NEX,
    val selectedType: GenerationType = GenerationType.REPLY,
    val selectedTone: ToneOption = ToneOption.PROFESSIONAL,
    val selectedSinhalaTone: SinhalaTone? = null,
    val processInputText: String = "",
    val searchQuery: String = "",
    val generationState: GenerationUiState = GenerationUiState.Idle,
    val chatContextState: ChatContextUiState = ChatContextUiState.Idle,
    val writingStyleProfile: com.example.domain.model.WritingStyleProfile = com.example.domain.model.WritingStyleProfile(),
    val localOnlyHistory: Boolean = true,
    val notificationAssistantEnabled: Boolean = false,
    val floatingBubbleEnabled: Boolean = false,
    val floatingBubbleSize: String = "Medium",
    val floatingBubbleAutoClipboard: Boolean = true,
    val floatingBubbleRememberPos: Boolean = true,
    val qualityReport: com.example.domain.model.CommunicationQualityReport? = null,
    val capturedNotifications: List<com.example.domain.model.CapturedNotification> = emptyList()
)

class MainViewModel(
    private val repository: ReplyRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _appUiState = MutableStateFlow(AppUiState())
    val appUiState: StateFlow<AppUiState> = _appUiState.asStateFlow()

    val historyReplies: StateFlow<List<ReplyEntity>> = repository.allReplies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteReplies: StateFlow<List<ReplyEntity>> = repository.favoriteReplies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            combine(
                combine(
                    userPreferencesRepository.themeModeFlow,
                    userPreferencesRepository.defaultModelFlow,
                    userPreferencesRepository.defaultLanguageFlow
                ) { theme, modelId, language -> Triple(theme, modelId, language) },
                combine(
                    userPreferencesRepository.onboardingCompletedFlow,
                    userPreferencesRepository.setupWizardCompletedFlow,
                    userPreferencesRepository.userNameFlow
                ) { onboarding, setup, name -> Triple(onboarding, setup, name) },
                combine(
                    userPreferencesRepository.writingStyleFlow,
                    userPreferencesRepository.localOnlyHistoryFlow,
                    userPreferencesRepository.notificationAssistantEnabledFlow
                ) { style, localOnly, notifEnabled -> Triple(style, localOnly, notifEnabled) },
                combine(
                    userPreferencesRepository.floatingBubbleEnabledFlow,
                    userPreferencesRepository.floatingBubbleSizeFlow,
                    userPreferencesRepository.floatingBubbleAutoClipboardFlow,
                    userPreferencesRepository.floatingBubbleRememberPosFlow
                ) { bubbleEnabled, bubbleSize, autoClip, rememberPos ->
                    BubbleSettingsTuple(bubbleEnabled, bubbleSize, autoClip, rememberPos)
                }
            ) { (theme, modelId, language), (onboarding, setup, name), (style, localOnly, notifEnabled), (bubbleEnabled, bubbleSize, autoClip, rememberPos) ->
                val model = AiModel.fromId(modelId)
                _appUiState.value.copy(
                    themeMode = theme,
                    defaultModel = model,
                    selectedModel = model,
                    defaultLanguage = language,
                    onboardingCompleted = onboarding,
                    setupWizardCompleted = setup,
                    userName = name,
                    writingStyleProfile = style,
                    localOnlyHistory = localOnly,
                    notificationAssistantEnabled = notifEnabled,
                    floatingBubbleEnabled = bubbleEnabled,
                    floatingBubbleSize = bubbleSize,
                    floatingBubbleAutoClipboard = autoClip,
                    floatingBubbleRememberPos = rememberPos
                )
            }.collect { updatedState ->
                _appUiState.value = updatedState
            }
        }
    }

    fun setProcessText(text: String) {
        _appUiState.value = _appUiState.value.copy(processInputText = text)
    }

    fun setSelectedModel(model: AiModel) {
        _appUiState.value = _appUiState.value.copy(selectedModel = model)
    }

    fun setSelectedType(type: GenerationType) {
        _appUiState.value = _appUiState.value.copy(selectedType = type)
    }

    fun setSelectedTone(tone: ToneOption) {
        _appUiState.value = _appUiState.value.copy(
            selectedTone = tone,
            selectedSinhalaTone = null
        )
    }

    fun setSelectedSinhalaTone(sinhalaTone: SinhalaTone) {
        _appUiState.value = _appUiState.value.copy(
            selectedSinhalaTone = sinhalaTone,
            selectedModel = AiModel.GEMINI_SINHALA
        )
    }

    fun setSearchQuery(query: String) {
        _appUiState.value = _appUiState.value.copy(searchQuery = query)
    }

    fun generateReply(
        inputText: String,
        model: AiModel = _appUiState.value.selectedModel,
        type: GenerationType = _appUiState.value.selectedType,
        tone: ToneOption = _appUiState.value.selectedTone,
        sinhalaTone: SinhalaTone? = _appUiState.value.selectedSinhalaTone
    ) {
        if (inputText.isBlank()) return

        _appUiState.value = _appUiState.value.copy(
            generationState = GenerationUiState.Loading,
            qualityReport = null
        )

        viewModelScope.launch {
            val result = repository.generateReply(
                text = inputText,
                model = model,
                type = type,
                tone = tone,
                targetLanguage = _appUiState.value.defaultLanguage,
                sinhalaTone = sinhalaTone,
                writingStyle = _appUiState.value.writingStyleProfile
            )

            result.onSuccess { replyEntity ->
                val qReport = repository.evaluateCommunicationQuality(replyEntity.generatedReply)
                _appUiState.value = _appUiState.value.copy(
                    generationState = GenerationUiState.Success(replyEntity),
                    qualityReport = qReport
                )
            }.onFailure { error ->
                _appUiState.value = _appUiState.value.copy(
                    generationState = GenerationUiState.Error(
                        error.localizedMessage ?: "Failed to generate reply"
                    )
                )
            }
        }
    }

    fun analyzeChatContext(conversationText: String) {
        if (conversationText.isBlank()) return

        _appUiState.value = _appUiState.value.copy(
            chatContextState = ChatContextUiState.Loading
        )

        viewModelScope.launch {
            val result = repository.analyzeChatContext(
                conversationText = conversationText,
                model = _appUiState.value.selectedModel
            )

            result.onSuccess { analysis ->
                _appUiState.value = _appUiState.value.copy(
                    chatContextState = ChatContextUiState.Success(analysis)
                )
                com.example.data.repository.LiveChatReaderRepository.setAnalysisResult(analysis)
            }.onFailure { error ->
                _appUiState.value = _appUiState.value.copy(
                    chatContextState = ChatContextUiState.Error(
                        error.localizedMessage ?: "Failed to analyze conversation context"
                    )
                )
            }
        }
    }

    fun updateWritingStyleProfile(profile: com.example.domain.model.WritingStyleProfile) {
        viewModelScope.launch {
            userPreferencesRepository.updateWritingStyle(profile)
        }
    }

    fun setLocalOnlyHistory(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setLocalOnlyHistory(enabled)
        }
    }

    fun setNotificationAssistantEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationAssistantEnabled(enabled)
        }
    }

    fun setFloatingBubbleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setFloatingBubbleEnabled(enabled)
        }
    }

    fun setFloatingBubbleSize(size: String) {
        viewModelScope.launch {
            userPreferencesRepository.setFloatingBubbleSize(size)
        }
    }

    fun setFloatingBubbleAutoClipboard(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setFloatingBubbleAutoClipboard(enabled)
        }
    }

    fun setFloatingBubbleRememberPos(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setFloatingBubbleRememberPos(enabled)
        }
    }

    fun addSimulatedNotification(appName: String, sender: String, messageText: String) {
        val newNotif = com.example.domain.model.CapturedNotification(
            packageName = appName.lowercase().replace(" ", "."),
            appName = appName,
            sender = sender,
            messageText = messageText
        )
        val currentList = _appUiState.value.capturedNotifications.toMutableList()
        currentList.add(0, newNotif)
        _appUiState.value = _appUiState.value.copy(capturedNotifications = currentList)
    }

    fun toggleFavorite(reply: ReplyEntity) {
        viewModelScope.launch {
            repository.setFavorite(reply.id, !reply.isFavorite)
        }
    }

    fun deleteReply(id: Long) {
        viewModelScope.launch {
            repository.deleteReply(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun resetGenerationState() {
        _appUiState.value = _appUiState.value.copy(generationState = GenerationUiState.Idle)
    }

    // Settings & Wizard updates
    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun updateDefaultModel(model: AiModel) {
        viewModelScope.launch {
            userPreferencesRepository.setDefaultModel(model.id)
        }
    }

    fun updateDefaultLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDefaultLanguage(language)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun completeSetupWizard() {
        viewModelScope.launch {
            userPreferencesRepository.setSetupWizardCompleted(true)
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getInstance(context)
                    val repository = ReplyRepository(ApiClient.apiService, db.replyDao())
                    val prefsRepo = UserPreferencesRepository(context)
                    return MainViewModel(repository, prefsRepo) as T
                }
            }
        }
    }
}

private data class BubbleSettingsTuple(
    val enabled: Boolean,
    val size: String,
    val autoClip: Boolean,
    val rememberPos: Boolean
)
