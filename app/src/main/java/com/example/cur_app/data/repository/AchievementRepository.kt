 package com.example.cur_app.data.repository

import com.example.cur_app.data.database.HabitTrackerDatabase
import com.example.cur_app.data.database.entities.UserAchievementEntity
import com.example.cur_app.data.database.entities.AchievementProgressEntity
import com.example.cur_app.data.local.entity.CheckInType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 成就系统仓库
 */
@Singleton
class AchievementRepository @Inject constructor(
    private val database: HabitTrackerDatabase
) {
    
    /**
     * 等级定义
     */
    data class LevelDefinition(
        val levelIndex: Int,
        val title: String,
        val expThreshold: Int,
        val icon: String,
        val description: String
    )
    
    /**
     * 学习类型等级定义
     */
    val studyLevels = listOf(
        LevelDefinition(0, "学习新手", 0, "🌱", "刚开始学习之旅"),
        LevelDefinition(1, "学习达人", 500, "📚", "坚持学习，持续进步"),
        LevelDefinition(2, "学霸", 1500, "🎓", "学习成果显著"),
        LevelDefinition(3, "知识大师", 3000, "🧠", "知识渊博，学习高手"),
        LevelDefinition(4, "智慧导师", 5000, "👨‍🏫", "智慧如海，引领他人")
    )
    
    /**
     * 运动类型等级定义
     */
    val exerciseLevels = listOf(
        LevelDefinition(0, "运动新手", 0, "🚶", "开始健康生活"),
        LevelDefinition(1, "健身爱好者", 500, "🏃", "坚持运动，活力满满"),
        LevelDefinition(2, "运动达人", 1500, "💪", "运动成效显著"),
        LevelDefinition(3, "健身大师", 3000, "🏆", "身体素质超群"),
        LevelDefinition(4, "运动导师", 5000, "🥇", "健康典范，激励他人")
    )
    
    /**
     * 理财类型等级定义
     */
    val moneyLevels = listOf(
        LevelDefinition(0, "储蓄新手", 0, "🐷", "开始理财规划"),
        LevelDefinition(1, "理财爱好者", 500, "💰", "培养理财习惯"),
        LevelDefinition(2, "投资达人", 1500, "📈", "理财观念成熟"),
        LevelDefinition(3, "财富大师", 3000, "💎", "财富管理高手"),
        LevelDefinition(4, "理财导师", 5000, "👑", "财富自由，指导他人")
    )
    
    /**
     * 获取等级定义（优先从数据库获取，如果为空则使用默认值）
     */
    suspend fun getLevelDefinitions(type: CheckInType): List<LevelDefinition> {
        return try {
            val dbLevels = database.levelDefinitionDao().getLevelsByCategory(type.name)
            if (dbLevels.isNotEmpty()) {
                // 从数据库转换
                dbLevels.map { entity ->
                    LevelDefinition(
                        levelIndex = entity.levelIndex,
                        title = entity.title,
                        expThreshold = entity.expThreshold,
                        icon = entity.icon,
                        description = entity.description
                    )
                }
            } else {
                // 使用默认值并初始化数据库
                val defaultLevels = getDefaultLevelDefinitions(type)
                initializeDefaultLevels(type, defaultLevels)
                defaultLevels
            }
        } catch (e: Exception) {
            // 数据库出错时使用默认值
            getDefaultLevelDefinitions(type)
        }
    }
    
    /**
     * 获取默认等级定义
     */
    fun getDefaultLevelDefinitions(type: CheckInType): List<LevelDefinition> {
        return when (type) {
            CheckInType.STUDY -> studyLevels
            CheckInType.EXERCISE -> exerciseLevels
            CheckInType.MONEY -> moneyLevels
        }
    }
    
    /**
     * 获取用户成就信息
     */
    suspend fun getUserAchievement(userId: String, category: CheckInType): UserAchievementEntity? {
        return database.userAchievementDao().getUserAchievement(userId, category.name)
    }
    
    /**
     * 获取用户所有成就信息
     */
    fun getUserAchievements(userId: String): Flow<List<UserAchievementEntity>> {
        return database.userAchievementDao().getUserAchievements(userId)
    }
    
    /**
     * 初始化用户成就数据
     */
    suspend fun initializeUserAchievements(userId: String) {
        CheckInType.values().forEach { category ->
            val existing = getUserAchievement(userId, category)
            if (existing == null) {
                val userAchievement = UserAchievementEntity(
                    userId = userId,
                    category = category.name,
                    currentLevel = getLevelDefinitions(category)[0].title,
                    currentExp = 0,
                    levelIndex = 0,
                    totalStudyTime = 0,
                    totalExerciseTime = 0,
                    totalMoney = 0.0,
                    totalCheckInDays = 0,
                    currentStreak = 0,
                    maxStreak = 0
                )
                database.userAchievementDao().insertUserAchievement(userAchievement)
            }
        }
    }
    
    /**
     * 增加经验值并检查升级
     */
    suspend fun addExperience(userId: String, category: CheckInType, expGain: Int): Boolean {
        val currentAchievement = getUserAchievement(userId, category) ?: return false
        val newExp = currentAchievement.currentExp + expGain
        
        // 检查是否升级
        val levelDefs = getLevelDefinitions(category)
        var newLevelIndex = currentAchievement.levelIndex
        var newLevelTitle = currentAchievement.currentLevel
        
        // 查找新等级
        for (i in (currentAchievement.levelIndex + 1) until levelDefs.size) {
            if (newExp >= levelDefs[i].expThreshold) {
                newLevelIndex = i
                newLevelTitle = levelDefs[i].title
            } else {
                break
            }
        }
        
        database.userAchievementDao().updateExperience(userId, category.name, newExp)
        
        if (newLevelIndex > currentAchievement.levelIndex) {
            database.userAchievementDao().updateLevel(userId, category.name, newLevelTitle, newLevelIndex)
            return true // 升级了
        }
        
        return false // 没有升级
    }
    
    /**
     * 更新学习时间统计
     */
    suspend fun updateStudyTime(userId: String, additionalMinutes: Int) {
        database.userAchievementDao().updateStudyTime(userId, CheckInType.STUDY.name, additionalMinutes)
    }
    
    /**
     * 更新运动时间统计
     */
    suspend fun updateExerciseTime(userId: String, additionalMinutes: Int) {
        database.userAchievementDao().updateExerciseTime(userId, CheckInType.EXERCISE.name, additionalMinutes)
    }
    
    /**
     * 更新储蓄金额统计（传入分，转换为元）
     */
    suspend fun updateMoney(userId: String, additionalCents: Int) {
        val additionalYuan = additionalCents / 100.0
        database.userAchievementDao().updateMoney(userId, CheckInType.MONEY.name, additionalYuan)
    }
    
    /**
     * 更新打卡统计（连续天数等）
     */
    suspend fun updateCheckInStats(userId: String, category: CheckInType) {
        // 增加总打卡天数
        database.userAchievementDao().updateCheckInDays(userId, category.name, 1)
        
        // 更新连续打卡天数的逻辑可以在这里实现
        // 目前简化处理，获取当前连续天数并增加
        val currentAchievement = getUserAchievement(userId, category)
        if (currentAchievement != null) {
            val newStreak = currentAchievement.currentStreak + 1
            val newMaxStreak = maxOf(newStreak, currentAchievement.maxStreak)
            database.userAchievementDao().updateStreak(userId, category.name, newStreak, newMaxStreak)
        }
    }
    
    /**
     * 初始化默认等级到数据库
     */
    suspend fun initializeDefaultLevels(type: CheckInType, defaultLevels: List<LevelDefinition>) {
        try {
            val entities = defaultLevels.map { level ->
                com.example.cur_app.data.database.entities.LevelDefinitionEntity(
                    category = type.name,
                    levelIndex = level.levelIndex,
                    title = level.title,
                    expThreshold = level.expThreshold,
                    icon = level.icon,
                    description = level.description,
                    isDefault = true
                )
            }
            database.levelDefinitionDao().insertLevels(entities)
        } catch (e: Exception) {
            // 初始化失败不抛出异常，继续使用默认值
        }
    }
    
    /**
     * 初始化所有类型的默认等级定义
     */
    suspend fun initializeAllDefaultLevels() {
        try {
            val hasDefaults = database.levelDefinitionDao().hasDefaultLevels() > 0
            if (!hasDefaults) {
                CheckInType.values().forEach { type ->
                    val defaultLevels = getDefaultLevelDefinitions(type)
                    initializeDefaultLevels(type, defaultLevels)
                }
            }
        } catch (e: Exception) {
            // 初始化失败时静默处理
        }
    }
}