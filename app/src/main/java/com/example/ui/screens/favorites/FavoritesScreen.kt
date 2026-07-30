package com.example.ui.screens.favorites

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.data.local.ReplyEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.screens.home.ReplyCardItem

@Composable
fun FavoritesScreen(
    favoriteList: List<ReplyEntity>,
    onToggleFavorite: (ReplyEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val folders = listOf("All", "Work", "Social", "Sinhala", "Personal")
    var selectedFolder by remember { mutableStateOf("All") }
    val context = LocalContext.current

    val displayedFavorites = remember(favoriteList, selectedFolder) {
        if (selectedFolder == "All") favoriteList
        else favoriteList.filter { it.folder.equals(selectedFolder, ignoreCase = true) || it.tone.contains(selectedFolder, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Saved Favorites",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Folder organization and tagged replies",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Folder filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(folders) { folder ->
                FilterChip(
                    selected = selectedFolder == folder,
                    onClick = { selectedFolder = folder },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (selectedFolder == folder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text(folder) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (displayedFavorites.isEmpty()) {
            EmptyStateCard(
                title = "No Saved Favorites",
                description = "Tap the heart icon on any generated reply to save it to your favorites.",
                icon = Icons.Default.Bookmark
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedFavorites, key = { it.id }) { reply ->
                    ReplyCardItem(
                        reply = reply,
                        onToggleFavorite = { onToggleFavorite(reply) },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, reply.generatedReply)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Reply"))
                        }
                    )
                }
            }
        }
    }
}
