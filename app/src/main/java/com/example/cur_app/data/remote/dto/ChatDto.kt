package com.example.cur_app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== 聊天相关 ==========

/**
 * 聊天消息响应
 */
data class ChatMessagesResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ChatMessagesData? = null
)

/**
 * 聊天消息数据
 */
data class ChatMessagesData(
    @SerializedName("messages")
    val messages: List<RemoteChatMessage>,
    @SerializedName("conversation")
    val conversation: RemoteConversation? = null,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("hasMore")
    val hasMore: Boolean
)

/**
 * 远程聊天消息
 */
data class RemoteChatMessage(
    @SerializedName("messageId")
    val messageId: String,
    @SerializedName("conversationId")
    val conversationId: String,
    @SerializedName("senderId")
    val senderId: String,
    @SerializedName("senderInfo")
    val senderInfo: RemoteUserProfile? = null,
    @SerializedName("receiverId")
    val receiverId: String? = null,
    @SerializedName("content")
    val content: String,
    @SerializedName("messageType")
    val messageType: String = "TEXT", // TEXT, IMAGE, AUDIO, VIDEO, FILE, LOCATION, STICKER
    @SerializedName("mediaUrl")
    val mediaUrl: String? = null,
    @SerializedName("mediaMetadata")
    val mediaMetadata: MediaMetadata? = null,
    @SerializedName("replyTo")
    val replyTo: String? = null, // 回复的消息ID
    @SerializedName("isRead")
    val isRead: Boolean = false,
    @SerializedName("readAt")
    val readAt: Long? = null,
    @SerializedName("isDelivered")
    val isDelivered: Boolean = false,
    @SerializedName("deliveredAt")
    val deliveredAt: Long? = null,
    @SerializedName("isDeleted")
    val isDeleted: Boolean = false,
    @SerializedName("deletedAt")
    val deletedAt: Long? = null,
    @SerializedName("isEdited")
    val isEdited: Boolean = false,
    @SerializedName("editedAt")
    val editedAt: Long? = null,
    @SerializedName("reactions")
    val reactions: List<MessageReaction> = emptyList(),
    @SerializedName("mentions")
    val mentions: List<String> = emptyList(), // 提及的用户ID
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("localId")
    val localId: String? = null // 本地临时ID
)

/**
 * 媒体元数据
 */
data class MediaMetadata(
    @SerializedName("fileName")
    val fileName: String? = null,
    @SerializedName("fileSize")
    val fileSize: Long? = null,
    @SerializedName("duration")
    val duration: Long? = null, // 音视频时长(毫秒)
    @SerializedName("width")
    val width: Int? = null,
    @SerializedName("height")
    val height: Int? = null,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,
    @SerializedName("mimeType")
    val mimeType: String? = null
)

/**
 * 消息反应
 */
data class MessageReaction(
    @SerializedName("emoji")
    val emoji: String,
    @SerializedName("count")
    val count: Int,
    @SerializedName("users")
    val users: List<String> = emptyList(), // 用户ID列表
    @SerializedName("isMyReaction")
    val isMyReaction: Boolean = false
)

/**
 * 发送消息请求
 */
data class SendMessageRequest(
    @SerializedName("conversationId")
    val conversationId: String,
    @SerializedName("receiverId")
    val receiverId: String? = null,
    @SerializedName("content")
    val content: String,
    @SerializedName("messageType")
    val messageType: String = "TEXT",
    @SerializedName("mediaUrl")
    val mediaUrl: String? = null,
    @SerializedName("mediaMetadata")
    val mediaMetadata: MediaMetadata? = null,
    @SerializedName("replyTo")
    val replyTo: String? = null,
    @SerializedName("mentions")
    val mentions: List<String> = emptyList(),
    @SerializedName("localId")
    val localId: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 发送消息响应
 */
data class SendMessageResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: SendMessageData? = null
)

/**
 * 发送消息数据
 */
data class SendMessageData(
    @SerializedName("message")
    val message: RemoteChatMessage,
    @SerializedName("conversation")
    val conversation: RemoteConversation? = null
)

/**
 * 对话列表响应
 */
data class ConversationsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ConversationsData? = null
)

/**
 * 对话列表数据
 */
data class ConversationsData(
    @SerializedName("conversations")
    val conversations: List<RemoteConversation>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("hasMore")
    val hasMore: Boolean
)

/**
 * 远程对话
 */
data class RemoteConversation(
    @SerializedName("conversationId")
    val conversationId: String,
    @SerializedName("type")
    val type: String, // PRIVATE, GROUP, AI
    @SerializedName("name")
    val name: String? = null, // 群聊名称
    @SerializedName("avatar")
    val avatar: String? = null, // 群聊头像
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("participants")
    val participants: List<ConversationParticipant> = emptyList(),
    @SerializedName("creatorId")
    val creatorId: String? = null,
    @SerializedName("adminIds")
    val adminIds: List<String> = emptyList(),
    @SerializedName("lastMessage")
    val lastMessage: RemoteChatMessage? = null,
    @SerializedName("unreadCount")
    val unreadCount: Int = 0,
    @SerializedName("isPinned")
    val isPinned: Boolean = false,
    @SerializedName("isMuted")
    val isMuted: Boolean = false,
    @SerializedName("isArchived")
    val isArchived: Boolean = false,
    @SerializedName("settings")
    val settings: ConversationSettings? = null,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("updatedAt")
    val updatedAt: Long
)

/**
 * 对话参与者
 */
data class ConversationParticipant(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("user")
    val user: RemoteUserProfile? = null,
    @SerializedName("role")
    val role: String = "member", // owner, admin, member
    @SerializedName("nickname")
    val nickname: String? = null, // 群昵称
    @SerializedName("joinedAt")
    val joinedAt: Long,
    @SerializedName("lastReadTime")
    val lastReadTime: Long? = null,
    @SerializedName("isMuted")
    val isMuted: Boolean = false
)

/**
 * 对话设置
 */
data class ConversationSettings(
    @SerializedName("allowInvite")
    val allowInvite: Boolean = true,
    @SerializedName("allowMemberModify")
    val allowMemberModify: Boolean = true,
    @SerializedName("requireApproval")
    val requireApproval: Boolean = false,
    @SerializedName("messageHistoryVisible")
    val messageHistoryVisible: Boolean = true,
    @SerializedName("autoDelete")
    val autoDelete: Boolean = false,
    @SerializedName("autoDeleteDays")
    val autoDeleteDays: Int = 7
)

/**
 * 创建对话请求
 */
data class CreateConversationRequest(
    @SerializedName("type")
    val type: String, // PRIVATE, GROUP
    @SerializedName("participantIds")
    val participantIds: List<String>,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("settings")
    val settings: ConversationSettings? = null
)

/**
 * 创建对话响应
 */
data class CreateConversationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: RemoteConversation? = null
)