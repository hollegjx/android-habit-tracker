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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cur_app.data.local.entity.*
import com.example.cur_app.data.local.AiCharacterManager
import com.example.cur_app.data.local.SelectedAiCharacter
import com.example.cur_app.data.database.entities.AiCharacterEntity
import com.example.cur_app.presentation.components.*
import com.example.cur_app.presentation.viewmodel.AiCharacterSelectionViewModel
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
    onNavigateBack: () -> Unit = {},
    viewModel: AiCharacterSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isVisible by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingCharacter by remember { mutableStateOf<AiCharacter?>(null) }
    
    // 将数据库实体转换为UI模型
    val aiCharacters = remember(uiState.characters) {
        uiState.characters.map { it.toUiModel() }
    }
    
    val selectedCharacter = remember(uiState.selectedCharacter) {
        uiState.selectedCharacter?.toUiModel()
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
                pendingCharacter?.let { character ->
                    // 在数据库中找到对应的实体
                    val selectedEntity = uiState.characters.find { it.characterId == character.id }
                    selectedEntity?.let { entity ->
                        viewModel.selectCharacter(entity)
                        // 更新全局状态
                        AiCharacterManager.updateCurrentCharacter(
                            SelectedAiCharacter(
                                id = character.id,
                                name = character.name,
                                iconEmoji = character.iconEmoji,
                                subtitle = character.subtitle,
                                backgroundColor = character.backgroundColor
                            )
                        )
                    }
                }
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
