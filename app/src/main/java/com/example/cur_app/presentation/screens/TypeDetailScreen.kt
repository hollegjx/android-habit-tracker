package com.example.cur_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.cur_app.data.repository.CheckInRepository
import com.example.cur_app.presentation.components.AddCheckInCard
import com.example.cur_app.presentation.components.AddCheckInItemDialog
import com.example.cur_app.presentation.components.CardAppearAnimation
import com.example.cur_app.presentation.components.CheckInItemCard
import com.example.cur_app.presentation.components.CheckInStatsCard
import com.example.cur_app.presentation.components.DeleteConfirmDialog
import com.example.cur_app.presentation.components.DynamicThemeBackground
import com.example.cur_app.presentation.components.EditCheckInItemDialog
import com.example.cur_app.presentation.components.FocusModeDialog
import com.example.cur_app.presentation.components.LevelUpgradeCard
import com.example.cur_app.presentation.components.ModernAiSuggestionCard
import com.example.cur_app.presentation.components.ModernPlanCard
import com.example.cur_app.presentation.components.SmartCalendarSection
import com.example.cur_app.presentation.components.UpgradeRequirement
import com.example.cur_app.presentation.components.getDynamicThemeColor
import com.example.cur_app.presentation.viewmodel.TypeDetailViewModel
import kotlinx.coroutines.delay
import android.util.Log

