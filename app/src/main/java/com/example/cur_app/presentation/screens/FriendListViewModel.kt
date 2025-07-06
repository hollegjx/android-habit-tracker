package com.example.cur_app.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.repository.FriendRepository
import com.example.cur_app.data.repository.ChatRepository
import com.example.cur_app.data.remote.dto.FriendInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 好友列表界面的ViewModel
 */
@HiltViewModel
class FriendListViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FriendListUiState())
    val uiState: StateFlow<FriendListUiState> = _uiState.asStateFlow()
    
    private var allFriends = listOf<FriendInfo>()
    
    /**
     * 加载好友列表
     */
    fun loadFriendList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val result = friendRepository.getFriendList()
                result.fold(
                    onSuccess = { friends ->
                        allFriends = friends
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            friends = friends,
                            errorMessage = null
                        )
                        
                        // 加载好友请求数量
                        loadFriendRequestCount()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "加载好友列表失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "加载好友列表失败"
                )
            }
        }
    }
    
    /**
     * 加载好友请求数量
     */
    private fun loadFriendRequestCount() {
        viewModelScope.launch {
            try {
                val result = friendRepository.getFriendRequests(type = "received")
                result.fold(
                    onSuccess = { requests ->
                        val unreadCount = requests.count { !it.isRead }
                        _uiState.value = _uiState.value.copy(
                            unreadRequestCount = unreadCount
                        )
                    },
                    onFailure = { 
                        // 忽略好友请求加载失败，不影响主界面
                    }
                )
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }
    
    /**
     * 筛选在线好友
     */
    fun filterOnlineFriends() {
        val onlineFriends = allFriends.filter { it.isOnline }
        _uiState.value = _uiState.value.copy(
            friends = onlineFriends
        )
    }
    
    /**
     * 筛选特别关注的好友
     */
    fun filterStarredFriends() {
        val starredFriends = allFriends.filter { it.isStarred }
        _uiState.value = _uiState.value.copy(
            friends = starredFriends
        )
    }
    
    /**
     * 重置筛选，显示所有好友
     */
    fun resetFilter() {
        _uiState.value = _uiState.value.copy(
            friends = allFriends
        )
    }
    
    /**
     * 更新好友设置
     */
    fun updateFriendSettings(
        friendshipId: String,
        alias: String? = null,
        isStarred: Boolean? = null,
        isMuted: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                val result = friendRepository.updateFriendSettings(
                    friendId = friendshipId,
                    alias = alias,
                    isStarred = isStarred,
                    isMuted = isMuted
                )
                
                result.fold(
                    onSuccess = {
                        // 更新本地缓存的好友信息
                        allFriends = allFriends.map { friend ->
                            if (friend.friendshipId == friendshipId) {
                                friend.copy(
                                    isStarred = isStarred ?: friend.isStarred,
                                    isMuted = isMuted ?: friend.isMuted
                                )
                            } else {
                                friend
                            }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            friends = _uiState.value.friends.map { friend ->
                                if (friend.friendshipId == friendshipId) {
                                    friend.copy(
                                        isStarred = isStarred ?: friend.isStarred,
                                        isMuted = isMuted ?: friend.isMuted
                                    )
                                } else {
                                    friend
                                }
                            }
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "更新好友设置失败: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "更新好友设置失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 删除好友
     */
    fun removeFriend(friendshipId: String) {
        viewModelScope.launch {
            try {
                val result = friendRepository.removeFriend(friendshipId)
                result.fold(
                    onSuccess = {
                        // 从本地缓存中移除
                        allFriends = allFriends.filter { it.friendshipId != friendshipId }
                        _uiState.value = _uiState.value.copy(
                            friends = _uiState.value.friends.filter { it.friendshipId != friendshipId }
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "删除好友失败: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "删除好友失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
    
    // ========== 聊天集成功能 ==========
    
    /**
     * 创建与好友的聊天对话
     */
    fun createChatWithFriend(friendUserId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = chatRepository.getOrCreatePrivateConversation(friendUserId)
                result.fold(
                    onSuccess = { conversation ->
                        onResult(conversation.conversationId)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "创建聊天失败: ${exception.message}"
                        )
                        onResult(null)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "创建聊天失败: ${e.message}"
                )
                onResult(null)
            }
        }
    }
}

/**
 * 好友列表界面的UI状态
 */
data class FriendListUiState(
    val isLoading: Boolean = false,
    val friends: List<FriendInfo> = emptyList(),
    val unreadRequestCount: Int = 0,
    val errorMessage: String? = null
)