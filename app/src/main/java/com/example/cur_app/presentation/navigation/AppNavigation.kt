package com.example.cur_app.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cur_app.presentation.components.BottomNavigationBar
import com.example.cur_app.presentation.screens.MainScreen
import com.example.cur_app.presentation.viewmodel.AuthViewModel

/**
 * 应用主导航组件
 * 根据用户认证状态决定显示认证页面还是主应用页面
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    
    // 根据登录状态决定初始路由
    val startDestination = if (uiState.isLoggedIn) {
        AppScreens.Main.route
    } else {
        AppScreens.Auth.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 认证流程
        composable(AppScreens.Auth.route) {
            AuthNavigation(
                onNavigateToMain = {
                    navController.navigate(AppScreens.Main.route) {
                        popUpTo(AppScreens.Auth.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        // 主应用
        composable(AppScreens.Main.route) {
            AuthGuard(authViewModel = authViewModel) {
                MainAppWithNavigation(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(AppScreens.Auth.route) {
                            popUpTo(AppScreens.Main.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
    
    // 监听登录状态变化，自动导航
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn && navController.currentDestination?.route == AppScreens.Auth.route) {
            navController.navigate(AppScreens.Main.route) {
                popUpTo(AppScreens.Auth.route) { inclusive = true }
            }
        } else if (!uiState.isLoggedIn && navController.currentDestination?.route == AppScreens.Main.route) {
            navController.navigate(AppScreens.Auth.route) {
                popUpTo(AppScreens.Main.route) { inclusive = true }
            }
        }
    }
}

/**
 * 认证守卫组件
 * 确保只有已认证的用户才能访问受保护的内容
 */
@Composable
fun AuthGuard(
    authViewModel: AuthViewModel,
    content: @Composable () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.isLoggedIn || uiState.isTestMode) {
        content()
    } else {
        // 如果用户未登录，显示加载或重定向到认证页面
        // 这里可以显示一个加载界面或者空白页面
        // 实际的导航会由上层的 AppNavigation 处理
    }
}

/**
 * 应用主要屏幕路由定义
 */
/**
 * 主应用导航组件 (包含底部导航栏的完整应用)
 */
@Composable
fun MainAppWithNavigation(
    onLogout: () -> Unit
) {
    val mainNavController = rememberNavController()
    
    // 动态获取未读消息数
    val unreadMessageCount = com.example.cur_app.data.local.ChatStateManager.totalUnreadCount
    
    // 获取专注模式状态
    val isFocusModeActive = com.example.cur_app.data.local.ChatStateManager.isFocusModeActive
    
    // 获取当前路由，用于判断是否显示底部导航栏
    val currentRoute = mainNavController.currentBackStackEntryAsState().value?.destination?.route
    
    // 底部导航栏显示逻辑：专注模式时隐藏，聊天详情页面隐藏，认证页面隐藏
    val shouldShowBottomBar = !isFocusModeActive && 
        !(currentRoute?.startsWith("chat_detail") == true) &&
        !(currentRoute?.startsWith("auth_") == true)
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // 保持透明，让页面自己控制背景
        contentWindowInsets = WindowInsets(0), // 移除默认的window insets
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    navController = mainNavController,
                    unreadMessageCount = unreadMessageCount
                )
            }
        }
    ) { paddingValues ->
        HabitTrackerNavigation(
            navController = mainNavController,
            onLogout = onLogout,
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                // 不应用底部padding，让背景延伸到导航栏区域
                bottom = 0.dp
            )
        )
    }
}

sealed class AppScreens(val route: String) {
    object Auth : AppScreens("app_auth")
    object Main : AppScreens("app_main")
}