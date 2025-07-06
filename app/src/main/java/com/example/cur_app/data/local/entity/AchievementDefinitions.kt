package com.example.cur_app.data.local.entity

import com.example.cur_app.data.local.entity.CheckInType

/**
 * 成就定义静态配置类
 * 集中管理所有成就和等级的定义
 */
object AchievementDefinitions {
    
    // ============ 等级定义 ============
    
    /**
     * 等级数据类
     */
    data class LevelDefinition(
        val levelIndex: Int,
        val title: String,
        val expThreshold: Int,
        val icon: String,
        val description: String,
        val color: String = "#6650a4"
    )
    
    /**
     * 学习类型等级定义
     */
    val STUDY_LEVELS = listOf(
        LevelDefinition(0, "学习新手", 0, "🌱", "刚开始学习之旅", "#4CAF50"),
        LevelDefinition(1, "学习达人", 500, "📚", "坚持学习，持续进步", "#2196F3"),
        LevelDefinition(2, "学霸", 1500, "🎓", "学习成果显著", "#9C27B0"),
        LevelDefinition(3, "知识大师", 3000, "🧠", "知识渊博，学习高手", "#FF9800"),
        LevelDefinition(4, "智慧导师", 5000, "👨‍🏫", "智慧如海，引领他人", "#F44336")
    )
    
    /**
     * 运动类型等级定义
     */
    val EXERCISE_LEVELS = listOf(
        LevelDefinition(0, "运动新手", 0, "🚶", "开始健康生活", "#4CAF50"),
        LevelDefinition(1, "健身爱好者", 500, "🏃", "坚持运动，活力满满", "#2196F3"),
        LevelDefinition(2, "运动达人", 1500, "💪", "运动成效显著", "#9C27B0"),
        LevelDefinition(3, "健身大师", 3000, "🏆", "身体素质超群", "#FF9800"),
        LevelDefinition(4, "运动导师", 5000, "🥇", "健康典范，激励他人", "#F44336")
    )
    
    /**
     * 理财类型等级定义
     */
    val MONEY_LEVELS = listOf(
        LevelDefinition(0, "储蓄新手", 0, "🐷", "开始理财规划", "#4CAF50"),
        LevelDefinition(1, "理财爱好者", 500, "💰", "培养理财习惯", "#2196F3"),
        LevelDefinition(2, "投资达人", 1500, "📈", "理财观念成熟", "#9C27B0"),
        LevelDefinition(3, "财富大师", 3000, "💎", "财富管理高手", "#FF9800"),
        LevelDefinition(4, "理财导师", 5000, "👑", "财富自由，指导他人", "#F44336")
    )
    
    /**
     * 获取等级定义
     */
    fun getLevels(type: CheckInType): List<LevelDefinition> {
        return when (type) {
            CheckInType.STUDY -> STUDY_LEVELS
            CheckInType.EXERCISE -> EXERCISE_LEVELS
            CheckInType.MONEY -> MONEY_LEVELS
        }
    }
    
    // ============ 成就定义 ============
    
    /**
     * 成就类型枚举
     */
    enum class AchievementType {
        FIRST_COMPLETE,    // 首次完成
        CONSECUTIVE_DAYS,  // 连续天数
        TOTAL_COUNT,       // 总次数
        TOTAL_TIME,        // 总时长
        TOTAL_AMOUNT,      // 总金额
        PERFECT_WEEK,      // 完美一周
        MONTHLY_GOAL       // 月度目标
    }
    
    /**
     * 成就定义数据类
     */
    data class AchievementDefinition(
        val id: String,
        val title: String,
        val description: String,
        val icon: String,
        val type: AchievementType,
        val category: CheckInType,
        val targetValue: Int,
        val difficulty: String, // 青铜、白银、黄金、铂金、钻石
        val color: String,
        val expReward: Int = 100
    )
    
