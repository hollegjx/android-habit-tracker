package com.example.cur_app.data.local

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.cur_app.data.local.entity.ChatConversation
import com.example.cur_app.data.local.entity.MockChatData

/**
 * 聊天状态管理器 - 管理AI聊天相关的全局状态
 * 包括未读消息数量、专注模式状态等
 */
object ChatStateManager {
    
    // 对话列表状态
    private var _conversations by mutableStateOf(MockChatData.getMockConversations())
    val conversations: List<ChatConversation> get() = _conversations
    
    // 未读消息总数 - 在底部导航栏显示红点
    private val _totalUnreadCount = mutableIntStateOf(0)
    val totalUnreadCount: Int get() = _totalUnreadCount.intValue
    
    // 专注模式状态 - 控制全局UI显示
    private val _isFocusModeActive = mutableStateOf(false)
    val isFocusModeActive: Boolean get() = _isFocusModeActive.value
    
    /**
     * 更新未读消息总数
     */
    fun updateTotalUnreadCount(count: Int) {
        _totalUnreadCount.intValue = count
    }
    
    /**
     * 设置专注模式状态
     * @param active true表示进入专注模式，false表示退出专注模式
     */
    fun setFocusModeActive(active: Boolean) {
        _isFocusModeActive.value = active
    }
    
    /**
     * 进入专注模式
     */
    fun enterFocusMode() {
        setFocusModeActive(true)
    }
    
    /**
     * 退出专注模式
     */
    fun exitFocusMode() {
        setFocusModeActive(false)
    }
    
    /**
     * 标记对话为已读
     */
    fun markConversationAsRead(conversationId: String) {
        _conversations = _conversations.map { conversation ->
            if (conversation.conversationId == conversationId) {
                conversation.copy(unreadCount = 0)
            } else {
                conversation
            }
        }
    }
    
    /**
     * 增加未读消息数
     */
    fun incrementUnreadCount(conversationId: String, count: Int = 1) {
        _conversations = _conversations.map { conversation ->
            if (conversation.conversationId == conversationId) {
                conversation.copy(unreadCount = conversation.unreadCount + count)
            } else {
                conversation
            }
        }
    }
    
    /**
     * 获取指定对话的未读数
     */
    fun getUnreadCount(conversationId: String): Int {
        return _conversations.find { it.conversationId == conversationId }?.unreadCount ?: 0
    }
    
    /**
     * 更新最后消息
     */
    fun updateLastMessage(conversationId: String, lastMessage: String) {
        _conversations = _conversations.map { conversation ->
            if (conversation.conversationId == conversationId) {
                conversation.copy(
                    lastMessage = lastMessage,
                    lastMessageTime = java.util.Date()
                )
            } else {
                conversation
            }
        }
    }
    
    /**
     * 重置状态（用于测试）
     */
    fun resetState() {
        _conversations = MockChatData.getMockConversations()
    }
} 