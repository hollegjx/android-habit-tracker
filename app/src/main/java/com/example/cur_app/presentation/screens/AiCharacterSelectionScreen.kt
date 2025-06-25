package com.example.cur_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cur_app.data.local.entity.*
import com.example.cur_app.data.local.AiCharacterManager
import com.example.cur_app.data.local.SelectedAiCharacter
import com.example.cur_app.presentation.components.*
import com.example.cur_app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * AI角色数据类 - 扩展属性支持完整的角色信息
 */
data class AiCharacter(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val skills: List<String>,
    val backgroundColor: List<Color>,
    val iconEmoji: String,
    val greeting: String,
    val personality: String,
    val speakingStyle: String,
    val mood: String
)

/**
 * AI角色选择界面 - 完全按照HTML源码风格重新设计
 * 智能打卡助手风格的人物选择界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCharacterSelectionScreen(
    onNavigateToChat: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var selectedCharacter by remember { mutableStateOf<AiCharacter?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingCharacter by remember { mutableStateOf<AiCharacter?>(null) }
    
    // AI角色数据 - 扩展到6个角色，按照HTML源码设计
    val aiCharacters = remember {
        listOf(
            AiCharacter(
                id = "sakura",
                name = "小樱",
                subtitle = "温柔学习伙伴",
                description = "温柔体贴，善解人意。总是能在你需要鼓励的时候给予温暖的话语，学习遇到困难时会耐心地陪伴你一起克服。",
                skills = listOf("学习计划制定", "情绪调节", "时间管理", "特别擅长帮助用户养成良好的学习习惯"),
                backgroundColor = listOf(Color(0xFFff9a9e), Color(0xFFfecfef)),
                iconEmoji = "🌸",
                greeting = "你好呀～我是小樱，很高兴见到你呢！💕",
                personality = "温柔体贴，善解人意。总是能在你需要鼓励的时候给予温暖的话语，学习遇到困难时会耐心地陪伴你一起克服。",
                speakingStyle = "语气温和，经常使用\"呢~\"、\"哦~\"等可爱语气词，会用emoji表达情感，给人亲切感。",
                mood = "😊 小樱今天心情很好呢～想和你一起学习！"
            ),
            AiCharacter(
                id = "leon",
                name = "雷恩",
                subtitle = "活力运动教练",
                description = "充满活力，积极向上。永远精神饱满，能够激发你的运动热情，让每一次锻炼都充满乐趣。",
                skills = listOf("运动计划制定", "体能训练指导", "健康生活建议", "擅长各种运动项目的指导"),
                backgroundColor = listOf(Color(0xFFffeaa7), Color(0xFFfab1a0)),
                iconEmoji = "⚡",
                greeting = "嘿！我是雷恩，准备好一起燃烧卡路里了吗？💪",
                personality = "充满活力，积极向上。永远精神饱满，能够激发你的运动热情，让每一次锻炼都充满乐趣。",
                speakingStyle = "语气活泼有力，经常使用\"加油！\"、\"冲！\"等激励性词汇，充满正能量。",
                mood = "💪 雷恩已经准备好和你一起挥汗如雨啦！"
            ),
            AiCharacter(
                id = "luna",
                name = "露娜",
                subtitle = "高冷御姐",
                description = "细心谨慎，理性分析。对数字敏感，善于规划，能帮你制定合理的学习计划。",
                skills = listOf("理财规划", "预算管理", "投资建议", "擅长帮助用户建立正确的金钱观念"),
                backgroundColor = listOf(Color(0xFFa8edea), Color(0xFFfed6e3)),
                iconEmoji = "🌙",
                greeting = "你好，我是露娜。让我们一起规划美好的未来吧～💎",
                personality = "细心谨慎，理性分析。对数字敏感，善于规划，能帮你制定合理的理财计划。",
                speakingStyle = "语气专业而亲和，经常用数据说话，但也会用温柔的方式解释复杂概念。",
                mood = "💎 露娜今天要和你一起规划美好的财务未来！"
            ),
            AiCharacter(
                id = "alex",
                name = "苏柒",
                subtitle = "霸道高冷总裁",
                description = "严格认真，目标导向。会督促你坚持目标，不轻易妥协，帮你克服懒惰和拖延。",
                skills = listOf("目标管理", "习惯养成", "时间规划", "擅长帮助用户保持自律和专注"),
                backgroundColor = listOf(Color(0xFFff8a80), Color(0xFFffab91)),
                iconEmoji = "💎",
                greeting = "我是苏柒，你的目标就是我的使命！🎯",
                personality = "严格认真，目标导向。会督促你坚持目标，不轻易妥协，帮你克服懒惰和拖延。",
                speakingStyle = "语气严肃但关怀，会直接指出问题，但也会给予建设性建议。",
                mood = "🎯 苏柒正在监督你的进度，不要松懈哦！"
            ),
            AiCharacter(
                id = "miki",
                name = "美琪",
                subtitle = "温柔小秘书",
                description = "机智灵活，多才多艺。什么都懂一点，能在各个方面给你提供帮助和建议。",
                skills = listOf("综合管理", "信息整理", "日程安排", "擅长统筹规划和多任务处理"),
                backgroundColor = listOf(Color(0xFFd299c2), Color(0xFFfef9d7)),
                iconEmoji = "🌟",
                greeting = "Hi～我是美琪，有什么需要帮助的尽管说！✨",
                personality = "机智灵活，多才多艺。什么都懂一点，能在各个方面给你提供帮助和建议。",
                speakingStyle = "语气活泼聪明，经常有新点子，会用生动的比喻来解释问题。",
                mood = "✨ 美琪今天准备了很多小妙招要分享给你！"
            ),
            AiCharacter(
                id = "zen",
                name = "JZ",
                subtitle = "研究生导师",
                description = "沉稳平和，富有智慧。能帮你在浮躁的世界中找到内心的平静和专注。",
                skills = listOf("冥想指导", "压力释放", "心理调节", "擅长帮助用户保持心理健康"),
                backgroundColor = listOf(Color(0xFFb2fefa), Color(0xFF0ed2f7)),
                iconEmoji = "🧘",
                greeting = "阿弥陀佛，我是JZ，愿你内心平静如水🧘‍♂️",
                personality = "沉稳平和，富有智慧。能帮你在浮躁的世界中找到内心的平静和专注。",
                speakingStyle = "语气平和深沉，经常引用哲理名言，善于用简单的话语点醒他人。",
                mood = "🕯️ JZ在这里，让我们一起寻找内心的宁静吧"
            )
        )
    }
    
    // 设置沉浸式状态栏和导航栏
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, view)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars()) // 隐藏状态栏和导航栏
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        selectedCharacter = aiCharacters.first()
        delay(200)
        isVisible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF667eea),
                        0.4f to Color(0xFF764ba2),
                        1f to Color(0xFFf8fafc)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 头部区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌸 AI 伙伴选择",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "选择你的专属学习伙伴",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // 内容区域 - 可滚动，居中显示
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // 当前选中角色展示
                selectedCharacter?.let { character ->
                    item {
                        CurrentCharacterDisplay(character = character)
                    }
                    
                    // 角色详细介绍
                    item {
                        CharacterIntroduction(character = character)
                    }
                }
                
                // 角色选择网格
                item {
                    CharacterSelectionGrid(
                        characters = aiCharacters,
                        selectedCharacter = selectedCharacter,
                        onCharacterSelected = { character ->
                            if (character.id != selectedCharacter?.id) {
                                showConfirmDialog = true
                                pendingCharacter = character
                            }
                        }
                    )
                }
                
                // 互动体验面板
                selectedCharacter?.let { character ->
                    item {
                        InteractionPanel(character = character)
                    }
                }
                
                item {
                    // 底部间距，确保内容不被系统UI遮挡
                    Spacer(modifier = Modifier.height(150.dp))
                }
                }
            }
        }
    }
    
    // AI角色切换确认弹窗
    if (showConfirmDialog && pendingCharacter != null) {
        AiCharacterConfirmDialog(
            character = pendingCharacter!!,
            onConfirm = {
                selectedCharacter = pendingCharacter
                // 更新全局状态
                AiCharacterManager.updateCurrentCharacter(
                    SelectedAiCharacter(
                        id = pendingCharacter!!.id,
                        name = pendingCharacter!!.name,
                        iconEmoji = pendingCharacter!!.iconEmoji,
                        subtitle = pendingCharacter!!.subtitle,
                        backgroundColor = pendingCharacter!!.backgroundColor
                    )
                )
                showConfirmDialog = false
                pendingCharacter = null
            },
            onDismiss = {
                showConfirmDialog = false
                pendingCharacter = null
            }
        )
    }
}

/**
 * 当前选中角色展示 - 按照HTML源码中的character-display样式
 */
