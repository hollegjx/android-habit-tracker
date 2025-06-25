package com.example.cur_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cur_app.R
import com.example.cur_app.data.local.entity.CheckInType
import com.example.cur_app.data.repository.UserProfile
import com.example.cur_app.presentation.components.*
import com.example.cur_app.presentation.viewmodel.SettingsViewModel
import com.example.cur_app.ui.theme.*
import com.example.cur_app.utils.ImageUtils
import com.example.cur_app.utils.UserAvatarDisplay
import com.example.cur_app.utils.rememberImagePicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * 现代化设置界面
 * 包含用户信息、外观设置、功能选项等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    
    // 从ViewModel获取用户信息
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    // 用户信息状态
    var userNickname by remember { mutableStateOf(userProfile.nickname) }
    var userSignature by remember { mutableStateOf(userProfile.signature) }
    var userAvatarType by remember { mutableStateOf(userProfile.avatarType) }
    var userAvatarValue by remember { mutableStateOf(userProfile.avatarValue) }
    val userId = "ID: ${userProfile.userId}"
    
    // 监听userProfile变化并更新本地状态
    LaunchedEffect(userProfile) {
        userNickname = userProfile.nickname
        userSignature = userProfile.signature
        userAvatarType = userProfile.avatarType
        userAvatarValue = userProfile.avatarValue
    }
    
    // 启动进入动画
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    // 使用动态主题背景 - 设置页面使用紫色主题
    DynamicThemeBackground(selectedType = CheckInType.STUDY) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "⚙️ 个人设置",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 用户信息卡片
                item {
                    CardAppearAnimation(visible = isVisible, index = 0) {
                        UserProfileCard(
                            nickname = userNickname,
                            signature = userSignature,
                            avatarType = userAvatarType,
                            avatarValue = userAvatarValue,
                            userId = userId,
                            onClick = { showEditDialog = true },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                // 外观设置
                item {
                    CardAppearAnimation(visible = isVisible, index = 1) {
                        SettingsSection(
                            title = "🎨 外观设置",
                            items = listOf(
                                SettingsItem(
                                    icon = Icons.Default.Settings,
                                    title = "深色主题",
                                    subtitle = "保护您的眼睛",
                                    hasSwitch = true,
                                    switchState = uiState.isDarkTheme,
                                    onSwitchChange = viewModel::updateTheme
                                ),
                                SettingsItem(
                                    icon = Icons.Default.Settings,
                                    title = "语言设置",
                                    subtitle = "简体中文",
                                    onClick = { /* TODO: 语言设置 */ }
                                )
                            )
                        )
                    }
                }
                
                // 通知设置
                item {
                    CardAppearAnimation(visible = isVisible, index = 2) {
                        SettingsSection(
                            title = "🔔 通知设置",
                            items = listOf(
                                SettingsItem(
                                    icon = Icons.Default.Notifications,
                                    title = "推送通知",
                                    subtitle = "接收打卡提醒",
                                    hasSwitch = true,
                                    switchState = uiState.isNotificationEnabled,
                                    onSwitchChange = viewModel::updateNotificationEnabled
                                ),
                                SettingsItem(
                                    icon = Icons.Default.Notifications,
                                    title = "提醒时间",
                                    subtitle = "每日 09:00",
                                    onClick = { /* TODO: 提醒时间设置 */ }
                                )
                            )
                        )
                    }
                }
                
                // 数据管理
                item {
                    CardAppearAnimation(visible = isVisible, index = 3) {
                        SettingsSection(
                            title = "🗂️ 数据管理",
                            items = listOf(
                                SettingsItem(
                                    icon = Icons.Default.Refresh,
                                    title = "重置所有数据",
                                    subtitle = "清空所有进度，恢复初始状态",
                                    onClick = { showResetDialog = true }
                                ),
                                SettingsItem(
                                    icon = Icons.Default.Info,
                                    title = "数据导出",
                                    subtitle = "备份您的数据",
                                    onClick = { /* TODO: 数据导出 */ }
                                )
                            )
                        )
                    }
                }
                
                // 帮助与支持
                item {
                    CardAppearAnimation(visible = isVisible, index = 4) {
                        SettingsSection(
                            title = "💬 帮助与支持",
                            items = listOf(
                                SettingsItem(
                                    icon = Icons.Default.Email,
                                    title = "联系开发者",
                                    subtitle = "反馈问题或建议",
                                    onClick = { /* TODO: 联系开发者 */ }
                                ),
                                SettingsItem(
                                    icon = Icons.Default.Info,
                                    title = "隐私协议",
                                    subtitle = "了解我们的隐私政策",
                                    onClick = { /* TODO: 隐私协议 */ }
                                ),
                                SettingsItem(
                                    icon = Icons.Default.Info,
                                    title = "关于应用",
                                    subtitle = "版本 1.0.0",
                                    onClick = { /* TODO: 关于应用 */ }
                                )
                            )
                        )
                    }
                }
                
                // 退出登录
                item {
                    CardAppearAnimation(visible = isVisible, index = 5) {
                        LogoutSection(
                            onLogout = { /* TODO: 退出登录逻辑 */ }
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
    
    // 编辑用户信息弹窗
    if (showEditDialog) {
        EditProfileDialog(
            nickname = userNickname,
            signature = userSignature,
            avatarType = userAvatarType,
            avatarValue = userAvatarValue,
            onDismiss = { showEditDialog = false },
            onSave = { newNickname, newSignature, newAvatarType, newAvatarValue ->
                userNickname = newNickname
                userSignature = newSignature
                userAvatarType = newAvatarType
                userAvatarValue = newAvatarValue
                
                // 保存用户信息到持久化存储
                viewModel.saveUserProfile(
                    UserProfile(
                        nickname = newNickname,
                        signature = newSignature,
                        avatarType = newAvatarType,
                        avatarValue = newAvatarValue,
                        userId = userProfile.userId
                    )
                )
                
                showEditDialog = false
            }
        )
    }
    
    // 重置确认对话框
    if (showResetDialog) {
        ResetConfirmDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                viewModel.resetAllData()
                showResetDialog = false
            }
        )
    }
}

/**
 * 用户信息卡片
 */
@Composable
fun UserProfileCard(
    nickname: String,
    signature: String,
    avatarType: String,
    avatarValue: String,
    userId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 头像
            UserAvatarDisplay(
                avatarType = avatarType,
                avatarValue = avatarValue,
                size = 80.dp,
                fontSize = 32.sp
            )
            
            // 用户信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = nickname,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = userId,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = signature,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
            }
            
            // 编辑图标
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "编辑",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 设置项数据类
 */
data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val hasSwitch: Boolean = false,
    val switchState: Boolean = false,
    val onSwitchChange: ((Boolean) -> Unit)? = null,
    val onClick: (() -> Unit)? = null
)

/**
 * 设置分组组件
 */
@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 0.5.dp
                    )
                }
                
                SettingsItemRow(item = item)
            }
        }
    }
}

