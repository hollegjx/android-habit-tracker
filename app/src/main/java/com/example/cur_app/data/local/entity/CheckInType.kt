package com.example.cur_app.data.local.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cur_app.ui.theme.AchievementBlue
import com.example.cur_app.ui.theme.AchievementOrange
import com.example.cur_app.ui.theme.AchievementGreen

/**
 * 打卡类型枚举
 * 支持学习、运动、攒钱三种不同的打卡类型
 */
enum class CheckInType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val colorString: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val description: String
) {
    STUDY(
        displayName = "学习",
        icon = Icons.Default.Star,
        color = AchievementBlue,
        colorString = "#667EEA",
        gradientStart = Color(0xFF667EEA),
        gradientEnd = Color(0xFF764BA2),
        description = "知识积累，每日进步"
    ),
    
    EXERCISE(
        displayName = "运动",
        icon = Icons.Default.Favorite,
        color = AchievementOrange,
        colorString = "#FF7043",
        gradientStart = Color(0xFFFF7043),
        gradientEnd = Color(0xFFFF5722),
        description = "强健体魄，活力满满"
    ),
    
    MONEY(
        displayName = "攒钱",
        icon = Icons.Default.Home,
        color = AchievementGreen,
        colorString = "#43A047",
        gradientStart = Color(0xFF43A047),
        gradientEnd = Color(0xFF2E7D32),
        description = "理财有道，财富增长"
    );
    
    companion object {
        fun fromString(type: String): CheckInType {
            return values().find { it.name.equals(type, ignoreCase = true) } ?: STUDY
        }
    }
}

/**
 * 打卡记录数据类
 */
data class CheckInRecord(
    val id: Long = 0,
    val type: CheckInType,
    val title: String,
    val value: String, // 学习时长/运动时长/攒钱金额
    val unit: String, // 分钟/千卡/元
    val date: String,
    val isCompleted: Boolean = false,
    val note: String = ""
)

/**
 * 打卡统计数据类
 */
data class CheckInStats(
    val type: CheckInType,
    val todayValue: String,
    val totalValue: String,
    val streakDays: Int,
    val completionRate: Float,
    val weeklyData: List<Float> = emptyList()
) {
    companion object {
        /**
         * 从Repository的统计数据转换为UI使用的CheckInStats
         */
        fun fromTypeStats(stats: com.example.cur_app.data.repository.CheckInRepository.CheckInTypeStats): CheckInStats {
            return CheckInStats(
                type = stats.type,
                todayValue = stats.totalActualValue.toString(),
                totalValue = "${stats.completedToday}/${stats.totalToday}",
                streakDays = stats.currentStreak,
                completionRate = stats.completionRate,
                weeklyData = emptyList() // 这里可以后续扩展
            )
        }
    }
} 