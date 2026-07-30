package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Onboarding : Screen("onboarding")
    object SetupWizard : Screen("setup_wizard")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.History)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Bookmark)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object ProcessText : Screen("process_text")
    object Sinhala : Screen("sinhala")
    object Profile : Screen("profile")
    object Search : Screen("search")
    object Guide : Screen("guide")
    object Developer : Screen("developer")
    object Playground : Screen("playground")
    object NotificationAssistant : Screen("notification_assistant")
    object StyleProfile : Screen("style_profile")
    object ScreenshotAnalyzer : Screen("screenshot_analyzer")
    object VoiceAssistant : Screen("voice_assistant")
    object PrivacyCenter : Screen("privacy_center")
    object Landing : Screen("landing")
    object FloatingAssistant : Screen("floating_assistant")

    companion object {
        val bottomNavItems = listOf(Home, History, Favorites, Settings)
    }
}
