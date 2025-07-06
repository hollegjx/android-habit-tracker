package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.FriendshipEntity
import kotlinx.coroutines.flow.Flow

/**
 * 好友关系数据访问对象
 */
@Dao
interface FriendshipDao {
    
    /**
     * 插入好友关系
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendship(friendship: FriendshipEntity): Long
    
    /**
     * 插入多个好友关系
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendships(friendships: List<FriendshipEntity>)
    
    /**
     * 更新好友关系
     */
    @Update
    suspend fun updateFriendship(friendship: FriendshipEntity)
    
    /**
     * 删除好友关系
     */
    @Delete
    suspend fun deleteFriendship(friendship: FriendshipEntity)
    
    /**
     * 根据ID删除好友关系
     */
    @Query("DELETE FROM friendships WHERE id = :friendshipId")
    suspend fun deleteFriendshipById(friendshipId: Long)
    
    /**
     * 根据用户ID删除所有相关好友关系
     */
    @Query("DELETE FROM friendships WHERE requester_id = :userId OR addressee_id = :userId")
    suspend fun deleteFriendshipsByUserId(userId: String)
    
    /**
     * 根据ID获取好友关系
     */
    @Query("SELECT * FROM friendships WHERE id = :friendshipId")
    suspend fun getFriendshipById(friendshipId: Long): FriendshipEntity?
    
    /**
     * 获取两个用户之间的好友关系
     */
    @Query("""
        SELECT * FROM friendships 
        WHERE (requester_id = :userId1 AND addressee_id = :userId2) 
           OR (requester_id = :userId2 AND addressee_id = :userId1)
        LIMIT 1
    """)
    suspend fun getFriendshipBetweenUsers(userId1: String, userId2: String): FriendshipEntity?
    
    /**
     * 获取用户的所有好友关系（实时更新）
     */
    @Query("""
        SELECT * FROM friendships 
        WHERE (requester_id = :userId OR addressee_id = :userId) 
          AND status = :status
        ORDER BY 
            CASE WHEN is_starred = 1 THEN 0 ELSE 1 END,
            CASE WHEN last_message_at IS NOT NULL THEN last_message_at ELSE created_at END DESC
    """)
    fun getFriendshipsByStatus(userId: String, status: String): Flow<List<FriendshipEntity>>
    
    /**
     * 获取用户的已接受好友关系
     */
    @Query("""
        SELECT * FROM friendships 
        WHERE (requester_id = :userId OR addressee_id = :userId) 
          AND status = 'accepted'
        ORDER BY 
            CASE WHEN is_starred = 1 THEN 0 ELSE 1 END,
            CASE WHEN last_message_at IS NOT NULL THEN last_message_at ELSE created_at END DESC
    """)
    fun getAcceptedFriendships(userId: String): Flow<List<FriendshipEntity>>
    
    /**
     * 获取用户的待处理好友请求
     */
    @Query("""
        SELECT * FROM friendships 
        WHERE addressee_id = :userId AND status = 'pending'
        ORDER BY created_at DESC
    """)
    fun getPendingFriendRequests(userId: String): Flow<List<FriendshipEntity>>
    
    /**
     * 获取用户发送的好友请求
     */
    @Query("""
        SELECT * FROM friendships 
        WHERE requester_id = :userId AND status = 'pending'
        ORDER BY created_at DESC
    """)
    fun getSentFriendRequests(userId: String): Flow<List<FriendshipEntity>>
    
    /**
     * 获取特别关注的好友
     */
    @Query("""
        SELECT * FROM friendships 
        WHERE (requester_id = :userId OR addressee_id = :userId) 
          AND status = 'accepted' 
          AND is_starred = 1
        ORDER BY last_message_at DESC
    """)
    fun getStarredFriendships(userId: String): Flow<List<FriendshipEntity>>
    
    /**
     * 搜索好友（根据备注名）
     */
    @Query("""
        SELECT * FROM friendships 
        WHERE (requester_id = :userId OR addressee_id = :userId) 
          AND status = 'accepted'
          AND friendship_alias LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN is_starred = 1 THEN 0 ELSE 1 END,
            last_message_at DESC
    """)
    fun searchFriendships(userId: String, query: String): Flow<List<FriendshipEntity>>
    
    /**
     * 更新好友关系的未读消息数
     */
    @Query("UPDATE friendships SET unread_count = :count WHERE id = :friendshipId")
    suspend fun updateUnreadCount(friendshipId: Long, count: Int)
    
    /**
     * 更新好友关系的最后消息时间
     */
    @Query("UPDATE friendships SET last_message_at = :timestamp WHERE id = :friendshipId")
    suspend fun updateLastMessageAt(friendshipId: Long, timestamp: Long)
    
    /**
     * 更新好友关系的最后阅读时间
     */
    @Query("UPDATE friendships SET last_read_at = :timestamp WHERE id = :friendshipId")
    suspend fun updateLastReadAt(friendshipId: Long, timestamp: Long)
    
    /**
     * 设置/取消特别关注
     */
    @Query("UPDATE friendships SET is_starred = :isStarred WHERE id = :friendshipId")
    suspend fun updateStarred(friendshipId: Long, isStarred: Boolean)
    
    /**
     * 设置/取消静音
     */
    @Query("UPDATE friendships SET is_muted = :isMuted WHERE id = :friendshipId")
    suspend fun updateMuted(friendshipId: Long, isMuted: Boolean)
    
    /**
     * 更新好友备注名
     */
    @Query("UPDATE friendships SET friendship_alias = :alias WHERE id = :friendshipId")
    suspend fun updateFriendshipAlias(friendshipId: Long, alias: String?)
    
    /**
     * 更新好友关系状态
     */
    @Query("UPDATE friendships SET status = :status, updated_at = :timestamp WHERE id = :friendshipId")
    suspend fun updateFriendshipStatus(friendshipId: Long, status: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 获取好友总数
     */
    @Query("""
        SELECT COUNT(*) FROM friendships 
        WHERE (requester_id = :userId OR addressee_id = :userId) 
          AND status = 'accepted'
    """)
    suspend fun getFriendCount(userId: String): Int
    
    /**
     * 获取未处理的好友请求数量
     */
    @Query("SELECT COUNT(*) FROM friendships WHERE addressee_id = :userId AND status = 'pending'")
    suspend fun getPendingRequestCount(userId: String): Int
    
    /**
     * 清空所有好友关系（用于登出等场景）
     */
    @Query("DELETE FROM friendships")
    suspend fun clearAllFriendships()
}