package com.example.cur_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cur_app.presentation.screens.*

/**
 * 应用主导航图
 * 定义所有页面的导航路由和传参
 */
@Composable
fun HabitTrackerNavigation(
    navController: NavHostController,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.HOME,
        modifier = modifier
    ) {
        // 主页
        composable(Route.HOME) {
            HomeScreen(
                onNavigateToAddHabit = {
                    navController.navigate(Route.HABIT_ADD)
                },
                onNavigateToAiChat = {
                    navController.navigate(Route.AI_CHAT)
                },
                onNavigateToTypeDetail = { typeId ->
                    navController.navigate(Route.typeDetail(typeId))
                }
            )
        }

        // 打卡类型详情
        composable(
            route = Route.TYPE_DETAIL,
            arguments = listOf(
                navArgument(Route.Args.TYPE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val typeId = backStackEntry.arguments?.getString(Route.Args.TYPE_ID) ?: "STUDY"
            TypeDetailScreen(
                typeId = typeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAiChat = {
                    navController.navigate(Route.AI_CHAT)
                },
                onNavigateToAddHabit = {
                    navController.navigate(Route.HABIT_ADD)
                }
            )
        }
        
        // 聊天列表  
        composable(Route.CHAT_LIST) {
            ChatListScreen(
                onNavigateToChat = { conversationId ->
                    navController.navigate(Route.chatDetail(conversationId))
                },
                onNavigateToAddUser = {
                    android.util.Log.d("HabitTrackerNavigation", "onNavigateToAddUser called - this was empty before!")
                    // 不需要导航，ChatListScreen内部处理AddFriendDialog
                },
                onSearchMessages = { query ->
                    // TODO: 处理搜索消息
                }
            )
        }
        
        // 聊天详情
        composable(
            route = Route.CHAT_DETAIL,
            arguments = listOf(
                navArgument(Route.Args.CONVERSATION_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString(Route.Args.CONVERSATION_ID) ?: ""
            ChatDetailScreen(
                conversationId = conversationId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
                }
        
        // AI角色选择
        composable(Route.AI_CHAT) {
            AiCharacterSelectionScreen(
                onNavigateToChat = { characterId ->
                    // 这里应该创建AI对话并跳转到ChatDetailScreen
                    // TODO: 创建AI对话的逻辑
                    android.util.Log.d("Navigation", "选择了角色: $characterId")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 统计页面
        composable(Route.STATISTICS) {
            StatisticsScreen()
        }
        
        // 设置页面
        composable(Route.SETTINGS) {
            SettingsScreen(
                onNavigateToLogin = onLogout
            )
        }
    }
} 