package com.example.cur_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 添加/编辑习惯ViewModel
 * 管理习惯的创建和编辑表单
 * 
 * 注意：由于UseCase层还未实现，这是简化版本
 * 待后续层实现后再完善此ViewModel
 */
@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    // 待后续步骤注入实际的UseCase依赖
) : ViewModel() {

    // ========== UI状态定义 ==========
    
    private val _uiState = MutableStateFlow(AddEditHabitUiState())
    val uiState: StateFlow<AddEditHabitUiState> = _uiState.asStateFlow()
    
    private val _formState = MutableStateFlow(HabitFormState())
    val formState: StateFlow<HabitFormState> = _formState.asStateFlow()
    
    // 是否为编辑模式
    private var editingHabitId: Long? = null
    val isEditMode: Boolean get() = editingHabitId != null
    
    // ========== 表单管理 ==========
    
    /**
     * 更新习惯名称
     */
    fun updateName(name: String) {
        _formState.update { it.copy(name = name) }
        _uiState.update { it.copy(name = name) }
        validateForm()
    }
    
    /**
     * 更新习惯描述
     */
    fun updateDescription(description: String) {
        _formState.update { it.copy(description = description) }
        _uiState.update { it.copy(description = description) }
    }
    
    /**
     * 更新习惯分类
     */
    fun updateCategory(category: String) {
        _formState.update { it.copy(category = category) }
        _uiState.update { it.copy(category = category) }
    }
    
    /**
     * 更新习惯颜色
     */
    fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }
    
    /**
     * 更新频率类型
     */
    fun updateFrequencyType(type: Int) {
        _uiState.update { it.copy(frequencyType = type) }
    }
    
    /**
     * 更新目标次数
     */
    fun updateTargetCount(count: Int) {
        _uiState.update { it.copy(targetCount = count) }
    }
    
    /**
     * 更新提醒开关
     */
    fun updateReminderEnabled(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }
    
    /**
     * 加载习惯（编辑模式）
     */
    fun loadHabit(habitId: Long) {
        editingHabitId = habitId
        // 模拟加载习惯数据
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(500)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = "示例习惯",
                    description = "这是一个示例习惯描述",
                    category = "健康"
                )
            }
        }
    }
    
    /**
     * 创建习惯
     */
    fun createHabit() {
        saveHabit()
    }
    
    /**
     * 更新习惯
     */
    fun updateHabit() {
        saveHabit()
    }
    
    // ========== 表单验证 ==========
    
    /**
     * 验证表单
     */
    private fun validateForm() {
        val form = _formState.value
        val errors = mutableListOf<String>()
        
        // 验证名称
        if (form.name.isBlank()) {
            errors.add("习惯名称不能为空")
        } else if (form.name.length > 50) {
            errors.add("习惯名称不能超过50个字符")
        }
        
        _uiState.update { 
            it.copy(
                formErrors = errors,
                isFormValid = errors.isEmpty()
            )
        }
    }
    
    // ========== 保存操作 ==========
    
    /**
     * 保存习惯
     */
    fun saveHabit() {
        validateForm()
        
        if (!_uiState.value.isFormValid) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 模拟保存
            kotlinx.coroutines.delay(1000)
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    saveSuccess = true,
                    isHabitSaved = true,
                    successMessage = if (isEditMode) "习惯更新成功！" else "习惯创建成功！"
                )
            }
        }
    }
    
    // ========== 其他操作 ==========
    
    /**
     * 重置表单
     */
    fun resetForm() {
        _formState.value = HabitFormState()
        _uiState.update { 
            it.copy(
                formErrors = emptyList(),
                isFormValid = false,
                saveSuccess = false
            )
        }
    }
    
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
    
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "运动健身", "学习成长", "健康生活", "工作效率", 
            "人际关系", "兴趣爱好", "精神修养", "其他"
        )
    }
}

/**
 * 添加/编辑习惯UI状态
 */
data class AddEditHabitUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val saveSuccess: Boolean = false,
    val formErrors: List<String> = emptyList(),
    val isFormValid: Boolean = false,
    val isHabitSaved: Boolean = false,
    // 表单字段
    val name: String = "",
    val description: String = "",
    val category: String = "其他",
    val color: String = "#2196F3",
    val frequencyType: Int = 0, // 0=每天, 1=每周, 2=每月
    val targetCount: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val nameError: String? = null
)

/**
 * 习惯表单状态
 */
data class HabitFormState(
    val name: String = "",
    val description: String = "",
    val category: String = "其他"
) 