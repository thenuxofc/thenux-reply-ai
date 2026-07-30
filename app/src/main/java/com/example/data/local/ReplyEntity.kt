package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replies")
data class ReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalText: String,
    val generatedReply: String,
    val modelId: String,
    val generationType: String,
    val tone: String,
    val language: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val folder: String = "General",
    val tags: String = ""
)

@Entity(tableName = "favorite_folders")
data class FavoriteFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#2563EB"
)
