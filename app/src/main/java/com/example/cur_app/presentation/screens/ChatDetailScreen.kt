package com.example.cur_app.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cur_app.data.local.entity.*
import com.example.cur_app.presentation.components.*
import com.example.cur_app.ui.theme.*
import com.example.cur_app.data.repository.PreferencesRepository
import com.example.cur_app.data.repository.UserProfile
import com.example.cur_app.utils.UserAvatarDisplay
import com.example.cur_app.data.local.AiCharacterManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

/**
 * 聊天详情界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    onNavigateBack: () -> Unit = {}
) {
    // 获取HomeViewModel以访问用户信息
    val homeViewModel: com.example.cur_app.presentation.viewmodel.HomeViewModel = hiltViewModel()
    val userProfile by homeViewModel.userProfile.collectAsStateWithLifecycle()
    
    // 获取当前AI角色信息
    val currentAiCharacter by AiCharacterManager.currentCharacter.collectAsStateWithLifecycle()
    
    // 获取对话数据
    val chatUsers = remember { MockChatData.getMockChatUsers() }
    val conversations = com.example.cur_app.data.local.ChatStateManager.conversations
    
    // 进入聊天时标记为已读
    LaunchedEffect(conversationId) {
        com.example.cur_app.data.local.ChatStateManager.markConversationAsRead(conversationId)
    }
    
    val conversation = conversations.find { it.conversationId == conversationId }
    val otherUser = conversation?.let { conv ->
        // 如果是AI对话，使用当前选中的AI角色
        if (conv.otherUserId == "ai_current_character" || conv.otherUserId.startsWith("ai_")) {
            ChatUser(
                userId = "ai_current_character",
                nickname = currentAiCharacter.name,
                avatar = currentAiCharacter.iconEmoji,
                isAiBot = true,
                aiType = "current_character",
                isOnline = true
            )
        } else {
            chatUsers.find { it.userId == conv.otherUserId }
        }
    } ?: run {
        // 如果没有找到对话，检查conversationId是否为AI相关
        if (conversationId.contains("ai") || conversationId.contains("current_ai")) {
            ChatUser(
                userId = "ai_current_character",
                nickname = currentAiCharacter.name,
                avatar = currentAiCharacter.iconEmoji,
                isAiBot = true,
                aiType = "current_character",
                isOnline = true
            )
        } else {
            ChatUser("unknown", "未知用户", "❓")
        }
    }
    
    // 模拟消息数据
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                messageId = 1,
                conversationId = conversationId,
                senderId = otherUser.userId,
                receiverId = "current_user",
                content = conversation?.lastMessage ?: "你好！",
                timestamp = Date(System.currentTimeMillis() - 10 * 60 * 1000),
                isFromMe = false
            ),
            ChatMessage(
                messageId = 2,
                conversationId = conversationId,
                senderId = "current_user",
                receiverId = otherUser.userId,
                content = "你好，今天学习进度如何？",
                timestamp = Date(System.currentTimeMillis() - 5 * 60 * 1000),
                isFromMe = true
            )
        )
    }
    
    var inputText by remember { mutableStateOf("") }
    
    DynamicThemeBackground(selectedType = CheckInType.STUDY) {
        Scaffold(
            containerColor = Color.Transparent,
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
                ChatInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = {
                        if (inputText.isNotBlank()) {
                            messages.add(
                                ChatMessage(
                                    messageId = messages.size + 1L,
                                    conversationId = conversationId,
                                    senderId = "current_user",
                                    receiverId = otherUser.userId,
                                    content = inputText,
                                    timestamp = Date(),
                                    isFromMe = true
                                )
                            )
                            // 更新对话的最后消息
                            com.example.cur_app.data.local.ChatStateManager.updateLastMessage(
                                conversationId = conversationId,
                                lastMessage = inputText
                            )
                            inputText = ""
                            
                            // 模拟AI回复
                            if (otherUser.isAiBot) {
                                val aiReply = "感谢你的提问！作为你的AI学习伙伴，我会尽力帮助你。"
                                messages.add(
                                    ChatMessage(
                                        messageId = messages.size + 1L,
                                        conversationId = conversationId,
                                        senderId = otherUser.userId,
                                        receiverId = "current_user",
                                        content = aiReply,
                                        timestamp = Date(),
                                        isFromMe = false
                                    )
                                )
                                // 更新AI回复的最后消息
                                com.example.cur_app.data.local.ChatStateManager.updateLastMessage(
                                    conversationId = conversationId,
                                    lastMessage = aiReply
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
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
                enabled = inputText.isNotBlank(),
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (inputText.isNotBlank()) GradientStart else Color.Gray,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 