package com.example.cur_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cur_app.data.local.entity.CheckInType
import com.example.cur_app.data.local.entity.CheckInStats
import com.example.cur_app.data.local.entity.CheckInRecord
import com.example.cur_app.presentation.components.*
import com.example.cur_app.presentation.viewmodel.HomeViewModel
import com.example.cur_app.presentation.components.getDynamicTextColor
import kotlinx.coroutines.delay
import com.example.cur_app.ui.theme.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * 主界面 - 三类型打卡系统
 * 支持学习、运动、攒钱三种打卡类型的现代化界面
 * 集成动态主题切换和丝滑动画效果
 */
@Composable
fun HomeScreen(
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    onNavigateToTypeDetail: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val userAchievements by viewModel.userAchievements.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf(CheckInType.STUDY) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    // 模拟三类型统计数据
    val checkInStats = remember {
        listOf(
            CheckInStats(
                type = CheckInType.STUDY,
                todayValue = "120",
                totalValue = "2400",
                streakDays = 7,
                completionRate = 0.75f,
                weeklyData = listOf(0.8f, 0.6f, 0.9f, 0.7f, 0.85f, 0.75f, 0.9f)
            ),
            CheckInStats(
                type = CheckInType.EXERCISE,
                todayValue = "450",
                totalValue = "8900",
                streakDays = 5,
                completionRate = 0.60f,
                weeklyData = listOf(0.5f, 0.8f, 0.6f, 0.9f, 0.7f, 0.6f, 0.8f)
            ),
            CheckInStats(
                type = CheckInType.MONEY,
                todayValue = "50",
                totalValue = "1250",
                streakDays = 12,
                completionRate = 0.90f,
                weeklyData = listOf(0.9f, 1.0f, 0.8f, 0.9f, 1.0f, 0.9f, 0.85f)
            )
        )
    }

    // 启动进入动画
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    // 生命周期感知的用户信息刷新
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 当页面恢复时刷新用户信息，确保从设置页面返回时数据同步
                viewModel.refreshUserProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 使用动态主题背景
    DynamicThemeBackground(selectedType = selectedType) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 用户问候头部
            item {
                CardAppearAnimation(visible = isVisible, index = 0) {
                    UserGreetingHeader(
                        userName = userProfile.nickname,
                        userAvatarType = userProfile.avatarType,
                        userAvatarValue = userProfile.avatarValue,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
            
            // 可翻转语录模块
            item {
                CardAppearAnimation(visible = isVisible, index = 1) {
                    FlippableQuoteCard()
                }
            }
            
            // 总览统计卡片
            item {
                CardAppearAnimation(visible = isVisible, index = 2) {
                    Column {
                        Text(
                            text = "📊 今日总览",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = getDynamicTextColor(selectedType),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            checkInStats.forEach { stats ->
                                CheckInStatsCard(
                                    stats = stats,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onNavigateToTypeDetail(stats.type.name)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // AI智能建议模块
            item {
                CardAppearAnimation(visible = isVisible, index = 3) {
                    AiSuggestionCard()
                }
            }
            
            // 整体成就展示
            item {
                CardAppearAnimation(visible = isVisible, index = 4) {
                    Column {
                        Text(
                            text = "🏆 总体成就",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = getDynamicTextColor(selectedType),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        ModernAchievementGrid(
                            userAchievements = userAchievements,
                            onAchievementClick = { category ->
                                onNavigateToTypeDetail(category)
                            }
                        )
                    }
                }
            }
            
            // 底部间距，为导航栏留出空间，确保内容不被遮挡
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
    
    // 处理加载状态
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = getDynamicThemeColor(selectedType))
        }
    }
} 