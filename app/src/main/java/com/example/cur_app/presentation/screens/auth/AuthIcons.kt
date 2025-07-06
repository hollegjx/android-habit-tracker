package com.example.cur_app.presentation.screens.auth

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

/**
 * 自定义图标组件，使用emoji替代Material Icons中不可用的图标
 */
@Composable
fun EmojiIcon(
    emoji: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified
) {
    Text(
        text = emoji,
        fontSize = if (size != Dp.Unspecified) (size.value * 0.8).sp else 18.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

// 常用图标emoji
object AuthEmojis {
    const val VISIBILITY = "👁️"
    const val VISIBILITY_OFF = "🙈"
    const val ERROR = "❌"
    const val WARNING = "⚠️"
    const val SUCCESS = "✅"
    const val BUG_REPORT = "🐛"
    const val DEVELOPER_MODE = "👨‍💻"
    const val VERIFIED_USER = "✅"
    const val PERSON_ADD = "👤➕"
    const val BADGE = "🏷️"
    const val LOCK_OPEN = "🔓"
}