package com.example.cur_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.cur_app.ui.theme.BackgroundGradientStart
import com.example.cur_app.ui.theme.BackgroundGradientEnd

/**
 * 渐变背景组件
 * 创建从上到下的紫色渐变背景，匹配参考设计风格
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    startColor: Color = BackgroundGradientStart,
    endColor: Color = BackgroundGradientEnd,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        content()
    }
}

/**
 * 简化版渐变背景，仅提供背景色
 */
@Composable
fun GradientBox(
    modifier: Modifier = Modifier,
    startColor: Color = BackgroundGradientStart,
    endColor: Color = BackgroundGradientEnd
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    )
} 