@Composable
fun CurrentCharacterDisplay(
    character: AiCharacter,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(character.id) {
        isVisible = false
        delay(100)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(tween(600)) + fadeIn(tween(600)),
        exit = scaleOut(tween(300)) + fadeOut(tween(300))
    ) {
        Card(
            modifier = modifier
                .width(350.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 角色头像 - 120x160的矩形
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            brush = Brush.verticalGradient(character.backgroundColor)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = character.iconEmoji,
                        fontSize = 48.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 角色名称
                Text(
                    text = character.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // 角色标题标签
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFF667eea).copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = character.subtitle,
                        fontSize = 14.sp,
                        color = Color(0xFF667eea),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 角色详细介绍 - 按照HTML源码中的character-intro样式
 */
@Composable
fun CharacterIntroduction(
    character: AiCharacter,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(350.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFf8fafc)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 性格特点
            IntroSection(
                icon = "💫",
                label = "性格特点",
                content = character.personality
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // 擅长领域
            IntroSection(
                icon = "🎯",
                label = "擅长领域",
                content = character.skills.joinToString("、")
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // 说话风格
            IntroSection(
                icon = "💬",
                label = "说话风格",
                content = character.speakingStyle
            )
        }
    }
}

/**
 * 介绍部分的子组件
 */
@Composable
fun IntroSection(
    icon: String,
    label: String,
    content: String
) {
    Column {
        // 标签
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 14.sp
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 内容
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .background(
                            Color(0xFF667eea),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content,
                    fontSize = 13.sp,
                    color = Color(0xFF6b7280),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * 角色选择网格 - 3x2布局
 */
@Composable
fun CharacterSelectionGrid(
    characters: List<AiCharacter>,
    selectedCharacter: AiCharacter?,
    onCharacterSelected: (AiCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(350.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(25.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🎭",
                    fontSize = 18.sp
                )
                Text(
                    text = "选择你的AI伙伴",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 3x2网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.height(200.dp) // 固定高度以适应2行
            ) {
                items(characters) { character ->
                    CharacterGridItem(
                        character = character,
                        isSelected = selectedCharacter?.id == character.id,
                        onClick = { onCharacterSelected(character) }
                    )
                }
            }
        }
    }
}

/**
 * 角色网格项 - 按照HTML源码中的character-option样式
 */
@Composable
fun CharacterGridItem(
    character: AiCharacter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(300)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .scale(animatedScale),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color.Transparent
            } else {
                Color(0xFFf8fafc)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            )
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(15.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 圆形头像
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            brush = if (isSelected) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.2f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(character.backgroundColor)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = character.iconEmoji,
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // 角色名称
                Text(
                    text = character.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(5.dp))
                
                // 角色描述
                Text(
                    text = character.subtitle,
                    fontSize = 10.sp,
                    color = if (isSelected) {
                        Color.White.copy(alpha = 0.9f)
                    } else {
                        Color(0xFF666666).copy(alpha = 0.7f)
                    },
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

/**
 * 互动体验面板 - 按照HTML源码中的interaction-panel样式
 */
@Composable
fun InteractionPanel(
    character: AiCharacter,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(350.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(25.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🎮",
                    fontSize = 18.sp
                )
                Text(
                    text = "互动体验",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 语音控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                VoiceButton(
                    icon = "🎵",
                    text = "问候语音",
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: 播放问候语音 */ }
                )
                
                VoiceButton(
                    icon = "💪",
                    text = "鼓励语音",
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: 播放鼓励语音 */ }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 心情展示
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFffeaa7),
                                    Color(0xFFfab1a0)
                                )
                            )
                        )
                        .padding(15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = character.mood,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 语音按钮组件
 */
@Composable
fun VoiceButton(
    icon: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .clickable { 
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) {
                Color.Transparent
            } else {
                Color(0xFFf8fafc)
            }
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (isPressed) Color(0xFF667eea) else Color(0xFFe5e7eb)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isPressed) {
                        Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            )
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 14.sp
                )
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isPressed) Color.White else Color(0xFF333333)
                )
            }
        }
    }
    
    // 重置按下状态
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(200)
            isPressed = false
        }
    }
}

/**
 * AI角色切换确认弹窗 - 现代化设计
 */
@Composable
fun AiCharacterConfirmDialog(
    character: AiCharacter,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // 角色头像
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(character.backgroundColor)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = character.iconEmoji,
                        fontSize = 32.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题
                Text(
                    text = "更换AI伙伴",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 角色信息
                Text(
                    text = "${character.name} · ${character.subtitle}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = GradientStart
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 提示信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3F4F6)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ 重要提醒",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7C2D12)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "更换AI伙伴后，以下功能将同步变更：",
                            fontSize = 14.sp,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• 打卡页面的AI智能分析助手\n• 聊天对话的AI伙伴角色\n• 所有个性化建议和互动",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "确认更换",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "取消",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
            }
        }
    )
}
