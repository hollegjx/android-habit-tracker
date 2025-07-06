package com.example.cur_app.data.remote.datasource

import com.example.cur_app.data.remote.api.ChatApiService
import com.example.cur_app.data.remote.api.*
import com.example.cur_app.utils.ApiResult
import com.example.cur_app.utils.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 聊天远程数据源
 * 负责处理所有聊天相关的网络请求
 */
@Singleton
class ChatRemoteDataSource @Inject constructor(
    private val chatApiService: ChatApiService
) {
    
    // ========== AI聊天功能 ==========
    
    /**
     * 与AI角色聊天
     */
    suspend fun chatWithAI(characterId: String, message: String): ApiResult<AiChatData> {
        return safeApiCall {
            val request = AiChatRequest(characterId = characterId, message = message)
            val response = chatApiService.chatWithAI(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: throw Exception("AI聊天响应数据为空")
            } else {
                throw Exception(response.body()?.message ?: "AI聊天失败")
            }
        }
    }
    
    // ========== 好友聊天功能 ==========
    
    /**
     * 获取对话列表
     */
    suspend fun getConversations(page: Int = 1, size: Int = 20): ApiResult<List<ConversationInfo>> {
        return safeApiCall {
            val response = chatApiService.getConversations(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                throw Exception(response.body()?.message ?: "获取对话列表失败")
            }
        }
    }
    
    /**
     * 创建对话
     */
    suspend fun createConversation(
        type: String,
        participantIds: List<String>,
        name: String? = null
    ): ApiResult<ConversationInfo> {
        return safeApiCall {
            val request = CreateConversationRequest(
                type = type,
                participantIds = participantIds,
                name = name
            )
            val response = chatApiService.createConversation(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: throw Exception("创建对话响应数据为空")
            } else {
                throw Exception(response.body()?.message ?: "创建对话失败")
            }
        }
    }
    
    /**
     * 获取聊天消息
     */
    suspend fun getChatMessages(
        conversationId: String,
        page: Int = 1,
        size: Int = 50
    ): ApiResult<List<ChatMessageInfo>> {
        return safeApiCall {
            val response = chatApiService.getChatMessages(conversationId, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                throw Exception(response.body()?.message ?: "获取聊天消息失败")
            }
        }
    }
    
    /**
     * 发送消息
     */
    suspend fun sendMessage(
        conversationId: String,
        content: String,
        messageType: String = "text",
        replyToId: String? = null
    ): ApiResult<ChatMessageInfo> {
        return safeApiCall {
            val request = SendMessageRequest(
                conversationId = conversationId,
                content = content,
                messageType = messageType,
                replyToId = replyToId
            )
            val response = chatApiService.sendMessage(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: throw Exception("发送消息响应数据为空")
            } else {
                throw Exception(response.body()?.message ?: "发送消息失败")
            }
        }
    }
    
    /**
     * 标记消息为已读
     */
    suspend fun markAsRead(conversationId: String): ApiResult<Boolean> {
        return safeApiCall {
            val response = chatApiService.markAsRead(conversationId)
            if (response.isSuccessful && response.body()?.success == true) {
                true
            } else {
                throw Exception(response.body()?.message ?: "标记已读失败")
            }
        }
    }
}