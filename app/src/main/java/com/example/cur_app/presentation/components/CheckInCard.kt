package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.cur_app.data.local.entity.CheckInType
import com.example.cur_app.data.local.entity.CheckInStats
import com.example.cur_app.ui.theme.*
import kotlin.math.roundToInt


/**
 * 任务项数据类 - 包含完整的任务信息
 */
data class TaskItem(
    val id: String,                    // 任务ID
    val title: String,                 // 任务名称
    val description: String = "",      // 任务描述
    val currentValue: Float,           // 当前进度值
    val targetValue: Float,            // 目标值
    val unit: String,                  // 单位
    val type: CheckInType,             // 任务类型
    val isCompleted: Boolean,          // 是否已完成
    val experienceReward: Int = 0,     // 完成后获得的经验值
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    val completedAt: Long? = null,     // 完成时间
    val priority: TaskPriority = TaskPriority.NORMAL,  // 优先级
    val tags: List<String> = emptyList() // 标签
) {
    val progress: Float get() = if (targetValue > 0) (currentValue / targetValue).coerceAtMost(1f) else 0f
    val progressPercentage: Int get() = (progress * 100).toInt()
}

/**
 * 任务优先级枚举
 */
enum class TaskPriority(val displayName: String, val color: Color) {
    LOW("低", Color(0xFF4CAF50)),
    NORMAL("普通", Color(0xFF2196F3)),
    HIGH("高", Color(0xFFFF9800)),
    URGENT("紧急", Color(0xFFFF5722))
}

/**
 * 打卡类型统计卡片
 * 显示特定类型的打卡统计信息
 */
@Composable
fun CheckInStatsCard(
    stats: CheckInStats,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val animatedTodayValue by animateIntAsState(
        targetValue = stats.todayValue.toIntOrNull() ?: 0,
        animationSpec = tween(1000)
    )
    
    val animatedStreakDays by animateIntAsState(
        targetValue = stats.streakDays,
        animationSpec = tween(1000)
    )
    
    // 极简静态进度（无动画，最佳性能）
    val staticProgress = stats.completionRate
    
    // 简化的一次性动画效果（移除无限循环动画以优化性能）
    val isInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isInitialized.value = true
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp) // 略微减小高度
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 液体背景层 - 根据进度填充
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.05f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // 动态液体效果层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                stats.type.gradientStart.copy(alpha = 0.9f),
                                stats.type.gradientEnd.copy(alpha = 0.7f),
                                stats.type.gradientStart.copy(alpha = 0.9f)
                            ),
                            startY = (1f - staticProgress) * 1000f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // 简化的静态光效（移除流动动画）
            if (staticProgress > 0.7f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 400f
                            )
                        )
                )
            }
            
            // 内容层
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 顶部：类型标题和图标
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stats.type.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (staticProgress > 0.5f) Color.White else stats.type.color,
                            maxLines = 1
                        )
                        Text(
                            text = "今日打卡",
                            fontSize = 11.sp,
                            color = if (staticProgress > 0.5f) 
                                Color.White.copy(alpha = 0.8f) 
                            else 
                                stats.type.color.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (staticProgress > 0.5f)
                                    Color.White.copy(alpha = 0.2f)
                                else
                                    stats.type.color.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = stats.type.icon,
                            contentDescription = stats.type.displayName,
                            tint = if (staticProgress > 0.5f) Color.White else stats.type.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                // 中部：数值显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 今日数据
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = animatedTodayValue.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (staticProgress > 0.3f) Color.White else stats.type.color,
                            maxLines = 1
                        )
                        Text(
                            text = when (stats.type) {
                                CheckInType.STUDY -> "分钟"
                                CheckInType.EXERCISE -> "千卡" 
                                CheckInType.MONEY -> "元"
                            },
                            fontSize = 11.sp,
                            color = if (staticProgress > 0.3f) 
                                Color.White.copy(alpha = 0.7f) 
                            else 
                                stats.type.color.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                    
                    // 连续天数
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "$animatedStreakDays",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (staticProgress > 0.3f) Color.White else stats.type.color,
                            maxLines = 1
                        )
                        Text(
                            text = "连续",
                            fontSize = 10.sp,
                            color = if (staticProgress > 0.3f) 
                                Color.White.copy(alpha = 0.7f) 
                            else 
                                stats.type.color.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }
                
                // 底部：进度百分比
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "完成度",
                        fontSize = 10.sp,
                        color = if (staticProgress > 0.1f) 
                            Color.White.copy(alpha = 0.7f) 
                        else 
                            stats.type.color.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${(staticProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (staticProgress > 0.1f) Color.White else stats.type.color
                    )
                }
            }
        }
    }
}

