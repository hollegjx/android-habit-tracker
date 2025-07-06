package com.example.cur_app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cur_app.ui.theme.*
import java.time.LocalDate

/**
 * 智能日历切换组件
 * 支持双栏/单栏布局切换和丝滑动画过渡
 */
@Composable
fun SmartCalendarSection(
    todayCompleted: Int,
    currentStreak: Int,
    completedDates: Set<LocalDate>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) 430.dp else 200.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "height_animation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .padding(20.dp)
        ) {
            if (!isExpanded) {
                // 双栏布局模式
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 左侧统计信息
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        TimeStatsCard(
                            todayCompleted = todayCompleted,
                            currentStreak = currentStreak,
                            isCompact = false,
                            onClick = { isExpanded = true }
                        )
                    }
                    
                    // 右侧迷你日历
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        CompactCalendar(
                            completedDates = completedDates,
                            onClick = { isExpanded = true }
                        )
                    }
                }
            } else {
                // 单栏布局模式
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 上方压缩的时间统计
                    TimeStatsCard(
                        todayCompleted = todayCompleted,
                        currentStreak = currentStreak,
                        isCompact = true,
                        onClick = { isExpanded = false }
                    )
                    
                    // 下方扩展的日历
                    ExpandedCalendar(
                        completedDates = completedDates,
                        onClick = { isExpanded = false }
                    )
                }
            }
        }
    }
}

/**
 * 时间统计卡片
 */
@Composable
private fun TimeStatsCard(
    todayCompleted: Int,
    currentStreak: Int,
    isCompact: Boolean,
    onClick: () -> Unit
) {
    val animatedTodayValue by animateIntAsState(
        targetValue = todayCompleted,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    val animatedStreakValue by animateIntAsState(
        targetValue = currentStreak,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 60.dp else 160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        StatsPrimary.copy(alpha = 0.1f),
                        StatsSecondary.copy(alpha = 0.1f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        if (isCompact) {
            // 压缩模式 - 水平布局
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = StatsPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "今日 $animatedTodayValue",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatsPrimary
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = StatsSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "连续 $animatedStreakValue 天",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatsSecondary
                    )
                }
            }
        } else {
            // 完整模式 - 垂直布局
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // 今日完成
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = StatsPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "今日完成",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = animatedTodayValue.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatsPrimary
                    )
                }
                
                // 连续天数
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = StatsSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "连续天数",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = animatedStreakValue.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatsSecondary
                    )
                }
            }
        }
    }
}

/**
 * 紧凑型日历
 */
@Composable
private fun CompactCalendar(
    completedDates: Set<LocalDate>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GradientStart.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = GradientStart,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "本月打卡",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "${completedDates.size} 天",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GradientStart
            )
            Text(
                text = "点击查看详情",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * 扩展型日历
 */
@Composable
private fun ExpandedCalendar(
    completedDates: Set<LocalDate>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        MiniCalendar(
            completedDates = completedDates,
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
        )
        
        // 点击提示
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(
                    color = TextPrimary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "点击收起",
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
} 