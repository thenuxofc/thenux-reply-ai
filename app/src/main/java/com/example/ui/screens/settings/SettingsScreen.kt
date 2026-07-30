package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AiModel
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    currentModel: AiModel,
    currentLanguage: String,
    onThemeSelected: (ThemeMode) -> Unit,
    onModelSelected: (AiModel) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onClearCache: () -> Unit,
    onNavigateToGuide: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToPlayground: () -> Unit = {},
    onNavigateToLanding: () -> Unit = {},
    onNavigateToFloatingAssistant: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Configure preferences, models, and privacy",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Section: Preferences
        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingRowCard(
                title = "Appearance / Theme",
                subtitle = when (currentTheme) {
                    ThemeMode.SYSTEM -> "System Default"
                    ThemeMode.LIGHT -> "Light Theme"
                    ThemeMode.DARK -> "Dark Theme"
                },
                icon = Icons.Default.DarkMode,
                onClick = { showThemeDialog = true }
            )
        }

        item {
            SettingRowCard(
                title = "Default AI Model",
                subtitle = currentModel.displayName,
                icon = Icons.Default.Psychology,
                onClick = { showModelDialog = true }
            )
        }

        item {
            SettingRowCard(
                title = "Primary Language",
                subtitle = currentLanguage,
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )
        }

        // Section: Data & Privacy
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Data & Privacy",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingRowCard(
                title = "Clear Cache & History",
                subtitle = "Permanently delete saved generated replies",
                icon = Icons.Default.CleaningServices,
                onClick = onClearCache
            )
        }

        item {
            SettingRowCard(
                title = "Privacy Policy & Transparency",
                subtitle = "Read how text data is processed securely",
                icon = Icons.Default.Lock,
                onClick = { showPrivacyDialog = true }
            )
        }

        // Section: Resources & Tools
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Resources & Interactive Tools",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingRowCard(
                title = "🤖 AI Floating Assistant",
                subtitle = "Manage floating bubble overlay, actions & size",
                icon = Icons.Default.AutoAwesome,
                onClick = onNavigateToFloatingAssistant
            )
        }

        item {
            SettingRowCard(
                title = "App Showcase & Landing Page",
                subtitle = "Explore AI Superpowers, stats & features",
                icon = Icons.Default.Info,
                onClick = onNavigateToLanding
            )
        }

        item {
            SettingRowCard(
                title = "How to Use Guide",
                subtitle = "Master text selection, tones & Sinhala mode",
                icon = Icons.Default.Info,
                onClick = onNavigateToGuide
            )
        }

        item {
            SettingRowCard(
                title = "AI Testing Playground",
                subtitle = "Model speed benchmark & tone quiz game",
                icon = Icons.Default.Psychology,
                onClick = onNavigateToPlayground
            )
        }

        // Section: About & Developer
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "About & Developer",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingRowCard(
                title = "Developer & Trademark Info",
                subtitle = "THENUX™ • Thenula Panapitiya (17y)",
                icon = Icons.Default.Code,
                onClick = onNavigateToDeveloper
            )
        }

        item {
            SettingRowCard(
                title = "About THENUX Reply AI",
                subtitle = "Version 1.0.0 | Production Release",
                icon = Icons.Default.Info,
                onClick = { showAboutDialog = true }
            )
        }
    }

    // Dialogs
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeSelected(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = mode == currentTheme,
                                onClick = {
                                    onThemeSelected(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> "System Default"
                                    ThemeMode.LIGHT -> "Light Theme"
                                    ThemeMode.DARK -> "Dark Theme"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    if (showModelDialog) {
        AlertDialog(
            onDismissRequest = { showModelDialog = false },
            title = { Text("Choose Default AI Model") },
            text = {
                Column {
                    AiModel.values().forEach { model ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onModelSelected(model)
                                    showModelDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = model == currentModel,
                                onClick = {
                                    onModelSelected(model)
                                    showModelDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = model.displayName, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelDialog = false }) { Text("Close") }
            }
        )
    }

    if (showLanguageDialog) {
        val langs = listOf("English", "Sinhala", "Tamil")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Choose Primary Language") },
            text = {
                Column {
                    langs.forEach { lang ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageSelected(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = lang.equals(currentLanguage, ignoreCase = true),
                                onClick = {
                                    onLanguageSelected(lang)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Close") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Data Transparency") },
            text = {
                Text("THENUX Reply AI processes your selected text solely to generate requested responses. Your text is transmitted over encrypted HTTPS to our Cloudflare Worker AI gateway. No personal chat history is permanently stored on external servers.")
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("I Understand") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("THENUX Reply AI") },
            text = {
                Column {
                    Text("Version 1.0.0 (Build 2026)")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Owner: Thenux Thenula Panapiti")
                    Text("Website: www.thenuxofc.store")
                    Text("Instagram: @thenux_ofc")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SettingRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
