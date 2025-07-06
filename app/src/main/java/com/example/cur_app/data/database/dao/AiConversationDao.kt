package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.AiConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * AI对话数据访问对象
 */
@Dao
interface AiConversationDao {
    
    @Query("SELECT * FROM ai_conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<AiConversationEntity>>
    
    @Query("SELECT * FROM ai_conversations WHERE characterId = :characterId ORDER BY timestamp DESC")
    fun getConversationsByCharacter(characterId: Long): Flow<List<AiConversationEntity>>
    
    @Query("SELECT * FROM ai_conversations WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getConversationsByHabit(habitId: Long): Flow<List<AiConversationEntity>>
    
    @Query("SELECT * FROM ai_conversations WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getConversationsBySession(sessionId: String): Flow<List<AiConversationEntity>>
    
    @Query("SELECT * FROM ai_conversations WHERE messageType = :messageType ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getConversationsByType(messageType: String, limit: Int = 10): List<AiConversationEntity>
    
    @Query("SELECT * FROM ai_conversations WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadConversations(): Flow<List<AiConversationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AiConversationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<AiConversationEntity>): List<Long>
    
    @Update
    suspend fun updateConversation(conversation: AiConversationEntity)
    
    @Query("UPDATE ai_conversations SET isRead = 1 WHERE id = :conversationId")
    suspend fun markAsRead(conversationId: Long)
    
    @Query("UPDATE ai_conversations SET isRead = 1 WHERE characterId = :characterId")
    suspend fun markAllAsReadByCharacter(characterId: Long)
    
    @Query("UPDATE ai_conversations SET userRating = :rating WHERE id = :conversationId")
    suspend fun rateConversation(conversationId: Long, rating: Int)
    
    @Delete
    suspend fun deleteConversation(conversation: AiConversationEntity)
    
    @Query("DELETE FROM ai_conversations WHERE timestamp < :timestamp")
    suspend fun deleteOldConversations(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM ai_conversations WHERE isRead = 0")
    suspend fun getUnreadCount(): Int
} 