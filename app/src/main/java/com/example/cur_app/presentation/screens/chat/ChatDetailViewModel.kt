package com.example.cur_app.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.database.entities.ChatMessageEntity
import com.example.cur_app.data.database.entities.ChatConversationEntity
import com.example.cur_app.data.database.entities.ChatUserEntity
import com.example.cur_app.data.repository.ChatRepository
import com.example.cur_app.data.repository.AiRepository
import com.example.cur_app.data.repository.AiMessageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * 聊天详情页面的ViewModel
 * 管理单个对话的消息和用户交互
 */
@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val aiRepository: AiRepository
) : ViewModel() {
    
    init {
        android.util.Log.e("ChatDetailViewModel", "🔴 ChatDetailViewModel创建，AiRepository: $aiRepository")
    }
    
    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()
    
    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()
    
    private var currentConversationId: String? = null
    private var currentUserId: String = "current_user" // 当前用户ID，实际应用中从用户管理获取
    
    fun loadConversation(conversationId: String) {
        if (currentConversationId == conversationId) return
        
        currentConversationId = conversationId
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // 加载对话信息
                val conversation = chatRepository.getConversationById(conversationId)
                if (conversation == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "对话不存在",
                        isLoading = false
                    )
                    return@launch
                }
                
                // 加载对方用户信息
                val otherUser = chatRepository.getUserById(conversation.otherUserId)
                
                _uiState.value = _uiState.value.copy(
                    conversation = conversation,
                    otherUser = otherUser,
                    isLoading = false
                )
                
                // 加载消息
                loadMessages(conversationId)
                
                // 标记对话为已读
                markAsRead(conversationId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "加载对话失败",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                chatRepository.getMessagesByConversation(conversationId).collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "加载消息失败"
                )
            }
        }
    }
    
    fun onMessageInputChanged(input: String) {
        _messageInput.value = input
    }
    
    fun sendMessage() {
        val conversationId = currentConversationId ?: return
        val content = _messageInput.value.trim()
        
        if (content.isEmpty()) return
        
        val conversation = _uiState.value.conversation ?: return
        val receiverId = conversation.otherUserId
        
        viewModelScope.launch {
            try {
                android.util.Log.e("ChatDetailVM", "🔴 开始发送消息: $content")
                _uiState.value = _uiState.value.copy(isSending = true)
                
                // 发送消息
                chatRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    isFromMe = true
                )
                android.util.Log.e("ChatDetailVM", "🔴 消息已发送到数据库")
                
                // 清空输入框
                _messageInput.value = ""
                
                // 检查对话类型
                android.util.Log.e("ChatDetailVM", "🔴 对话类型: ${conversation.conversationType}")
                android.util.Log.e("ChatDetailVM", "🔴 接收者ID: $receiverId")
                
                // 如果是AI对话，调用AI API
                if (conversation.conversationType == "AI") {
                    android.util.Log.e("ChatDetailVM", "🔴 进入AI对话分支，即将调用AI API")
                    simulateAiResponse(conversationId, receiverId, content)
                } else {
                    android.util.Log.e("ChatDetailVM", "🔴 不是AI对话，类型为: ${conversation.conversationType}")
                }
                
                _uiState.value = _uiState.value.copy(isSending = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "发送消息失败",
                    isSending = false
                )
            }
        }
    }
    
    private suspend fun simulateAiResponse(conversationId: String, aiUserId: String, userMessage: String) {
        try {
            android.util.Log.e("ChatDetailVM", "🔴 simulateAiResponse被调用！")
            android.util.Log.e("ChatDetailVM", "🔴 用户消息: $userMessage")
            android.util.Log.e("ChatDetailVM", "🔴 AI用户ID: $aiUserId")
            
            // 获取当前角色信息
            val currentCharacter = com.example.cur_app.data.local.AiCharacterManager.getCurrentCharacter()
            val characterId = currentCharacter.id  // 直接使用字符串ID
            android.util.Log.e("ChatDetailVM", "🔴 当前角色: ${currentCharacter.name} (ID: ${currentCharacter.id})")
            android.util.Log.e("ChatDetailVM", "🔴 角色详细信息: subtitle=${currentCharacter.subtitle}, emoji=${currentCharacter.iconEmoji}")
            
            // 获取对话历史（取最近10条消息作为上下文）
            val messages = _uiState.value.messages
            val conversationHistory = messages.takeLast(10).map { it.content }
            android.util.Log.e("ChatDetailVM", "🔴 对话历史数量: ${conversationHistory.size}")
            
            // 调用真实AI API，传递角色ID
            android.util.Log.e("ChatDetailVM", "🔴 即将调用aiRepository.chatWithAi，角色ID: $characterId")
            when (val result = aiRepository.chatWithAi(
                userMessage = userMessage,
                characterId = characterId,
                conversationHistory = conversationHistory
            )) {
                is AiMessageResult.Success -> {
                    android.util.Log.e("ChatDetailVM", "🔴 AI API调用成功！回复: ${result.message}")
                    android.util.Log.e("ChatDetailVM", "🔴 是否来自网络: ${result.isFromNetwork}")
                    chatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = aiUserId,
                        receiverId = currentUserId,
                        content = result.message,
                        isFromMe = false
                    )
                }
                is AiMessageResult.Error -> {
                    android.util.Log.e("ChatDetailVM", "🔴 AI API调用失败: ${result.message}")
                    // AI API失败时的备用回复
                    val fallbackResponse = getFallbackResponse(userMessage)
                    android.util.Log.e("ChatDetailVM", "🔴 使用备用回复: $fallbackResponse")
                    chatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = aiUserId,
                        receiverId = currentUserId,
                        content = fallbackResponse,
                        isFromMe = false
                    )
                }
                is AiMessageResult.Loading -> {
                    android.util.Log.e("ChatDetailVM", "🔴 AI API正在加载中...")
                    // 正在加载中，稍后再试
                    delay(1000)
                    val fallbackResponse = "抱歉，我现在有点忙，请稍后再试。"
                    android.util.Log.e("ChatDetailVM", "🔴 加载超时，使用备用回复: $fallbackResponse")
                    chatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = aiUserId,
                        receiverId = currentUserId,
                        content = fallbackResponse,
                        isFromMe = false
                    )
                }
            }
        } catch (e: Exception) {
            // 异常情况下的备用回复
            val fallbackResponse = "抱歉，我遇到了一些技术问题，请稍后再试。"
            chatRepository.sendMessage(
                conversationId = conversationId,
                senderId = aiUserId,
                receiverId = currentUserId,
                content = fallbackResponse,
                isFromMe = false
            )
        }
    }
    
    /**
     * 获取备用回复
     */
    private fun getFallbackResponse(userMessage: String): String {
        val responses = listOf(
            "我理解你的想法，让我来帮助你！",
            "这是一个很好的问题，我来为你分析一下。",
            "根据你的情况，我建议...",
            "你说得很对，我们可以这样做。",
            "让我想想最好的解决方案。",
            "这个问题我之前也遇到过，建议这样处理。"
        )
        return responses.random()
    }
    
    fun markAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                chatRepository.markConversationAsRead(conversationId)
                chatRepository.markConversationMessagesAsRead(conversationId)
            } catch (e: Exception) {
                // 忽略标记已读失败
            }
        }
    }
    
    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.deleteMessage(messageId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "删除消息失败"
                )
            }
        }
    }
    
    fun editMessage(messageId: Long, newContent: String) {
        viewModelScope.launch {
            try {
                chatRepository.editMessage(messageId, newContent)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "编辑消息失败"
                )
            }
        }
    }
    
    fun loadMoreMessages() {
        val conversationId = currentConversationId ?: return
        
        viewModelScope.launch {
            try {
                // 加载更多历史消息（这里简化处理，实际可能需要分页）
                val messages = chatRepository.getRecentMessages(conversationId, 100)
                _uiState.value = _uiState.value.copy(messages = messages)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "加载更多消息失败"
                )
            }
        }
    }
    
    fun searchMessages(searchText: String) {
        val conversationId = currentConversationId ?: return
        
        viewModelScope.launch {
            try {
                val results = chatRepository.searchMessagesInConversation(conversationId, searchText)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "搜索消息失败"
                )
            }
        }
    }
    
    fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun retry() {
        val conversationId = currentConversationId ?: return
        loadConversation(conversationId)
    }
}

/**
 * 聊天详情页面的UI状态
 */
data class ChatDetailUiState(
    val conversation: ChatConversationEntity? = null,
    val otherUser: ChatUserEntity? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val searchResults: List<ChatMessageEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null
)