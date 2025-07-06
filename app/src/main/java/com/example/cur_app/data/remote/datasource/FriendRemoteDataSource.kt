package com.example.cur_app.data.remote.datasource

import com.example.cur_app.data.remote.api.FriendApiService
import com.example.cur_app.data.remote.dto.*
import com.example.cur_app.utils.ApiResult
import com.example.cur_app.utils.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 好友远程数据源
 * 负责处理所有好友相关的网络请求
 */
@Singleton
class FriendRemoteDataSource @Inject constructor(
    private val apiService: FriendApiService
) {
    
    // ========== 用户搜索 ==========
    
    /**
     * 根据UID搜索用户
     */
    suspend fun searchUserByUid(uid: String): ApiResult<FriendSearchUserInfo> {
        return safeApiCall {
            val response = apiService.searchUserByUid(uid)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: throw Exception("搜索结果为空")
            } else {
                throw Exception(response.body()?.message ?: "搜索用户失败")
            }
        }
    }
    
    // ========== 好友请求管理 ==========
    
    /**
     * 发送好友请求
     */
    suspend fun sendFriendRequest(uid: String, message: String? = null): ApiResult<Boolean> {
        return safeApiCall {
            val request = FriendRequestBody(uid = uid, message = message)
            val response = apiService.sendFriendRequest(request)
            if (response.isSuccessful && response.body()?.success == true) {
                true
            } else {
                throw Exception(response.body()?.message ?: "发送好友请求失败")
            }
        }
    }
    
    /**
     * 获取好友请求列表
     */
    suspend fun getFriendRequests(type: String = "received"): ApiResult<List<FriendRequestInfo>> {
        return safeApiCall {
            val response = apiService.getFriendRequests(type)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                throw Exception(response.body()?.message ?: "获取好友请求失败")
            }
        }
    }
    
    /**
     * 处理好友请求
     */
    suspend fun handleFriendRequest(
        requestId: String, 
        action: String, 
        message: String? = null
    ): ApiResult<Boolean> {
        return safeApiCall {
            val request = HandleFriendRequestBody(action = action, message = message)
            val response = apiService.handleFriendRequest(requestId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                true
            } else {
                throw Exception(response.body()?.message ?: "处理好友请求失败")
            }
        }
    }
    
    // ========== 好友列表管理 ==========
    
    /**
     * 获取好友列表
     */
    suspend fun getFriendList(): ApiResult<List<FriendInfo>> {
        return safeApiCall {
            val response = apiService.getFriendList()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                throw Exception(response.body()?.message ?: "获取好友列表失败")
            }
        }
    }
    
    /**
     * 删除好友
     */
    suspend fun removeFriend(friendId: String): ApiResult<Boolean> {
        return safeApiCall {
            val response = apiService.removeFriend(friendId)
            if (response.isSuccessful && response.body()?.success == true) {
                true
            } else {
                throw Exception(response.body()?.message ?: "删除好友失败")
            }
        }
    }
    
    /**
     * 更新好友设置
     */
    suspend fun updateFriendSettings(
        friendId: String,
        alias: String? = null,
        isStarred: Boolean? = null,
        isMuted: Boolean? = null
    ): ApiResult<Boolean> {
        return safeApiCall {
            val request = FriendSettingsRequest(
                alias = alias,
                isStarred = isStarred,
                isMuted = isMuted
            )
            val response = apiService.updateFriendSettings(friendId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                true
            } else {
                throw Exception(response.body()?.message ?: "更新好友设置失败")
            }
        }
    }
    
    // ========== 好友通知 ==========
    
    /**
     * 获取好友通知列表
     */
    suspend fun getFriendNotifications(
        limit: Int = 20, 
        offset: Int = 0
    ): ApiResult<List<FriendNotificationInfo>> {
        return safeApiCall {
            val response = apiService.getFriendNotifications(limit, offset)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                throw Exception(response.body()?.message ?: "获取通知失败")
            }
        }
    }
    
    /**
     * 标记通知为已读
     */
    suspend fun markNotificationAsRead(notificationId: String): ApiResult<Boolean> {
        return safeApiCall {
            val response = apiService.markNotificationAsRead(notificationId)
            if (response.isSuccessful && response.body()?.success == true) {
                true
            } else {
                throw Exception(response.body()?.message ?: "标记通知失败")
            }
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    suspend fun markAllNotificationsAsRead(): ApiResult<Boolean> {
        return safeApiCall {
            val response = apiService.markAllNotificationsAsRead()
            if (response.isSuccessful && response.body()?.success == true) {
                true
            } else {
                throw Exception(response.body()?.message ?: "标记所有通知失败")
            }
        }
    }
    
    // ========== 健康检查 ==========
    
    /**
     * 检查好友API健康状态
     */
    suspend fun healthCheck(): ApiResult<Boolean> {
        return safeApiCall {
            val response = apiService.healthCheck()
            response.isSuccessful && response.body()?.success == true
        }
    }
}