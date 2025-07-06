package com.example.cur_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cur_app.presentation.screens.AddFriendViewModel
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onAddFriend: (String) -> Unit = {},
    viewModel: AddFriendViewModel = hiltViewModel()
) {
    Log.d("AddFriendDialog", "AddFriendDialog composable is being rendered")
    
    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    
    // 使用ViewModel的状态
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState.isLoading
    val searchResult = uiState.searchResult
    val errorMessage = uiState.errorMessage
    val friendRequestStatus = uiState.friendRequestStatus
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 400.dp, max = 600.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "添加好友",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )
                    IconButton(onClick = { 
                        Log.d("AddFriendDialog", "Close button clicked")
                        onDismiss() 
                    }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "关闭",
                            tint = Color.Gray
                        )
                    }
                }
                
                // 搜索说明
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF667EEA),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "输入好友的用户ID进行搜索",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // 搜索输入框
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { 
                        searchText = it
                        viewModel.clearSearchResults()
                    },
                    label = { Text("用户ID") },
                    placeholder = { Text("请输入要添加的用户ID") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(
                                onClick = { 
                                    searchText = ""
                                    viewModel.clearSearchResults()
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667EEA),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchText.isNotBlank()) {
                                scope.launch {
                                    viewModel.searchUserByUid(searchText.trim())
                                }
                                keyboardController?.hide()
                            }
                        }
                    ),
                    singleLine = true
                )
                
                // 搜索按钮
                Button(
                    onClick = {
                        if (searchText.isNotBlank()) {
                            scope.launch {
                                viewModel.searchUserByUid(searchText.trim())
                            }
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = searchText.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("搜索中...")
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("搜索用户")
                    }
                }
                
                // 错误消息
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = error,
                                color = Color(0xFFE91E63),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                // 好友请求成功提示
                if (friendRequestStatus == "sent") {
                    LaunchedEffect(friendRequestStatus) {
                        kotlinx.coroutines.delay(2000)
                        onDismiss() // 自动关闭对话框
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E8)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "好友申请已发送，将自动关闭",
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                // 搜索结果
                searchResult?.let { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "搜索结果",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF667EEA)
                            )
                            
                            // 用户信息
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 头像
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFF667EEA).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = result.nickname?.firstOrNull()?.toString() ?: result.username.firstOrNull()?.toString() ?: "?",
                                        fontSize = 20.sp,
                                        color = Color(0xFF667EEA)
                                    )
                                }
                                
                                // 用户信息
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = result.nickname ?: result.username,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "UID: ${result.uid}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "用户名: ${result.username}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            // 添加好友按钮
                            Button(
                                onClick = {
                                    Log.d("AddFriendDialog", "Add friend button clicked for uid: ${result.uid}")
                                    scope.launch {
                                        viewModel.sendFriendRequest(result.uid)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = when (friendRequestStatus) {
                                    "already_friend" -> false
                                    "sent" -> false
                                    "sending" -> false
                                    "declined" -> false
                                    "blocked" -> false
                                    "cannot_send" -> false
                                    else -> result.canSendRequest
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF667EEA),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (friendRequestStatus == "sending") {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("发送中...")
                                } else {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    when (friendRequestStatus) {
                                        "already_friend" -> Text("已是好友")
                                        "sent" -> Text("已发送")
                                        "declined" -> Text("已拒绝")
                                        "blocked" -> Text("无法添加")
                                        "cannot_send" -> Text("无法添加")
                                        else -> Text("发送好友申请")
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 底部提示
                if (searchResult == null && errorMessage == null && !isLoading && friendRequestStatus != "sent") {
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👥",
                            fontSize = 32.sp
                        )
                        Text(
                            text = "开始搜索好友",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "输入准确的用户ID来查找想要添加的好友",
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}