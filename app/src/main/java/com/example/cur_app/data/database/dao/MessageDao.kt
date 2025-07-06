package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 消息数据访问对象
 */
@Dao
interface MessageDao {
    
    /**
     * 插入消息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long
    
    /**
     * 插入多个消息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    /**
     * 更新消息
     */
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    /**
     * 删除消息
     */
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    /**
     * 根据ID删除消息
     */
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)
    
    /**
     * 根据消息ID删除消息
     */
    @Query("DELETE FROM messages WHERE message_id = :messageId")
    suspend fun deleteByMessageId(messageId: String)
    
    /**
     * 删除对话的所有消息
     */
    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: Long)
    
    /**
     * 根据ID获取消息
     */
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): MessageEntity?
    
    /**
     * 根据消息ID获取消息
     */
    @Query("SELECT * FROM messages WHERE message_id = :messageId")
    suspend fun getByMessageId(messageId: String): MessageEntity?
    
    /**
     * 获取对话的所有消息（实时更新）
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId AND is_deleted = 0
        ORDER BY sent_at ASC
    """)
    fun getMessagesByConversationId(conversationId: Long): Flow<List<MessageEntity>>
    
    /**
     * 获取对话的最近消息（分页）
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId AND is_deleted = 0
        ORDER BY sent_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getRecentMessages(conversationId: Long, limit: Int = 20, offset: Int = 0): List<MessageEntity>
    
    /**
     * 获取对话的最后一条消息
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId AND is_deleted = 0
        ORDER BY sent_at DESC 
        LIMIT 1
    """)
    suspend fun getLastMessage(conversationId: Long): MessageEntity?
    
    /**
     * 获取特定时间之后的消息
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
          AND sent_at > :timestamp 
          AND is_deleted = 0
        ORDER BY sent_at ASC
    """)
    suspend fun getMessagesAfter(conversationId: Long, timestamp: Long): List<MessageEntity>
    
    /**
     * 获取特定时间之前的消息
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
          AND sent_at < :timestamp 
          AND is_deleted = 0
        ORDER BY sent_at DESC 
        LIMIT :limit
    """)
    suspend fun getMessagesBefore(conversationId: Long, timestamp: Long, limit: Int = 20): List<MessageEntity>
    
    /**
     * 获取特定类型的消息
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
          AND message_type = :messageType 
          AND is_deleted = 0
        ORDER BY sent_at DESC
    """)
    fun getMessagesByType(conversationId: Long, messageType: String): Flow<List<MessageEntity>>
    
    /**
     * 获取用户发送的消息
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
          AND sender_id = :senderId 
          AND is_deleted = 0
        ORDER BY sent_at DESC
    """)
    fun getMessagesBySender(conversationId: Long, senderId: String): Flow<List<MessageEntity>>
    
    /**
     * 搜索消息内容
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
          AND content LIKE '%' || :query || '%' 
          AND is_deleted = 0
        ORDER BY sent_at DESC
    """)
    suspend fun searchMessages(conversationId: Long, query: String): List<MessageEntity>
    
    /**
     * 获取未读消息
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
          AND is_read = 0 
          AND sender_id != :currentUserId
          AND is_deleted = 0
        ORDER BY sent_at ASC
    """)
    fun getUnreadMessages(conversationId: Long, currentUserId: String): Flow<List<MessageEntity>>
    
    /**
     * 获取回复特定消息的消息
     */
    @Query("""
        SELECT * FROM messages 
        WHERE reply_to_id = :replyToId AND is_deleted = 0
        ORDER BY sent_at ASC
    """)
    suspend fun getReplies(replyToId: Long): List<MessageEntity>
    
    /**
     * 标记消息为已读
     */
    @Query("""
        UPDATE messages 
        SET is_read = 1, read_at = :readAt, updated_at = :timestamp
        WHERE id = :messageId
    """)
    suspend fun markAsRead(
        messageId: Long,
        readAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 标记对话的所有消息为已读
     */
    @Query("""
        UPDATE messages 
        SET is_read = 1, read_at = :readAt, updated_at = :timestamp
        WHERE conversation_id = :conversationId 
          AND sender_id != :currentUserId 
          AND is_read = 0
    """)
    suspend fun markConversationAsRead(
        conversationId: Long,
        currentUserId: String,
        readAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 标记消息为已送达
     */
    @Query("""
        UPDATE messages 
        SET is_delivered = 1, delivered_at = :deliveredAt, updated_at = :timestamp
        WHERE id = :messageId
    """)
    suspend fun markAsDelivered(
        messageId: Long,
        deliveredAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 软删除消息
     */
    @Query("""
        UPDATE messages 
        SET is_deleted = 1, deleted_at = :deletedAt, updated_at = :timestamp
        WHERE id = :messageId
    """)
    suspend fun softDeleteMessage(
        messageId: Long,
        deletedAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 更新消息内容（编辑）
     */
    @Query("""
        UPDATE messages 
        SET content = :content, is_edited = 1, edited_at = :editedAt, updated_at = :timestamp
        WHERE id = :messageId
    """)
    suspend fun editMessage(
        messageId: Long,
        content: String,
        editedAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 更新消息反应
     */
    @Query("""
        UPDATE messages 
        SET reactions = :reactions, updated_at = :timestamp
        WHERE id = :messageId
    """)
    suspend fun updateReactions(
        messageId: Long,
        reactions: String?,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 获取对话的消息总数
     */
    @Query("SELECT COUNT(*) FROM messages WHERE conversation_id = :conversationId AND is_deleted = 0")
    suspend fun getMessageCount(conversationId: Long): Int
    
    /**
     * 获取对话的未读消息数
     */
    @Query("""
        SELECT COUNT(*) FROM messages 
        WHERE conversation_id = :conversationId 
          AND is_read = 0 
          AND sender_id != :currentUserId
          AND is_deleted = 0
    """)
    suspend fun getUnreadCount(conversationId: Long, currentUserId: String): Int
    
    /**
     * 获取特定类型的消息数量
     */
    @Query("""
        SELECT COUNT(*) FROM messages 
        WHERE conversation_id = :conversationId 
          AND message_type = :messageType 
          AND is_deleted = 0
    """)
    suspend fun getMessageCountByType(conversationId: Long, messageType: String): Int
    
    /**
     * 删除过期的已删除消息（永久删除）
     */
    @Query("""
        DELETE FROM messages 
        WHERE is_deleted = 1 AND deleted_at < :expireTime
    """)
    suspend fun deleteExpiredMessages(expireTime: Long)
    
    /**
     * 清空所有消息（用于登出等场景）
     */
    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}