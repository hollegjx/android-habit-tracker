package com.example.cur_app.data.repository

import com.example.cur_app.data.database.HabitTrackerDatabase
import com.example.cur_app.data.database.entities.ChatMessageEntity
import com.example.cur_app.data.database.entities.ChatConversationEntity
import com.example.cur_app.data.database.entities.ChatUserEntity
import com.example.cur_app.data.remote.socket.SocketService
import com.example.cur_app.data.remote.AuthApiService
import com.example.cur_app.data.remote.dto.RemoteChatMessage
import com.example.cur_app.data.remote.datasource.ChatRemoteDataSource
import com.example.cur_app.data.remote.api.*
import com.example.cur_app.utils.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 聊天功能数据仓库
 * 提供聊天用户、对话和消息的数据访问接口，集成Socket.IO实时功能
 */
@Singleton
class ChatRepository @Inject constructor(
    private val database: HabitTrackerDatabase,
    private val socketService: SocketService,
    private val authApiService: AuthApiService
    // TODO: private val chatRemoteDataSource: ChatRemoteDataSource
) {
    
    // 用户相关操作
    fun getAllUsers(): Flow<List<ChatUserEntity>> = database.chatUserDao().getAllUsers()
    
    fun getAiUsers(): Flow<List<ChatUserEntity>> = database.chatUserDao().getAiUsers()
    
    fun getHumanUsers(): Flow<List<ChatUserEntity>> = database.chatUserDao().getHumanUsers()
    
    fun getOnlineUsers(): Flow<List<ChatUserEntity>> = database.chatUserDao().getOnlineUsers()
    
    suspend fun getUserById(userId: String): ChatUserEntity? = database.chatUserDao().getUserById(userId)
    
    suspend fun searchUsers(searchText: String): List<ChatUserEntity> = database.chatUserDao().searchUsers(searchText)
    
    suspend fun insertUser(user: ChatUserEntity): Long = database.chatUserDao().insertUser(user)
    
    suspend fun updateUser(user: ChatUserEntity) = database.chatUserDao().updateUser(user)
    
    suspend fun updateOnlineStatus(userId: String, online: Boolean) = 
        database.chatUserDao().updateOnlineStatus(userId, online)
    
    suspend fun updateUserStatus(userId: String, status: String, message: String = "") = 
        database.chatUserDao().updateStatus(userId, status, message)
    
    suspend fun incrementUserMessageCount(userId: String) = 
        database.chatUserDao().incrementMessageCount(userId)
    
    suspend fun getOnlineUserCount(): Int = database.chatUserDao().getOnlineUserCount()
    
    suspend fun getHumanUserCount(): Int = database.chatUserDao().getHumanUserCount()
    
    // 对话相关操作
    fun getAllConversations(): Flow<List<ChatConversationEntity>> = database.chatConversationDao().getAllConversations()
    
    fun getConversationsByType(type: String): Flow<List<ChatConversationEntity>> = 
        database.chatConversationDao().getConversationsByType(type)
    
    fun getPinnedConversations(): Flow<List<ChatConversationEntity>> = 
        database.chatConversationDao().getPinnedConversations()
    
    fun getConversationsWithUnread(): Flow<List<ChatConversationEntity>> = 
        database.chatConversationDao().getConversationsWithUnread()
    
    suspend fun getConversationById(conversationId: String): ChatConversationEntity? = 
        database.chatConversationDao().getConversationById(conversationId)
    
    suspend fun getConversationByUserId(userId: String): ChatConversationEntity? = 
        database.chatConversationDao().getConversationByUserId(userId)
    
    suspend fun searchConversations(searchText: String): List<ChatConversationEntity> = 
        database.chatConversationDao().searchConversations(searchText)
    
    suspend fun insertConversation(conversation: ChatConversationEntity): Long = 
        database.chatConversationDao().insertConversation(conversation)
    
    suspend fun updateConversation(conversation: ChatConversationEntity) = 
        database.chatConversationDao().updateConversation(conversation)
    
    suspend fun updateLastMessage(
        conversationId: String,
        message: String,
        time: Long,
        senderId: String,
        type: String = "TEXT",
        unreadCount: Int
    ) = database.chatConversationDao().updateLastMessage(conversationId, message, time, senderId, type, unreadCount)
    
    suspend fun markConversationAsRead(conversationId: String) = 
        database.chatConversationDao().markAsRead(conversationId)
    
    suspend fun incrementConversationUnreadCount(conversationId: String) = 
        database.chatConversationDao().incrementUnreadCount(conversationId)
    
    suspend fun updatePinStatus(conversationId: String, pinned: Boolean) = 
        database.chatConversationDao().updatePinStatus(conversationId, pinned)
    
    suspend fun updateArchiveStatus(conversationId: String, archived: Boolean) = 
        database.chatConversationDao().updateArchiveStatus(conversationId, archived)
    
    suspend fun updateMuteStatus(conversationId: String, muted: Boolean) = 
        database.chatConversationDao().updateMuteStatus(conversationId, muted)
    
    suspend fun getTotalUnreadCount(): Int = database.chatConversationDao().getTotalUnreadCount()
    
    suspend fun deleteConversation(conversationId: String) = 
        database.chatConversationDao().deleteConversationById(conversationId)
    
    // 消息相关操作
    fun getMessagesByConversation(conversationId: String): Flow<List<ChatMessageEntity>> = 
        database.chatMessageDao().getMessagesByConversation(conversationId)
    
    suspend fun getRecentMessages(conversationId: String, limit: Int = 50): List<ChatMessageEntity> = 
        database.chatMessageDao().getRecentMessages(conversationId, limit)
    
    suspend fun getMessageById(messageId: Long): ChatMessageEntity? = 
        database.chatMessageDao().getMessageById(messageId)
    
    suspend fun getUnreadMessages(conversationId: String): List<ChatMessageEntity> = 
        database.chatMessageDao().getUnreadMessages(conversationId)
    
    suspend fun getUnreadCount(conversationId: String): Int = 
        database.chatMessageDao().getUnreadCount(conversationId)
    
    suspend fun searchMessages(searchText: String): List<ChatMessageEntity> = 
        database.chatMessageDao().searchMessages(searchText)
    
    suspend fun searchMessagesInConversation(conversationId: String, searchText: String): List<ChatMessageEntity> = 
        database.chatMessageDao().searchMessagesInConversation(conversationId, searchText)
    
    suspend fun insertMessage(message: ChatMessageEntity): Long = 
        database.chatMessageDao().insertMessage(message)
    
    suspend fun updateMessage(message: ChatMessageEntity) = 
        database.chatMessageDao().updateMessage(message)
    
    suspend fun markConversationMessagesAsRead(conversationId: String) = 
        database.chatMessageDao().markConversationAsRead(conversationId)
    
    suspend fun markMessageAsRead(messageId: Long) = 
        database.chatMessageDao().markMessageAsRead(messageId)
    
    suspend fun deleteMessage(messageId: Long) = 
        database.chatMessageDao().softDeleteMessage(messageId)
    
    suspend fun editMessage(messageId: Long, newContent: String) = 
        database.chatMessageDao().editMessage(messageId, newContent)
    
    suspend fun getMessageCount(conversationId: String): Int = 
        database.chatMessageDao().getMessageCount(conversationId)
    
    suspend fun getLastMessage(conversationId: String): ChatMessageEntity? = 
        database.chatMessageDao().getLastMessage(conversationId)
    
    suspend fun deleteMessagesByConversation(conversationId: String) = 
        database.chatMessageDao().deleteMessagesByConversation(conversationId)
    
    // 综合操作
    /**
     * 发送消息的完整流程
     * 1. 插入消息
     * 2. 更新对话的最后消息信息
     * 3. 增加发送者的消息计数
     */
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        receiverId: String,
        content: String,
        messageType: String = "TEXT",
        isFromMe: Boolean = true
    ): Long {
        val currentTime = System.currentTimeMillis()
        
        // 创建消息实体
        val message = ChatMessageEntity(
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            messageType = messageType,
            isFromMe = isFromMe,
            timestamp = currentTime,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        // 插入消息
        val messageId = insertMessage(message)
        
        // 更新对话信息
        val unreadCount = if (isFromMe) 0 else 1
        updateLastMessage(conversationId, content, currentTime, senderId, messageType, unreadCount)
        
        // 增加发送者的消息计数
        incrementUserMessageCount(senderId)
        
        return messageId
    }
    
    /**
     * 创建新对话的完整流程
     * 1. 创建对话
     * 2. 发送第一条消息（如果提供）
     */
    suspend fun createConversation(
        conversationId: String,
        otherUserId: String,
        conversationType: String = "PRIVATE",
        firstMessage: String? = null,
        senderId: String = "current_user"
    ): String {
        val currentTime = System.currentTimeMillis()
        
        // 创建对话实体
        val conversation = ChatConversationEntity(
            conversationId = conversationId,
            otherUserId = otherUserId,
            conversationType = conversationType,
            lastMessage = firstMessage ?: "",
            lastMessageTime = if (firstMessage != null) currentTime else 0L,
            lastMessageSenderId = if (firstMessage != null) senderId else "",
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        // 插入对话
        insertConversation(conversation)
        
        // 如果有第一条消息，发送它
        if (firstMessage != null) {
            sendMessage(conversationId, senderId, otherUserId, firstMessage, isFromMe = senderId == "current_user")
        }
        
        return conversationId
    }
    
    /**
     * 获取或创建与指定用户的对话
     */
    suspend fun getOrCreateConversation(userId: String): ChatConversationEntity {
        // 先尝试获取现有对话
        var conversation = getConversationByUserId(userId)
        
        if (conversation == null) {
            // 如果不存在，创建新对话
            val conversationId = "conv_${userId}_${System.currentTimeMillis()}"
            createConversation(conversationId, userId)
            conversation = getConversationById(conversationId)
                ?: throw IllegalStateException("Failed to create conversation")
        }
        
        return conversation
    }
    
    /**
     * 清除所有聊天数据
     */
    suspend fun clearAllChatData() {
        database.chatMessageDao().deleteAllMessages()
        database.chatConversationDao().deleteAllConversations()
        database.chatUserDao().deleteAllUsers()
    }
    
    // ========== Socket.IO 实时聊天功能 ==========
    
    companion object {
        private const val TAG = "ChatRepository"
    }
    
    /**
     * 连接到Socket.IO服务器
     */
    fun connectSocket(accessToken: String) {
        socketService.connect(accessToken)
        Log.d(TAG, "Socket connection initiated")
    }
    
    /**
     * 断开Socket连接
     */
    fun disconnectSocket() {
        socketService.disconnect()
        Log.d(TAG, "Socket disconnected")
    }
    
    /**
     * 加入对话房间
     */
    fun joinConversation(conversationId: String) {
        socketService.joinConversation(conversationId)
        Log.d(TAG, "Joined conversation: $conversationId")
    }
    
    /**
     * 离开对话房间
     */
    fun leaveConversation(conversationId: String) {
        socketService.leaveConversation(conversationId)
        Log.d(TAG, "Left conversation: $conversationId")
    }
    
    /**
     * 通过Socket发送实时消息
     */
    fun sendRealtimeMessage(
        conversationId: String,
        content: String,
        messageType: String = "text",
        replyToId: String? = null
    ) {
        socketService.sendMessage(conversationId, content, messageType, replyToId)
        Log.d(TAG, "Realtime message sent to conversation: $conversationId")
    }
    
    /**
     * 标记消息为已读
     */
    fun markMessagesAsRead(conversationId: String, messageIds: List<String>) {
        socketService.markAsRead(conversationId, messageIds)
        Log.d(TAG, "Messages marked as read in conversation: $conversationId")
    }
    
    /**
     * 发送正在输入状态
     */
    fun sendTypingStatus(conversationId: String, isTyping: Boolean) {
        socketService.sendTyping(conversationId, isTyping)
    }
    
    /**
     * 监听接收到的实时消息
     */
    fun observeRealtimeMessages(): Flow<RemoteChatMessage> {
        return socketService.messageReceived.onEach { message ->
            Log.d(TAG, "Received realtime message: ${message.messageId}")
            // 将接收到的消息保存到本地数据库
            saveReceivedMessage(message)
        }
    }
    
    /**
     * 监听Socket连接状态
     */
    fun observeConnectionStatus(): Flow<Boolean> {
        return socketService.connectionStatus
    }
    
    /**
     * 监听Socket错误
     */
    fun observeSocketErrors(): Flow<String> {
        return socketService.errors
    }
    
    /**
     * 检查Socket连接状态
     */
    fun isSocketConnected(): Boolean {
        return socketService.isConnected()
    }
    
    /**
     * 保存接收到的实时消息到本地数据库
     */
    private suspend fun saveReceivedMessage(remoteMessage: RemoteChatMessage) {
        try {
            val messageEntity = ChatMessageEntity(
                conversationId = remoteMessage.conversationId,
                senderId = remoteMessage.senderId ?: "unknown",
                receiverId = "current_user", // 当前用户ID
                content = remoteMessage.content,
                messageType = remoteMessage.messageType,
                isFromMe = false,
                timestamp = remoteMessage.timestamp,
                isRead = false,
                isSent = remoteMessage.isDelivered,
                createdAt = remoteMessage.timestamp,
                updatedAt = remoteMessage.timestamp,
                metadata = remoteMessage.mediaMetadata?.toString() ?: ""
            )
            
            // 插入消息到数据库
            insertMessage(messageEntity)
            
            // 更新对话的最后消息信息
            updateLastMessage(
                conversationId = remoteMessage.conversationId,
                message = remoteMessage.content,
                time = remoteMessage.timestamp,
                senderId = remoteMessage.senderId ?: "unknown",
                type = remoteMessage.messageType,
                unreadCount = 1
            )
            
            Log.d(TAG, "Saved received message to database: ${remoteMessage.messageId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving received message: ${e.message}")
        }
    }
    
    /**
     * 获取聊天对话和实时消息的组合流
     */
    fun getCombinedMessages(conversationId: String): Flow<List<ChatMessageEntity>> {
        return combine(
            getMessagesByConversation(conversationId),
            observeRealtimeMessages()
        ) { localMessages, _ ->
            // 返回本地消息，实时消息已经通过saveReceivedMessage保存到本地
            localMessages
        }
    }
    
    // ========== 新增好友聊天功能 ==========
    
    /**
     * 与AI角色聊天
     */
    suspend fun chatWithAI(characterId: String, message: String): Result<AiChatData> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 获取服务器端对话列表
     */
    suspend fun getRemoteConversations(page: Int = 1, size: Int = 20): Result<List<ConversationInfo>> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 创建私聊对话
     */
    suspend fun createPrivateConversation(friendUserId: String): Result<ConversationInfo> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 创建群聊对话
     */
    suspend fun createGroupConversation(
        participantIds: List<String>,
        groupName: String
    ): Result<ConversationInfo> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 获取聊天消息
     */
    suspend fun getRemoteChatMessages(
        conversationId: String,
        page: Int = 1,
        size: Int = 50
    ): Result<List<ChatMessageInfo>> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 发送文本消息到服务器
     */
    suspend fun sendRemoteTextMessage(
        conversationId: String,
        content: String,
        replyToId: String? = null
    ): Result<ChatMessageInfo> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 发送图片消息到服务器
     */
    suspend fun sendRemoteImageMessage(
        conversationId: String,
        imageUrl: String,
        replyToId: String? = null
    ): Result<ChatMessageInfo> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 发送文件消息到服务器
     */
    suspend fun sendRemoteFileMessage(
        conversationId: String,
        fileUrl: String,
        fileName: String,
        replyToId: String? = null
    ): Result<ChatMessageInfo> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 标记对话为已读（服务器端）
     */
    suspend fun markRemoteConversationAsRead(conversationId: String): Result<Boolean> {
        // TODO: Implement when chatRemoteDataSource is available
        return Result.failure(Exception("Not implemented - chatRemoteDataSource not available"))
    }
    
    /**
     * 检查是否已经存在与指定用户的私聊对话
     */
    suspend fun findPrivateConversationWithUser(userId: String): Result<ConversationInfo?> {
        return try {
            val conversationsResult = getRemoteConversations()
            conversationsResult.fold(
                onSuccess = { conversations ->
                    val privateConversation = conversations.find { conversation ->
                        conversation.type == "private" && 
                        conversation.participants.any { it.userId == userId }
                    }
                    Result.success(privateConversation)
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
     * 获取或创建与指定用户的私聊对话
     */
    suspend fun getOrCreatePrivateConversation(userId: String): Result<ConversationInfo> {
        return try {
            // 首先查找是否已存在对话
            val findResult = findPrivateConversationWithUser(userId)
            findResult.fold(
                onSuccess = { existingConversation ->
                    if (existingConversation != null) {
                        Result.success(existingConversation)
                    } else {
                        // 不存在则创建新对话
                        createPrivateConversation(userId)
                    }
                },
                onFailure = { exception ->
                    // 查找失败，尝试创建新对话
                    createPrivateConversation(userId)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 同步服务器对话到本地数据库
     */
    suspend fun syncConversationsFromServer(): Result<Unit> {
        return try {
            val remoteConversationsResult = getRemoteConversations()
            remoteConversationsResult.fold(
                onSuccess = { remoteConversations ->
                    // 将服务器对话转换为本地实体并保存
                    remoteConversations.forEach { remoteConv ->
                        val localConv = ChatConversationEntity(
                            conversationId = remoteConv.conversationId,
                            otherUserId = remoteConv.participants.firstOrNull()?.userId ?: "",
                            conversationType = when (remoteConv.type) {
                                "private" -> "PRIVATE"
                                "group" -> "GROUP"
                                "ai" -> "AI"
                                else -> "PRIVATE"
                            },
                            lastMessage = remoteConv.lastMessage ?: "",
                            lastMessageTime = remoteConv.lastMessageTime ?: 0L,
                            lastMessageSenderId = "",
                            unreadCount = remoteConv.unreadCount,
                            createdAt = remoteConv.createdAt,
                            updatedAt = remoteConv.updatedAt
                        )
                        insertConversation(localConv)
                    }
                    Result.success(Unit)
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
     * 同步服务器消息到本地数据库
     */
    suspend fun syncMessagesFromServer(conversationId: String): Result<Unit> {
        return try {
            val remoteMessagesResult = getRemoteChatMessages(conversationId)
            remoteMessagesResult.fold(
                onSuccess = { remoteMessages ->
                    // 将服务器消息转换为本地实体并保存
                    remoteMessages.forEach { remoteMsg ->
                        val localMsg = ChatMessageEntity(
                            conversationId = remoteMsg.conversationId,
                            senderId = remoteMsg.senderId,
                            receiverId = "", // 需要根据对话参与者确定
                            content = remoteMsg.content,
                            messageType = remoteMsg.messageType.uppercase(),
                            isFromMe = false, // 需要根据当前用户ID判断
                            timestamp = remoteMsg.createdAt,
                            createdAt = remoteMsg.createdAt,
                            updatedAt = remoteMsg.createdAt
                        )
                        insertMessage(localMsg)
                    }
                    Result.success(Unit)
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