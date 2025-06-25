package com.example.cur_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cur_app.ui.theme.*

/**
 * AI聊天气泡组件
 * 用于显示AI角色的对话内容
 */
@Composable
fun AiChatBubble(
    message: String,
    aiCharacterName: String = "小美",
    aiCharacterColor: Color = AiXiaoMei,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // AI头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(aiCharacterColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = aiCharacterName,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 消息内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // AI名称
                Text(
                    text = aiCharacterName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = aiCharacterColor
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 消息文本
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * AI角色选择卡片
 * 用于选择不同的AI角色
 */
@Composable
fun AiCharacterCard(
    name: String,
    description: String,
    characterColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) characterColor.copy(alpha = 0.1f) else CardBackground
        ),
                 border = if (isSelected) {
             CardDefaults.outlinedCardBorder(enabled = true)
         } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 角色头像
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(characterColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = name,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 角色名称
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 角色描述
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

/**
 * AI建议卡片
 * 用于在主界面显示AI的简短建议
 */
@Composable
fun AiSuggestionCard(
    suggestion: String,
    characterName: String = "小美",
    characterColor: Color = AiXiaoMei,
    modifier: Modifier = Modifier,
    onClickMore: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = characterColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 小头像
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(characterColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = characterName,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = characterName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = characterColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onClickMore,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = characterColor
                )
            ) {
                Text(
                    text = "查看更多建议",
                    fontSize = 12.sp
                )
            }
        }
    }
} 