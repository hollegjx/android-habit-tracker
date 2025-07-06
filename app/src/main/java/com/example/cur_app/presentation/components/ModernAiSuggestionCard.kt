package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.cur_app.data.local.entity.CheckInType
import com.example.cur_app.presentation.components.getDynamicTextColor

/**
 * 现代化AI建议卡片
 */
@Composable
fun ModernAiSuggestionCard(
    selectedType: CheckInType,
    suggestion: String,
    onClickMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    // AI角色配置
    val aiCharacter = when (selectedType) {
        CheckInType.STUDY -> AICharacter(
            name = "小美",
            emoji = "💕",
            color = Color(0xFF7986CB),
            description = "学习小助手"
        )
        CheckInType.EXERCISE -> AICharacter(
            name = "雷鸣",
            emoji = "💪",
            color = Color(0xFFFF7043),
            description = "运动教练"
        )
        CheckInType.MONEY -> AICharacter(
            name = "萌萌",
            emoji = "🎯",
            color = Color(0xFF66BB6A),
            description = "理财顾问"
        )
    }
    
    // 卡片动画
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500)
        isVisible = true
    }
    
    // 点击动画
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "press_scale"
    )
    
    // 背景渐变动画
    val animatedGradientStart by animateColorAsState(
        targetValue = selectedType.gradientStart.copy(alpha = 0.15f),
        animationSpec = tween(800),
        label = "gradient_start"
    )
    
    val animatedGradientEnd by animateColorAsState(
        targetValue = selectedType.gradientEnd.copy(alpha = 0.08f),
        animationSpec = tween(800),
        label = "gradient_end"
    )
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 60 },
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(700))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(150)
                        isPressed = false
                        onClickMore()
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                animatedGradientStart,
                                animatedGradientEnd,
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    // 顶部AI角色信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // AI头像
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                aiCharacter.color,
                                                aiCharacter.color.copy(alpha = 0.7f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = aiCharacter.emoji,
                                    fontSize = 18.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = aiCharacter.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getDynamicTextColor(selectedType)
                                )
                                Text(
                                    text = aiCharacter.description,
                                    fontSize = 12.sp,
                                    color = getDynamicTextColor(selectedType, alpha = 0.6f)
                                )
                            }
                        }
                        
                        // 箭头指示
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "查看更多",
                            tint = selectedType.color.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // AI建议内容
                    Text(
                        text = suggestion,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = getDynamicTextColor(selectedType, alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 底部操作区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💭 AI建议",
                            fontSize = 12.sp,
                            color = selectedType.color.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = selectedType.color.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { onClickMore() }
                        ) {
                            Text(
                                text = "查看更多",
                                fontSize = 12.sp,
                                color = selectedType.color,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * AI角色数据类
 */
data class AICharacter(
    val name: String,
    val emoji: String,
    val color: Color,
    val description: String
)

/**
 * AI建议气泡动画组件
 */
@Composable
fun AiSpeechBubble(
    text: String,
    selectedType: CheckInType,
    modifier: Modifier = Modifier
) {
    // 打字机效果
    var displayedText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        displayedText = ""
        text.forEachIndexed { index, char ->
            delay(50)
            displayedText = text.substring(0, index + 1)
        }
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = selectedType.color.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = displayedText,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = getDynamicTextColor(selectedType),
            textAlign = TextAlign.Start
        )
    }
} 