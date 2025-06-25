/**
 * 专注模式全屏组件
 * 完全沉浸式横屏显示，左侧25%控制区域，右侧75%计时器区域
 */
package com.example.cur_app.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.ViewCompat
import com.example.cur_app.data.local.ChatStateManager
import com.example.cur_app.data.local.entity.CheckInType
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * 专注模式全屏界面 - 完全沉浸式体验
 */
@Composable
fun FocusModeDialog(
    isVisible: Boolean,
    itemTitle: String,
    itemType: CheckInType,
    targetMinutes: Int,
    currentMinutes: Int = 0,
    onDismiss: () -> Unit,
    onComplete: (focusedMinutes: Int) -> Unit
) {
    Log.d("FocusModeDialog", "===== FocusModeDialog 开始渲染，isVisible=$isVisible =====")
    
    if (!isVisible) {
        Log.d("FocusModeDialog", "专注模式不可见，直接返回")
        return
    }
    
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // 响应式尺寸
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLargeScreen = screenWidth > 800.dp
    
    var isRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(currentMinutes * 60) }
    var sessionSeconds by remember { mutableIntStateOf(0) }
    
    Log.d("FocusModeDialog", "专注模式状态：isRunning=$isRunning, 屏幕: ${screenWidth}x${screenHeight}")
    
    // 强化沉浸式全屏设置
    DisposableEffect(Unit) {
        Log.d("FocusModeDialog", "===== 开始设置完全沉浸式全屏 =====")
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        
        // 进入专注模式 - 隐藏底部导航栏
        ChatStateManager.enterFocusMode()
        Log.d("FocusModeDialog", "已设置专注模式状态: 隐藏底部导航栏")
        
        // 设置横屏
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // 完全隐藏系统UI - 最强设置
        activity?.window?.let { window ->
            // 设置窗口全屏
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // 使用WindowInsetsControllerCompat获得最佳兼容性
            val windowInsetsController = WindowInsetsControllerCompat(window, view)
            
            // 隐藏所有系统栏
            windowInsetsController.hide(
                WindowInsetsCompat.Type.statusBars() 
                or WindowInsetsCompat.Type.navigationBars()
                or WindowInsetsCompat.Type.systemBars()
                or WindowInsetsCompat.Type.systemGestures()
                or WindowInsetsCompat.Type.mandatorySystemGestures()
            )
            
            // 设置系统栏行为 - 完全沉浸式
            windowInsetsController.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            
            // 额外的窗口设置
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            // 强制设置窗口标志（兼容旧版本）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+额外设置
                window.insetsController?.let { controller ->
                    controller.hide(
                        WindowInsetsCompat.Type.statusBars() 
                        or WindowInsetsCompat.Type.navigationBars()
                    )
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Android 10及以下 - 使用最强的标志组合
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LOW_PROFILE // 额外：隐藏导航栏图标
                )
            }
        }
        
        // 消费所有窗口插入，确保全屏
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            // 强制消费所有插入，包括手势导航区域
            Log.d("FocusModeDialog", "窗口插入: ${insets}")
            WindowInsetsCompat.CONSUMED
        }
        
        Log.d("FocusModeDialog", "已设置横屏和完全沉浸式全屏")
        
        onDispose {
            Log.d("FocusModeDialog", "===== 恢复系统UI设置 =====")
            // 退出专注模式 - 显示底部导航栏
            ChatStateManager.exitFocusMode()
            Log.d("FocusModeDialog", "已恢复专注模式状态: 显示底部导航栏")
            
            // 恢复屏幕方向
            activity?.requestedOrientation = originalOrientation
            
            // 恢复系统UI
            activity?.window?.let { window ->
                // 恢复窗口装饰
                WindowCompat.setDecorFitsSystemWindows(window, true)
                
                // 使用WindowInsetsControllerCompat恢复
                val windowInsetsController = WindowInsetsControllerCompat(window, view)
                windowInsetsController.show(
                    WindowInsetsCompat.Type.statusBars() 
                    or WindowInsetsCompat.Type.navigationBars()
                    or WindowInsetsCompat.Type.systemBars()
                )
                
                // 兼容性处理
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.show(WindowInsetsCompat.Type.systemBars())
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }
            
            // 清除窗口插入监听器
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
            Log.d("FocusModeDialog", "已恢复系统UI设置")
        }
    }
    
    // 计时器逻辑
    LaunchedEffect(isRunning) {
        Log.d("FocusModeDialog", "计时器状态变化: isRunning=$isRunning")
        while (isRunning) {
            delay(1000)
            elapsedSeconds++
            sessionSeconds++
            Log.d("FocusModeDialog", "计时更新: elapsedSeconds=$elapsedSeconds, sessionSeconds=$sessionSeconds")
        }
    }
    
    // 返回键处理
    BackHandler {
        Log.d("FocusModeDialog", "返回键被按下")
        onDismiss()
    }
    
    // 主界面内容 - 响应式设计
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f0f1e),
                        Color(0xFF000000)
                    ),
                    radius = with(density) { (screenWidth.coerceAtLeast(screenHeight) * 0.8f).toPx() }
                )
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // 左侧控制区域 (30%宽度)
            ControlPanel(
                modifier = Modifier
                    .fillMaxWidth(0.30f)
                    .fillMaxHeight(),
                itemTitle = itemTitle,
                itemType = itemType,
                targetMinutes = targetMinutes,
                currentMinutes = elapsedSeconds / 60,
                sessionMinutes = sessionSeconds / 60,
                isRunning = isRunning,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isLargeScreen = isLargeScreen,
                onStartPause = { 
                    Log.d("FocusModeDialog", "开始/暂停按钮点击: $isRunning -> ${!isRunning}")
                    isRunning = !isRunning 
                },
                onStop = {
                    Log.d("FocusModeDialog", "停止按钮点击: sessionSeconds=$sessionSeconds")
                    if (sessionSeconds > 0) {
                        Log.d("FocusModeDialog", "调用onComplete: focusedMinutes=${sessionSeconds / 60}")
                        onComplete(sessionSeconds / 60)
                    }
                    onDismiss()
                },
                onExit = {
                    Log.d("FocusModeDialog", "退出按钮点击")
                    onDismiss()
                }
            )
            
            // 右侧计时器区域 (70%宽度)
            TimerDisplay(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                elapsedSeconds = elapsedSeconds,
                sessionSeconds = sessionSeconds,
                isRunning = isRunning,
                itemType = itemType,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isLargeScreen = isLargeScreen
            )
        }
    }
}

