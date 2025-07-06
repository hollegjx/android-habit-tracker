package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 聊天消息数据访问对象
 */
@Dao
interface ChatMessageDao {
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId AND isDeleted = 0 ORDER BY timestamp ASC")
    fun getMessagesByConversation(conversationId: String): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId AND isDeleted = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: String, limit: Int = 50): List<ChatMessageEntity>
    
    @Query("SELECT * FROM chat_messages WHERE id = :messageId AND isDeleted = 0")
    suspend fun getMessageById(messageId: Long): ChatMessageEntity?
    
    @Query("SELECT * FROM chat_messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getMessagesByUser(userId: String): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId AND isRead = 0 AND isFromMe = 0 AND isDeleted = 0")
    suspend fun getUnreadMessages(conversationId: String): List<ChatMessageEntity>
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId AND isRead = 0 AND isFromMe = 0 AND isDeleted = 0")
    suspend fun getUnreadCount(conversationId: String): Int
    
    @Query("SELECT * FROM chat_messages WHERE content LIKE '%' || :searchText || '%' AND isDeleted = 0 ORDER BY timestamp DESC")
    suspend fun searchMessages(searchText: String): List<ChatMessageEntity>
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId AND content LIKE '%' || :searchText || '%' AND isDeleted = 0 ORDER BY timestamp DESC")
    suspend fun searchMessagesInConversation(conversationId: String, searchText: String): List<ChatMessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>): List<Long>
    
    @Update
    suspend fun updateMessage(message: ChatMessageEntity)
    
    @Query("UPDATE chat_messages SET isRead = 1, readTimestamp = :readTime WHERE conversationId = :conversationId AND isFromMe = 0 AND isRead = 0")
    suspend fun markConversationAsRead(conversationId: String, readTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_messages SET isRead = 1, readTimestamp = :readTime WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: Long, readTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_messages SET isDeleted = 1, updatedAt = :deleteTime WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: Long, deleteTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_messages SET content = :newContent, editTimestamp = :editTime, updatedAt = :updateTime WHERE id = :messageId")
    suspend fun editMessage(messageId: Long, newContent: String, editTime: Long = System.currentTimeMillis(), updateTime: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)
    
    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversation(conversationId: String)
    
    @Query("DELETE FROM chat_messages WHERE timestamp < :timestamp")
    suspend fun deleteOldMessages(timestamp: Long)
    
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId AND isDeleted = 0")
    suspend fun getMessageCount(conversationId: String): Int
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId AND isDeleted = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: String): ChatMessageEntity?
    
    @Query("DELETE FROM chat_messages WHERE conversationId NOT IN (SELECT conversationId FROM chat_conversations WHERE conversationType = 'AI')")
    suspend fun deleteNonAiMessages()
}