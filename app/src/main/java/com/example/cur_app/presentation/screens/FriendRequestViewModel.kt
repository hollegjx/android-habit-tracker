package com.example.cur_app.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.repository.FriendRepository
import com.example.cur_app.data.remote.dto.FriendRequestInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 好友请求界面的ViewModel
 */
@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val friendRepository: FriendRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FriendRequestUiState())
    val uiState: StateFlow<FriendRequestUiState> = _uiState.asStateFlow()
    
    /**
     * 加载好友请求列表
     */
    fun loadFriendRequests(type: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val result = friendRepository.getFriendRequests(type)
                result.fold(
                    onSuccess = { requests ->
                        if (type == "received") {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                receivedRequests = requests,
                                errorMessage = null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                sentRequests = requests,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "加载好友请求失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "加载好友请求失败"
                )
            }
        }
    }
    
    /**
     * 处理好友请求（接受/拒绝）
     */
    fun handleFriendRequest(requestId: String, action: String, message: String? = null) {
        viewModelScope.launch {
            try {
                val result = friendRepository.handleFriendRequest(requestId, action, message)
                result.fold(
                    onSuccess = {
                        // 更新本地缓存的请求状态
                        val newStatus = if (action == "accept") "accepted" else "declined"
                        
                        _uiState.value = _uiState.value.copy(
                            receivedRequests = _uiState.value.receivedRequests.map { request ->
                                if (request.id == requestId) {
                                    request.copy(status = newStatus)
                                } else {
                                    request
                                }
                            }
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "处理好友请求失败: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "处理好友请求失败: ${e.message}"
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
}

/**
 * 好友请求界面的UI状态
 */
data class FriendRequestUiState(
    val isLoading: Boolean = false,
    val receivedRequests: List<FriendRequestInfo> = emptyList(),
    val sentRequests: List<FriendRequestInfo> = emptyList(),
    val errorMessage: String? = null
)