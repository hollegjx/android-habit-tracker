package com.example.cur_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.cur_app.data.repository.PreferencesRepository
import com.example.cur_app.data.repository.UserProfile
import com.example.cur_app.domain.usecase.AiMessage
import com.example.cur_app.domain.usecase.AiUseCase
import com.example.cur_app.domain.usecase.AchievementUseCase
import com.example.cur_app.domain.usecase.PreferencesUseCase
import com.example.cur_app.data.database.entities.UserAchievementEntity
import com.example.cur_app.data.local.entity.CheckInType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主页面ViewModel
 * 管理今日打卡列表、统计概览和AI交互
 * 
 * 注意：由于CheckInUseCase层还未实现，这是简化版本
 * 待后续层实现后再完善此ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val achievementUseCase: AchievementUseCase
) : ViewModel() {

    // ========== UI状态定义 ==========
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // 用户信息 - 监听共享的Flow
    val userProfile: StateFlow<UserProfile> = preferencesRepository.userProfileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile("小明", "习惯成就卓越，坚持成就梦想！", "emoji", "😊", "HT2024001")
    )
    
    // 用户成就数据 - 从数据库获取
    private val _userAchievements = MutableStateFlow<List<UserAchievementEntity>>(emptyList())
    val userAchievements: StateFlow<List<UserAchievementEntity>> = _userAchievements.asStateFlow()
    
    // ========== 初始化 ==========
    
    init {
        // 初始化逻辑
        _uiState.value = HomeUiState(
            isLoading = false,
            welcomeMessage = "欢迎使用AI打卡追踪器！"
        )
        
        // 初始化用户成就数据（如果首次使用）
        initializeUserAchievements()
        
        // 加载用户成就数据
        loadUserAchievements()
        
        // 用户信息会自动从共享Flow中获取，无需手动加载
    }
    
    /**
     * 刷新用户信息（如果需要手动刷新）
     */
    fun refreshUserProfile() {
        viewModelScope.launch {
            try {
                val profile = preferencesRepository.getUserProfile()
                // 共享Flow会自动更新，这里主要用于错误处理
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "加载用户信息失败：${e.message}") 
                }
            }
        }
    }
    
    // ========== UI事件处理 ==========
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // 模拟加载
            kotlinx.coroutines.delay(1000)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    successMessage = "数据已刷新"
                )
            }
        }
    }
    
    /**
     * 切换打卡完成状态
     */
    fun toggleCheckInComplete(checkInId: Long) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedCheckIns = currentState.todayCheckIns.map { checkIn ->
                    if (checkIn.id == checkInId) {
                        checkIn.copy(isCompletedToday = !checkIn.isCompletedToday)
                    } else {
                        checkIn
                    }
                }
                currentState.copy(todayCheckIns = updatedCheckIns)
            }
        }
    }
    
    /**
     * 初始化用户成就数据（首次使用时）
     */
    private fun initializeUserAchievements() {
        viewModelScope.launch {
            try {
                achievementUseCase.initializeCurrentUserAchievements()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "初始化成就数据失败：${e.message}") 
                }
            }
        }
    }
    
    /**
     * 获取用户成就信息（用于UI）
     */
    suspend fun getUserAchievement(category: CheckInType): UserAchievementEntity? {
        return try {
            achievementUseCase.getCurrentUserAchievement(category)
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(error = "获取成就信息失败：${e.message}") 
            }
            null
        }
    }
    
    /**
     * 获取升级要求信息
     */
    suspend fun getUpgradeRequirement(category: CheckInType): AchievementUseCase.UpgradeRequirement? {
        return try {
            achievementUseCase.getUpgradeRequirement(category)
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(error = "获取升级要求失败：${e.message}") 
            }
            null
        }
    }
    
    /**
     * 加载用户成就数据
     */
    private fun loadUserAchievements() {
        viewModelScope.launch {
            try {
                achievementUseCase.getCurrentUserAchievements().collect { achievements ->
                    _userAchievements.value = achievements
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "加载成就数据失败：${e.message}") 
                }
            }
        }
    }
}

/**
 * 主页UI状态
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val welcomeMessage: String? = null,
    val todayCompleted: Int = 0,
    val todayTotal: Int = 0,
    val currentStreak: Int = 0,
    val aiSuggestion: String = "",
    val todayCheckIns: List<TodayCheckIn> = emptyList()
)

/**
 * 今日打卡项
 */
data class TodayCheckIn(
    val id: Long,
    val name: String,
    val isCompletedToday: Boolean
) 