/**
 * 现代化任务卡片 - 支持左滑编辑和圆形进度显示
 */
@Composable
fun ModernTaskCard(
    taskItem: TaskItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(500)),
        label = "task_card_visibility"
    ) {
        SwipeableCheckInCard(
            type = taskItem.type,
            title = taskItem.title,
            value = taskItem.currentValue.toString(),
            unit = taskItem.unit,
            targetValue = taskItem.targetValue.toString(),
            progress = taskItem.progress,
            isCompleted = taskItem.isCompleted,
            experienceValue = taskItem.experienceReward,
            onToggle = onToggle,
            onEdit = onEdit,
            onDelete = onDelete,
            onFocus = onFocus,
            modifier = modifier
        )
    }
}

/**
 * 现代化打卡项目卡片 - 支持左滑编辑和进度显示（保持向后兼容）
 */
@Composable
fun EnhancedCheckInItemCard(
    type: CheckInType,
    title: String,
    value: String,
    unit: String,
    targetValue: String = value,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onFocus: () -> Unit = {},
    modifier: Modifier = Modifier,
    experienceValue: Int = 30
) {
    val taskItem = remember(type, title, value, unit, targetValue, isCompleted, experienceValue) {
        TaskItem(
            id = "temp_${title.hashCode()}",
            title = title,
            currentValue = value.toFloatOrNull() ?: 0f,
            targetValue = targetValue.toFloatOrNull() ?: value.toFloatOrNull() ?: 0f,
            unit = unit,
            type = type,
            isCompleted = isCompleted,
            experienceReward = experienceValue
        )
    }
    
    ModernTaskCard(
        taskItem = taskItem,
        onToggle = onToggle,
        onEdit = onEdit,
        onDelete = onDelete,
        onFocus = onFocus,
        modifier = modifier
    )
}

/**
 * 可滑动的任务卡片组件 - 现代化设计
 */
