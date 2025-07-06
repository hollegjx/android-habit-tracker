package com.example.cur_app.data.repository

import com.example.cur_app.data.remote.datasource.FriendRemoteDataSource
import com.example.cur_app.data.remote.dto.*
import com.example.cur_app.utils.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 好友管理数据仓库
 * 负责用户搜索、好友请求、好友列表等功能
 * 使用新的好友API服务和数据源
 */
@Singleton
class FriendRepository @Inject constructor(
    private val friendRemoteDataSource: FriendRemoteDataSource
) {
    
    // ========== 用户搜索 ==========
    
    /**
     * 根据UID搜索用户
     */
    suspend fun searchUserByUid(uid: String): Result<FriendSearchUserInfo> {
        return when (val result = friendRemoteDataSource.searchUserByUid(uid)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    // ========== 好友请求 ==========
    
    /**
     * 发送好友请求
     */
    suspend fun sendFriendRequest(
        uid: String,
        message: String? = null
    ): Result<Unit> {
        return when (val result = friendRemoteDataSource.sendFriendRequest(uid, message)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    /**
     * 处理好友请求（接受/拒绝）
     */
    suspend fun handleFriendRequest(
        requestId: String,
        action: String, // accept, decline
        message: String? = null
    ): Result<Unit> {
        return when (val result = friendRemoteDataSource.handleFriendRequest(requestId, action, message)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    // ========== 好友列表 ==========
    
    /**
     * 获取好友列表
     */
    suspend fun getFriendList(): Result<List<FriendInfo>> {
        return when (val result = friendRemoteDataSource.getFriendList()) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    /**
     * 获取好友请求列表
     */
    suspend fun getFriendRequests(
        type: String = "received" // received, sent
    ): Result<List<FriendRequestInfo>> {
        return when (val result = friendRemoteDataSource.getFriendRequests(type)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    /**
     * 删除好友
     */
    suspend fun removeFriend(friendId: String): Result<Unit> {
        return when (val result = friendRemoteDataSource.removeFriend(friendId)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    // ========== 好友设置 ==========
    
    /**
     * 更新好友设置
     */
    suspend fun updateFriendSettings(
        friendId: String,
        alias: String? = null,
        isStarred: Boolean? = null,
        isMuted: Boolean? = null
    ): Result<Unit> {
        return when (val result = friendRemoteDataSource.updateFriendSettings(friendId, alias, isStarred, isMuted)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    // ========== 好友通知 ==========
    
    /**
     * 获取好友通知列表
     */
    suspend fun getFriendNotifications(
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<FriendNotificationInfo>> {
        return when (val result = friendRemoteDataSource.getFriendNotifications(limit, offset)) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    /**
     * 标记通知为已读
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return when (val result = friendRemoteDataSource.markNotificationAsRead(notificationId)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    suspend fun markAllNotificationsAsRead(): Result<Unit> {
        return when (val result = friendRemoteDataSource.markAllNotificationsAsRead()) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    // ========== 健康检查 ==========
    
    /**
     * 检查好友API健康状态
     */
    suspend fun healthCheck(): Result<Boolean> {
        return when (val result = friendRemoteDataSource.healthCheck()) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            is ApiResult.Loading -> Result.failure(Exception("Loading"))
        }
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 检查是否已经是好友
     */
    suspend fun isFriend(userId: String): Result<Boolean> {
        return try {
            val friendListResult = getFriendList()
            friendListResult.fold(
                onSuccess = { friends ->
                    val isFriend = friends.any { it.userId == userId }
                    Result.success(isFriend)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检查是否已经发送过好友请求
     */
    suspend fun hasSentFriendRequest(userId: String): Result<Boolean> {
        return try {
            val requestsResult = getFriendRequests(type = "sent")
            requestsResult.fold(
                onSuccess = { requests ->
                    val hasSent = requests.any { request ->
                        request.user.userId == userId && request.status == "pending"
                    }
                    Result.success(hasSent)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}