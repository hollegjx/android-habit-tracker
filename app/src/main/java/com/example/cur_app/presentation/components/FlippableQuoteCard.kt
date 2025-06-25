//翻转卡片特效

package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random
import com.example.cur_app.network.model.DailyQuote
import com.example.cur_app.network.service.DailyQuoteService
import com.example.cur_app.presentation.viewmodel.QuoteViewModel
import com.example.cur_app.R

// 定义样式常量避免编译器混淆
private object QuoteCardStyles {
    val cardHeight = 280.dp
    val cardRadius = 24.dp
    val cardElevation = 12.dp
    val contentPadding = 24.dp
    
    val quoteMarkSize = 36.sp
    val quoteFontSize = 20.sp
    val authorFontSize = 14.sp
    val sourceFontSize = 10.sp
    
    val quoteLineHeight = 28.sp
    val contentPadding_horizontal = 16.dp
    val contentPadding_vertical = 12.dp
    
    val quoteMarkAlpha = 0.6f
    val authorAlpha = 0.8f
    val sourceAlpha_network = 0.8f
    val sourceAlpha_local = 0.8f
    
    val backgroundAlpha = 0.9f
    val overlayAlpha = 0.3f
}

/**
 * 重新设计的可翻转语录卡片
 * 解决编译器混淆问题
 */
@Composable
fun FlippableQuoteCard(
    modifier: Modifier = Modifier
) {
    val quoteViewModel: QuoteViewModel = hiltViewModel()
    val currentQuote by quoteViewModel.currentQuote.collectAsStateWithLifecycle()
    val uiState by quoteViewModel.uiState.collectAsStateWithLifecycle()
    
    var isFlipped by remember { mutableStateOf(false) }
    
    val backgroundImages = remember {
        listOf(R.drawable.bj_quote_bg_1, R.drawable.bj_quote_bg_2)
    }
    
    var currentImageIndex by remember { 
        mutableStateOf(if (backgroundImages.isNotEmpty()) Random.nextInt(backgroundImages.size) else 0)
    }
    
    val flipAnimation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "card_flip_animation"
    )
    
    var imageAlpha by remember { mutableStateOf(1f) }
    
    LaunchedEffect(backgroundImages.size) {
        while (true) {
            delay(30000)
            if (backgroundImages.size > 1) {
                imageAlpha = 0f
                delay(300)
                currentImageIndex = (currentImageIndex + 1) % backgroundImages.size
                imageAlpha = 1f
            }
        }
    }
    
    val animatedImageAlpha by animateFloatAsState(
        targetValue = imageAlpha,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "image_alpha_animation"
    )
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(600000) // 10分钟
            quoteViewModel.refresh()
        }
    }
    
    val infiniteAnimation = rememberInfiniteTransition(label = "halo_animation")
    val animationValue by infiniteAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_float_animation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(QuoteCardStyles.cardHeight)
            .clickable { 
                isFlipped = !isFlipped
                if (!isFlipped) {
                    quoteViewModel.forceReset()
                }
            }
            .graphicsLayer {
                rotationY = flipAnimation
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(QuoteCardStyles.cardRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = QuoteCardStyles.cardElevation)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val safeImageIndex = currentImageIndex.coerceIn(0, backgroundImages.size - 1)
            
            if (flipAnimation <= 90f) {
                QuoteContentView(
                    quote = currentQuote,
                    backgroundRes = backgroundImages[safeImageIndex],
                    animationValue = animationValue,
                    imageAlpha = animatedImageAlpha,
                    isLoading = uiState.isLoading,
                    isFromNetwork = uiState.isFromNetwork
                )
            } else {
                QuoteInfoView(
                    backgroundRes = backgroundImages[safeImageIndex],
                    animationValue = animationValue,
                    imageAlpha = animatedImageAlpha,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}

@Composable
private fun QuoteContentView(
    quote: DailyQuote,
    backgroundRes: Int,
    animationValue: Float,
    imageAlpha: Float,
    isLoading: Boolean,
    isFromNetwork: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景图片
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alpha = imageAlpha * QuoteCardStyles.backgroundAlpha
        )
        
        // 遮罩层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = QuoteCardStyles.overlayAlpha))
        )
        
        // 装饰光晕
        CreateHaloEffects(animationValue)
        
        // 内容区域
        if (isLoading) {
            LoadingView()
        } else {
            QuoteTextContent(quote, isFromNetwork)
        }
    }
}