    /**
     * 学习类成就定义
     */
    val STUDY_ACHIEVEMENTS = listOf(
        AchievementDefinition("study_first", "学习起航", "完成第一次学习打卡", "🎯", AchievementType.FIRST_COMPLETE, CheckInType.STUDY, 1, "青铜", "#CD7F32", 50),
        AchievementDefinition("study_streak_7", "学习专注", "连续学习7天", "🔥", AchievementType.CONSECUTIVE_DAYS, CheckInType.STUDY, 7, "白银", "#C0C0C0", 100),
        AchievementDefinition("study_streak_30", "学习恒心", "连续学习30天", "⭐", AchievementType.CONSECUTIVE_DAYS, CheckInType.STUDY, 30, "黄金", "#FFD700", 200),
        AchievementDefinition("study_count_100", "学习达人", "累计学习100次", "📚", AchievementType.TOTAL_COUNT, CheckInType.STUDY, 100, "铂金", "#E5E4E2", 300),
        AchievementDefinition("study_time_1000", "时间大师", "累计学习1000分钟", "⏰", AchievementType.TOTAL_TIME, CheckInType.STUDY, 1000, "钻石", "#B9F2FF", 500)
    )
    
    /**
     * 运动类成就定义
     */
    val EXERCISE_ACHIEVEMENTS = listOf(
        AchievementDefinition("exercise_first", "运动起步", "完成第一次运动打卡", "🎯", AchievementType.FIRST_COMPLETE, CheckInType.EXERCISE, 1, "青铜", "#CD7F32", 50),
        AchievementDefinition("exercise_streak_7", "运动习惯", "连续运动7天", "🔥", AchievementType.CONSECUTIVE_DAYS, CheckInType.EXERCISE, 7, "白银", "#C0C0C0", 100),
        AchievementDefinition("exercise_streak_30", "运动坚持", "连续运动30天", "⭐", AchievementType.CONSECUTIVE_DAYS, CheckInType.EXERCISE, 30, "黄金", "#FFD700", 200),
        AchievementDefinition("exercise_count_100", "健身达人", "累计运动100次", "💪", AchievementType.TOTAL_COUNT, CheckInType.EXERCISE, 100, "铂金", "#E5E4E2", 300),
        AchievementDefinition("exercise_time_1000", "运动专家", "累计运动1000分钟", "⏱️", AchievementType.TOTAL_TIME, CheckInType.EXERCISE, 1000, "钻石", "#B9F2FF", 500)
    )
    
    /**
     * 理财类成就定义
     */
    val MONEY_ACHIEVEMENTS = listOf(
        AchievementDefinition("money_first", "理财开始", "完成第一次理财打卡", "🎯", AchievementType.FIRST_COMPLETE, CheckInType.MONEY, 1, "青铜", "#CD7F32", 50),
        AchievementDefinition("money_streak_7", "理财习惯", "连续理财7天", "🔥", AchievementType.CONSECUTIVE_DAYS, CheckInType.MONEY, 7, "白银", "#C0C0C0", 100),
        AchievementDefinition("money_streak_30", "理财坚持", "连续理财30天", "⭐", AchievementType.CONSECUTIVE_DAYS, CheckInType.MONEY, 30, "黄金", "#FFD700", 200),
        AchievementDefinition("money_count_100", "理财达人", "累计理财100次", "💰", AchievementType.TOTAL_COUNT, CheckInType.MONEY, 100, "铂金", "#E5E4E2", 300),
        AchievementDefinition("money_amount_10000", "财富积累", "累计理财10000元", "💎", AchievementType.TOTAL_AMOUNT, CheckInType.MONEY, 10000, "钻石", "#B9F2FF", 500)
    )
    
    /**
     * 获取成就定义
     */
    fun getAchievements(type: CheckInType): List<AchievementDefinition> {
        return when (type) {
            CheckInType.STUDY -> STUDY_ACHIEVEMENTS
            CheckInType.EXERCISE -> EXERCISE_ACHIEVEMENTS
            CheckInType.MONEY -> MONEY_ACHIEVEMENTS
        }
    }
    
    /**
     * 获取所有成就定义
     */
    fun getAllAchievements(): List<AchievementDefinition> {
        return STUDY_ACHIEVEMENTS + EXERCISE_ACHIEVEMENTS + MONEY_ACHIEVEMENTS
    }
    
    /**
     * 根据ID获取成就定义
     */
    fun getAchievementById(id: String): AchievementDefinition? {
        return getAllAchievements().find { it.id == id }
    }
}