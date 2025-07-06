package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.ChatUserEntity
import kotlinx.coroutines.flow.Flow

/**
 * 聊天用户数据访问对象
 */
@Dao
interface ChatUserDao {
    
    @Query("SELECT * FROM chat_users ORDER BY nickname ASC")
    fun getAllUsers(): Flow<List<ChatUserEntity>>
    
    @Query("SELECT * FROM chat_users WHERE userId = :userId")
    suspend fun getUserById(userId: String): ChatUserEntity?
    
    @Query("SELECT * FROM chat_users WHERE isAiBot = :isAi ORDER BY nickname ASC")
    fun getUsersByType(isAi: Boolean): Flow<List<ChatUserEntity>>
    
    @Query("SELECT * FROM chat_users WHERE isAiBot = 1 ORDER BY nickname ASC")
    fun getAiUsers(): Flow<List<ChatUserEntity>>
    
    @Query("SELECT * FROM chat_users WHERE isAiBot = 0 ORDER BY nickname ASC")
    fun getHumanUsers(): Flow<List<ChatUserEntity>>
    
    @Query("SELECT * FROM chat_users WHERE isOnline = 1 ORDER BY lastSeenTime DESC")
    fun getOnlineUsers(): Flow<List<ChatUserEntity>>
    
    @Query("SELECT * FROM chat_users WHERE aiType = :aiType")
    suspend fun getUsersByAiType(aiType: String): List<ChatUserEntity>
    
    @Query("SELECT * FROM chat_users WHERE nickname LIKE '%' || :searchText || '%' OR realName LIKE '%' || :searchText || '%'")
    suspend fun searchUsers(searchText: String): List<ChatUserEntity>
    
    @Query("SELECT * FROM chat_users WHERE email = :email")
    suspend fun getUserByEmail(email: String): ChatUserEntity?
    
    @Query("SELECT * FROM chat_users WHERE phone = :phone")
    suspend fun getUserByPhone(phone: String): ChatUserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: ChatUserEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<ChatUserEntity>): List<Long>
    
    @Update
    suspend fun updateUser(user: ChatUserEntity)
    
    @Query("UPDATE chat_users SET isOnline = :online, lastSeenTime = :time, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun updateOnlineStatus(userId: String, online: Boolean, time: Long = System.currentTimeMillis(), updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_users SET status = :status, statusMessage = :message, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun updateStatus(userId: String, status: String, message: String = "", updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_users SET nickname = :nickname, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun updateNickname(userId: String, nickname: String, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_users SET avatar = :avatar, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun updateAvatar(userId: String, avatar: String, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_users SET bio = :bio, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun updateBio(userId: String, bio: String, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_users SET totalMessages = totalMessages + :count, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun incrementMessageCount(userId: String, count: Int = 1, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_users SET totalConversations = totalConversations + :count, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun incrementConversationCount(userId: String, count: Int = 1, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_users SET averageResponseTime = :time, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun updateAverageResponseTime(userId: String, time: Long, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_users SET isVerified = :verified, verificationLevel = :level, updatedAt = :updateTime WHERE userId = :userId")
    suspend fun updateVerificationStatus(userId: String, verified: Boolean, level: String, updateTime: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteUser(user: ChatUserEntity)
    
    @Query("DELETE FROM chat_users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("DELETE FROM chat_users")
    suspend fun deleteAllUsers()
    
    @Query("SELECT COUNT(*) FROM chat_users WHERE isOnline = 1")
    suspend fun getOnlineUserCount(): Int
    
    @Query("SELECT COUNT(*) FROM chat_users WHERE isAiBot = 1")
    suspend fun getAiUserCount(): Int
    
    @Query("SELECT COUNT(*) FROM chat_users WHERE isAiBot = 0")
    suspend fun getHumanUserCount(): Int
    
    @Query("DELETE FROM chat_users WHERE isAiBot = 0")
    suspend fun deleteNonAiUsers()
}