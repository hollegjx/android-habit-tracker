package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 动画工具类
 * 提供各种现代化的动画效果
 */
object AnimationUtils {
    
    // 标准动画持续时间
    const val DURATION_SHORT = 300
    const val DURATION_MEDIUM = 500
    const val DURATION_LONG = 800
    
    // 标准缓动曲线
    val EaseInOutCubic = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val EaseOutBack = CubicBezierEasing(0.175f, 0.885f, 0.32f, 1.275f)
    val EaseInBack = CubicBezierEasing(0.6f, -0.28f, 0.735f, 0.045f)
}

/**
 * 滑动进入动画
 */
@Composable
fun SlideInAnimation(
    visible: Boolean,
    direction: SlideDirection = SlideDirection.Up,
    duration: Int = AnimationUtils.DURATION_MEDIUM,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val slideDistance = with(density) { 30.dp.toPx() }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = AnimationUtils.EaseOutBack
            )
        ) { 
            when (direction) {
                SlideDirection.Up -> slideDistance.toInt()
                SlideDirection.Down -> -slideDistance.toInt()
            }
        } + fadeIn(
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = LinearEasing
            )
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = duration / 2,
                easing = AnimationUtils.EaseInBack
            )
        ) {
            when (direction) {
                SlideDirection.Up -> -slideDistance.toInt()
                SlideDirection.Down -> slideDistance.toInt()
            }
        } + fadeOut(
            animationSpec = tween(durationMillis = duration / 2)
        )
    ) {
        content()
    }
}

/**
 * 缩放进入动画
 */
@Composable
fun ScaleInAnimation(
    visible: Boolean,
    duration: Int = AnimationUtils.DURATION_MEDIUM,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = AnimationUtils.EaseOutBack
            ),
            initialScale = 0.8f
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay
            )
        ),
        exit = scaleOut(
            animationSpec = tween(
                durationMillis = duration / 2,
                easing = AnimationUtils.EaseInBack
            ),
            targetScale = 0.8f
        ) + fadeOut(
            animationSpec = tween(durationMillis = duration / 2)
        )
    ) {
        content()
    }
}

/**
 * 数字变化动画
 */
@Composable
fun animateIntAsState(
    targetValue: Int,
    animationSpec: AnimationSpec<Int> = tween(
        durationMillis = AnimationUtils.DURATION_MEDIUM,
        easing = AnimationUtils.EaseInOutCubic
    )
): State<Int> {
    return androidx.compose.animation.core.animateIntAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = "number_animation"
    )
}

/**
 * 浮点数变化动画
 */
@Composable
fun animateFloatAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = AnimationUtils.DURATION_MEDIUM,
        easing = AnimationUtils.EaseInOutCubic
    )
): State<Float> {
    return androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = "float_animation"
    )
}

/**
 * 弹跳按钮动画
 */
@Composable
fun Modifier.bounceClick(
    onClick: () -> Unit
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

/**
 * 滑动方向枚举
 */
enum class SlideDirection {
    Up, Down
}

/**
 * 标签页切换动画
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TabSwitchAnimation(
    currentTab: Int,
    content: @Composable (Int) -> Unit
) {
    AnimatedContent(
        targetState = currentTab,
        transitionSpec = {
            if (targetState > initialState) {
                // 向右滑动
                slideInHorizontally { width -> width } + fadeIn() with
                slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                // 向左滑动
                slideInHorizontally { width -> -width } + fadeIn() with
                slideOutHorizontally { width -> width } + fadeOut()
            }.using(
                SizeTransform(clip = false)
            )
        },
        label = "tab_switch"
    ) { tab ->
        content(tab)
    }
}

/**
 * 卡片出现动画
 */
@Composable
fun CardAppearAnimation(
    visible: Boolean,
    index: Int = 0,
    content: @Composable () -> Unit
) {
    SlideInAnimation(
        visible = visible,
        direction = SlideDirection.Up,
        duration = AnimationUtils.DURATION_MEDIUM,
        delay = index * 100, // 错开动画时间
        content = content
    )
} 