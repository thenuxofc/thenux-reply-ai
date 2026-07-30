package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplyDao {
    @Query("SELECT * FROM replies ORDER BY timestamp DESC")
    fun getAllReplies(): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteReplies(): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE folder = :folderName ORDER BY timestamp DESC")
    fun getRepliesByFolder(folderName: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE originalText LIKE '%' || :query || '%' OR generatedReply LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchReplies(query: String): Flow<List<ReplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ReplyEntity): Long

    @Update
    suspend fun updateReply(reply: ReplyEntity)

    @Query("UPDATE replies SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM replies WHERE id = :id")
    suspend fun deleteReplyById(id: Long)

    @Query("DELETE FROM replies")
    suspend fun clearAllReplies()

    // Folders
    @Query("SELECT * FROM favorite_folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FavoriteFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FavoriteFolderEntity): Long

    @Query("DELETE FROM favorite_folders WHERE name = :folderName")
    suspend fun deleteFolderByName(folderName: String)
}
