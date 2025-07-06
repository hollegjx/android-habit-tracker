package com.example.cur_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.local.entity.CheckInType
import com.example.cur_app.domain.usecase.CheckInUseCase
import com.example.cur_app.domain.usecase.AchievementUseCase
import com.example.cur_app.data.repository.CheckInRepository
import com.example.cur_app.data.database.entities.UserAchievementEntity
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 类型详情页面ViewModel
 * 管理特定类型的打卡项目和统计数据
 */
@HiltViewModel
class TypeDetailViewModel @Inject constructor(
    private val checkInUseCase: CheckInUseCase,
    private val checkInRepository: CheckInRepository,
    private val achievementUseCase: AchievementUseCase
) : ViewModel() {

    // UI状态数据类
    data class TypeDetailUiState(
        val isLoading: Boolean = false,
        val checkInItemsWithStatus: List<CheckInRepository.CheckInItemWithTodayStatus> = emptyList(),
        val typeStats: CheckInRepository.CheckInTypeStats? = null,
        val userAchievement: UserAchievementEntity? = null,
        val upgradeRequirement: AchievementUseCase.UpgradeRequirement? = null,
        val completedDates: Set<LocalDate> = emptySet(),
        val currentStreak: Int = 0,
        val error: String? = null
    )

    // 当前类型
    private val _currentType = MutableStateFlow<CheckInType?>(null)

    // UI状态
    private val _uiState = MutableStateFlow(TypeDetailUiState())
    val uiState: StateFlow<TypeDetailUiState> = _uiState.asStateFlow()

    /**
     * 初始化指定类型的数据
     */
    fun initializeType(type: CheckInType) {
        _currentType.value = type
        loadTypeData(type)
    }

    /**
     * 加载类型数据
     */
    private fun loadTypeData(type: CheckInType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 加载项目和今日状态
                checkInRepository.getItemsWithTodayStatusByType(type)
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "加载数据失败: ${e.message}"
                        )
                    }
                    .collect { itemsWithStatus ->
                        // 同时加载统计数据
                        val stats = checkInRepository.getTypeStats(type)
                        
                        // 加载成就数据
                        val userAchievement = achievementUseCase.getCurrentUserAchievement(type)
                        val upgradeRequirement = achievementUseCase.getUpgradeRequirement(type)
                        
                        // 加载日历数据
                        val completedDates = checkInRepository.getCompletedDatesByTypeThisMonth(type)
                        val currentStreak = checkInRepository.getCurrentStreakDays()
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            checkInItemsWithStatus = itemsWithStatus,
                            typeStats = stats,
                            userAchievement = userAchievement,
                            upgradeRequirement = upgradeRequirement,
                            completedDates = completedDates,
                            currentStreak = currentStreak,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载数据失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 创建新的打卡项目
     */
    fun createCheckInItem(
        type: CheckInType,
        title: String,
        description: String,
        targetValue: Int,
        unit: String,
        icon: String,
        color: String
    ) {
        viewModelScope.launch {
            try {
                val result = checkInRepository.createCheckInItem(
                    type = type,
                    title = title,
                    description = description,
                    targetValue = targetValue,
                    unit = unit,
                    icon = icon,
                    color = color
                )
                
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "创建项目失败: ${result.exceptionOrNull()?.message}"
                    )
                }
                // 成功时数据会通过Flow自动更新
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "创建项目失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 切换打卡项目的完成状态
     */
    fun toggleCheckInItem(itemId: Long) {
        viewModelScope.launch {
            try {
                val currentItem = _uiState.value.checkInItemsWithStatus
                    .find { it.item.id == itemId }
                
                if (currentItem != null) {
                    if (currentItem.isCompletedToday) {
                        // 取消完成
                        val result = checkInUseCase.cancelCheckIn(itemId)
                        if (!result.success) {
                            _uiState.value = _uiState.value.copy(
                                error = "取消完成失败: ${result.message}"
                            )
                        }
                    } else {
                        // 标记为完成 - 使用CheckInUseCase以触发成就系统
                        val result = checkInUseCase.completeCheckIn(
                            itemId = itemId,
                            actualValue = currentItem.item.targetValue,
                            note = "快速完成"
                        )
                        if (!result.success) {
                            _uiState.value = _uiState.value.copy(
                                error = "完成打卡失败: ${result.message}"
                            )
                        } else if (result.isNewAchievement && result.achievementMessage != null) {
                            // 可以在这里显示成就提示
                            _uiState.value = _uiState.value.copy(
                                error = null // 先清除错误，后续可以添加成就提示UI
                            )
                        }
                    }
                }
                // 数据会通过Flow自动更新
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "操作失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 获取今日完成情况（用于兼容现有UI）
     */
    fun getTodayCompletionForType(type: CheckInType): Pair<Int, Int> {
        val currentState = _uiState.value
        val completed = currentState.checkInItemsWithStatus.count { it.isCompletedToday }
        val total = currentState.checkInItemsWithStatus.size
        return completed to total
    }

    /**
     * 编辑打卡项目
     */
    fun updateCheckInItem(
        itemId: Long,
        title: String,
        description: String,
        targetValue: Int,
        unit: String,
        icon: String,
        color: String
    ) {
        viewModelScope.launch {
            try {
                val result = checkInRepository.updateCheckInItem(
                    itemId = itemId,
                    title = title,
                    description = description,
                    targetValue = targetValue,
                    unit = unit,
                    icon = icon,
                    color = color
                )
                
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "更新项目失败: ${result.exceptionOrNull()?.message}"
                    )
                }
                // 成功时数据会通过Flow自动更新
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "更新项目失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 删除打卡项目
     */
    fun deleteCheckInItem(itemId: Long) {
        viewModelScope.launch {
            try {
                val result = checkInRepository.deleteCheckInItem(itemId)
                
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "删除项目失败: ${result.exceptionOrNull()?.message}"
                    )
                }
                // 成功时数据会通过Flow自动更新
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "删除项目失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 提交专注模式结果
     */
    fun submitFocusResult(itemId: Long, focusedMinutes: Int) {
        viewModelScope.launch {
            try {
                val currentItem = _uiState.value.checkInItemsWithStatus
                    .find { it.item.id == itemId }
                
                if (currentItem != null) {
                    // 累加专注时间到当前进度
                    val newActualValue = currentItem.todayActualValue + focusedMinutes
                    
                    // 使用CheckInUseCase以触发成就系统
                    val result = checkInUseCase.completeCheckIn(
                        itemId = itemId,
                        actualValue = newActualValue,
                        note = "专注模式完成 ${focusedMinutes} 分钟"
                    )
                    
                    if (!result.success) {
                        _uiState.value = _uiState.value.copy(
                            error = "提交专注结果失败: ${result.message}"
                        )
                    } else if (result.isNewAchievement && result.achievementMessage != null) {
                        // 可以在这里显示成就提示
                        _uiState.value = _uiState.value.copy(
                            error = null // 先清除错误，后续可以添加成就提示UI
                        )
                    }
                }
                // 数据会通过Flow自动更新
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "提交专注结果失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 