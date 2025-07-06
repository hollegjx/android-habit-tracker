package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.cur_app.ui.theme.*
import com.example.cur_app.data.local.AiCharacterManager
import com.example.cur_app.data.local.entity.CheckInType
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * AI分析数据
 */
data class AiAnalysis(
    val type: String,
    val title: String,
    val insight: String,
    val suggestion: String,
    val improvement: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

/**
 * AI建议卡片组件
 * 基于用户数据提供智能分析和个性化建议
 * 支持自动滚动和手动滑动控制
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AiSuggestionCard(
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val currentCharacter by AiCharacterManager.currentCharacter.collectAsStateWithLifecycle()
    
    // AI分析数据
    val aiAnalyses = remember {
        listOf(
            AiAnalysis(
                type = "学习分析",
                title = "学习效率持续提升",
                insight = "过去7天平均学习时长165分钟，较上周提升25%",
                suggestion = "建议在上午9-11点专注学习，效率最高",
                improvement = "可尝试番茄工作法，25分钟专注+5分钟休息",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
            ),
            AiAnalysis(
                type = "运动分析",
                title = "运动强度需要调整",
                insight = "连续3天运动量偏低，建议增加有氧运动",
                suggestion = "推荐每日步行30分钟或跑步15分钟",
                improvement = "可设置运动提醒，保持规律运动习惯",
                icon = Icons.Default.Favorite,
                gradientColors = listOf(Color(0xFFFF7043), Color(0xFFFF5722))
            ),
            AiAnalysis(
                type = "理财分析",
                title = "储蓄目标达成良好",
                insight = "本月储蓄完成度92%，超出预期8%",
                suggestion = "可考虑增加10%投资配置，优化收益",
                improvement = "建议学习基金定投，长期财富增长",
                icon = Icons.Default.Home,
                gradientColors = listOf(Color(0xFF43A047), Color(0xFF2E7D32))
            ),
            AiAnalysis(
                type = "总体评估",
                title = "习惯养成进展优秀",
                insight = "整体坚持度78%，在所有用户中排名前20%",
                suggestion = "继续保持现有节奏，适当增加挑战难度",
                improvement = "建议制定下个月的进阶目标",
                icon = Icons.Default.Star,
                gradientColors = listOf(Color(0xFF9C27B0), Color(0xFF673AB7))
            )
        )
    }
    
    // Pager状态和自动滚动
    val pagerState = rememberPagerState(pageCount = { aiAnalyses.size })
    var isUserInteracting by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // 监听用户交互状态
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            isUserInteracting = true
            delay(3000) // 用户停止交互3秒后恢复自动滚动
            isUserInteracting = false
        }
    }
    
    // 自动滚动（用户交互时暂停）
    LaunchedEffect(pagerState.currentPage, isUserInteracting) {
        while (!isUserInteracting) {
            delay(5000) // 每5秒自动切换
            if (!isUserInteracting && !pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % aiAnalyses.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 标题和指示器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 当前AI角色头像
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(currentCharacter.backgroundColor)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentCharacter.iconEmoji,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${currentCharacter.name} · AI 智能分析",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = getDynamicTextColor(CheckInType.STUDY) // 使用与"总体成就"相同的颜色
                )
            }
            
            // 页面指示器
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(aiAnalyses.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) 
                                    GradientStart 
                                else 
                                    GradientStart.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
        
        // 可滑动的AI分析卡片
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val analysis = aiAnalyses[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 渐变背景
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = analysis.gradientColors.map { it.copy(alpha = 0.1f) }
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // 头部信息
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 图标背景
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        brush = Brush.linearGradient(analysis.gradientColors)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = analysis.icon,
                                    contentDescription = analysis.type,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = analysis.type,
                                    fontSize = 12.sp,
                                    color = analysis.gradientColors[0],
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = analysis.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 数据洞察
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = analysis.gradientColors[0].copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "📊 数据洞察",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = analysis.gradientColors[0]
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = analysis.insight,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 建议内容
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = analysis.gradientColors[1].copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "💡 智能建议",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = analysis.gradientColors[1]
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = analysis.suggestion,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 改进方案
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "🎯 行动计划",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = analysis.improvement,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 