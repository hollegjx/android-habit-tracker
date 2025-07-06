package com.example.cur_app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== 新好友API相关数据传输对象 ==========

/**
 * 好友搜索响应
 */
data class FriendSearchResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: FriendSearchUserInfo? = null
)

/**
 * 搜索到的用户信息
 */
data class FriendSearchUserInfo(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("isOnline")
    val isOnline: Boolean = false,
    @SerializedName("friendshipStatus")
    val friendshipStatus: String? = null, // null, pending, accepted, declined, blocked
    @SerializedName("friendshipId")
    val friendshipId: String? = null,
    @SerializedName("canSendRequest")
    val canSendRequest: Boolean = true
)

/**
 * 发送好友请求体
 */
data class FriendRequestBody(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("message")
    val message: String? = null
)

/**
 * 处理好友请求体
 */
data class HandleFriendRequestBody(
    @SerializedName("action")
    val action: String, // accept, decline
    @SerializedName("message")
    val message: String? = null
)

/**
 * 好友请求列表响应
 */
data class FriendRequestsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<FriendRequestInfo> = emptyList()
)

/**
 * 好友请求信息
 */
data class FriendRequestInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("isRead")
    val isRead: Boolean = false,
    @SerializedName("user")
    val user: FriendUserInfo
)

/**
 * 好友列表响应
 */
data class FriendListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<FriendInfo> = emptyList()
)

/**
 * 好友信息
 */
data class FriendInfo(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("isOnline")
    val isOnline: Boolean = false,
    @SerializedName("lastSeenTime")
    val lastSeenTime: Long? = null,
    @SerializedName("friendSince")
    val friendSince: Long,
    @SerializedName("friendshipId")
    val friendshipId: String,
    @SerializedName("conversationId")
    val conversationId: String? = null,
    @SerializedName("isStarred")
    val isStarred: Boolean = false,
    @SerializedName("isMuted")
    val isMuted: Boolean = false,
    @SerializedName("unreadCount")
    val unreadCount: Int = 0,
    @SerializedName("lastMessageAt")
    val lastMessageAt: Long? = null
)

/**
 * 好友用户信息
 */
data class FriendUserInfo(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("isOnline")
    val isOnline: Boolean = false
)

/**
 * 好友设置请求体
 */
data class FriendSettingsRequest(
    @SerializedName("alias")
    val alias: String? = null,
    @SerializedName("isStarred")
    val isStarred: Boolean? = null,
    @SerializedName("isMuted")
    val isMuted: Boolean? = null
)

/**
 * 好友通知列表响应
 */
data class FriendNotificationsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<FriendNotificationInfo> = emptyList()
)

/**
 * 好友通知信息
 */
data class FriendNotificationInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String, // request, accepted, declined, blocked
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("isRead")
    val isRead: Boolean = false,
    @SerializedName("readAt")
    val readAt: Long? = null,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("fromUser")
    val fromUser: FriendUserInfo
)