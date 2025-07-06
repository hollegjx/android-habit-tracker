package com.example.cur_app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AI角色简化数据类
 */
data class SelectedAiCharacter(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val subtitle: String,
    val backgroundColor: List<androidx.compose.ui.graphics.Color>
)

/**
 * AI角色状态管理器
 * 管理当前选中的AI角色，支持全局状态同步
 */
object AiCharacterManager {
    
    // 默认AI角色（小樱）
    private val defaultCharacter = SelectedAiCharacter(
        id = "sakura",
        name = "小樱",
        iconEmoji = "🌸",
        subtitle = "温柔学习伙伴",
        backgroundColor = listOf(
            androidx.compose.ui.graphics.Color(0xFFff9a9e), 
            androidx.compose.ui.graphics.Color(0xFFfecfef)
        )
    )
    
    private val _currentCharacter = MutableStateFlow(defaultCharacter)
    val currentCharacter: StateFlow<SelectedAiCharacter> = _currentCharacter.asStateFlow()
    
    /**
     * 更新当前选中的AI角色
     */
    fun updateCurrentCharacter(character: SelectedAiCharacter) {
        _currentCharacter.value = character
    }
    
    /**
     * 获取当前角色
     */
    fun getCurrentCharacter(): SelectedAiCharacter = _currentCharacter.value
} 