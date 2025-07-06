package com.example.cur_app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay



/**
 * 首页用户等级网格展示组件 - 三个类型的等级卡片
 */
@Composable
fun ModernAchievementGrid(
    userAchievements: List<com.example.cur_app.data.database.entities.UserAchievementEntity>,
    modifier: Modifier = Modifier,
    onAchievementClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 从数据库数据转换为UI数据
        val userLevelCards = userAchievements.mapIndexed { index, achievement ->
            val categoryType = when (achievement.category) {
                "STUDY" -> com.example.cur_app.data.local.entity.CheckInType.STUDY
                "EXERCISE" -> com.example.cur_app.data.local.entity.CheckInType.EXERCISE
                "MONEY" -> com.example.cur_app.data.local.entity.CheckInType.MONEY
                else -> com.example.cur_app.data.local.entity.CheckInType.STUDY
            }
            
            val levelDefs = com.example.cur_app.data.local.entity.AchievementDefinitions.getLevels(categoryType)
            val nextLevel = if (achievement.levelIndex + 1 < levelDefs.size) {
                levelDefs[achievement.levelIndex + 1]
            } else {
                levelDefs.last()
            }
            
            UserLevelCardData(
                userLevel = UserLevel(
                    title = achievement.currentLevel,
                    currentExp = achievement.currentExp,
                    maxExp = nextLevel.expThreshold,
                    icon = when (achievement.category) {
                        "STUDY" -> Icons.Default.Star
                        "EXERCISE" -> Icons.Default.Favorite
                        "MONEY" -> Icons.Default.Home
                        else -> Icons.Default.Star
                    },
                    gradientColors = when (achievement.category) {
                        "STUDY" -> listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                        "EXERCISE" -> listOf(Color(0xFFFF7043), Color(0xFFFF5722))
                        "MONEY" -> listOf(Color(0xFF43A047), Color(0xFF2E7D32))
                        else -> listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                    },
                    category = achievement.category
                ),
                upgradeRequirements = listOf(
                    UpgradeRequirement(
                        "经验值", 
                        achievement.currentExp, 
                        nextLevel.expThreshold, 
                        "点"
                    ),
                    when (achievement.category) {
                        "STUDY" -> UpgradeRequirement(
                            "学习总时长", 
                            achievement.totalStudyTime / 60, 
                            (nextLevel.expThreshold / 10), 
                            "小时"
                        )
                        "EXERCISE" -> UpgradeRequirement(
                            "运动总时长", 
                            achievement.totalExerciseTime / 60, 
                            (nextLevel.expThreshold / 15), 
                            "小时"
                        )
                        "MONEY" -> UpgradeRequirement(
                            "储蓄金额", 
                            (achievement.totalMoney / 100).toInt(), 
                            nextLevel.expThreshold * 2, 
                            "元"
                        )
                        else -> UpgradeRequirement("连续天数", achievement.currentStreak, 7, "天")
                    }
                )
            )
        }
        
        userLevelCards.forEachIndexed { index, data ->
            UserLevelCard(
                data = data,
                animationDelay = index * 150,
                onClick = { onAchievementClick(data.userLevel.category) }
            )
        }
    }
}

/**
 * 分类成就网格展示组件 - 用于详细页面
 */
@Composable
fun CategoryAchievementGrid(
    category: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val achievements = when (category) {
            "STUDY" -> getStudyAchievements()
            "EXERCISE" -> getExerciseAchievements()
            "MONEY" -> getMoneyAchievements()
            else -> emptyList()
        }
        
        achievements.forEachIndexed { index, achievement ->
            EnhancedAchievementCard(
                achievement = achievement,
                animationDelay = index * 200,
                onClick = { /* TODO: 成就详情 */ }
            )
        }
    }
}

/**
 * 成就数据类
 */
data class AchievementData(
    val title: String,
    val subtitle: String,
    val progress: Float,
    val level: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val category: String = ""
)

