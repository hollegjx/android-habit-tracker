package com.example.cur_app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.cur_app.data.local.entity.CheckInType
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import java.util.*

/**
 * 动态主题背景组件
 * 根据选中的打卡类型动态变化渐变背景色
 * 现代化设计，三种主题色风格一致
 * 支持沉浸式状态栏
 */
@Composable
fun DynamicThemeBackground(
    selectedType: CheckInType,
    modifier: Modifier = Modifier,
    enableImmersiveStatusBar: Boolean = true,
    content: @Composable () -> Unit
) {
    // 时间感知的基础背景颜色
    val timeBasedColors = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 5..8 -> Pair(Color(0xFF0F1419), Color(0xFF1A2332)) // 清晨
            hour in 9..11 -> Pair(Color(0xFF1A1A2E), Color(0xFF16213E)) // 上午
            hour in 12..17 -> Pair(Color(0xFF1E1E3F), Color(0xFF2A2A5A)) // 下午
            hour in 18..20 -> Pair(Color(0xFF2D1B69), Color(0xFF1A1A2E)) // 傍晚
            else -> Pair(Color(0xFF0D0D23), Color(0xFF1A1A2E)) // 夜晚
        }
    }
    
    // 现代化主题色配置
    val themeConfig = remember(selectedType) {
        when (selectedType) {
            CheckInType.STUDY -> ThemeConfig(
                primary = Color(0xFF667EEA),
                secondary = Color(0xFF764BA2),
                accent = Color(0xFF4F46E5),
                surface = Color(0xFF1E1B4B),
                name = "学习主题"
            )
            CheckInType.EXERCISE -> ThemeConfig(
                primary = Color(0xFFFF6B6B),
                secondary = Color(0xFFFF8E53),
                accent = Color(0xFFEF4444),
                surface = Color(0xFF4C1D95),
                name = "运动主题"
            )
            CheckInType.MONEY -> ThemeConfig(
                primary = Color(0xFF10B981),
                secondary = Color(0xFF34D399),
                accent = Color(0xFF059669),
                surface = Color(0xFF1F2937),
                name = "理财主题"
            )
        }
    }
    
    // 沉浸式状态栏设置
    if (enableImmersiveStatusBar) {
        val systemUiController = rememberSystemUiController()
        val statusBarColor = remember(selectedType, timeBasedColors) {
            // 使用时间感知的基础背景色作为状态栏颜色
            timeBasedColors.first.copy(alpha = 0.95f)
        }
        
        SideEffect {
            systemUiController.setStatusBarColor(
                color = statusBarColor,
                darkIcons = false
            )
        }
    }
    
    // 简化的静态背景（高性能），确保完全覆盖到底部边缘
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        timeBasedColors.first.copy(alpha = 0.95f),
                        timeBasedColors.second.copy(alpha = 0.9f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // 主题色简单叠加
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            themeConfig.primary.copy(alpha = 0.12f),
                            themeConfig.secondary.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(400f, 300f),
                        radius = 600f
                    )
                )
        )
        
        content()
    }
}

/**
 * 主题配置数据类
 */
data class ThemeConfig(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val surface: Color,
    val name: String
)

/**
 * 获取动态主题色
 */
@Composable
fun getDynamicThemeColor(selectedType: CheckInType): Color {
    return selectedType.color
}

/**
 * 获取动态文本颜色
 */
@Composable
fun getDynamicTextColor(selectedType: CheckInType, alpha: Float = 1f): Color {
    val baseColor = when (selectedType) {
        CheckInType.STUDY -> Color(0xFFE8EAF6)
        CheckInType.EXERCISE -> Color(0xFFFFF3E0)
        CheckInType.MONEY -> Color(0xFFE8F5E8)
    }
    
    return baseColor.copy(alpha = alpha)
} 