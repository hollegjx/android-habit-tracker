package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.FriendNotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 好友通知数据访问对象
 */
@Dao
interface FriendNotificationDao {
    
    /**
     * 插入通知
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: FriendNotificationEntity): Long
    
    /**
     * 插入多个通知
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<FriendNotificationEntity>)
    
    /**
     * 更新通知
     */
    @Update
    suspend fun updateNotification(notification: FriendNotificationEntity)
    
    /**
     * 删除通知
     */
    @Delete
    suspend fun deleteNotification(notification: FriendNotificationEntity)
    
    /**
     * 根据ID删除通知
     */
    @Query("DELETE FROM friend_notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: Long)
    
    /**
     * 根据用户ID删除所有通知
     */
    @Query("DELETE FROM friend_notifications WHERE user_id = :userId")
    suspend fun deleteNotificationsByUserId(userId: String)
    
    /**
     * 根据好友关系ID删除通知
     */
    @Query("DELETE FROM friend_notifications WHERE friendship_id = :friendshipId")
    suspend fun deleteNotificationsByFriendshipId(friendshipId: Long)
    
    /**
     * 根据ID获取通知
     */
    @Query("SELECT * FROM friend_notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: Long): FriendNotificationEntity?
    
    /**
     * 获取用户的所有通知（实时更新）
     */
    @Query("""
        SELECT * FROM friend_notifications 
        WHERE user_id = :userId 
        ORDER BY created_at DESC
    """)
    fun getNotificationsByUserId(userId: String): Flow<List<FriendNotificationEntity>>
    
    /**
     * 获取用户的未读通知
     */
    @Query("""
        SELECT * FROM friend_notifications 
        WHERE user_id = :userId AND is_read = 0
        ORDER BY created_at DESC
    """)
    fun getUnreadNotifications(userId: String): Flow<List<FriendNotificationEntity>>
    
    /**
     * 获取特定类型的通知
     */
    @Query("""
        SELECT * FROM friend_notifications 
        WHERE user_id = :userId AND type = :type
        ORDER BY created_at DESC
    """)
    fun getNotificationsByType(userId: String, type: String): Flow<List<FriendNotificationEntity>>
    
    /**
     * 获取最近的通知（限制数量）
     */
    @Query("""
        SELECT * FROM friend_notifications 
        WHERE user_id = :userId 
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    suspend fun getRecentNotifications(userId: String, limit: Int = 20): List<FriendNotificationEntity>
    
    /**
     * 标记通知为已读
     */
    @Query("""
        UPDATE friend_notifications 
        SET is_read = 1, read_at = :readAt, updated_at = :timestamp
        WHERE id = :notificationId
    """)
    suspend fun markAsRead(
        notificationId: Long, 
        readAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 标记用户的所有通知为已读
     */
    @Query("""
        UPDATE friend_notifications 
        SET is_read = 1, read_at = :readAt, updated_at = :timestamp
        WHERE user_id = :userId AND is_read = 0
    """)
    suspend fun markAllAsRead(
        userId: String,
        readAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 标记特定类型的通知为已读
     */
    @Query("""
        UPDATE friend_notifications 
        SET is_read = 1, read_at = :readAt, updated_at = :timestamp
        WHERE user_id = :userId AND type = :type AND is_read = 0
    """)
    suspend fun markTypeAsRead(
        userId: String,
        type: String,
        readAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 获取未读通知数量
     */
    @Query("SELECT COUNT(*) FROM friend_notifications WHERE user_id = :userId AND is_read = 0")
    suspend fun getUnreadCount(userId: String): Int
    
    /**
     * 获取特定类型的未读通知数量
     */
    @Query("""
        SELECT COUNT(*) FROM friend_notifications 
        WHERE user_id = :userId AND type = :type AND is_read = 0
    """)
    suspend fun getUnreadCountByType(userId: String, type: String): Int
    
    /**
     * 删除过期的通知（比如30天前的已读通知）
     */
    @Query("""
        DELETE FROM friend_notifications 
        WHERE is_read = 1 AND read_at < :expireTime
    """)
    suspend fun deleteExpiredNotifications(expireTime: Long)
    
    /**
     * 清空所有通知（用于登出等场景）
     */
    @Query("DELETE FROM friend_notifications")
    suspend fun clearAllNotifications()
}