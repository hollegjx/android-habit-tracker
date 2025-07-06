package com.example.cur_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cur_app.data.remote.dto.FriendInfo
import com.example.cur_app.presentation.components.AddFriendDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * 好友列表界面 - 全新设计，比聊天列表更加专业和美观
 * 提供完整的好友管理功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    onNavigateToAddFriend: () -> Unit = {},
    onNavigateToFriendRequests: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: FriendListViewModel = hiltViewModel()
) {
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showFriendMenu by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<FriendInfo?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        isVisible = true
        viewModel.loadFriendList()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2),
                        Color(0xFF667EEA)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "👥 我的好友",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (uiState.friends.isNotEmpty()) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${uiState.friends.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // 好友请求按钮
                        Box {
                            IconButton(onClick = onNavigateToFriendRequests) {
                                Badge(
                                    modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                                ) {
                                    if (uiState.unreadRequestCount > 0) {
                                        Text(
                                            text = if (uiState.unreadRequestCount > 99) "99+" else uiState.unreadRequestCount.toString(),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "好友请求",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        // 添加好友按钮
                        IconButton(onClick = { showAddFriendDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "添加好友",
                                tint = Color.White
                            )
                        }
                        
                        // 更多选项
                        Box {
                            IconButton(onClick = { showFriendMenu = !showFriendMenu }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "更多",
                                    tint = Color.White
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showFriendMenu,
                                onDismissRequest = { showFriendMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("刷新列表") },
                                    onClick = {
                                        showFriendMenu = false
                                        viewModel.loadFriendList()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("在线好友") },
                                    onClick = {
                                        showFriendMenu = false
                                        viewModel.filterOnlineFriends()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("特别关注") },
                                    onClick = {
                                        showFriendMenu = false
                                        viewModel.filterStarredFriends()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(600))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                    Text(
                                        text = "加载好友列表中...",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    // 错误状态
                    uiState.errorMessage?.let { error ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE).copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "加载失败",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE91E63)
                                        )
                                        Text(
                                            text = error,
                                            fontSize = 14.sp,
                                            color = Color(0xFFE91E63).copy(alpha = 0.8f)
                                        )
                                    }
                                    TextButton(
                                        onClick = { viewModel.loadFriendList() }
                                    ) {
                                        Text("重试", color = Color(0xFFE91E63))
                                    }
                                }
                            }
                        }
                    }
                    
                    // 好友列表
                    if (uiState.friends.isNotEmpty()) {
                        // 在线好友
                        val onlineFriends = uiState.friends.filter { it.isOnline }
                        if (onlineFriends.isNotEmpty()) {
                            item {
                                FriendSectionHeader(
                                    title = "🟢 在线好友",
                                    subtitle = "${onlineFriends.size}人在线"
                                )
                            }
                            
                            items(
                                items = onlineFriends,
                                key = { it.friendshipId }
                            ) { friend ->
                                FriendListItem(
                                    friend = friend,
                                    onClick = { 
                                        // 创建或获取与好友的聊天对话
                                        viewModel.createChatWithFriend(friend.userId) { conversationId ->
                                            conversationId?.let { onNavigateToChat(it) }
                                        }
                                    },
                                    onLongClick = { selectedFriend = friend },
                                    modifier = Modifier
                                )
                            }
                        }
                        
                        // 离线好友
                        val offlineFriends = uiState.friends.filter { !it.isOnline }
                        if (offlineFriends.isNotEmpty()) {
                            item {
                                FriendSectionHeader(
                                    title = "⚫ 离线好友", 
                                    subtitle = "${offlineFriends.size}人离线"
                                )
                            }
                            
                            items(
                                items = offlineFriends,
                                key = { it.friendshipId }
                            ) { friend ->
                                FriendListItem(
                                    friend = friend,
                                    onClick = { 
                                        // 创建或获取与好友的聊天对话
                                        viewModel.createChatWithFriend(friend.userId) { conversationId ->
                                            conversationId?.let { onNavigateToChat(it) }
                                        }
                                    },
                                    onLongClick = { selectedFriend = friend },
                                    modifier = Modifier
                                )
                            }
                        }
                    } else if (!uiState.isLoading && uiState.errorMessage == null) {
                        // 空状态
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "👥",
                                        fontSize = 64.sp
                                    )
                                    Text(
                                        text = "还没有好友",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "添加好友开始聊天吧！",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showAddFriendDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF667EEA)
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("添加第一个好友")
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
    
    // 添加好友对话框
    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onAddFriend = { userId ->
                viewModel.loadFriendList() // 刷新好友列表
            }
        )
    }
    
    // 好友操作底部弹窗
    selectedFriend?.let { friend ->
        FriendActionBottomSheet(
            friend = friend,
            onDismiss = { selectedFriend = null },
            onChat = { 
                // 创建或获取与好友的聊天对话
                viewModel.createChatWithFriend(friend.userId) { conversationId ->
                    conversationId?.let { onNavigateToChat(it) }
                }
                selectedFriend = null
            },
            onStar = { 
                viewModel.updateFriendSettings(friend.friendshipId, isStarred = !friend.isStarred)
                selectedFriend = null
            },
            onMute = { 
                viewModel.updateFriendSettings(friend.friendshipId, isMuted = !friend.isMuted)
                selectedFriend = null
            },
            onRemove = { 
                viewModel.removeFriend(friend.friendshipId)
                selectedFriend = null
            }
        )
    }
}

