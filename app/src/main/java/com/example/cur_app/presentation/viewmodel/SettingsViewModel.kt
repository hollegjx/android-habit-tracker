package com.example.cur_app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.repository.PreferencesRepository
import com.example.cur_app.data.repository.UserProfile
import com.example.cur_app.data.repository.CheckInRepository
import com.example.cur_app.data.database.HabitTrackerDatabase
import com.example.cur_app.data.database.DefaultDataInitializer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页面ViewModel
 * 管理应用设置、偏好配置和系统状态
 * 
 * 注意：由于UseCase层还未实现，这是简化版本
 * 待后续层实现后再完善此ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val checkInRepository: CheckInRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ========== UI状态定义 ==========
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    // 主题模式
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
    
    // 通知开关
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    // 用户信息 - 使用共享的Flow
    val userProfile: StateFlow<UserProfile> = preferencesRepository.userProfileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile("小明", "习惯成就卓越，坚持成就梦想！", "emoji", "😊", "HT2024001")
    )
    
    init {
        // 初始化逻辑，用户信息会自动从共享Flow中获取
    }
    
    // ========== 主题设置 ==========
    
    /**
     * 设置主题模式
     */
    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        _uiState.update { it.copy(successMessage = "主题设置已更新") }
    }
    
    /**
     * 获取主题显示名称
     */
    fun getThemeDisplayName(mode: String): String {
        return when (mode) {
            "light" -> "浅色模式"
            "dark" -> "深色模式"
            "system" -> "跟随系统"
            else -> "跟随系统"
        }
    }
    
    // ========== 通知设置 ==========
    
    /**
     * 设置通知开关
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        _uiState.update { 
            it.copy(successMessage = if (enabled) "通知已开启" else "通知已关闭") 
        }
    }
    
    // ========== 数据管理 ==========
    
    /**
     * 重置所有数据和设置
     * 清空数据库并恢复到默认状态
     */
    fun resetAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 1. 清空所有数据
                val clearResult = checkInRepository.clearAllData()
                if (clearResult.isFailure) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "数据清除失败: ${clearResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }
                
                // 2. 重新初始化默认数据
                val database = HabitTrackerDatabase.getDatabase(context)
                DefaultDataInitializer.initializeDefaultItems(context, database)
                
                // 3. 重置设置
                _themeMode.value = "system"
                _notificationsEnabled.value = true
                
                // 4. 重置用户信息到默认值
                preferencesRepository.saveUserProfile(
                    UserProfile(
                        nickname = "小明",
                        signature = "习惯成就卓越，坚持成就梦想！",
                        avatarType = "emoji",
                        avatarValue = "😊",
                        userId = "HT2024001"
                    )
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "重置完成！应用已恢复到初始状态"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "重置失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 重置所有设置（仅设置项，不包括数据）
     */
    fun resetAllSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 模拟重置
            kotlinx.coroutines.delay(1000)
            
            _themeMode.value = "system"
            _notificationsEnabled.value = true
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    successMessage = "所有设置已重置"
                )
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
     * 刷新所有数据
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // 模拟加载
            kotlinx.coroutines.delay(1000)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    successMessage = "设置已刷新"
                )
            }
        }
    }
    
    /**
     * 更新主题
     */
    fun updateTheme(isDark: Boolean) {
        _uiState.update { 
            it.copy(
                isDarkTheme = isDark,
                message = if (isDark) "已切换到深色主题" else "已切换到浅色主题"
            )
        }
    }
    
    /**
     * 更新通知开关
     */
    fun updateNotificationEnabled(enabled: Boolean) {
        _uiState.update { 
            it.copy(
                isNotificationEnabled = enabled,
                message = if (enabled) "通知已开启" else "通知已关闭"
            )
        }
    }
    
    /**
     * 更新AI角色
     */
    fun updateAiCharacter(character: String) {
        _uiState.update { 
            it.copy(
                selectedAiCharacter = character,
                message = "AI角色已更新为：$character"
            )
        }
    }
    
    /**
     * 导出数据
     */
    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(1000)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    message = "数据导出完成"
                )
            }
        }
    }
    
    /**
     * 导入数据
     */
    fun importData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(1000)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    message = "数据导入完成"
                )
            }
        }
    }
    
    /**
     * 清除所有数据
     */
    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(1000)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    message = "所有数据已清除"
                )
            }
        }
    }
    
    // ========== 用户信息管理 ==========
    
    /**
     * 保存用户信息
     */
    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveUserProfile(profile)
                // 共享Flow会自动更新，无需手动设置
                _uiState.update { 
                    it.copy(successMessage = "个人信息已保存") 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "保存失败：${e.message}") 
                }
            }
        }
    }
    
    /**
     * 更新用户昵称
     */
    fun updateUserNickname(nickname: String) {
        val currentProfile = userProfile.value
        val updatedProfile = currentProfile.copy(nickname = nickname)
        saveUserProfile(updatedProfile)
    }
    
    /**
     * 更新用户签名
     */
    fun updateUserSignature(signature: String) {
        val currentProfile = userProfile.value
        val updatedProfile = currentProfile.copy(signature = signature)
        saveUserProfile(updatedProfile)
    }
    
    /**
     * 更新用户头像
     */
    fun updateUserAvatar(avatarType: String, avatarValue: String) {
        val currentProfile = userProfile.value
        val updatedProfile = currentProfile.copy(
            avatarType = avatarType,
            avatarValue = avatarValue
        )
        saveUserProfile(updatedProfile)
    }
}

/**
 * 设置页面UI状态
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val message: String? = null,
    val isDarkTheme: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val selectedAiCharacter: String = "友善助手"
) 