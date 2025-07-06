package com.example.cur_app.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.repository.FriendRepository
import com.example.cur_app.data.remote.dto.FriendSearchUserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 添加好友界面的ViewModel
 */
@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendRepository: FriendRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddFriendUiState())
    val uiState: StateFlow<AddFriendUiState> = _uiState.asStateFlow()
    
    /**
     * 根据UID搜索用户
     */
    fun searchUserByUid(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                searchResult = null,
                friendRequestStatus = null
            )
            
            try {
                val result = friendRepository.searchUserByUid(uid)
                result.fold(
                    onSuccess = { user ->
                        // 直接显示搜索结果，包含好友状态信息
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            searchResult = user,
                            friendRequestStatus = when (user.friendshipStatus) {
                                "accepted" -> "already_friend"
                                "pending" -> "sent"
                                "declined" -> "declined"
                                "blocked" -> "blocked"
                                else -> if (user.canSendRequest) null else "cannot_send"
                            }
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "搜索失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "搜索失败"
                )
            }
        }
    }
    
    /**
     * 发送好友请求
     */
    fun sendFriendRequest(targetUserId: String, message: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                friendRequestStatus = "sending"
            )
            
            try {
                val result = friendRepository.sendFriendRequest(
                    uid = targetUserId,
                    message = message ?: "我想添加您为好友"
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            friendRequestStatus = "sent"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            friendRequestStatus = null,
                            errorMessage = exception.message ?: "发送好友请求失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    friendRequestStatus = null,
                    errorMessage = e.message ?: "发送好友请求失败"
                )
            }
        }
    }
    
    /**
     * 清除搜索结果
     */
    fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(
            searchResult = null,
            errorMessage = null,
            friendRequestStatus = null
        )
    }
    
    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}

/**
 * 添加好友界面的UI状态
 */
data class AddFriendUiState(
    val isLoading: Boolean = false,
    val searchResult: FriendSearchUserInfo? = null,
    val errorMessage: String? = null,
    val friendRequestStatus: String? = null // null, "sending", "sent"
)