@Composable
private fun SwipeableCheckInCard(
    type: CheckInType,
    title: String,
    value: String,
    unit: String,
    targetValue: String,
    progress: Float,
    isCompleted: Boolean,
    experienceValue: Int,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val actionButtonWidth = with(density) { 60.dp.toPx() } // 减小按钮宽度确保两个按钮都能显示
    val maxSwipeDistance = actionButtonWidth * 2 // 两个按钮的总宽度
    
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "progress_animation"
    )
    
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(200),
        label = "card_scale"
    )
    
    // 所有任务都可以滑动，但完成和未完成的功能不同
    val draggableState = rememberDraggableState { delta ->
        offsetX = (offsetX + delta).coerceIn(-maxSwipeDistance, 0f)
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
    ) {
        // 背景操作按钮（根据完成状态显示不同操作）
        if (animatedOffsetX < -10f) {
            if (isCompleted) {
                // 已完成任务：显示取消完成和删除按钮
                CompletedTaskActionButtons(
                    onUncomplete = {
                        offsetX = 0f
                        onToggle() // 取消完成状态
                    },
                    onDelete = {
                        offsetX = 0f
                        onDelete()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(100.dp)
                )
            } else {
                // 未完成任务：显示编辑和删除按钮
                ModernEditActionButtons(
                    onEdit = {
                        offsetX = 0f
                        onEdit()
                    },
                    onDelete = {
                        offsetX = 0f
                        onDelete()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(100.dp)
                )
            }
        }
        
        // 主卡片内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .shadow(
                    elevation = if (isCompleted) 12.dp else if (isPressed) 8.dp else 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = if (isCompleted) type.color.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = if (isCompleted) {
                        Brush.linearGradient(
                            colors = listOf(
                                type.color.copy(alpha = 0.12f),
                                type.color.copy(alpha = 0.06f),
                                Color.White
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    }
                )
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { isPressed = true },
                    onDragStopped = { 
                        isPressed = false
                        // 改进的自动回弹逻辑 - 支持三个停止位置
                        when {
                            offsetX > -maxSwipeDistance / 4 -> {
                                // 滑动距离小于1/4，回到初始位置
                                offsetX = 0f
                            }
                            offsetX > -maxSwipeDistance * 3 / 4 -> {
                                // 滑动距离在1/4到3/4之间，停在显示两个按钮的位置
                                offsetX = -maxSwipeDistance
                            }
                            else -> {
                                // 滑动距离超过3/4，也停在显示两个按钮的位置
                                offsetX = -maxSwipeDistance
                            }
                        }
                    }
                )
                .clickable { 
                    if (!isCompleted) {
                        onFocus() // 未完成的任务点击进入专注模式
                    }
                    // 已完成的任务不响应点击，避免误操作
                }
        ) {
            // 完成状态的装饰效果
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    type.color.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                radius = 400f,
                                center = Offset(0.3f, 0.7f)
                            )
                        )
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 左侧圆形进度指示器
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 圆形进度条
                    CircularProgressRing(
                        progress = animatedProgress,
                        color = type.color,
                        backgroundColor = type.color.copy(alpha = 0.12f),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    // 中心图标或完成标识
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                brush = if (isCompleted) {
                                    Brush.radialGradient(
                                        colors = listOf(type.color, type.color.copy(alpha = 0.8f))
                                    )
                                } else {
                                    Brush.radialGradient(
                                        colors = listOf(
                                            type.color.copy(alpha = 0.15f),
                                            type.color.copy(alpha = 0.08f)
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = type.displayName,
                                tint = type.color,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                
                // 中间内容信息
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 任务标题
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) 
                            type.color.copy(alpha = 0.8f) 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // 进度信息 - 显示当前值/目标值格式
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (targetValue != value) "$value/$targetValue $unit" else "$value $unit",
                            fontSize = 14.sp,
                            color = type.color,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (!isCompleted && progress > 0 && progress < 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = type.color.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    color = type.color,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    // 经验值显示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isCompleted) "已获得 $experienceValue 经验值" else "+$experienceValue 经验值",
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // 右侧优先级指示器（仅未完成任务显示）
                if (!isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(type.color.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

/**
 * 完成状态指示器
 */
@Composable
private fun CompletionIndicator(
    isCompleted: Boolean,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val indicatorSize by animateDpAsState(
        targetValue = if (isCompleted) 32.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicator_size"
    )
    
    if (isCompleted) {
        Box(
            modifier = modifier
                .size(indicatorSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color, color.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(indicatorSize)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.4f),
                                color.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
        }
    }
}

/**
 * 圆形进度环组件
 */
@Composable
private fun CircularProgressRing(
    progress: Float,
    color: Color,
    backgroundColor: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidthPx = strokeWidth.toPx()
        val diameter = size.minDimension
        val radius = (diameter - strokeWidthPx) / 2f
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        
        // 背景圆环
        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
        
        // 进度圆环
        if (progress > 0) {
            val sweepAngle = 360f * progress.coerceAtMost(1f)
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                size = Size(diameter - strokeWidthPx, diameter - strokeWidthPx),
                topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - diameter + strokeWidthPx) / 2f,
                    (size.height - diameter + strokeWidthPx) / 2f
                )
            )
        }
    }
}

/**
 * 已完成任务操作按钮
 */
@Composable
private fun CompletedTaskActionButtons(
    onUncomplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 取消完成按钮
        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF9800),
                            Color(0xFFF57C00)
                        )
                    )
                )
                .clickable { onUncomplete() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "重做",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "重做",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // 删除按钮
        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF5722),
                            Color(0xFFD32F2F)
                        )
                    )
                )
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "删除",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 现代化编辑操作按钮
 */
