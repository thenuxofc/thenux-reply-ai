package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_MODEL = stringPreferencesKey("default_model")
        val DEFAULT_LANGUAGE = stringPreferencesKey("default_language")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SETUP_WIZARD_COMPLETED = booleanPreferencesKey("setup_wizard_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val STYLE_LENGTH = stringPreferencesKey("style_length")
        val STYLE_EMOJI = stringPreferencesKey("style_emoji")
        val STYLE_FORMALITY = stringPreferencesKey("style_formality")
        val STYLE_SINHALA = stringPreferencesKey("style_sinhala")
        val STYLE_CUSTOM_PHRASES = stringPreferencesKey("style_custom_phrases")
        val LOCAL_ONLY_HISTORY = booleanPreferencesKey("local_only_history")
        val NOTIFICATION_ASSISTANT_ENABLED = booleanPreferencesKey("notification_assistant_enabled")
        val FLOATING_BUBBLE_ENABLED = booleanPreferencesKey("floating_bubble_enabled")
        val FLOATING_BUBBLE_SIZE = stringPreferencesKey("floating_bubble_size")
        val FLOATING_BUBBLE_AUTO_CLIPBOARD = booleanPreferencesKey("floating_bubble_auto_clipboard")
        val FLOATING_BUBBLE_REMEMBER_POS = booleanPreferencesKey("floating_bubble_remember_pos")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.THEME_MODE]) {
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            ThemeMode.DARK.name -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val defaultModelFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_MODEL] ?: "t-nex"
    }

    val defaultLanguageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_LANGUAGE] ?: "English"
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val setupWizardCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SETUP_WIZARD_COMPLETED] ?: false
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_NAME] ?: "Thenux User"
    }

    val writingStyleFlow: Flow<com.example.domain.model.WritingStyleProfile> = context.dataStore.data.map { prefs ->
        com.example.domain.model.WritingStyleProfile(
            length = prefs[Keys.STYLE_LENGTH] ?: "Medium",
            emojiUsage = prefs[Keys.STYLE_EMOJI] ?: "Subtle",
            formality = prefs[Keys.STYLE_FORMALITY] ?: "Balanced",
            sinhalaPreference = prefs[Keys.STYLE_SINHALA] ?: "Native Sinhala",
            customPhrases = prefs[Keys.STYLE_CUSTOM_PHRASES] ?: ""
        )
    }

    val localOnlyHistoryFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOCAL_ONLY_HISTORY] ?: true
    }

    val notificationAssistantEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATION_ASSISTANT_ENABLED] ?: false
    }

    val floatingBubbleEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.FLOATING_BUBBLE_ENABLED] ?: false
    }

    val floatingBubbleSizeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.FLOATING_BUBBLE_SIZE] ?: "Medium"
    }

    val floatingBubbleAutoClipboardFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.FLOATING_BUBBLE_AUTO_CLIPBOARD] ?: true
    }

    val floatingBubbleRememberPosFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.FLOATING_BUBBLE_REMEMBER_POS] ?: true
    }

    suspend fun setFloatingBubbleEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FLOATING_BUBBLE_ENABLED] = enabled
        }
    }

    suspend fun setFloatingBubbleSize(size: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FLOATING_BUBBLE_SIZE] = size
        }
    }

    suspend fun setFloatingBubbleAutoClipboard(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FLOATING_BUBBLE_AUTO_CLIPBOARD] = enabled
        }
    }

    suspend fun setFloatingBubbleRememberPos(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FLOATING_BUBBLE_REMEMBER_POS] = enabled
        }
    }

    suspend fun updateWritingStyle(profile: com.example.domain.model.WritingStyleProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STYLE_LENGTH] = profile.length
            prefs[Keys.STYLE_EMOJI] = profile.emojiUsage
            prefs[Keys.STYLE_FORMALITY] = profile.formality
            prefs[Keys.STYLE_SINHALA] = profile.sinhalaPreference
            prefs[Keys.STYLE_CUSTOM_PHRASES] = profile.customPhrases
        }
    }

    suspend fun setLocalOnlyHistory(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOCAL_ONLY_HISTORY] = enabled
        }
    }

    suspend fun setNotificationAssistantEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_ASSISTANT_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDefaultModel(modelId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_MODEL] = modelId
        }
    }

    suspend fun setDefaultLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_LANGUAGE] = language
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setSetupWizardCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SETUP_WIZARD_COMPLETED] = completed
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name
        }
    }
}
