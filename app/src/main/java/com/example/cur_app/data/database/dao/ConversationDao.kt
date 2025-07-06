package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 对话数据访问对象
 */
@Dao
interface ConversationDao {
    
    /**
     * 插入对话
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long
    
    /**
     * 插入多个对话
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)
    
    /**
     * 更新对话
     */
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)
    
    /**
     * 删除对话
     */
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    /**
     * 根据ID删除对话
     */
    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: Long)
    
    /**
     * 根据对话ID删除对话
     */
    @Query("DELETE FROM conversations WHERE conversation_id = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)
    
    /**
     * 根据ID获取对话
     */
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: Long): ConversationEntity?
    
    /**
     * 根据对话ID获取对话
     */
    @Query("SELECT * FROM conversations WHERE conversation_id = :conversationId")
    suspend fun getByConversationId(conversationId: String): ConversationEntity?
    
    /**
     * 获取所有活跃对话
     */
    @Query("""
        SELECT * FROM conversations 
        WHERE is_active = 1 
        ORDER BY 
            CASE WHEN last_message_at IS NOT NULL THEN last_message_at ELSE created_at END DESC
    """)
    fun getActiveConversations(): Flow<List<ConversationEntity>>
    
    /**
     * 获取特定类型的对话
     */
    @Query("""
        SELECT * FROM conversations 
        WHERE type = :type AND is_active = 1
        ORDER BY 
            CASE WHEN last_message_at IS NOT NULL THEN last_message_at ELSE created_at END DESC
    """)
    fun getConversationsByType(type: String): Flow<List<ConversationEntity>>
    
    /**
     * 获取用户创建的对话
     */
    @Query("""
        SELECT * FROM conversations 
        WHERE created_by = :userId AND is_active = 1
        ORDER BY 
            CASE WHEN last_message_at IS NOT NULL THEN last_message_at ELSE created_at END DESC
    """)
    fun getConversationsByCreator(userId: String): Flow<List<ConversationEntity>>
    
    /**
     * 搜索对话（根据名称）
     */
    @Query("""
        SELECT * FROM conversations 
        WHERE name LIKE '%' || :query || '%' AND is_active = 1
        ORDER BY 
            CASE WHEN last_message_at IS NOT NULL THEN last_message_at ELSE created_at END DESC
    """)
    fun searchConversations(query: String): Flow<List<ConversationEntity>>
    
    /**
     * 更新对话的最后消息时间
     */
    @Query("""
        UPDATE conversations 
        SET last_message_at = :timestamp, updated_at = :updatedAt
        WHERE id = :conversationId
    """)
    suspend fun updateLastMessageAt(
        conversationId: Long, 
        timestamp: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * 通过对话ID更新最后消息时间
     */
    @Query("""
        UPDATE conversations 
        SET last_message_at = :timestamp, updated_at = :updatedAt
        WHERE conversation_id = :conversationId
    """)
    suspend fun updateLastMessageAtByConversationId(
        conversationId: String,
        timestamp: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * 设置对话为非活跃状态
     */
    @Query("""
        UPDATE conversations 
        SET is_active = 0, updated_at = :timestamp
        WHERE id = :conversationId
    """)
    suspend fun setInactive(conversationId: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 通过对话ID设置对话为非活跃状态
     */
    @Query("""
        UPDATE conversations 
        SET is_active = 0, updated_at = :timestamp
        WHERE conversation_id = :conversationId
    """)
    suspend fun setInactiveByConversationId(conversationId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 重新激活对话
     */
    @Query("""
        UPDATE conversations 
        SET is_active = 1, updated_at = :timestamp
        WHERE id = :conversationId
    """)
    suspend fun setActive(conversationId: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 更新对话名称
     */
    @Query("""
        UPDATE conversations 
        SET name = :name, updated_at = :timestamp
        WHERE id = :conversationId
    """)
    suspend fun updateName(conversationId: Long, name: String?, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 更新对话描述
     */
    @Query("""
        UPDATE conversations 
        SET description = :description, updated_at = :timestamp
        WHERE id = :conversationId
    """)
    suspend fun updateDescription(conversationId: Long, description: String?, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 更新对话头像
     */
    @Query("""
        UPDATE conversations 
        SET avatar_url = :avatarUrl, updated_at = :timestamp
        WHERE id = :conversationId
    """)
    suspend fun updateAvatar(conversationId: Long, avatarUrl: String?, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 获取对话总数
     */
    @Query("SELECT COUNT(*) FROM conversations WHERE is_active = 1")
    suspend fun getActiveConversationCount(): Int
    
    /**
     * 获取特定类型的对话数量
     */
    @Query("SELECT COUNT(*) FROM conversations WHERE type = :type AND is_active = 1")
    suspend fun getConversationCountByType(type: String): Int
    
    /**
     * 清空所有对话（用于登出等场景）
     */
    @Query("DELETE FROM conversations")
    suspend fun clearAllConversations()
}