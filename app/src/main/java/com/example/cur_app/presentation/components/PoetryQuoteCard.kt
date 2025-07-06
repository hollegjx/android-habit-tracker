package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime

/**
 * 诗意语录卡片
 * 显示励志、哲理、美学语录，根据时间段变化
 */
@Composable
fun PoetryQuoteCard(
    modifier: Modifier = Modifier
) {
    var currentQuoteIndex by remember { mutableStateOf(0) }
    var isVisible by remember { mutableStateOf(false) }
    
    // 根据时间段选择不同的语录
    val quotes = remember {
        val hour = LocalTime.now().hour
        when {
            hour in 5..8 -> listOf(
                "晨光熹微，万物新生\n愿你今日的每一步都充满希望",
                "清晨的阳光，照亮内心的方向\n新的一天，从这份美好开始",
                "日出东方，心向阳光\n让今天成为改变的起点"
            )
            hour in 9..11 -> listOf(
                "上午时光，思维清晰\n专注当下，创造无限可能",
                "知识如甘露，滋润心田\n学习的路上，每一步都是成长",
                "工作如修行，用心则灵\n认真对待，收获属于你的精彩"
            )
            hour in 12..14 -> listOf(
                "午后时光，宁静致远\n在忙碌中找到内心的平静",
                "餐后片刻，思考人生\n简单的幸福，往往最珍贵",
                "日当正午，精神饱满\n继续前行，梦想就在前方"
            )
            hour in 15..17 -> listOf(
                "下午斜阳，温暖如画\n坚持的路上，风景正好",
                "时光荏苒，岁月如歌\n珍惜当下，感恩所有",
                "努力的意义，在于遇见更好的自己"
            )
            hour in 18..20 -> listOf(
                "夕阳西下，一天辛劳\n收获的不只是成果，更是成长",
                "黄昏时分，反思今日\n每一份努力都值得被看见",
                "晚霞如诗，生活如画\n用心生活，处处皆美"
            )
            else -> listOf(
                "星空璀璨，夜色如诗\n静心思考，沉淀今日收获",
                "月光如水，心静如镜\n在宁静中规划明天的美好",
                "夜深人静，适合与内心对话\n愿你拥有安详的睡眠"
            )
        }
    }
    
    // 定期切换语录
    LaunchedEffect(Unit) {
        isVisible = true
        while (true) {
            delay(8000) // 每8秒切换一次
            currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
        }
    }
    
    // 背景动画
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val animatedFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "background_float"
    )
    
    // 渐变色彩动画
    val colors = listOf(
        listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
        listOf(Color(0xFFFF7043), Color(0xFFFF5722)),
        listOf(Color(0xFF43A047), Color(0xFF2E7D32)),
        listOf(Color(0xFF9C27B0), Color(0xFF673AB7)),
        listOf(Color(0xFFFF9800), Color(0xFFF57C00))
    )
    
    val currentColors = colors[currentQuoteIndex % colors.size]
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // 动态渐变背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = currentColors.map { 
                                it.copy(alpha = 0.9f + 0.1f * kotlin.math.sin(animatedFloat * 2 * kotlin.math.PI).toFloat())
                            },
                            start = Offset(
                                animatedFloat * 100f,
                                animatedFloat * 50f
                            ),
                            end = Offset(
                                Float.POSITIVE_INFINITY - animatedFloat * 100f,
                                Float.POSITIVE_INFINITY - animatedFloat * 50f
                            )
                        )
                    )
            )
            
            // 装饰性模糊圆圈
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(
                        x = (20 + 30 * kotlin.math.sin(animatedFloat * kotlin.math.PI)).dp,
                        y = (10 + 20 * kotlin.math.cos(animatedFloat * kotlin.math.PI)).dp
                    )
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(60.dp)
                    )
                    .blur(20.dp)
            )
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(
                        x = (200 + 20 * kotlin.math.cos(animatedFloat * 1.5f * kotlin.math.PI)).dp,
                        y = (40 + 15 * kotlin.math.sin(animatedFloat * 1.5f * kotlin.math.PI)).dp
                    )
                    .background(
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(40.dp)
                    )
                    .blur(15.dp)
            )
            
            // 语录内容
            AnimatedContent(
                targetState = quotes[currentQuoteIndex],
                transitionSpec = {
                    slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = tween(800, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(800)) togetherWith slideOutVertically(
                        targetOffsetY = { -30 },
                        animationSpec = tween(800, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(400))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                label = "quote_content"
            ) { quote ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = quote,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            // 底部时间指示器
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(quotes.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (index == currentQuoteIndex) 
                                    Color.White 
                                else 
                                    Color.White.copy(alpha = 0.4f)
                            )
                            .animateContentSize()
                    )
                }
            }
        }
    }
} 