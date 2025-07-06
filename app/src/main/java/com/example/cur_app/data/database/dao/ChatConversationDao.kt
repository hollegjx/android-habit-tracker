package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.ChatConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 聊天对话数据访问对象
 */
@Dao
interface ChatConversationDao {
    
    @Query("SELECT * FROM chat_conversations WHERE isArchived = 0 ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getAllConversations(): Flow<List<ChatConversationEntity>>
    
    @Query("SELECT * FROM chat_conversations WHERE conversationId = :conversationId")
    suspend fun getConversationById(conversationId: String): ChatConversationEntity?
    
    @Query("SELECT * FROM chat_conversations WHERE otherUserId = :userId")
    suspend fun getConversationByUserId(userId: String): ChatConversationEntity?
    
    @Query("SELECT * FROM chat_conversations WHERE conversationType = :type AND isArchived = 0 ORDER BY lastMessageTime DESC")
    fun getConversationsByType(type: String): Flow<List<ChatConversationEntity>>
    
    @Query("SELECT * FROM chat_conversations WHERE isPinned = 1 AND isArchived = 0 ORDER BY lastMessageTime DESC")
    fun getPinnedConversations(): Flow<List<ChatConversationEntity>>
    
    @Query("SELECT * FROM chat_conversations WHERE isArchived = 1 ORDER BY lastMessageTime DESC")
    fun getArchivedConversations(): Flow<List<ChatConversationEntity>>
    
    @Query("SELECT * FROM chat_conversations WHERE unreadCount > 0 AND isArchived = 0 ORDER BY lastMessageTime DESC")
    fun getConversationsWithUnread(): Flow<List<ChatConversationEntity>>
    
    @Query("SELECT SUM(unreadCount) FROM chat_conversations WHERE isArchived = 0 AND isMuted = 0")
    suspend fun getTotalUnreadCount(): Int
    
    @Query("SELECT * FROM chat_conversations WHERE title LIKE '%' || :searchText || '%' OR lastMessage LIKE '%' || :searchText || '%'")
    suspend fun searchConversations(searchText: String): List<ChatConversationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ChatConversationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ChatConversationEntity>): List<Long>
    
    @Update
    suspend fun updateConversation(conversation: ChatConversationEntity)
    
    @Query("UPDATE chat_conversations SET lastMessage = :message, lastMessageTime = :time, lastMessageSenderId = :senderId, lastMessageType = :type, unreadCount = :unreadCount, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun updateLastMessage(
        conversationId: String,
        message: String,
        time: Long,
        senderId: String,
        type: String = "TEXT",
        unreadCount: Int,
        updateTime: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE chat_conversations SET unreadCount = 0, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun markAsRead(conversationId: String, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_conversations SET unreadCount = unreadCount + :increment, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun incrementUnreadCount(conversationId: String, increment: Int = 1, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_conversations SET isPinned = :pinned, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun updatePinStatus(conversationId: String, pinned: Boolean, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_conversations SET isArchived = :archived, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun updateArchiveStatus(conversationId: String, archived: Boolean, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_conversations SET isMuted = :muted, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun updateMuteStatus(conversationId: String, muted: Boolean, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_conversations SET isBlocked = :blocked, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun updateBlockStatus(conversationId: String, blocked: Boolean, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_conversations SET customName = :name, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun updateCustomName(conversationId: String, name: String, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_conversations SET customAvatar = :avatar, updatedAt = :updateTime WHERE conversationId = :conversationId")
    suspend fun updateCustomAvatar(conversationId: String, avatar: String, updateTime: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteConversation(conversation: ChatConversationEntity)
    
    @Query("DELETE FROM chat_conversations WHERE conversationId = :conversationId")
    suspend fun deleteConversationById(conversationId: String)
    
    @Query("DELETE FROM chat_conversations WHERE lastMessageTime < :timestamp")
    suspend fun deleteOldConversations(timestamp: Long)
    
    @Query("DELETE FROM chat_conversations")
    suspend fun deleteAllConversations()
    
    @Query("DELETE FROM chat_conversations WHERE conversationType != 'AI'")
    suspend fun deleteNonAiConversations()
}