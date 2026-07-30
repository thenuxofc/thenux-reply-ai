package com.example.ui.screens.style

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.WritingStyleProfile

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleProfileScreen(
    currentProfile: WritingStyleProfile,
    onSaveProfile: (WritingStyleProfile) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var length by remember { mutableStateOf(currentProfile.length) }
    var emojiUsage by remember { mutableStateOf(currentProfile.emojiUsage) }
    var formality by remember { mutableStateOf(currentProfile.formality) }
    var sinhalaPref by remember { mutableStateOf(currentProfile.sinhalaPreference) }
    var customPhrases by remember { mutableStateOf(currentProfile.customPhrases) }

    val lengthOptions = listOf("Concise", "Medium", "Detailed")
    val emojiOptions = listOf("None", "Subtle", "Expressive")
    val formalityOptions = listOf("Casual", "Balanced", "Strictly Formal")
    val sinhalaOptions = listOf("Native Sinhala", "Singlish", "Mixed")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "My Style Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Personal writing style engine preferences",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Personal Writing Style Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Configure your individual messaging persona. THENUX AI will automatically adapt every response length, tone, and emoji usage to match your profile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                StyleSectionCard(title = "Preferred Reply Length") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        lengthOptions.forEach { option ->
                            FilterChip(
                                selected = length == option,
                                onClick = { length = option },
                                label = { Text(option) },
                                leadingIcon = if (length == option) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            item {
                StyleSectionCard(title = "Emoji Preference") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojiOptions.forEach { option ->
                            FilterChip(
                                selected = emojiUsage == option,
                                onClick = { emojiUsage = option },
                                label = { Text(option) },
                                leadingIcon = if (emojiUsage == option) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            item {
                StyleSectionCard(title = "Formality Level") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        formalityOptions.forEach { option ->
                            FilterChip(
                                selected = formality == option,
                                onClick = { formality = option },
                                label = { Text(option) },
                                leadingIcon = if (formality == option) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            item {
                StyleSectionCard(title = "Sinhala / Singlish Mode Preference") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        sinhalaOptions.forEach { option ->
                            FilterChip(
                                selected = sinhalaPref == option,
                                onClick = { sinhalaPref = option },
                                label = { Text(option) },
                                leadingIcon = if (sinhalaPref == option) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            item {
                StyleSectionCard(title = "Custom Signature / Favorite Phrases") {
                    OutlinedTextField(
                        value = customPhrases,
                        onValueChange = { customPhrases = it },
                        placeholder = { Text("e.g. Best regards, Thenula | Stuti!") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val newProfile = WritingStyleProfile(
                            length = length,
                            emojiUsage = emojiUsage,
                            formality = formality,
                            sinhalaPreference = sinhalaPref,
                            customPhrases = customPhrases
                        )
                        onSaveProfile(newProfile)
                        Toast.makeText(context, "Style Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Style Profile", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StyleSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
