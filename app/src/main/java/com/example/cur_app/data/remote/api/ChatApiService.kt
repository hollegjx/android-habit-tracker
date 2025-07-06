package com.example.cur_app.data.remote.api

import com.example.cur_app.data.remote.dto.BaseResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 聊天服务API接口
 * 调用服务器的聊天和AI功能，以及好友聊天功能
 */
interface ChatApiService {
    
    // ========== AI聊天功能 ==========
    
    /**
     * 与AI角色聊天
     */
    @POST("chat/ai/chat")
    @Headers("Content-Type: application/json")
    suspend fun chatWithAI(
        @Body request: AiChatRequest
    ): Response<AiChatResponse>
    
    // ========== 好友聊天功能 ==========
    
    /**
     * 获取对话列表
     */
    @GET("chat/conversations")
    suspend fun getConversations(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ConversationsResponse>
    
    /**
     * 创建对话
     */
    @POST("chat/conversations")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<CreateConversationResponse>
    
    /**
     * 获取聊天消息
     */
    @GET("chat/conversations/{conversationId}/messages")
    suspend fun getChatMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): Response<ChatMessagesResponse>
    
    /**
     * 发送消息
     */
    @POST("chat/messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
    
    /**
     * 标记消息为已读
     */
    @POST("chat/conversations/{conversationId}/read")
    suspend fun markAsRead(
        @Path("conversationId") conversationId: String
    ): Response<BaseResponse>
}

/**
 * AI聊天请求数据类
 */
data class AiChatRequest(
    val characterId: String,
    val message: String
)

/**
 * AI聊天响应数据类
 */
data class AiChatResponse(
    val success: Boolean,
    val data: AiChatData? = null,
    val message: String? = null
)

data class AiChatData(
    val conversationId: String,
    val message: String,
    val character: AiCharacterInfo
)

data class AiCharacterInfo(
    val id: String,
    val name: String,
    val avatar: String? = null
)

// ========== 好友聊天数据传输对象 ==========

/**
 * 对话列表响应
 */
data class ConversationsResponse(
    val success: Boolean,
    val data: List<ConversationInfo> = emptyList(),
    val message: String? = null
)

/**
 * 对话信息
 */
data class ConversationInfo(
    val conversationId: String,
    val type: String, // private, group, ai
    val name: String? = null,
    val participants: List<ConversationParticipant> = emptyList(),
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 对话参与者
 */
data class ConversationParticipant(
    val userId: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val role: String = "member", // owner, admin, member
    val joinedAt: Long
)

/**
 * 创建对话请求
 */
data class CreateConversationRequest(
    val type: String, // private, group
    val participantIds: List<String>,
    val name: String? = null
)

/**
 * 创建对话响应
 */
data class CreateConversationResponse(
    val success: Boolean,
    val data: ConversationInfo? = null,
    val message: String? = null
)

/**
 * 聊天消息列表响应
 */
data class ChatMessagesResponse(
    val success: Boolean,
    val data: List<ChatMessageInfo> = emptyList(),
    val message: String? = null
)

/**
 * 聊天消息信息
 */
data class ChatMessageInfo(
    val messageId: String,
    val conversationId: String,
    val senderId: String,
    val senderNickname: String? = null,
    val senderAvatar: String? = null,
    val content: String,
    val messageType: String = "text", // text, image, file, system
    val status: String = "sent", // sent, delivered, read
    val replyToId: String? = null,
    val createdAt: Long
)

/**
 * 发送消息请求
 */
data class SendMessageRequest(
    val conversationId: String,
    val content: String,
    val messageType: String = "text",
    val replyToId: String? = null
)

/**
 * 发送消息响应
 */
data class SendMessageResponse(
    val success: Boolean,
    val data: ChatMessageInfo? = null,
    val message: String? = null
)