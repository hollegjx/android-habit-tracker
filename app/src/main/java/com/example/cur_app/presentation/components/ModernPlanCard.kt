//今日计划组件

package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cur_app.data.local.entity.CheckInType
import com.example.cur_app.presentation.components.getDynamicTextColor
import kotlinx.coroutines.delay

/**
 * 现代化今日计划卡片
 */
@Composable
fun ModernPlanCard(
    selectedType: CheckInType,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    
    // 进度动画
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )
    
    // 数字变化动画
    val animatedCompleted by animateIntAsState(
        targetValue = completedCount,
        animationSpec = tween(durationMillis = 800),
        label = "completed_animation"
    )
    
    // 卡片出现动画
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(600))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            selectedType.gradientStart.copy(alpha = 0.1f),
                            selectedType.gradientEnd.copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧内容
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = selectedType.icon,
                            contentDescription = selectedType.displayName,
                            tint = selectedType.color,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "今日${selectedType.displayName}计划",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = getDynamicTextColor(selectedType)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "已完成 ${animatedCompleted}/${totalCount} 项任务",
                        fontSize = 14.sp,
                        color = getDynamicTextColor(selectedType, alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 进度条
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(selectedType.color.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            selectedType.gradientStart,
                                            selectedType.gradientEnd
                                        )
                                    )
                                )
                        )
                    }
                }
                
                // 右侧圆形进度
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(60.dp),
                    color = selectedType.color,
                    trackColor = selectedType.color.copy(alpha = 0.2f),
                    strokeWidth = 6.dp
                )
            }
        }
    }
}

/**
 * 圆形进度指示器（自定义样式）
 */
@Composable
fun CustomCircularProgress(
    progress: Float,
    selectedType: CheckInType,
    modifier: Modifier = Modifier,
    size: Int = 60
) {
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        // 背景圆圈
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    selectedType.color.copy(alpha = 0.2f),
                    CircleShape
                )
        )
        
        // 进度圆圈
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = selectedType.color,
            strokeWidth = 4.dp,
            trackColor = Color.Transparent
        )
        
        // 中心百分比文字
        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = selectedType.color,
            textAlign = TextAlign.Center
        )
    }
} 