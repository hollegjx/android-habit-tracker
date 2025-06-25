package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import com.example.cur_app.ui.theme.*

/**
 * 聊天搜索菜单组件
 */
@Composable
fun ChatSearchMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSearchMessages: (String) -> Unit,
    onAddUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    
    // 下拉菜单
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.9f)
                    )
                )
            )
    ) {
        // 搜索聊天记录
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "搜索聊天记录",
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
            },
            onClick = {
                showSearchDialog = true
                onDismiss()
            }
        )
        
        HorizontalDivider(
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )
        
        // 添加好友
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "添加好友",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "添加好友",
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
            },
            onClick = {
                showAddUserDialog = true 
                onDismiss()
            }
        )
    }
    
    // 搜索对话框
    if (showSearchDialog) {
        SearchMessagesDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { query ->
                showSearchDialog = false
                onSearchMessages(query)
            }
        )
    }
    
    // 添加用户对话框
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onAdd = { userId ->
                showAddUserDialog = false
                onAddUser()
            }
        )
    }
}

/**
 * 搜索消息对话框
 */
@Composable
fun SearchMessagesDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 标题
                Text(
                    text = "🔍 搜索聊天记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("输入关键词") },
                    placeholder = { Text("搜索消息内容...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        focusedLabelColor = GradientStart
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "清空"
                                )
                            }
                        }
                    }
                )
                
                // 提示信息
                Text(
                    text = "💡 将在所有聊天记录中搜索包含此关键词的消息",
                    fontSize = 12.sp,
                    color = TextSecondary
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
                        onClick = { onSearch(searchQuery) },
                        enabled = searchQuery.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        )
                    ) {
                        Text("搜索", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * 添加用户对话框
 */
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 标题
                Text(
                    text = "👥 添加好友",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // 输入框
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("用户ID") },
                    placeholder = { Text("输入好友的用户ID") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        focusedLabelColor = GradientStart
                    ),
                    trailingIcon = {
                        if (userId.isNotEmpty()) {
                            IconButton(
                                onClick = { userId = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "清空"
                                )
                            }
                        }
                    }
                )
                
                // 提示信息
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡 添加好友提示：",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = "• 请输入准确的用户ID\n• 添加后需要对方同意才能开始聊天\n• 可以通过扫码或分享ID邀请好友",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
                
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
                        onClick = { onAdd(userId) },
                        enabled = userId.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        )
                    ) {
                        Text("添加", color = Color.White)
                    }
                }
            }
        }
    }
} 