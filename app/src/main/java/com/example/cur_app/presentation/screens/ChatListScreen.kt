package com.example.cur_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cur_app.data.local.entity.*
import com.example.cur_app.presentation.components.*
import com.example.cur_app.ui.theme.*
import com.example.cur_app.data.local.AiCharacterManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

/**
 * 聊天列表界面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToAddUser: () -> Unit = {},
    onSearchMessages: (String) -> Unit = {}
) {
    var showSearchMenu by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    
    // 获取当前AI角色
    val currentAiCharacter by AiCharacterManager.currentCharacter.collectAsStateWithLifecycle()
    
    // 获取聊天数据
    val chatUsers = remember { MockChatData.getMockChatUsers() }
    val conversations = com.example.cur_app.data.local.ChatStateManager.conversations
    
    // 构建AI伙伴聊天项（只显示当前选中的AI角色）
    val aiChatItems = remember(currentAiCharacter, conversations) {
        val currentAiUser = AiBots.getCurrentAiCharacterAsUser()
        val aiConversation = conversations.find { it.otherUserId == "ai_current_character" }
            ?: ChatConversation(
                conversationId = "conv_current_ai",
                otherUserId = "ai_current_character",
                lastMessage = "你好！我是你的AI学习伙伴${currentAiUser.nickname}，有什么可以帮助你的吗？",
                lastMessageTime = Date(),
                unreadCount = 0
            )
        
        listOf(
            ChatListItem(
                conversation = aiConversation,
                otherUser = currentAiUser,
                lastMessage = aiConversation.lastMessage,
                lastMessageTime = aiConversation.lastMessageTime,
                unreadCount = aiConversation.unreadCount,
                isOnline = true
            )
        )
    }
    
    // 构建用户聊天列表项（排除AI机器人）
    val userChatItems = remember(chatUsers, conversations) {
        conversations.filter { conversation ->
            val otherUser = chatUsers.find { it.userId == conversation.otherUserId }
            otherUser != null && !otherUser.isAiBot
        }.map { conversation ->
            val otherUser = chatUsers.find { it.userId == conversation.otherUserId }
                ?: ChatUser("unknown", "未知用户", "❓")
            
            ChatListItem(
                conversation = conversation,
                otherUser = otherUser,
                lastMessage = conversation.lastMessage,
                lastMessageTime = conversation.lastMessageTime,
                unreadCount = conversation.unreadCount,
                isOnline = otherUser.isOnline
            )
        }.sortedWith(
            compareByDescending<ChatListItem> { it.conversation.isPinned }
                .thenByDescending { it.lastMessageTime }
        )
    }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    DynamicThemeBackground(selectedType = CheckInType.STUDY) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "💬 消息",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    actions = {
                        Box {
                            IconButton(
                                onClick = { showSearchMenu = !showSearchMenu }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索",
                                    tint = Color.White
                                )
                            }
                            
                            // 搜索菜单
                            ChatSearchMenu(
                                expanded = showSearchMenu,
                                onDismiss = { showSearchMenu = false },
                                onSearchMessages = { query ->
                                    showSearchMenu = false
                                    onSearchMessages(query)
                                },
                                onAddUser = {
                                    showSearchMenu = false
                                    onNavigateToAddUser()
                                }
                            )
                        }
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // AI伙伴分组
                item {
                    ChatSectionHeader(
                        title = "🤖 AI伙伴",
                        subtitle = "智能学习助手"
                    )
                }
                
                items(
                    items = aiChatItems,
                    key = { "ai_${it.conversation.conversationId}" }
                ) { chatItem ->
                    ChatListItemRow(
                        chatItem = chatItem,
                        onClick = { onNavigateToChat(chatItem.conversation.conversationId) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                // 用户分组
                if (userChatItems.isNotEmpty()) {
                    item {
                        ChatSectionHeader(
                            title = "👥 好友聊天",
                            subtitle = "${userChatItems.size}个联系人"
                        )
                    }
                    
                    items(
                        items = userChatItems,
                        key = { "user_${it.conversation.conversationId}" }
                    ) { chatItem ->
                        ChatListItemRow(
                            chatItem = chatItem,
                            onClick = { onNavigateToChat(chatItem.conversation.conversationId) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

/**
 * 聊天分组标题组件
 */
@Composable
fun ChatSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -20 },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(400))
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * 聊天列表项组件
 */
@Composable
fun ChatListItemRow(
    chatItem: ChatListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 30 },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(500))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (chatItem.otherUser.isAiBot) {
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        }
                    )
                )
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 头像区域
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // 头像背景
                    Box(
                        modifier = Modifier
                            .size(56.dp)
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
                            text = chatItem.otherUser.avatar,
                            fontSize = 24.sp
                        )
                    }
                    
                    // 在线状态指示器
                    if (chatItem.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .align(Alignment.BottomEnd)
                        )
                    }
                    
                    // AI机器人标识
                    if (chatItem.otherUser.isAiBot) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2196F3))
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🤖",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                
                // 内容区域
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 第一行：昵称和时间
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chatItem.otherUser.nickname,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = formatTime(chatItem.lastMessageTime),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    // 第二行：最后消息和未读数
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chatItem.lastMessage,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 未读消息数
                        if (chatItem.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (chatItem.unreadCount > 99) "99+" else chatItem.unreadCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 时间格式化函数
 */
private fun formatTime(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    
    return when {
        diff < 60 * 1000 -> "刚刚" // 1分钟内
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前" // 1小时内
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前" // 1天内
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前" // 1周内
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(date) // 超过1周显示日期
    }
} 