/**
 * 现代化重置确认对话框
 */
@Composable
fun ResetConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isAnimationVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isAnimationVisible = true
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFFF8F0)
                        )
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 警告图标
                AnimatedVisibility(
                    visible = isAnimationVisible,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B6B).copy(alpha = 0.2f),
                                        Color(0xFFFF6B6B).copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "警告",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                // 标题和内容
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⚠️ 确认重置",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "此操作将永久删除以下数据：",
                        fontSize = 16.sp,
                        color = Color(0xFF4A5568),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // 警告列表
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF2F2)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WarningItem("📊", "所有打卡记录和进度")
                            WarningItem("🎯", "所有学习、运动、理财项目")
                            WarningItem("🏆", "所有成就和统计数据")
                            WarningItem("💬", "所有AI聊天记录")
                            WarningItem("⚙️", "所有个人设置")
                        }
                    }
                    
                    Text(
                        text = "应用将恢复到初始状态，无法撤销！",
                        fontSize = 14.sp,
                        color = Color(0xFFE53E3E),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4A5568)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // 确认重置按钮
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B6B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "确认重置",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 警告项组件
 */
@Composable
private fun WarningItem(
    icon: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = icon,
            fontSize = 18.sp
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF4A5568),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color(0xFFE53E3E),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 设置项行组件
 */