/**
 * 好友分组标题
 */
@Composable
fun FriendSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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

/**
 * 好友列表项
 */
@Composable
fun FriendListItem(
    friend: FriendInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = if (isPressed) 0.3f else 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                                    Color.White.copy(alpha = 0.4f),
                                    Color.White.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = friend.displayName.firstOrNull()?.toString() ?: "?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )
                }
                
                // 在线状态指示器
                if (friend.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .align(Alignment.BottomEnd)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .align(Alignment.BottomEnd)
                    )
                }
                
                // 特别关注标识
                if (friend.isStarred) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700))
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "特别关注",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            
            // 好友信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = friend.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 状态指示器
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (friend.isMuted) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "已静音",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (friend.unreadCount > 0) {
                            Badge {
                                Text(
                                    text = if (friend.unreadCount > 99) "99+" else friend.unreadCount.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UID: ${friend.uid}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    friend.lastSeenTime?.let { lastSeen ->
                        Text(
                            text = formatLastSeen(lastSeen),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 好友操作底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendActionBottomSheet(
    friend: FriendInfo,
    onDismiss: () -> Unit,
    onChat: () -> Unit,
    onStar: () -> Unit,
    onMute: () -> Unit,
    onRemove: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 好友信息头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF667EEA).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = friend.displayName.firstOrNull()?.toString() ?: "?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = friend.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "UID: ${friend.uid}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Divider()
            
            // 操作选项
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FriendActionItem(
                    icon = Icons.Default.Send,
                    title = "发送消息",
                    iconColor = Color(0xFF2196F3),
                    onClick = onChat
                )
                
                FriendActionItem(
                    icon = if (friend.isStarred) Icons.Default.Star else Icons.Default.Star,
                    title = if (friend.isStarred) "取消特别关注" else "特别关注",
                    iconColor = Color(0xFFFFD700),
                    onClick = onStar
                )
                
                FriendActionItem(
                    icon = if (friend.isMuted) Icons.Default.PlayArrow else Icons.Default.Close,
                    title = if (friend.isMuted) "取消静音" else "静音消息",
                    iconColor = Color(0xFF9E9E9E),
                    onClick = onMute
                )
                
                Divider()
                
                FriendActionItem(
                    icon = Icons.Default.Delete,
                    title = "删除好友",
                    iconColor = Color(0xFFE91E63),
                    onClick = onRemove
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 好友操作项
 */
@Composable
fun FriendActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

/**
 * 格式化最后在线时间
 */
private fun formatLastSeen(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "刚刚在线"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前在线"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前在线"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前在线"
        else -> "很久没有在线"
    }
}