/**
 * 左侧控制面板 - 现代化响应式设计
 */
@Composable
private fun ControlPanel(
    modifier: Modifier = Modifier,
    itemTitle: String,
    itemType: CheckInType,
    targetMinutes: Int,
    currentMinutes: Int,
    sessionMinutes: Int,
    isRunning: Boolean,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    isLargeScreen: Boolean,
    onStartPause: () -> Unit,
    onStop: () -> Unit,
    onExit: () -> Unit
) {
    // 响应式尺寸
    val basePadding = screenWidth * 0.02f
    val titleSize = if (isLargeScreen) 20.sp else 18.sp
    val subtitleSize = if (isLargeScreen) 14.sp else 12.sp
    val buttonSize = screenWidth * 0.08f
    val cardPadding = screenWidth * 0.015f
    
    Column(
        modifier = modifier.padding(basePadding),
        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.03f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部项目信息
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.015f)
        ) {
            // 项目标题
            Text(
                text = itemTitle,
                color = Color.White,
                fontSize = titleSize,
                fontWeight = FontWeight.Bold
            )
            
            // 项目类型标签 - 现代化设计
            Card(
                modifier = Modifier.shadow(
                    elevation = screenWidth * 0.005f,
                    shape = RoundedCornerShape(screenWidth * 0.04f),
                    spotColor = itemType.color
                ),
                colors = CardDefaults.cardColors(
                    containerColor = itemType.color.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(screenWidth * 0.04f),
                border = BorderStroke(
                    width = (screenWidth * 0.002f).coerceAtLeast(1.dp),
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            itemType.color.copy(alpha = 0.8f),
                            itemType.color.copy(alpha = 0.4f)
                        )
                    )
                )
            ) {
                Text(
                    text = itemType.displayName,
                    color = itemType.color,
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(
                        horizontal = screenWidth * 0.025f,
                        vertical = screenHeight * 0.01f
                    )
                )
            }
            
            // 进度信息卡片 - 毛玻璃效果
            Card(
                modifier = Modifier.shadow(
                    elevation = screenWidth * 0.008f,
                    shape = RoundedCornerShape(screenWidth * 0.03f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(screenWidth * 0.03f)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            width = (screenWidth * 0.001f).coerceAtLeast(0.5.dp),
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(screenWidth * 0.03f)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(cardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.015f)
                    ) {
                        Text(
                            text = "今日目标",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = subtitleSize * 0.8f
                        )
                        Text(
                            text = "${targetMinutes}分钟",
                            color = Color.White,
                            fontSize = titleSize * 0.9f,
                            fontWeight = FontWeight.Bold
                        )

                        if (sessionMinutes > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height((screenHeight * 0.002f).coerceAtLeast(1.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                itemType.color.copy(alpha = 0.5f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            
                            Text(
                                text = "本次专注",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = subtitleSize * 0.8f
                            )
                            Text(
                                text = "${sessionMinutes}分钟",
                                color = itemType.color,
                                fontSize = titleSize * 0.85f,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // 增加顶部内容权重，确保底部按钮有空间
        Spacer(modifier = Modifier.weight(0.3f))
        
        // 中间控制按钮区域
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f)
        ) {
            // 主控制按钮 - 现代化设计
            val buttonScale by animateFloatAsState(
                targetValue = if (isRunning) 1.05f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "button_scale"
            )
            
            FloatingActionButton(
                onClick = onStartPause,
                modifier = Modifier
                    .size(buttonSize)
                    .shadow(
                        elevation = screenWidth * 0.01f,
                        shape = CircleShape,
                        spotColor = if (isRunning) Color(0xFFFF6B35) else itemType.color
                    )
                    .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale),
                containerColor = if (isRunning) Color(0xFFFF6B35) else itemType.color,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = screenWidth * 0.006f)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Favorite else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "暂停" else "开始",
                    modifier = Modifier.size(buttonSize * 0.5f)
                )
            }
            
            Text(
                text = if (isRunning) "暂停专注" else "开始专注",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = subtitleSize,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 增加底部间距，确保按钮有足够空间
        Spacer(modifier = Modifier.weight(0.2f))
        
        // 底部按钮区域 - 水平并列
        Row(
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.015f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成按钮（当有本次专注时间时显示）
            AnimatedVisibility(
                visible = sessionMinutes > 0,
                enter = fadeIn() + slideInHorizontally { -it } + scaleIn(),
                exit = fadeOut() + slideOutHorizontally { -it } + scaleOut()
            ) {
                OutlinedButton(
                    onClick = onStop,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = itemType.color
                    ),
                    border = BorderStroke(
                        width = (screenWidth * 0.002f).coerceAtLeast(1.dp),
                        brush = Brush.horizontalGradient(
                            colors = listOf(itemType.color, itemType.color.copy(alpha = 0.6f))
                        )
                    ),
                    modifier = Modifier
                        .height(screenHeight * 0.12f)
                        .weight(2f)
                        .shadow(
                            elevation = screenWidth * 0.000f,
                            shape = RoundedCornerShape(screenWidth * 0.02f)
                        ),
                    shape = RoundedCornerShape(screenWidth * 0.02f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "完成",
                        modifier = Modifier.size(subtitleSize.value.dp * 0.9f)
                    )
                    Spacer(modifier = Modifier.width(screenWidth * 0.008f))
                    Text("完成", fontSize = subtitleSize * 0.85f)
                }
            }
            
            // 退出按钮
            OutlinedButton(
                onClick = onExit,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.6f)
                ),
                border = BorderStroke(
                    width = (screenWidth * 0.001f).coerceAtLeast(0.5.dp),
                    color = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .height(screenHeight * 0.12f)
                    .weight(if (sessionMinutes > 0) 1f else 2f)
                    .shadow(
                        elevation = screenWidth * 0.000f,
                        shape = RoundedCornerShape(screenWidth * 0.02f)
                    ),
                shape = RoundedCornerShape(screenWidth * 0.02f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "退出",
                    modifier = Modifier.size(subtitleSize.value.dp * 0.8f)
                )
                Spacer(modifier = Modifier.width(screenWidth * 0.008f))
                Text("退出", fontSize = subtitleSize * 0.8f)
            }
        }
    }
}

/**
 * 右侧计时器显示 - 大型响应式动画计时器
 */
@Composable
private fun TimerDisplay(
    modifier: Modifier = Modifier,
    elapsedSeconds: Int,
    sessionSeconds: Int,
    isRunning: Boolean,
    itemType: CheckInType,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    isLargeScreen: Boolean
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val sessionMinutes = sessionSeconds / 60
    val sessionSecondsRemainder = sessionSeconds % 60
    
    // 响应式尺寸
    val timerSize = (screenWidth * 0.35f).coerceAtMost(screenHeight * 0.6f)
    val mainFontSize = if (isLargeScreen) 48.sp else 36.sp
    val sessionFontSize = if (isLargeScreen) 24.sp else 18.sp
    val labelFontSize = if (isLargeScreen) 14.sp else 12.sp
    
    // 高级动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "timer_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isRunning) 0.9f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_animation"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isRunning) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_animation"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            
            // 主计时器圆环 - 现代化设计
            Box(
                modifier = Modifier.size(timerSize),
                contentAlignment = Alignment.Center
            ) {
                // 背景装饰圆环
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = if (isRunning) rotation * 0.1f else 0f
                        )
                ) {
                    val strokeWidth = size.minDimension * 0.02f
                    val radius = size.minDimension / 2f - strokeWidth
                    
                    // 外圆环背景
                    drawCircle(
                        color = itemType.color.copy(alpha = 0.1f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                    
                    // 内圆环装饰
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = radius * 0.85f,
                        style = Stroke(width = strokeWidth * 0.5f)
                    )
                    
                    // 动态脉冲圆环
                    if (isRunning) {
                        drawCircle(
                            color = itemType.color.copy(alpha = pulseAlpha),
                            radius = radius,
                            style = Stroke(width = strokeWidth * 0.8f)
                        )
                        
                        // 内部脉冲
                        drawCircle(
                            color = itemType.color.copy(alpha = pulseAlpha * 0.5f),
                            radius = radius * 0.7f,
                            style = Stroke(width = strokeWidth * 0.3f)
                        )
                    }
                }
                
                // 计时器文字 - 毛玻璃背景
                Box(
                    modifier = Modifier
                        .size(timerSize * 0.8f)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.01f)
                    ) {
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = Color.White,
                            fontSize = mainFontSize,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = "累计时间",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = labelFontSize
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))
            
            // 本次专注时间显示 - 现代化卡片
            AnimatedVisibility(
                visible = sessionSeconds > 0,
                enter = fadeIn() + scaleIn() + slideInVertically(),
                exit = fadeOut() + scaleOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier.shadow(
                        elevation = screenWidth * 0.01f,
                        shape = RoundedCornerShape(screenWidth * 0.04f),
                        spotColor = itemType.color
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = itemType.color.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(screenWidth * 0.04f),
                    border = BorderStroke(
                        width = (screenWidth * 0.002f).coerceAtLeast(1.dp),
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                itemType.color.copy(alpha = 0.6f),
                                itemType.color.copy(alpha = 0.2f)
                            )
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    itemType.color.copy(alpha = 0.15f),
                                    itemType.color.copy(alpha = 0.05f)
                                )
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = screenWidth * 0.03f,
                                vertical = screenHeight * 0.02f
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.01f)
                        ) {
                            Text(
                                text = String.format("%02d:%02d", sessionMinutes, sessionSecondsRemainder),
                                color = Color.White,
                                fontSize = sessionFontSize,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "本次专注",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = labelFontSize
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(screenHeight * 0.04f))
            
            // 运行状态指示 - 动态效果
            AnimatedVisibility(
                visible = isRunning,
                enter = fadeIn() + slideInVertically() + scaleIn(),
                exit = fadeOut() + slideOutVertically() + scaleOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.02f)
                ) {
                    // 脉冲点 - 高级动画
                    Box(
                        modifier = Modifier
                            .size(screenWidth * 0.015f)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        itemType.color,
                                        itemType.color.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .graphicsLayer(
                                scaleX = scale * 1.2f,
                                scaleY = scale * 1.2f
                            )
                    )
                    Text(
                        text = "专注进行中...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = labelFontSize * 1.1f,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
} 