/**
 * 用户等级数据类
 */
data class UserLevel(
    val title: String,           // 当前称号，如"学习新手"
    val currentExp: Int,         // 当前经验值
    val maxExp: Int,            // 升级所需经验值
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val category: String
)

/**
 * 任务数据类
 */
data class Task(
    val name: String,           // 任务名称，如"每天学习编程"
    val currentValue: Int,      // 当前完成值
    val targetValue: Int,       // 目标值
    val unit: String,          // 单位，如"小时"、"次"
    val progress: Float = currentValue.toFloat() / targetValue
)

/**
 * 用户等级卡片数据类 - 用于首页展示
 */
data class UserLevelCardData(
    val userLevel: UserLevel,
    val upgradeRequirements: List<UpgradeRequirement>    // 升级条件（显示前两个）
)

/**
 * 增强版成就卡片 - 更丰富的视觉设计
 */
@Composable
fun EnhancedAchievementCard(
    achievement: AchievementData,
    animationDelay: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    
    // 进入动画
    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }
    
    // 动画值
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) achievement.progress else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)
        ),
        label = "progress_animation"
    )
    
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "card_scale"
    )
    
    // 简化的完成状态效果
    val isCompleted = achievement.progress >= 1.0f
    val shimmerAlpha by animateFloatAsState(
        targetValue = if (isCompleted && isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "completion_highlight"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(600))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                }
                .clip(RoundedCornerShape(24.dp))
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isCompleted) {
                                listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                            } else {
                                achievement.gradientColors
                            },
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                // 简单装饰效果（性能友好）
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.White.copy(alpha = shimmerAlpha * 0.1f)
                            )
                    )
                }
                
                // 主要内容
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 左侧进度和图标区域
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 外层装饰圆环
                        CircularProgressIndicator(
                            progress = 1f,
                            modifier = Modifier.size(80.dp),
                            backgroundColor = Color.White.copy(alpha = 0.2f),
                            color = Color.White.copy(alpha = 0.3f),
                            strokeWidth = 2.dp
                        )
                        
                        // 内层进度条
                        CircularProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier.size(70.dp),
                            backgroundColor = Color.White.copy(alpha = 0.2f),
                            color = Color.White,
                            strokeWidth = 6.dp
                        )
                        
                        // 中心图标和进度
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = achievement.icon,
                                contentDescription = achievement.title,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    // 右侧信息区域
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 等级徽章
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = achievement.level,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "完成",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        // 标题
                        Text(
                            text = achievement.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        // 副标题
                        Text(
                            text = achievement.subtitle,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                        
                        // 激励语句
                        Text(
                            text = when (achievement.category) {
                                "STUDY" -> "知识改变命运，坚持铸就辉煌！"
                                "EXERCISE" -> "强健的体魄是成功的基石"
                                "MONEY" -> "财富自由的路上稳步前行"
                                else -> "持续努力，终将收获成功！"
                            },
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 用户等级卡片 - 显示用户等级和升级要求
 */
@Composable
fun UserLevelCard(
    data: UserLevelCardData,
    animationDelay: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }
    
    val expProgress = data.userLevel.currentExp.toFloat() / data.userLevel.maxExp
    val animatedExpProgress by animateFloatAsState(
        targetValue = if (isVisible) expProgress else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "exp_progress"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(500))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = data.userLevel.gradientColors,
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 左侧用户等级区域（增大占比）
                    Row(
                        modifier = Modifier.weight(1.2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 等级图标和进度条
                        Box(
                            modifier = Modifier.size(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // 经验值进度圆环
                            androidx.compose.material3.CircularProgressIndicator(
                                progress = { animatedExpProgress },
                                modifier = Modifier.size(50.dp),
                                color = Color.White,
                                strokeWidth = 3.dp,
                                trackColor = Color.White.copy(alpha = 0.3f),
                            )
                            
                            // 中心图标
                            Icon(
                                imageVector = data.userLevel.icon,
                                contentDescription = data.userLevel.title,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // 称号和经验值信息
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = data.userLevel.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Text(
                                text = "${data.userLevel.currentExp}/${data.userLevel.maxExp}经验值",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // 中间分割线
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                    
                    // 右侧升级条件（缩小占比）
                    Column(
                        modifier = Modifier.weight(0.8f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        data.upgradeRequirements.take(2).forEach { requirement ->
                            CompactUpgradeRequirementItem(requirement = requirement)
                        }
                    }
                }
            }
        }
    }
}


/**
 * 紧凑升级条件项组件 - 用于首页右侧
 */
@Composable
fun CompactUpgradeRequirementItem(
    requirement: UpgradeRequirement,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) requirement.progress.coerceAtMost(1f) else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "compact_requirement_progress"
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // 升级条件名称和进度
        Text(
            text = "${requirement.name} ${requirement.currentValue}/${requirement.targetValue}${requirement.unit}",
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color.White.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(Color.White.copy(alpha = 0.9f))
            )
        }
    }
}

/**
 * 等级升级卡片（子打卡界面展示） - 显示升级条件和进度
 */
@Composable
fun LevelUpgradeCard(
    currentLevel: String,
    nextLevel: String,
    requirements: List<UpgradeRequirement>,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 30 },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(500))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = gradientColors.map { it.copy(alpha = 0.9f) },
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 标题
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "当前等级：$currentLevel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "下一等级：$nextLevel",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // 升级条件
                    Text(
                        text = "升级条件",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    requirements.forEach { requirement ->
                        UpgradeRequirementItem(requirement = requirement)
                    }
                }
            }
        }
    }
}

