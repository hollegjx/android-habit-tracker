package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.ConversationParticipantEntity
import kotlinx.coroutines.flow.Flow

/**
 * 对话参与者数据访问对象
 */
@Dao
interface ConversationParticipantDao {
    
    /**
     * 插入参与者
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ConversationParticipantEntity): Long
    
    /**
     * 插入多个参与者
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<ConversationParticipantEntity>)
    
    /**
     * 更新参与者
     */
    @Update
    suspend fun updateParticipant(participant: ConversationParticipantEntity)
    
    /**
     * 删除参与者
     */
    @Delete
    suspend fun deleteParticipant(participant: ConversationParticipantEntity)
    
    /**
     * 根据ID删除参与者
     */
    @Query("DELETE FROM conversation_participants WHERE id = :participantId")
    suspend fun deleteParticipantById(participantId: Long)
    
    /**
     * 从对话中移除用户
     */
    @Query("DELETE FROM conversation_participants WHERE conversation_id = :conversationId AND user_id = :userId")
    suspend fun removeUserFromConversation(conversationId: Long, userId: String)
    
    /**
     * 删除对话的所有参与者
     */
    @Query("DELETE FROM conversation_participants WHERE conversation_id = :conversationId")
    suspend fun deleteParticipantsByConversationId(conversationId: Long)
    
    /**
     * 根据用户ID删除所有参与记录
     */
    @Query("DELETE FROM conversation_participants WHERE user_id = :userId")
    suspend fun deleteParticipantsByUserId(userId: String)
    
    /**
     * 根据ID获取参与者
     */
    @Query("SELECT * FROM conversation_participants WHERE id = :participantId")
    suspend fun getParticipantById(participantId: Long): ConversationParticipantEntity?
    
    /**
     * 获取对话的所有参与者
     */
    @Query("""
        SELECT * FROM conversation_participants 
        WHERE conversation_id = :conversationId 
        ORDER BY 
            CASE role
                WHEN 'owner' THEN 1
                WHEN 'admin' THEN 2
                WHEN 'member' THEN 3
                ELSE 4
            END,
            joined_at ASC
    """)
    fun getParticipantsByConversationId(conversationId: Long): Flow<List<ConversationParticipantEntity>>
    
    /**
     * 获取用户参与的所有对话
     */
    @Query("""
        SELECT * FROM conversation_participants 
        WHERE user_id = :userId 
        ORDER BY joined_at DESC
    """)
    fun getParticipantsByUserId(userId: String): Flow<List<ConversationParticipantEntity>>
    
    /**
     * 获取对话中特定角色的参与者
     */
    @Query("""
        SELECT * FROM conversation_participants 
        WHERE conversation_id = :conversationId AND role = :role
        ORDER BY joined_at ASC
    """)
    fun getParticipantsByRole(conversationId: Long, role: String): Flow<List<ConversationParticipantEntity>>
    
    /**
     * 检查用户是否是对话参与者
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM conversation_participants 
        WHERE conversation_id = :conversationId AND user_id = :userId
    """)
    suspend fun isUserInConversation(conversationId: Long, userId: String): Boolean
    
    /**
     * 获取用户在对话中的角色
     */
    @Query("""
        SELECT role FROM conversation_participants 
        WHERE conversation_id = :conversationId AND user_id = :userId
    """)
    suspend fun getUserRoleInConversation(conversationId: Long, userId: String): String?
    
    /**
     * 获取对话的参与者数量
     */
    @Query("SELECT COUNT(*) FROM conversation_participants WHERE conversation_id = :conversationId")
    suspend fun getParticipantCount(conversationId: Long): Int
    
    /**
     * 获取特定角色的参与者数量
     */
    @Query("""
        SELECT COUNT(*) FROM conversation_participants 
        WHERE conversation_id = :conversationId AND role = :role
    """)
    suspend fun getParticipantCountByRole(conversationId: Long, role: String): Int
    
    /**
     * 更新参与者角色
     */
    @Query("""
        UPDATE conversation_participants 
        SET role = :role, updated_at = :timestamp
        WHERE conversation_id = :conversationId AND user_id = :userId
    """)
    suspend fun updateUserRole(
        conversationId: Long, 
        userId: String, 
        role: String,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 更新最后阅读时间
     */
    @Query("""
        UPDATE conversation_participants 
        SET last_read_at = :timestamp, updated_at = :updatedAt
        WHERE conversation_id = :conversationId AND user_id = :userId
    """)
    suspend fun updateLastReadAt(
        conversationId: Long,
        userId: String,
        timestamp: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * 设置/取消静音
     */
    @Query("""
        UPDATE conversation_participants 
        SET is_muted = :isMuted, updated_at = :timestamp
        WHERE conversation_id = :conversationId AND user_id = :userId
    """)
    suspend fun updateMuted(
        conversationId: Long,
        userId: String,
        isMuted: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 设置/取消置顶
     */
    @Query("""
        UPDATE conversation_participants 
        SET is_pinned = :isPinned, updated_at = :timestamp
        WHERE conversation_id = :conversationId AND user_id = :userId
    """)
    suspend fun updatePinned(
        conversationId: Long,
        userId: String,
        isPinned: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 获取用户置顶的对话
     */
    @Query("""
        SELECT * FROM conversation_participants 
        WHERE user_id = :userId AND is_pinned = 1
        ORDER BY updated_at DESC
    """)
    fun getPinnedConversations(userId: String): Flow<List<ConversationParticipantEntity>>
    
    /**
     * 获取用户静音的对话
     */
    @Query("""
        SELECT * FROM conversation_participants 
        WHERE user_id = :userId AND is_muted = 1
        ORDER BY updated_at DESC
    """)
    fun getMutedConversations(userId: String): Flow<List<ConversationParticipantEntity>>
    
    /**
     * 清空所有参与者记录（用于登出等场景）
     */
    @Query("DELETE FROM conversation_participants")
    suspend fun clearAllParticipants()
}