@Composable
fun SettingsItemRow(
    item: SettingsItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = item.onClick != null) { 
                item.onClick?.invoke() 
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = item.subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        if (item.hasSwitch) {
            Switch(
                checked = item.switchState,
                onCheckedChange = item.onSwitchChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.3f),
                    uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        } else if (item.onClick != null) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "进入",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 退出登录区域
 */
@Composable
fun LogoutSection(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B).copy(alpha = 0.8f),
                        Color(0xFFFF5252).copy(alpha = 0.6f)
                    )
                )
            )
            .clickable { onLogout() }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "退出登录",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "退出登录",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 编辑用户信息弹窗
 */
@Composable
fun EditProfileDialog(
    nickname: String,
    signature: String,
    avatarType: String,
    avatarValue: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var editNickname by remember { mutableStateOf(nickname) }
    var editSignature by remember { mutableStateOf(signature) }
    var editAvatarType by remember { mutableStateOf(avatarType) }
    var editAvatarValue by remember { mutableStateOf(avatarValue) }
    var selectedTabIndex by remember { mutableStateOf(if (avatarType == "emoji") 0 else 1) }
    var isProcessingImage by remember { mutableStateOf(false) }
    
    val availableAvatars = ImageUtils.DEFAULT_EMOJI_AVATARS
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 图片选择器
    val imagePicker = rememberImagePicker(
        onImageSelected = { uri ->
            scope.launch {
                isProcessingImage = true
                try {
                    val fileName = ImageUtils.generateAvatarFileName()
                    val savedPath = ImageUtils.saveUserAvatarFromUri(context, uri, fileName)
                    if (savedPath != null) {
                        editAvatarType = "image"
                        editAvatarValue = savedPath
                    }
                } catch (e: Exception) {
                    // 处理错误，可以显示提示
                } finally {
                    isProcessingImage = false
                }
            }
        },
        onPermissionDenied = {
            // 权限被拒绝的处理
        }
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 标题
                Text(
                    text = "编辑个人信息",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 头像选择标签
                Text(
                    text = "选择头像",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                
                // 头像类型切换标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("表情头像", "自定义头像").forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedTabIndex == index)
                                        GradientStart.copy(alpha = 0.2f)
                                    else
                                        Color.Gray.copy(alpha = 0.1f)
                                )
                                .border(
                                    width = if (selectedTabIndex == index) 1.dp else 0.dp,
                                    color = GradientStart,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTabIndex = index }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) GradientStart else TextSecondary
                            )
                        }
                    }
                }
                
                // 头像选择内容
                when (selectedTabIndex) {
                    0 -> {
                        // Emoji头像选择
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableAvatars.size) { index ->
                                val avatarEmoji = availableAvatars[index]
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (editAvatarType == "emoji" && avatarEmoji == editAvatarValue) 
                                                GradientStart.copy(alpha = 0.2f) 
                                            else 
                                                Color.Gray.copy(alpha = 0.1f)
                                        )
                                        .border(
                                            width = if (editAvatarType == "emoji" && avatarEmoji == editAvatarValue) 2.dp else 0.dp,
                                            color = GradientStart,
                                            shape = CircleShape
                                        )
                                        .clickable { 
                                            editAvatarType = "emoji"
                                            editAvatarValue = avatarEmoji 
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = avatarEmoji,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // 自定义头像选择
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 当前头像预览
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                UserAvatarDisplay(
                                    avatarType = editAvatarType,
                                    avatarValue = editAvatarValue,
                                    size = 60.dp,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "当前头像",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            // 选择图片按钮
                            Button(
                                onClick = { 
                                    imagePicker.onPickImage()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GradientStart.copy(alpha = 0.8f)
                                ),
                                enabled = !isProcessingImage
                            ) {
                                if (isProcessingImage) {
                                    Text(
                                        text = "处理中...",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        text = "从相册选择",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            Text(
                                text = "支持JPG、PNG等常见图片格式",
                                fontSize = 10.sp,
                                color = TextSecondary.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // 昵称输入
                OutlinedTextField(
                    value = editNickname,
                    onValueChange = { editNickname = it },
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        focusedLabelColor = GradientStart
                    )
                )
                
                // 个性签名输入
                OutlinedTextField(
                    value = editSignature,
                    onValueChange = { editSignature = it },
                    label = { Text("个性签名") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        focusedLabelColor = GradientStart
                    )
                )
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = { 
                            onSave(editNickname, editSignature, editAvatarType, editAvatarValue)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        )
                    ) {
                        Text("保存", color = Color.White)
                    }
                }
            }
        }
    }
} 