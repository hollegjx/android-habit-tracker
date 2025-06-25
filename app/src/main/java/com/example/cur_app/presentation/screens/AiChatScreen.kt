package com.example.cur_app.presentation.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * AI聊天界面 - 角色选择入口
 * 现在主要作为AI角色选择的入口界面
 */
@Composable
fun AiChatScreen(
    onNavigateBack: () -> Unit = {}
) {
    // 直接显示AI角色选择界面
    AiCharacterSelectionScreen(
        onNavigateToChat = { characterId ->
            // TODO: 可以导航到具体的AI聊天界面
            // 现在先跳转到聊天列表的AI对话
        },
        onNavigateBack = onNavigateBack
    )
} 