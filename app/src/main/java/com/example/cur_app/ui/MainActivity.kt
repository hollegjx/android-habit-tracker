package com.example.cur_app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cur_app.presentation.components.BottomNavigationBar
import com.example.cur_app.presentation.navigation.HabitTrackerNavigation
import com.example.cur_app.ui.theme.Cur_appTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

/**
 * AI习惯追踪应用主Activity
 * 使用Jetpack Compose构建UI，集成导航和底部导航栏
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 设置状态栏和导航栏透明
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            Cur_appTheme {
                // 状态栏沉浸式效果
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                    systemUiController.setNavigationBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                }
                
                HabitTrackerApp()
            }
        }
    }
}

@Composable
fun HabitTrackerApp() {
    val navController = rememberNavController()
    
    // 动态获取未读消息数
    val unreadMessageCount = com.example.cur_app.data.local.ChatStateManager.totalUnreadCount
    
    // 获取专注模式状态
    val isFocusModeActive = com.example.cur_app.data.local.ChatStateManager.isFocusModeActive
    
    // 获取当前路由，用于判断是否显示底部导航栏
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    // 底部导航栏显示逻辑：专注模式时隐藏，聊天详情页面隐藏
    val shouldShowBottomBar = !isFocusModeActive && !(currentRoute?.startsWith("chat_detail") == true)
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // 保持透明，让页面自己控制背景
        contentWindowInsets = WindowInsets(0), // 移除默认的window insets
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    unreadMessageCount = unreadMessageCount
                )
            }
        }
    ) { paddingValues ->
        HabitTrackerNavigation(
            navController = navController,
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                // 不应用底部padding，让背景延伸到导航栏区域
                bottom = 0.dp
            )
        )
    }
} 