/**
 * 升级条件数据类
 */
data class UpgradeRequirement(
    val name: String,
    val currentValue: Int,
    val targetValue: Int,
    val unit: String,
    val progress: Float = currentValue.toFloat() / targetValue
)

/**
 * 升级条件项组件
 */
@Composable
fun UpgradeRequirementItem(
    requirement: UpgradeRequirement,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) requirement.progress.coerceAtMost(1f) else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "requirement_progress"
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 条件名称和进度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = requirement.name,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${requirement.currentValue}/${requirement.targetValue}${requirement.unit}",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
        }
        
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(Color.White)
            )
        }
    }
}

/**
 * 获取学习类成就
 */
fun getStudyAchievements(): List<AchievementData> {
    return listOf(
        AchievementData(
            title = "初学者",
            subtitle = "完成第一次学习打卡\n学习的旅程从这里开始！",
            progress = 1.0f,
            level = "青铜",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFF8BC34A), Color(0xFF4CAF50)),
            category = "STUDY"
        ),
        AchievementData(
            title = "学霸",
            subtitle = "连续学习7天\n坚持就是胜利的开始",
            progress = 0.75f,
            level = "白银",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFF2196F3), Color(0xFF3F51B5)),
            category = "STUDY"
        ),
        AchievementData(
            title = "学习标兵",
            subtitle = "累计学习100小时\n知识的海洋在等着你",
            progress = 0.65f,
            level = "黄金",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
            category = "STUDY"
        ),
        AchievementData(
            title = "学者",
            subtitle = "连续学习30天\n已经养成了良好的学习习惯",
            progress = 0.40f,
            level = "铂金",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFF9C27B0), Color(0xFF673AB7)),
            category = "STUDY"
        ),
        AchievementData(
            title = "学习大师",
            subtitle = "累计学习500小时\n你已经是真正的学习大师了！",
            progress = 0.33f,
            level = "钻石",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
            category = "STUDY"
        )
    )
}

/**
 * 获取运动类成就
 */
