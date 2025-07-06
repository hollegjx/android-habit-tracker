package com.example.cur_app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import com.example.cur_app.data.local.entity.*
import com.example.cur_app.presentation.components.*
import com.example.cur_app.ui.theme.*
import com.example.cur_app.data.repository.PreferencesRepository
import com.example.cur_app.data.repository.UserProfile
import com.example.cur_app.utils.UserAvatarDisplay
import com.example.cur_app.data.local.AiCharacterManager
import com.example.cur_app.presentation.screens.chat.ChatDetailViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.*

/**
 * 聊天详情界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    // 获取HomeViewModel以访问用户信息
    val homeViewModel: com.example.cur_app.presentation.viewmodel.HomeViewModel = hiltViewModel()
    val userProfile by homeViewModel.userProfile.collectAsStateWithLifecycle()
    
    // 获取当前AI角色信息
    val currentAiCharacter by AiCharacterManager.currentCharacter.collectAsStateWithLifecycle()
    
    // 从ViewModel获取数据
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageInput by viewModel.messageInput.collectAsStateWithLifecycle()
    
    // 从UI状态获取对话和消息数据
    val conversation = uiState.conversation
    val otherUser = uiState.otherUser?.let { user ->
        // 如果是AI对话，使用当前选中的AI角色信息
        if (user.isAiBot && conversation?.conversationType == "AI") {
            ChatUser(
                userId = user.userId,
                nickname = currentAiCharacter.name,
                avatar = currentAiCharacter.iconEmoji,
                isAiBot = true,
                aiType = "current_character",
                isOnline = true
            )
        } else {
            ChatUser(
                userId = user.userId,
                nickname = user.nickname,
                avatar = user.avatar,
                isOnline = user.isOnline,
                isAiBot = user.isAiBot
            )
        }
    } ?: ChatUser("unknown", "未知用户", "❓")
    
    // 将数据库消息转换为UI模型
    val messages = remember(uiState.messages) {
        uiState.messages.map { entity ->
            ChatMessage(
                messageId = entity.id,
                conversationId = entity.conversationId,
                senderId = entity.senderId,
                receiverId = entity.receiverId,
                content = entity.content,
                timestamp = Date(entity.timestamp),
                isFromMe = entity.isFromMe
            )
        }
    }
    
    // 初始化数据加载
    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId)
    }
    
    DynamicThemeBackground(selectedType = CheckInType.STUDY) {
        Scaffold(
            containerColor = Color.Transparent,
            // 只对内容区域避开键盘，保持顶部栏固定
            contentWindowInsets = WindowInsets.ime,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 头像
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.3f),
                                                Color.White.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = otherUser.avatar,
                                    fontSize = 20.sp
                                )
                            }
                            
                            // 用户信息
                            Column {
                                Text(
                                    text = otherUser.nickname,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (otherUser.isOnline) {
                                    Text(
                                        text = if (otherUser.isAiBot) "AI助手在线" else "在线",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                EnhancedChatInputBar(
                    inputText = messageInput,
                    onInputChange = viewModel::onMessageInputChanged,
                    onSendMessage = viewModel::sendMessage,
                    isLoading = uiState.isSending
                )
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                // 加载状态
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = "加载聊天记录...",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            } else if (uiState.error != null) {
                // 错误状态
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "😔",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "加载失败",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = uiState.error ?: "未知错误",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text("重试", color = Color.White)
                        }
                    }
                }
            } else if (messages.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💬",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "开始对话吧",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "发送第一条消息开始聊天",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                // 消息列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true // 最新消息在底部
                ) {
                    items(
                        items = messages.reversed(),
                        key = { it.messageId }
                    ) { message ->
                        ChatMessageItem(
                            message = message,
                            otherUserAvatar = otherUser.avatar,
                            userProfile = userProfile
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * 聊天消息项
 */
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    otherUserAvatar: String,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromMe) {
            // 对方头像
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = otherUserAvatar,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // 消息气泡
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                        )
                    )
                    .background(
                        brush = if (message.isFromMe) {
                            Brush.horizontalGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (message.isFromMe) Color.White else TextPrimary,
                    lineHeight = 18.sp
                )
            }
            
            // 时间戳
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        
        if (message.isFromMe) {
            Spacer(modifier = Modifier.width(8.dp))
            
            // 自己的头像 - 使用真实用户头像
            UserAvatarDisplay(
                avatarType = userProfile.avatarType,
                avatarValue = userProfile.avatarValue,
                size = 36.dp,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * 聊天输入栏
 */
@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.1f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 输入框
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = { 
                    Text(
                        "输入消息...",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                maxLines = 4
            )
            
            // 发送按钮
            Button(
                onClick = onSendMessage,
                enabled = inputText.isNotBlank() && !isLoading,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (inputText.isNotBlank() && !isLoading) GradientStart else Color.Gray,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 增强版聊天输入栏（类似QQ聊天界面）
 */
@Composable
fun EnhancedChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    // 处理输入框点击：隐藏菜单，显示键盘
    val handleInputFocus = {
        if (isMenuExpanded) {
            isMenuExpanded = false
        }
    }
    
    // 处理加号按钮点击：隐藏键盘，显示菜单
    val handlePlusClick = {
        keyboardController?.hide()
        focusManager.clearFocus()
        isMenuExpanded = !isMenuExpanded
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 弹出菜单
        if (isMenuExpanded) {
            AttachmentMenu(
                onImageClick = { /* TODO: 上传图像 */ },
                onFileClick = { /* TODO: 上传文件 */ },
                onRecordClick = { /* TODO: 上传记录 */ },
                onDismiss = { isMenuExpanded = false }
            )
        }
        
        // 输入栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(16.dp)
                // 移除imePadding()，因为Scaffold的bottomBar已经自动处理键盘位置
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 输入框
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { 
                        onInputChange(it)
                        handleInputFocus()
                    },
                    placeholder = { 
                        Text(
                            "输入消息...",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    maxLines = 4
                )
                
                // 发送按钮
                Button(
                    onClick = onSendMessage,
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (inputText.isNotBlank() && !isLoading) GradientStart else Color.Gray,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "发送",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // 加号按钮（附件功能）
                Button(
                    onClick = handlePlusClick,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMenuExpanded) GradientStart else Color.White.copy(alpha = 0.3f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "附件",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 附件菜单
 */
@Composable
fun AttachmentMenu(
    onImageClick: () -> Unit,
    onFileClick: () -> Unit,
    onRecordClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.05f),
                        Color.Black.copy(alpha = 0.1f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 上传图像
            AttachmentMenuItem(
                icon = Icons.Default.Star,
                label = "图像",
                onClick = {
                    onImageClick()
                    onDismiss()
                },
                backgroundColor = Color(0xFF4CAF50)
            )
            
            // 上传文件
            AttachmentMenuItem(
                icon = Icons.Default.Favorite,
                label = "文件",
                onClick = {
                    onFileClick()
                    onDismiss()
                },
                backgroundColor = Color(0xFF2196F3)
            )
            
            // 上传记录
            AttachmentMenuItem(
                icon = Icons.Default.Settings,
                label = "记录",
                onClick = {
                    onRecordClick()
                    onDismiss()
                },
                backgroundColor = Color(0xFFFF9800)
            )
        }
    }
}

/**
 * 附件菜单项
 */
@Composable
fun AttachmentMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 图标圆形背景
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 标签
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
} 