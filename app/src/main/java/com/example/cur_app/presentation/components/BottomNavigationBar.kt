package com.example.cur_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cur_app.R
import com.example.cur_app.presentation.navigation.Route
import com.example.cur_app.ui.theme.*
import com.example.cur_app.data.local.AiCharacterManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*

/**
 * 根据当前路由获取底部导航栏背景颜色
 */
@Composable
private fun getNavigationBarBackground(currentRoute: String?): Brush {
    return when (currentRoute) {
        Route.HOME -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF667EEA).copy(alpha = 0.8f),
                Color(0xFF764BA2).copy(alpha = 0.9f)
            )
        )
        Route.CHAT_LIST -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF667EEA).copy(alpha = 0.8f),
                Color(0xFF764BA2).copy(alpha = 0.9f)
            )
        )
        Route.AI_CHAT -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6366F1).copy(alpha = 0.8f),
                Color(0xFF6366F1).copy(alpha = 0.9f)
            )
        )
        Route.STATISTICS -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF43A047).copy(alpha = 0.8f),
                Color(0xFF2E7D32).copy(alpha = 0.9f)
            )
        )
        Route.SETTINGS -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF334155).copy(alpha = 0.8f),
                Color(0xFF1E293B).copy(alpha = 0.9f)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF667EEA).copy(alpha = 0.8f),
                Color(0xFF764BA2).copy(alpha = 0.9f)
            )
        )
    }
}

/**
 * 底部导航栏数据项
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
)

/**
 * 底部导航栏组件 - 重新设计版
 * 采用新的视觉风格和色彩方案
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    unreadMessageCount: Int = 0 // 未读消息数
) {
    val currentCharacter by AiCharacterManager.currentCharacter.collectAsStateWithLifecycle()
    val items = listOf(
        BottomNavItem(
            route = Route.HOME,
            icon = Icons.Filled.Home,
            labelResId = R.string.nav_home
        ),
        BottomNavItem(
            route = Route.CHAT_LIST,
            icon = Icons.Filled.Email,
            labelResId = R.string.nav_chat
        ),
        BottomNavItem(
            route = Route.AI_CHAT,
            icon = Icons.Filled.Person,
            labelResId = R.string.nav_ai_chat
        ),
        BottomNavItem(
            route = Route.STATISTICS,
            icon = Icons.Filled.Star,
            labelResId = R.string.nav_statistics
        ),
        BottomNavItem(
            route = Route.SETTINGS,
            icon = Icons.Filled.Settings,
            labelResId = R.string.nav_settings
        )
    )
    
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    NavigationBar(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp) // 增加高度覆盖黑边
            .background(getNavigationBarBackground(currentRoute)) // 动态适配页面背景
            .padding(top = 12.dp) // 向上延展的padding
    ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Box {
                            // AI助手使用当前角色头像，其他使用图标
                            if (item.route == Route.AI_CHAT) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.radialGradient(currentCharacter.backgroundColor)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentCharacter.iconEmoji,
                                        fontSize = 16.sp
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.labelResId),
                                    tint = if (currentRoute == item.route) GradientStart else Color(0xFF64748B),
                                    modifier = Modifier.size(28.dp) // 增大图标尺寸
                                )
                            }
                            
                            // 显示未读消息红点
                            if (item.route == Route.CHAT_LIST && unreadMessageCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp) // 略微增大红点
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF4444))
                                        .offset(x = 8.dp, y = (-2).dp), // 调整位置适应更大的图标
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString(),
                                        fontSize = 8.sp, // 略微增大红点文字
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(item.labelResId),
                            fontSize = 13.sp, // 略微增大文字
                            fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentRoute == item.route) GradientStart else Color(0xFF64748B)
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // 只在切换到主要页面时清理返回栈
                                if (item.route == Route.HOME) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                } else {
                                    // 对于其他页面，保持返回栈以便正常返回
                                    popUpTo(Route.HOME) {
                                        saveState = true
                                    }
                                }
                                // 避免多实例
                                launchSingleTop = true
                                // 恢复状态
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GradientStart,
                        selectedTextColor = GradientStart,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = GradientStart.copy(alpha = 0.2f)
                    )
                )
            }
        }
} 