fun getExerciseAchievements(): List<AchievementData> {
    return listOf(
        AchievementData(
            title = "运动新手",
            subtitle = "完成第一次运动打卡\n健康生活从今天开始！",
            progress = 1.0f,
            level = "青铜",
            icon = Icons.Default.Favorite,
            gradientColors = listOf(Color(0xFF8BC34A), Color(0xFF4CAF50)),
            category = "EXERCISE"
        ),
        AchievementData(
            title = "健身达人",
            subtitle = "连续运动7天\n坚持运动，身体倍棒",
            progress = 0.80f,
            level = "白银",
            icon = Icons.Default.Favorite,
            gradientColors = listOf(Color(0xFF2196F3), Color(0xFF3F51B5)),
            category = "EXERCISE"
        ),
        AchievementData(
            title = "运动健将",
            subtitle = "累计运动100次\n你的毅力令人敬佩",
            progress = 0.78f,
            level = "黄金",
            icon = Icons.Default.Favorite,
            gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
            category = "EXERCISE"
        ),
        AchievementData(
            title = "马拉松王者",
            subtitle = "连续运动30天\n强健的体魄是成功的基石",
            progress = 0.45f,
            level = "铂金",
            icon = Icons.Default.Favorite,
            gradientColors = listOf(Color(0xFF9C27B0), Color(0xFF673AB7)),
            category = "EXERCISE"
        )
    )
}

/**
 * 获取理财类成就
 */
fun getMoneyAchievements(): List<AchievementData> {
    return listOf(
        AchievementData(
            title = "储蓄新手",
            subtitle = "完成第一次理财打卡\n财富积累的第一步！",
            progress = 1.0f,
            level = "青铜",
            icon = Icons.Default.Home,
            gradientColors = listOf(Color(0xFF8BC34A), Color(0xFF4CAF50)),
            category = "MONEY"
        ),
        AchievementData(
            title = "投资高手",
            subtitle = "连续储蓄7天\n小钱也能变大钱",
            progress = 0.85f,
            level = "白银",
            icon = Icons.Default.Home,
            gradientColors = listOf(Color(0xFF2196F3), Color(0xFF3F51B5)),
            category = "MONEY"
        ),
        AchievementData(
            title = "理财专家",
            subtitle = "累计储蓄10000元\n财富自由的路上稳步前行",
            progress = 0.95f,
            level = "黄金",
            icon = Icons.Default.Home,
            gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
            category = "MONEY"
        ),
        AchievementData(
            title = "财富自由",
            subtitle = "连续理财100天\n距离财富自由越来越近了",
            progress = 0.30f,
            level = "钻石",
            icon = Icons.Default.Home,
            gradientColors = listOf(Color(0xFF43A047), Color(0xFF2E7D32)),
            category = "MONEY"
        )
    )
}

/**
 * 自定义圆形进度条
 */
@Composable
fun CircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = color.copy(alpha = 0.2f),
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp
) {
    Canvas(modifier = modifier) {
        drawCircularProgress(
            progress = progress,
            color = color,
            backgroundColor = backgroundColor,
            strokeWidth = strokeWidth.toPx()
        )
    }
}

private fun DrawScope.drawCircularProgress(
    progress: Float,
    color: Color,
    backgroundColor: Color,
    strokeWidth: Float
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = (size.minDimension - strokeWidth) / 2
    
    // 背景圆环
    drawCircle(
        color = backgroundColor,
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // 进度圆弧
    if (progress > 0) {
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(center.x - radius, center.y - radius)
        )
    }
}

/**
 * 当日任务列表 - 替换原来的成就网格
 */
@Composable
fun DailyTasksList(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            DailyTaskItem(task = task)
        }
    }
}

/**
 * 当日任务项组件
 */
@Composable
fun DailyTaskItem(
    task: Task,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) task.progress.coerceAtMost(1f) else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "daily_task_progress"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 30 },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(500))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 任务标题和状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 完成状态图标
                    if (task.progress >= 1f) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "已完成",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 进度信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "进度：${task.currentValue}/${task.targetValue}${task.unit}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = "${(task.progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
} 