@Composable
private fun ModernEditActionButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 编辑按钮 - 减小宽度
        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF388E3C)
                        )
                    )
                )
                .clickable { onEdit() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "编辑",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // 删除按钮 - 减小宽度
        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF5722),
                            Color(0xFFD32F2F)
                        )
                    )
                )
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "删除",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 编辑操作按钮（保持向后兼容）
 */
@Composable
private fun EditActionButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 编辑按钮
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(Color(0xFF4CAF50))
                .clickable { onEdit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "编辑",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 删除按钮
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(Color(0xFFFF5722))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 原始打卡项目卡片（保持向后兼容）
 */
@Composable
fun CheckInItemCard(
    type: CheckInType,
    title: String,
    value: String,
    unit: String,
    targetValue: String = value,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    experienceValue: Int = 30,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onFocus: () -> Unit = {} // 新增专注模式回调
) {
    // 使用新的增强版组件
    EnhancedCheckInItemCard(
        type = type,
        title = title,
        value = value,
        unit = unit,
        targetValue = targetValue,
        isCompleted = isCompleted,
        onToggle = onToggle,
        onEdit = onEdit,
        onDelete = onDelete,
        onFocus = onFocus,
        modifier = modifier,
        experienceValue = experienceValue
    )
}

/**
 * 添加打卡项目卡片
 */
@Composable
fun AddCheckInCard(
    type: CheckInType,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
            Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onAdd() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = type.color.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加${type.displayName}计划",
                tint = type.color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加${type.displayName}计划",
                color = type.color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 类型切换标签
 */
@Composable
fun CheckInTypeTab(
    type: CheckInType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) type.color else Color.Transparent,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = AnimationUtils.DURATION_SHORT)
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else type.color,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = AnimationUtils.DURATION_SHORT)
    )
    
    Surface(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = type.icon,
                contentDescription = type.displayName,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = type.displayName,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun animateColorAsState(
    targetValue: Color,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Color> = androidx.compose.animation.core.tween()
): State<Color> {
    return androidx.compose.animation.animateColorAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = "color_animation"
    )
}

// 预览用示例数据
@Preview
@Composable
private fun ModernTaskCardPreview() {
    val sampleTasks = listOf(
        TaskItem(
            id = "1",
            title = "晨跑",
            description = "每日晨跑锻炼",
            currentValue = 27f,
            targetValue = 90f,
            unit = "分钟",
            type = CheckInType.EXERCISE,
            isCompleted = false,
            experienceReward = 50,
            priority = TaskPriority.HIGH
        ),
        TaskItem(
            id = "2",
            title = "阅读",
            description = "专业书籍阅读",
            currentValue = 45f,
            targetValue = 45f,
            unit = "页",
            type = CheckInType.STUDY,
            isCompleted = true,
            experienceReward = 80,
            priority = TaskPriority.NORMAL
        ),
        TaskItem(
            id = "3",
            title = "预算控制",
            description = "每日支出控制",
            currentValue = 120f,
            targetValue = 200f,
            unit = "元",
            type = CheckInType.MONEY,
            isCompleted = false,
            experienceReward = 30,
            priority = TaskPriority.URGENT
        )
    )
    
    Column(
        modifier = Modifier
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        sampleTasks.forEach { task ->
            ModernTaskCard(
                taskItem = task,
                onToggle = { },
                onEdit = { },
                onDelete = { }
            )
        }
    }
}



