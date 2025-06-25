package com.example.cur_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 习惯列表ViewModel
 * 管理习惯列表的显示、搜索、排序和操作
 * 
 * 注意：由于UseCase层还未实现，这是简化版本
 * 待后续层实现后再完善此ViewModel
 */
@HiltViewModel
class HabitListViewModel @Inject constructor(
    // 待后续步骤注入实际的UseCase依赖
) : ViewModel() {

    // ========== UI状态定义 ==========
    
    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // ========== 搜索功能 ==========
    
    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(isSearchActive = query.isNotBlank(), searchQuery = query) }
    }
    
    /**
     * 清除搜索
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.update { it.copy(isSearchActive = false) }
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
                    successMessage = "列表已刷新"
                )
            }
        }
    }
    
    /**
     * 更新选中的分类
     */
    fun updateSelectedCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
    
    /**
     * 更新排序选项
     */
    fun updateSelectedSortOption(sortOption: SortOption) {
        _uiState.update { it.copy(selectedSortOption = sortOption) }
    }
    
    /**
     * 切换习惯完成状态
     */
    fun toggleHabitComplete(habitId: Long) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedHabits = currentState.filteredHabits.map { habit ->
                    if (habit.id == habitId) {
                        habit.copy(isCompletedToday = !habit.isCompletedToday)
                    } else {
                        habit
                    }
                }
                currentState.copy(filteredHabits = updatedHabits)
            }
        }
    }
    
    /**
     * 删除习惯
     */
    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedHabits = currentState.filteredHabits.filter { it.id != habitId }
                currentState.copy(
                    filteredHabits = updatedHabits,
                    successMessage = "习惯已删除"
                )
            }
        }
    }
}

/**
 * 习惯列表UI状态
 */
data class HabitListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedSortOption: SortOption = SortOption.NAME,
    val filteredHabits: List<HabitItem> = emptyList()
)

/**
 * 排序选项
 */
enum class SortOption {
    NAME, CREATION_DATE, COMPLETION_RATE
}

/**
 * 习惯列表项
 */
data class HabitItem(
    val id: Long,
    val name: String,
    val description: String,
    val currentStreak: Int,
    val completionRate: Int,
    val isCompletedToday: Boolean
) 