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
import com.example.cur_app.presentation.components.AddFriendDialog
import com.example.cur_app.ui.theme.*
import com.example.cur_app.data.local.AiCharacterManager
import com.example.cur_app.presentation.screens.chat.ChatListViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import android.util.Log

/**
 * 聊天列表界面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToAddUser: () -> Unit = {},
    onSearchMessages: (String) -> Unit = {},
    viewModel: ChatListViewModel = hiltViewModel()
) {
    var showSearchMenu by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { 
        mutableStateOf(false).also {
            Log.d("ChatListScreen", "🏁 Initial showAddFriendDialog state: false")
        }
    }
    var isVisible by remember { mutableStateOf(false) }
    
    // 添加日志来调试状态变化
    LaunchedEffect(showAddFriendDialog) {
        Log.e("ChatListScreen", "🔄 STATE CHANGE: showAddFriendDialog = $showAddFriendDialog")
        println("CONSOLE: showAddFriendDialog changed to $showAddFriendDialog")
    }
    
    // 获取当前AI角色
    val currentAiCharacter by AiCharacterManager.currentCharacter.collectAsStateWithLifecycle()
    
    // 从ViewModel获取数据
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    
    // 构建聊天列表项，确保AI对话在顶部
    val chatItems = remember(uiState.conversations, uiState.users, currentAiCharacter) {
        uiState.conversations.map { conversation ->
            val otherUser = uiState.users[conversation.otherUserId]
            val displayUser = if (conversation.conversationType == "AI" && otherUser?.isAiBot == true) {
                // 对于AI对话，显示当前选中的AI角色信息
                val currentAiUser = AiBots.getCurrentAiCharacterAsUser()
                ChatUser(
                    userId = otherUser.userId,
                    nickname = currentAiUser.nickname,
                    avatar = currentAiUser.avatar,
                    isOnline = true,
                    isAiBot = true
                )
            } else {
                // 将数据库实体转换为UI模型
                otherUser?.let {
                    ChatUser(
                        userId = it.userId,
                        nickname = it.nickname,
                        avatar = it.avatar,
                        isOnline = it.isOnline,
                        isAiBot = it.isAiBot
                    )
                } ?: ChatUser("unknown", "未知用户", "❓")
            }
            
            ChatListItem(
                conversation = ChatConversation(
                    conversationId = conversation.conversationId,
                    otherUserId = conversation.otherUserId,
                    lastMessage = conversation.lastMessage,
                    lastMessageTime = Date(conversation.lastMessageTime),
                    unreadCount = conversation.unreadCount,
                    isPinned = conversation.isPinned,
                    isArchived = conversation.isArchived
                ),
                otherUser = displayUser,
                lastMessage = conversation.lastMessage,
                lastMessageTime = Date(conversation.lastMessageTime),
                unreadCount = conversation.unreadCount,
                isOnline = displayUser.isOnline
            )
        }.sortedWith(
            // 排序规则：AI对话优先，然后是置顶，最后按时间排序
            compareByDescending<ChatListItem> { it.otherUser.isAiBot }
                .thenByDescending { it.conversation.isPinned }
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
                                    Log.e("ChatListScreen", "🔴 SEARCH MENU BUTTON CLICKED!")
                                    println("CONSOLE: Search menu add friend clicked")
                                    showSearchMenu = false
                                    showAddFriendDialog = true
                                    Log.e("ChatListScreen", "🔴 SETTING showAddFriendDialog = true from search menu")
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
                
                // 根据会话类型分组显示
                val aiChatItems = chatItems.filter { it.otherUser.isAiBot }
                val userChatItems = chatItems.filter { !it.otherUser.isAiBot }
                
                // AI伙伴分组 - 总是显示
                item {
                    ChatSectionHeader(
                        title = "🤖 AI伙伴",
                        subtitle = "智能学习助手"
                    )
                }
                
                if (aiChatItems.isNotEmpty()) {
                    items(
                        items = aiChatItems,
                        key = { "ai_${it.conversation.conversationId}" }
                    ) { chatItem ->
                        ChatListItemRow(
                            chatItem = chatItem,
                            onClick = { 
                                viewModel.markConversationAsRead(chatItem.conversation.conversationId)
                                onNavigateToChat(chatItem.conversation.conversationId)
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                } else if (!uiState.isLoading) {
                    // AI对话不存在时的提示
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🤖",
                                    fontSize = 32.sp
                                )
                                Text(
                                    text = "AI学习伙伴尚未初始化",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "点击设置中的\"插入测试数据\"来体验AI聊天",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                
                // 用户分组
                item {
                    ChatSectionHeader(
                        title = "👥 好友聊天",
                        subtitle = if (userChatItems.isNotEmpty()) "${userChatItems.size}个联系人" else "暂无联系人"
                    )
                }
                
                if (userChatItems.isNotEmpty()) {
                    items(
                        items = userChatItems,
                        key = { "user_${it.conversation.conversationId}" }
                    ) { chatItem ->
                        ChatListItemRow(
                            chatItem = chatItem,
                            onClick = { 
                                viewModel.markConversationAsRead(chatItem.conversation.conversationId)
                                onNavigateToChat(chatItem.conversation.conversationId)
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                } else if (!uiState.isLoading) {
                    // 好友列表为空时的提示
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "👥",
                                    fontSize = 32.sp
                                )
                                Text(
                                    text = "还没有好友聊天",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "添加好友开始聊天吧！",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { 
                                        Log.e("ChatListScreen", "🔴 EMPTY STATE BUTTON CLICKED!")
                                        println("CONSOLE: Empty state add friend clicked")
                                        showAddFriendDialog = true 
                                        Log.e("ChatListScreen", "🔴 SETTING showAddFriendDialog = true from empty state")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "添加好友",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("添加好友")
                                }
                            }
                        }
                    }
                }
                
                // 加载状态
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
    
    // 添加好友对话框
    Log.e("ChatListScreen", "🔴 RECOMPOSITION: showAddFriendDialog = $showAddFriendDialog")
    println("CONSOLE: Recomposition with showAddFriendDialog = $showAddFriendDialog")
    
    if (showAddFriendDialog) {
        Log.e("ChatListScreen", "🔴 ✅ RENDERING AddFriendDialog")
        AddFriendDialog(
            onDismiss = { 
                Log.e("ChatListScreen", "🔴 AddFriendDialog onDismiss called")
                showAddFriendDialog = false 
            },
            onAddFriend = { userId ->
                Log.e("ChatListScreen", "🔴 AddFriendDialog onAddFriend called with userId: $userId")
                // 创建与新好友的对话
                viewModel.createNewConversation(userId)
            }
        )
    } else {
        Log.e("ChatListScreen", "🔴 ❌ AddFriendDialog NOT SHOWN - showAddFriendDialog is false")
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