/**
 * 打卡类型详情页面
 * 显示特定类型的完整功能：日历、成就、AI助手、打卡项目
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeDetailScreen(
    typeId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    onNavigateToAddHabit: () -> Unit = {},
    viewModel: TypeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedType = CheckInType.fromString(typeId)
    var isVisible by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFocusDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<CheckInRepository.CheckInItemWithTodayStatus?>(null) }
    
    // 监控状态变化
    LaunchedEffect(showFocusDialog) {
        Log.d("TypeDetailScreen", "===== showFocusDialog 状态变化: $showFocusDialog =====")
    }
    
    // 初始化数据
    LaunchedEffect(selectedType) {
        viewModel.initializeType(selectedType)
    }
    
    // 错误提示处理
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 可以在这里显示 Toast 或 Snackbar
            delay(3000)
            viewModel.clearError()
        }
    }

    // 启动进入动画
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    // 使用动态主题背景
    DynamicThemeBackground(selectedType = selectedType) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent, // 让Scaffold背景透明
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedType.displayName}打卡",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White // 改为白色以在深色背景上显示
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = androidx.compose.ui.graphics.Color.White // 改为白色
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent // 透明背景
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 类型统计卡片（暂时使用模拟数据）
                item {
                    CardAppearAnimation(visible = isVisible, index = 0) {
                        // 创建模拟统计数据
                        val mockStats = com.example.cur_app.data.local.entity.CheckInStats(
                            type = selectedType,
                            todayValue = "120",
                            totalValue = "2400",
                            streakDays = 7,
                            completionRate = 0.75f,
                            weeklyData = listOf(0.8f, 0.6f, 0.9f, 0.7f, 0.85f, 0.75f, 0.9f)
                        )
                        CheckInStatsCard(
                            stats = mockStats,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                
                // 智能日历模块
                item {
                    CardAppearAnimation(visible = isVisible, index = 1) {
                        val (completedToday, totalToday) = viewModel.getTodayCompletionForType(selectedType)
                        
                        SmartCalendarSection(
                            todayCompleted = completedToday,
                            currentStreak = uiState.currentStreak,
                            completedDates = uiState.completedDates
                        )
                    }
                }
                
                // 现代化今日计划卡片
                item {
                    CardAppearAnimation(visible = isVisible, index = 2) {
                        val (completedCount, totalCount) = viewModel.getTodayCompletionForType(selectedType)
                        
                        ModernPlanCard(
                            selectedType = selectedType,
                            completedCount = completedCount,
                            totalCount = totalCount
                        )
                    }
                }
                
                // 今日打卡项目列表
                if (uiState.checkInItemsWithStatus.isNotEmpty()) {
                    itemsIndexed(uiState.checkInItemsWithStatus) { index, itemWithStatus ->
                        CardAppearAnimation(visible = isVisible, index = 3 + index) {
                            CheckInItemCard(
                                type = selectedType,
                                title = itemWithStatus.item.title,
                                value = itemWithStatus.todayActualValue.toString(),
                                unit = itemWithStatus.item.unit,
                                targetValue = itemWithStatus.item.targetValue.toString(),
                                isCompleted = itemWithStatus.isCompletedToday,
                                experienceValue = itemWithStatus.item.experienceValue,
                                onToggle = { 
                                    viewModel.toggleCheckInItem(itemWithStatus.item.id)
                                },
                                onEdit = {
                                    selectedItem = itemWithStatus
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedItem = itemWithStatus
                                    showDeleteDialog = true
                                },
                                onFocus = {
                                    // 只有未完成的任务才能进入专注模式
                                    if (!itemWithStatus.isCompletedToday) {
                                        selectedItem = itemWithStatus
                                        showFocusDialog = true
                                    }
                                }
                            )
                        }
                    }
                } else if (!uiState.isLoading) {
                    // 显示空状态或默认项目
                    item {
                        CardAppearAnimation(visible = isVisible, index = 3) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "暂无${selectedType.displayName}项目",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "点击下方按钮创建第一个项目吧！",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 添加新计划按钮
                item {
                    CardAppearAnimation(visible = isVisible, index = 10) {
                        AddCheckInCard(
                            type = selectedType,
                            onAdd = { showAddDialog = true }
                        )
                    }
                }
                
                // 等级升级卡片
                item {
                    CardAppearAnimation(visible = isVisible, index = 11) {
                        // 使用动态数据而不是硬编码
                        uiState.userAchievement?.let { achievement ->
                            uiState.upgradeRequirement?.let { upgradeReq ->
                                val upgradeRequirements = listOf(
                                    UpgradeRequirement(
                                        "经验值", 
                                        upgradeReq.currentExp, 
                                        upgradeReq.requiredExp, 
                                        "点"
                                    ),
                                    when (selectedType) {
                                        CheckInType.STUDY -> UpgradeRequirement(
                                            "学习总时长", 
                                            achievement.totalStudyTime / 60, 
                                            (upgradeReq.requiredExp / 10), 
                                            "小时"
                                        )
                                        CheckInType.EXERCISE -> UpgradeRequirement(
                                            "运动总时长", 
                                            achievement.totalExerciseTime / 60, 
                                            (upgradeReq.requiredExp / 15), 
                                            "小时"
                                        )
                                        CheckInType.MONEY -> UpgradeRequirement(
                                            "储蓄金额", 
                                            (achievement.totalMoney / 100).toInt(), 
                                            upgradeReq.requiredExp * 2, 
                                            "元"
                                        )
                                    }
                                )
                                
                                LevelUpgradeCard(
                                    currentLevel = achievement.currentLevel,
                                    nextLevel = upgradeReq.nextLevel,
                                    requirements = upgradeRequirements,
                                    gradientColors = listOf(selectedType.color, selectedType.color.copy(alpha = 0.8f))
                                )
                            }
                        } ?: run {
                            // 如果没有成就数据，显示加载状态或占位符
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "正在加载成就数据...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // AI建议卡片
                item {
                    CardAppearAnimation(visible = isVisible, index = 13) {
                        ModernAiSuggestionCard(
                            selectedType = selectedType,
                            suggestion = when(selectedType) {
                                CheckInType.STUDY -> "今天学习了什么新知识？点击和AI伙伴分享你的收获吧！💕"
                                CheckInType.EXERCISE -> "运动后感觉怎么样？和AI伙伴聊聊你的运动体验！💪"
                                CheckInType.MONEY -> "理财规划进展如何？让AI伙伴帮你分析一下～🎯"
                            },
                            onClickMore = onNavigateToAiChat,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
    
    // 添加打卡项目对话框
    AddCheckInItemDialog(
        isVisible = showAddDialog,
        selectedType = selectedType,
        onDismiss = { showAddDialog = false },
        onConfirm = { title, description, targetValue, unit, icon, color ->
            viewModel.createCheckInItem(
                type = selectedType,
                title = title,
                description = description,
                targetValue = targetValue,
                unit = unit,
                icon = icon,
                color = color
            )
            showAddDialog = false
        }
    )

    // 编辑打卡项目对话框
    selectedItem?.let { item ->
        EditCheckInItemDialog(
            isVisible = showEditDialog,
            item = item.item,
            onDismiss = { 
                showEditDialog = false
                selectedItem = null
            },
            onConfirm = { title, description, targetValue, unit, icon, color ->
                viewModel.updateCheckInItem(
                    itemId = item.item.id,
                    title = title,
                    description = description,
                    targetValue = targetValue,
                    unit = unit,
                    icon = icon,
                    color = color
                )
                showEditDialog = false
                selectedItem = null
            }
        )
    }

    // 删除确认对话框
    selectedItem?.let { item ->
        DeleteConfirmDialog(
            isVisible = showDeleteDialog,
            itemTitle = item.item.title,
            itemType = selectedType,
            onDismiss = { 
                showDeleteDialog = false
                selectedItem = null
            },
            onConfirm = {
                viewModel.deleteCheckInItem(item.item.id)
                showDeleteDialog = false
                selectedItem = null
            }
        )
    }

    // 专注模式对话框
    selectedItem?.let { item ->
        Log.d("TypeDetailScreen", "===== 渲染FocusModeDialog =====")
        Log.d("TypeDetailScreen", "showFocusDialog: $showFocusDialog")
        Log.d("TypeDetailScreen", "selectedItem: ${item.item.title}")
        
        FocusModeDialog(
            isVisible = showFocusDialog,
            itemTitle = item.item.title,
            itemType = selectedType,
            targetMinutes = item.item.targetValue,
            currentMinutes = item.todayActualValue,
            onDismiss = { 
                Log.d("TypeDetailScreen", "===== FocusModeDialog onDismiss 被调用 =====")
                showFocusDialog = false
                selectedItem = null
            },
            onComplete = { focusedMinutes: Int ->
                Log.d("TypeDetailScreen", "===== FocusModeDialog onComplete 被调用: $focusedMinutes =====")
                viewModel.submitFocusResult(item.item.id, focusedMinutes)
                showFocusDialog = false
                selectedItem = null
            }
        )
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