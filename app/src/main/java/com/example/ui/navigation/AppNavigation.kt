package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.AiModel
import com.example.ui.MainViewModel
import com.example.ui.screens.developer.DeveloperScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.guide.UserGuideScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.playground.AiPlaygroundScreen
import com.example.ui.screens.process.ProcessTextScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.setup.SetupWizardScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.sinhala.SinhalaSection

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    initialProcessText: String? = null,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val appState by viewModel.appUiState.collectAsState()
    val historyReplies by viewModel.historyReplies.collectAsState()
    val favoriteReplies by viewModel.favoriteReplies.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = when {
        initialProcessText != null -> Screen.ProcessText.route
        !appState.onboardingCompleted -> Screen.Onboarding.route
        !appState.setupWizardCompleted -> Screen.SetupWizard.route
        else -> Screen.Home.route
    }

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                screen.title?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.SetupWizard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.SetupWizard.route) {
                SetupWizardScreen(
                    currentTheme = appState.themeMode,
                    currentModel = appState.defaultModel,
                    currentLanguage = appState.defaultLanguage,
                    onThemeSelected = { viewModel.updateThemeMode(it) },
                    onModelSelected = { viewModel.updateDefaultModel(it) },
                    onLanguageSelected = { viewModel.updateDefaultLanguage(it) },
                    onFinishSetup = {
                        viewModel.completeSetupWizard()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SetupWizard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    userName = appState.userName,
                    selectedModel = appState.selectedModel,
                    selectedType = appState.selectedType,
                    selectedTone = appState.selectedTone,
                    generationState = appState.generationState,
                    recentReplies = historyReplies,
                    onModelSelect = { viewModel.setSelectedModel(it) },
                    onTypeSelect = { viewModel.setSelectedType(it) },
                    onToneSelect = { viewModel.setSelectedTone(it) },
                    onGenerate = { viewModel.generateReply(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onNavigateToSinhala = { navController.navigate(Screen.Sinhala.route) },
                    onNavigateToProcessText = {
                        viewModel.setProcessText(it)
                        navController.navigate(Screen.ProcessText.route)
                    },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToGuide = { navController.navigate(Screen.Guide.route) },
                    onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) },
                    onNavigateToPlayground = { navController.navigate(Screen.Playground.route) },
                    onNavigateToNotificationAssistant = { navController.navigate(Screen.NotificationAssistant.route) },
                    onNavigateToStyleProfile = { navController.navigate(Screen.StyleProfile.route) },
                    onNavigateToScreenshotAnalyzer = { navController.navigate(Screen.ScreenshotAnalyzer.route) },
                    onNavigateToVoiceAssistant = { navController.navigate(Screen.VoiceAssistant.route) },
                    onNavigateToPrivacyCenter = { navController.navigate(Screen.PrivacyCenter.route) },
                    onNavigateToLanding = { navController.navigate(Screen.Landing.route) },
                    onNavigateToFloatingAssistant = { navController.navigate(Screen.FloatingAssistant.route) }
                )
            }

            composable(Screen.Landing.route) {
                com.example.ui.screens.landing.LandingScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToGuide = { navController.navigate(Screen.Guide.route) },
                    onNavigateToNotificationAssistant = { navController.navigate(Screen.NotificationAssistant.route) },
                    onNavigateToStyleProfile = { navController.navigate(Screen.StyleProfile.route) },
                    onNavigateToScreenshotAnalyzer = { navController.navigate(Screen.ScreenshotAnalyzer.route) },
                    onNavigateToVoiceAssistant = { navController.navigate(Screen.VoiceAssistant.route) },
                    onNavigateToSinhala = { navController.navigate(Screen.Sinhala.route) },
                    onNavigateToPrivacyCenter = { navController.navigate(Screen.PrivacyCenter.route) },
                    onNavigateToFloatingAssistant = { navController.navigate(Screen.FloatingAssistant.route) }
                )
            }

            composable(Screen.NotificationAssistant.route) {
                com.example.ui.screens.notifications.NotificationAssistantScreen(
                    isEnabled = appState.notificationAssistantEnabled,
                    capturedNotifications = appState.capturedNotifications,
                    onToggleEnabled = { viewModel.setNotificationAssistantEnabled(it) },
                    onAddSimulatedNotif = { app, sender, msg -> viewModel.addSimulatedNotification(app, sender, msg) },
                    onGenerateReplyForText = { text ->
                        viewModel.setProcessText(text)
                        navController.navigate(Screen.ProcessText.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.StyleProfile.route) {
                com.example.ui.screens.style.StyleProfileScreen(
                    currentProfile = appState.writingStyleProfile,
                    onSaveProfile = { viewModel.updateWritingStyleProfile(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ScreenshotAnalyzer.route) {
                com.example.ui.screens.vision.ScreenshotAnalyzerScreen(
                    onAnalyzeText = { text ->
                        com.example.domain.model.ScreenshotAnalysisResult(
                            extractedText = text,
                            detectedContext = "Chat Screenshot Analysis",
                            summary = "Extracted query or message from image.",
                            suggestedReply = "Thank you for sending this! I have reviewed the image and will update you shortly."
                        )
                    },
                    onNavigateToReply = { text ->
                        viewModel.setProcessText(text)
                        navController.navigate(Screen.ProcessText.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.VoiceAssistant.route) {
                com.example.ui.screens.voice.VoiceAssistantScreen(
                    onGenerateVoiceReply = { text ->
                        viewModel.setProcessText(text)
                        navController.navigate(Screen.ProcessText.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PrivacyCenter.route) {
                com.example.ui.screens.privacy.PrivacyCenterScreen(
                    localOnlyHistory = appState.localOnlyHistory,
                    onToggleLocalOnly = { viewModel.setLocalOnlyHistory(it) },
                    onClearAllHistory = { viewModel.clearHistory() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ProcessText.route) {
                ProcessTextScreen(
                    initialText = initialProcessText ?: appState.processInputText,
                    selectedModel = appState.selectedModel,
                    selectedTone = appState.selectedTone,
                    generationState = appState.generationState,
                    onModelSelect = { viewModel.setSelectedModel(it) },
                    onToneSelect = { viewModel.setSelectedTone(it) },
                    onGenerate = { viewModel.generateReply(it) },
                    onBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0)
                            }
                        }
                    }
                )
            }

            composable(Screen.Sinhala.route) {
                SinhalaSection(
                    generationState = appState.generationState,
                    selectedSinhalaTone = appState.selectedSinhalaTone,
                    onSelectSinhalaTone = { viewModel.setSelectedSinhalaTone(it) },
                    onGenerateSinhala = { text, tone ->
                        viewModel.generateReply(
                            inputText = text,
                            model = AiModel.GEMINI_SINHALA,
                            sinhalaTone = tone
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    historyList = historyReplies,
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onDeleteReply = { viewModel.deleteReply(it) },
                    onClearAllHistory = { viewModel.clearHistory() }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    favoriteList = favoriteReplies,
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentTheme = appState.themeMode,
                    currentModel = appState.defaultModel,
                    currentLanguage = appState.defaultLanguage,
                    onThemeSelected = { viewModel.updateThemeMode(it) },
                    onModelSelected = { viewModel.updateDefaultModel(it) },
                    onLanguageSelected = { viewModel.updateDefaultLanguage(it) },
                    onClearCache = { viewModel.clearHistory() },
                    onNavigateToGuide = { navController.navigate(Screen.Guide.route) },
                    onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) },
                    onNavigateToPlayground = { navController.navigate(Screen.Playground.route) },
                    onNavigateToLanding = { navController.navigate(Screen.Landing.route) },
                    onNavigateToFloatingAssistant = { navController.navigate(Screen.FloatingAssistant.route) }
                )
            }

            composable(Screen.FloatingAssistant.route) {
                com.example.ui.screens.floating.FloatingAssistantScreen(
                    isEnabled = appState.floatingBubbleEnabled,
                    bubbleSize = appState.floatingBubbleSize,
                    autoClipboard = appState.floatingBubbleAutoClipboard,
                    rememberPos = appState.floatingBubbleRememberPos,
                    chatContextState = appState.chatContextState,
                    onAnalyzeChatContext = { text -> viewModel.analyzeChatContext(text) },
                    onToggleEnabled = { viewModel.setFloatingBubbleEnabled(it) },
                    onChangeSize = { viewModel.setFloatingBubbleSize(it) },
                    onToggleAutoClipboard = { viewModel.setFloatingBubbleAutoClipboard(it) },
                    onToggleRememberPos = { viewModel.setFloatingBubbleRememberPos(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    userName = appState.userName,
                    favoriteModel = appState.defaultModel,
                    totalRepliesCount = historyReplies.size,
                    favoriteRepliesCount = favoriteReplies.size
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    allReplies = historyReplies,
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Guide.route) {
                UserGuideScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Developer.route) {
                DeveloperScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Playground.route) {
                AiPlaygroundScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
