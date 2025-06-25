package com.example.cur_app.domain.usecase

import com.example.cur_app.data.repository.AchievementRepository
import com.example.cur_app.data.repository.PreferencesRepository
import com.example.cur_app.data.database.entities.UserAchievementEntity
import com.example.cur_app.data.local.entity.CheckInType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 成就系统业务逻辑用例
 */
@Singleton
class AchievementUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val preferencesRepository: PreferencesRepository
) {
    
    /**
     * 升级要求数据类
     */
    data class UpgradeRequirement(
        val nextLevel: String,
        val currentExp: Int,
        val requiredExp: Int,
        val progress: Float
    )
    
    /**
     * 成就定义数据类
     */
    data class AchievementDefinition(
        val id: String,
        val title: String,
        val description: String,
        val icon: String,
        val category: CheckInType
    )
    
    /**
     * 成就数据类
     */
    data class AchievementData(
        val definition: AchievementDefinition,
        val isCompleted: Boolean,
        val progress: Float,
        val currentValue: Int,
        val targetValue: Int
    )
    
    /**
     * 获取当前用户成就
     */
    suspend fun getCurrentUserAchievement(category: CheckInType): UserAchievementEntity? {
        val userId = preferencesRepository.getUserId()
        return achievementRepository.getUserAchievement(userId, category)
    }
    
    /**
     * 获取当前用户所有成就
     */
    suspend fun getCurrentUserAchievements(): Flow<List<UserAchievementEntity>> {
        val userId = preferencesRepository.getUserId()
        return achievementRepository.getUserAchievements(userId)
    }
    
    /**
     * 初始化当前用户成就数据
     */
    suspend fun initializeCurrentUserAchievements() {
        val userId = preferencesRepository.getUserId()
        achievementRepository.initializeUserAchievements(userId)
    }
    
    /**
     * 获取升级要求
     */
    suspend fun getUpgradeRequirement(category: CheckInType): UpgradeRequirement? {
        val achievement = getCurrentUserAchievement(category) ?: return null
        val levelDefs = achievementRepository.getLevelDefinitions(category)
        val currentLevelIndex = achievement.levelIndex
        
        if (currentLevelIndex >= levelDefs.size - 1) {
            // 已达到最高等级
            return UpgradeRequirement(
                nextLevel = "已达到最高等级",
                currentExp = achievement.currentExp,
                requiredExp = levelDefs.last().expThreshold,
                progress = 1.0f
            )
        }
        
        val nextLevel = levelDefs[currentLevelIndex + 1]
        val currentExp = achievement.currentExp
        val requiredExp = nextLevel.expThreshold
        val progress = if (requiredExp > 0) currentExp.toFloat() / requiredExp else 1.0f
        
        return UpgradeRequirement(
            nextLevel = nextLevel.title,
            currentExp = currentExp,
            requiredExp = requiredExp,
            progress = progress.coerceAtMost(1.0f)
        )
    }
    
    /**
     * 处理经验值增加和升级
     */
    suspend fun processExperienceGain(category: CheckInType, expGain: Int): Boolean {
        val userId = preferencesRepository.getUserId()
        return achievementRepository.addExperience(userId, category, expGain)
    }
    
    /**
     * 获取等级定义
     */
    suspend fun getLevelDefinitions(category: CheckInType): List<AchievementRepository.LevelDefinition> {
        return achievementRepository.getLevelDefinitions(category)
    }
    
    /**
     * 根据打卡完成度计算经验值
     */
    fun calculateExperience(completionRate: Float, baseExp: Int = 50): Int {
        return (baseExp * completionRate).toInt().coerceAtLeast(10)
    }
}