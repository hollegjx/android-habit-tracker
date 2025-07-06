package com.example.cur_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import java.util.*
import com.example.cur_app.ui.theme.*
import com.example.cur_app.utils.UserAvatarDisplay

/**
 * 用户问候头部组件
 * 显示用户名、动态问候语和头像
 */
@Composable
fun UserGreetingHeader(
    userName: String = "小明",
    userAvatarType: String = "emoji",
    userAvatarValue: String = "😊",
    modifier: Modifier = Modifier
) {
    // 根据时间生成动态问候语
    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 5..8 -> "早上好"
            hour in 9..11 -> "上午好"
            hour in 12..13 -> "中午好"
            hour in 14..17 -> "下午好"
            hour in 18..19 -> "傍晚好"
            hour in 20..22 -> "晚上好"
            else -> "夜深了"
        }
    }
    
    // 根据时间选择问候图标
    val greetingIcon = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 5..8 -> "🌅"
            hour in 9..11 -> "☀️"
            hour in 12..13 -> "🌞"
            hour in 14..17 -> "🌤️"
            hour in 18..19 -> "🌇"
            hour in 20..22 -> "🌙"
            else -> "⭐"
        }
    }
    
    // 鼓励文案
    val encouragementText = remember {
        val texts = listOf(
            "今天也要坚持打卡哦！",
            "每一次坚持都是进步！",
            "继续加油，你很棒！",
            "习惯的力量正在改变你！",
            "今天又是充满希望的一天！"
        )
        texts.random()
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
            
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
                // 用户信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 问候语
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = greetingIcon,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$greetingText，$userName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 鼓励文案
                    Text(
                        text = encouragementText,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 用户头像 - 使用统一的头像显示组件
                UserAvatarDisplay(
                    avatarType = userAvatarType,
                    avatarValue = userAvatarValue,
                    size = 60.dp,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(GradientStart.copy(alpha = 0.6f), GradientEnd.copy(alpha = 0.6f))
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
    } 