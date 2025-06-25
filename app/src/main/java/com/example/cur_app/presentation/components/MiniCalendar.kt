package com.example.cur_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cur_app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 迷你日历组件
 * 显示当月的日历，并标记打卡完成的日期
 */
@Composable
fun MiniCalendar(
    completedDates: Set<LocalDate> = emptySet(),
    currentDate: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier
) {
    val yearMonth = YearMonth.from(currentDate)
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = 周日
    
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 月份标题
            Text(
                text = currentDate.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 日期网格
            val weeks = mutableListOf<List<Int?>>()
            var currentWeek = mutableListOf<Int?>()
            
            // 填充月初空白
            repeat(firstDayOfWeek) {
                currentWeek.add(null)
            }
            
            // 填充日期
            for (day in 1..daysInMonth) {
                currentWeek.add(day)
                if (currentWeek.size == 7) {
                    weeks.add(currentWeek.toList())
                    currentWeek.clear()
                }
            }
            
            // 填充月末空白
            if (currentWeek.isNotEmpty()) {
                while (currentWeek.size < 7) {
                    currentWeek.add(null)
                }
                weeks.add(currentWeek.toList())
            }
            
            // 渲染星期
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.forEach { day ->
                        CalendarDayCell(
                            day = day,
                            isCompleted = day?.let { d ->
                                val date = yearMonth.atDay(d)
                                completedDates.contains(date)
                            } ?: false,
                            isToday = day == currentDate.dayOfMonth,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 日历单日组件
 */
@Composable
private fun CalendarDayCell(
    day: Int?,
    isCompleted: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> StatsPrimary
                            isToday -> GradientStart.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    fontSize = 12.sp,
                    color = when {
                        isCompleted -> Color.White
                        isToday -> GradientStart
                        else -> TextPrimary
                    },
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
} 