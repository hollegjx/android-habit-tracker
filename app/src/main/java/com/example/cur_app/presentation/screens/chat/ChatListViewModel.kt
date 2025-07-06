package com.example.cur_app.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.database.entities.ChatConversationEntity
import com.example.cur_app.data.database.entities.ChatUserEntity
import com.example.cur_app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 聊天列表页面的ViewModel
 * 管理聊天对话列表和用户信息
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    init {
        loadConversations()
        loadUsers()
        observeUnreadCount()
    }
    
    private fun loadConversations() {
        viewModelScope.launch {
            try {
                combine(
                    chatRepository.getAllConversations(),
                    _searchText
                ) { conversations, search ->
                    if (search.isBlank()) {
                        conversations
                    } else {
                        conversations.filter { conversation ->
                            conversation.lastMessage.contains(search, ignoreCase = true) ||
                            conversation.customName.contains(search, ignoreCase = true)
                        }
                    }
                }.collect { conversations ->
                    _uiState.value = _uiState.value.copy(
                        conversations = conversations,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "加载对话失败",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadUsers() {
        viewModelScope.launch {
            try {
                chatRepository.getAllUsers().collect { users ->
                    _uiState.value = _uiState.value.copy(
                        users = users.associateBy { it.userId }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "加载用户失败"
                )
            }
        }
    }
    
    private fun observeUnreadCount() {
        viewModelScope.launch {
            try {
                val totalUnread = chatRepository.getTotalUnreadCount()
                _uiState.value = _uiState.value.copy(totalUnreadCount = totalUnread)
            } catch (e: Exception) {
                // 忽略未读数量获取失败
            }
        }
    }
    
    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }
    
    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                chatRepository.markConversationAsRead(conversationId)
                chatRepository.markConversationMessagesAsRead(conversationId)
                observeUnreadCount() // 刷新未读数量
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "标记已读失败"
                )
            }
        }
    }
    
    fun pinConversation(conversationId: String, pinned: Boolean) {
        viewModelScope.launch {
            try {
                chatRepository.updatePinStatus(conversationId, pinned)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "置顶操作失败"
                )
            }
        }
    }
    
    fun muteConversation(conversationId: String, muted: Boolean) {
        viewModelScope.launch {
            try {
                chatRepository.updateMuteStatus(conversationId, muted)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "静音操作失败"
                )
            }
        }
    }
    
    fun archiveConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                chatRepository.updateArchiveStatus(conversationId, true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "归档操作失败"
                )
            }
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteConversation(conversationId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "删除对话失败"
                )
            }
        }
    }
    
    fun createNewConversation(userId: String) {
        viewModelScope.launch {
            try {
                chatRepository.getOrCreateConversation(userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "创建对话失败"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getUserById(userId: String): ChatUserEntity? {
        return _uiState.value.users[userId]
    }
    
    fun refreshConversations() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadConversations()
        observeUnreadCount()
    }
}

/**
 * 聊天列表页面的UI状态
 */
data class ChatListUiState(
    val conversations: List<ChatConversationEntity> = emptyList(),
    val users: Map<String, ChatUserEntity> = emptyMap(),
    val totalUnreadCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)