@Composable
private fun CreateHaloEffects(animationValue: Float) {
    val halo1Alpha = 0.12f + 0.03f * sin(animationValue * 2)
    val halo2Alpha = 0.08f + 0.02f * cos(animationValue * 1.5f)
    val halo3Alpha = 0.06f + 0.02f * sin(animationValue * 0.8f)
    
    // 第一个光晕
    Box(
        modifier = Modifier
            .size(120.dp)
            .offset(
                x = (60 + 45 * cos(animationValue)).dp,
                y = (40 + 25 * sin(animationValue)).dp
            )
            .background(
                Color.White.copy(alpha = halo1Alpha),
                RoundedCornerShape(60.dp)
            )
            .blur(20.dp)
    )
    
    // 第二个光晕
    Box(
        modifier = Modifier
            .size(80.dp)
            .offset(
                x = (220 + 35 * cos(animationValue + PI/3)).dp,
                y = (80 + 20 * sin(animationValue + PI/3)).dp
            )
            .background(
                Color.White.copy(alpha = halo2Alpha),
                RoundedCornerShape(40.dp)
            )
            .blur(15.dp)
    )
    
    // 第三个光晕
    Box(
        modifier = Modifier
            .size(60.dp)
            .offset(
                x = (150 + 25 * sin(animationValue + PI/2)).dp,
                y = (20 + 15 * cos(animationValue + PI/2)).dp
            )
            .background(
                Color.White.copy(alpha = halo3Alpha),
                RoundedCornerShape(30.dp)
            )
            .blur(12.dp)
    )
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun QuoteTextContent(quote: DailyQuote, isFromNetwork: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(QuoteCardStyles.contentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 开始引号
        CreateQuoteMark(isStart = true)
        
        // 语录内容
        CreateQuoteText(quote.content)
        
        // 结束引号
        CreateQuoteMark(isStart = false)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 作者信息
        CreateAuthorText(quote.author)
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 数据来源标识
        CreateSourceIndicator(isFromNetwork)
    }
}

@Composable
private fun CreateQuoteMark(isStart: Boolean) {
    val quote = if (isStart) "\"" else "\""
    Text(
        text = quote,
        fontSize = QuoteCardStyles.quoteMarkSize,
        fontWeight = FontWeight.Light,
        color = Color.White.copy(alpha = QuoteCardStyles.quoteMarkAlpha)
    )
}

@Composable
private fun CreateQuoteText(content: String) {
    Text(
        text = content,
        fontSize = QuoteCardStyles.quoteFontSize,
        fontWeight = FontWeight.Medium,
        color = Color.White,
        textAlign = TextAlign.Center,
        lineHeight = QuoteCardStyles.quoteLineHeight,
        modifier = Modifier.padding(
            horizontal = QuoteCardStyles.contentPadding_horizontal, 
            vertical = QuoteCardStyles.contentPadding_vertical
        )
    )
}

@Composable
private fun CreateAuthorText(author: String) {
    Text(
        text = "—— $author",
        fontSize = QuoteCardStyles.authorFontSize,
        fontWeight = FontWeight.Light,
        color = Color.White.copy(alpha = QuoteCardStyles.authorAlpha),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CreateSourceIndicator(isFromNetwork: Boolean) {
    val sourceText = if (isFromNetwork) "📡 来自API每日一句" else "📚 本地语录库"
    val sourceColor = if (isFromNetwork) {
        Color.Green.copy(alpha = QuoteCardStyles.sourceAlpha_network)
    } else {
        Color.Yellow.copy(alpha = QuoteCardStyles.sourceAlpha_local)
    }
    
    Text(
        text = sourceText,
        fontSize = QuoteCardStyles.sourceFontSize,
        fontWeight = FontWeight.Light,
        color = sourceColor,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun QuoteInfoView(
    backgroundRes: Int,
    animationValue: Float,
    imageAlpha: Float,
    modifier: Modifier = Modifier
) {
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val currentDate = remember {
        SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault()).format(Date())
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alpha = imageAlpha * 0.9f
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            CreateInfoCard("时间", currentTime, currentDate, Icons.Default.DateRange)
            CreateInfoCard("位置", "北京市朝阳区", "国贸CBD • 已定位", Icons.Default.LocationOn)
            CreateInfoCard("天气", "晴朗 22°C", "空气质量优", Icons.Default.Star)
        }
    }
}

@Composable
private fun CreateInfoCard(
    title: String,
    mainText: String,
    subText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = mainText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subText,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
} 