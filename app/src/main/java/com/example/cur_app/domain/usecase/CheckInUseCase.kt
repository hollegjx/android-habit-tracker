package com.example.cur_app.domain.usecase

import com.example.cur_app.data.database.entities.CheckInItemEntity
import com.example.cur_app.data.database.entities.CheckInRecordEntity
import com.example.cur_app.data.local.entity.CheckInType
import com.example.cur_app.data.repository.CheckInRepository
import com.example.cur_app.data.repository.PreferencesRepository
import com.example.cur_app.data.repository.AchievementRepository
import com.example.cur_app.domain.usecase.AchievementUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 打卡业务逻辑处理类
 * 封装与打卡相关的复杂业务逻辑，调用Repository进行数据操作
 */
@Singleton
class CheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val preferencesRepository: PreferencesRepository,
    private val achievementRepository: AchievementRepository,
    private val achievementUseCase: AchievementUseCase
) {
    
    companion object {
        private const val TAG = "CheckInUseCase"
    }

    // ============ 数据类定义 ============
    
    /**
     * 今日总览数据
     */
    data class TodayOverview(
        val totalActiveItems: Int,
        val completedItems: Int,
        val completionRate: Float,
        val typeStats: List<TypeCompletionStat>
    )
    
    /**
     * 类型完成统计
     */
    data class TypeCompletionStat(
        val type: CheckInType,
        val completed: Int,
        val total: Int
    ) {
        val completionRate: Float
            get() = if (total > 0) completed.toFloat() / total else 0f
    }
    
    /**
     * 打卡完成结果
     */
    data class CheckInCompletionResult(
        val success: Boolean,
        val message: String,
        val isNewAchievement: Boolean = false,
        val achievementMessage: String? = null
    )

    // ============ 项目管理 ============

    /**
     * 创建新打卡项目
     */
    suspend fun createCheckInItem(
        type: CheckInType,
        title: String,
        description: String = "",
        targetValue: Int = 1,
        unit: String = "次",
        icon: String = "⭐",
        color: String = "#6650a4"
    ): CheckInCompletionResult {
        return try {
            // 输入验证
            if (title.isBlank()) {
                return CheckInCompletionResult(false, "项目标题不能为空")
            }
            
            if (targetValue <= 0) {
                return CheckInCompletionResult(false, "目标数值必须大于0")
            }
            
            val result = checkInRepository.createCheckInItem(
                type = type,
                title = title,
                description = description,
                targetValue = targetValue,
                unit = unit,
                icon = icon,
                color = color
            )
            
            if (result.isSuccess) {
                CheckInCompletionResult(true, "创建项目成功！")
            } else {
                CheckInCompletionResult(false, "创建项目失败: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            CheckInCompletionResult(false, "创建项目失败: ${e.message}")
        }
    }

    /**
     * 删除打卡项目
     */
    suspend fun deleteCheckInItem(itemId: Long): CheckInCompletionResult {
        return try {
            val result = checkInRepository.deleteCheckInItem(itemId)
            
            if (result.isSuccess) {
                CheckInCompletionResult(true, "删除项目成功！")
            } else {
                CheckInCompletionResult(false, "删除项目失败: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            CheckInCompletionResult(false, "删除项目失败: ${e.message}")
        }
    }

    // ============ 打卡操作 ============

    /**
     * 完成今日打卡
     */
    suspend fun completeCheckIn(
        itemId: Long,
        actualValue: Int,
        note: String = ""
    ): CheckInCompletionResult {
        return try {
            val result = checkInRepository.completeCheckInToday(
                itemId = itemId,
                actualValue = actualValue,
                note = note
            )
            
            if (result.isSuccess) {
                // 处理成就和经验值
                val achievementResult = processAchievementsAndExperience(itemId, actualValue)
                
                CheckInCompletionResult(
                    success = true,
                    message = "打卡成功！",
                    isNewAchievement = achievementResult.isNotEmpty(),
                    achievementMessage = achievementResult.takeIf { it.isNotEmpty() }
                )
            } else {
                CheckInCompletionResult(false, "打卡失败: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            CheckInCompletionResult(false, "打卡失败: ${e.message}")
        }
    }

    /**
     * 取消今日打卡
     */
    suspend fun cancelCheckIn(itemId: Long): CheckInCompletionResult {
        return try {
            val result = checkInRepository.uncompleteCheckInToday(itemId)
            
            if (result.isSuccess) {
                CheckInCompletionResult(true, "取消打卡成功！")
            } else {
                CheckInCompletionResult(false, "取消打卡失败: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            CheckInCompletionResult(false, "取消打卡失败: ${e.message}")
        }
    }

    // ============ 数据查询 ============

    /**
     * 获取指定类型的项目（包含今日状态）
     */
    fun getItemsWithTodayStatusByType(type: CheckInType): Flow<List<CheckInRepository.CheckInItemWithTodayStatus>> {
        return checkInRepository.getItemsWithTodayStatusByType(type)
    }

    /**
     * 获取类型统计信息
     */
    suspend fun getTypeStats(type: CheckInType): CheckInRepository.CheckInTypeStats {
        return checkInRepository.getTypeStats(type)
    }

    /**
     * 获取今日总览
     */
    suspend fun getTodayOverview(): TodayOverview {
        // 这里可以聚合多个类型的数据
        val studyStats = checkInRepository.getTypeStats(CheckInType.STUDY)
        val exerciseStats = checkInRepository.getTypeStats(CheckInType.EXERCISE)
        val moneyStats = checkInRepository.getTypeStats(CheckInType.MONEY)
        
        val totalActiveItems = studyStats.totalToday + exerciseStats.totalToday + moneyStats.totalToday
        val completedItems = studyStats.completedToday + exerciseStats.completedToday + moneyStats.completedToday
        val completionRate = if (totalActiveItems > 0) completedItems.toFloat() / totalActiveItems else 0f
        
        return TodayOverview(
            totalActiveItems = totalActiveItems,
            completedItems = completedItems,
            completionRate = completionRate,
            typeStats = listOf(
                TypeCompletionStat(CheckInType.STUDY, studyStats.completedToday, studyStats.totalToday),
                TypeCompletionStat(CheckInType.EXERCISE, exerciseStats.completedToday, exerciseStats.totalToday),
                TypeCompletionStat(CheckInType.MONEY, moneyStats.completedToday, moneyStats.totalToday)
            )
        )
    }

    // ============ 初始化和默认数据 ============

    /**
     * 初始化默认打卡项目
     */
    suspend fun initializeDefaultItems(): CheckInCompletionResult {
        return try {
            // 学习类项目
            val studyItems = listOf(
                Triple("背单词", "每天记忆新单词，提升词汇量", 50),
                Triple("阅读", "培养阅读习惯，增长知识", 30),
                Triple("练字", "练习书法，提高写字水平", 20)
            )
            
            // 运动类项目
            val exerciseItems = listOf(
                Triple("跑步", "有氧运动，增强体质", 30),
                Triple("俯卧撑", "力量训练，增强上肢力量", 20)
            )
            
            // 储蓄类项目
            val moneyItems = listOf(
                Triple("日常储蓄", "每日储蓄，积少成多", 10),
                Triple("投资学习", "学习投资理财知识", 1)
            )
            
            // 创建所有默认项目
            val allCreated = mutableListOf<Boolean>()
            
            studyItems.forEach { (title, desc, target) ->
                val result = checkInRepository.createCheckInItem(
                    CheckInType.STUDY, title, desc, target,
                    if (title.contains("单词")) "个" else if (title.contains("阅读")) "分钟" else "分钟",
                    when {
                        title.contains("单词") -> "📖"
                        title.contains("阅读") -> "📚"
                        else -> "✍️"
                    },
                    "#6650a4"
                )
                allCreated.add(result.isSuccess)
            }
            
            exerciseItems.forEach { (title, desc, target) ->
                val result = checkInRepository.createCheckInItem(
                    CheckInType.EXERCISE, title, desc, target,
                    if (title.contains("跑步")) "分钟" else "个",
                    if (title.contains("跑步")) "🏃" else "💪",
                    "#e91e63"
                )
                allCreated.add(result.isSuccess)
            }
            
            moneyItems.forEach { (title, desc, target) ->
                val result = checkInRepository.createCheckInItem(
                    CheckInType.MONEY, title, desc, target,
                    if (title.contains("储蓄")) "元" else "次",
                    if (title.contains("储蓄")) "💰" else "📊",
                    "#ff9800"
                )
                allCreated.add(result.isSuccess)
            }
            
            val successCount = allCreated.count { it }
            val totalCount = allCreated.size
            
            if (successCount == totalCount) {
                CheckInCompletionResult(true, "成功创建 $totalCount 个默认项目！")
            } else {
                CheckInCompletionResult(false, "部分项目创建失败，成功 $successCount/$totalCount")
            }
            
        } catch (e: Exception) {
            CheckInCompletionResult(false, "初始化失败: ${e.message}")
        }
    }

    // ============ 私有辅助方法 ============

    /**
     * 处理成就和经验值计算
     * 从打卡记录累加统计数据并更新用户成就
     */
    private suspend fun processAchievementsAndExperience(itemId: Long, actualValue: Int): String {
        return try {
            Log.d(TAG, "处理打卡项目 $itemId 的成就和经验值，完成值: $actualValue")
            
            // 获取打卡项目信息以确定类型
            // 由于没有直接的getAllItemsWithTodayStatus方法，我们使用替代方案
            val allTypes = listOf(CheckInType.STUDY, CheckInType.EXERCISE, CheckInType.MONEY)
            var checkInType: CheckInType? = null
            var targetValue = 0
            var unit = ""
            
            // 在所有类型中查找该项目
            for (type in allTypes) {
                val items = checkInRepository.getItemsWithTodayStatusByType(type).first()
                val foundItem = items.find { it.item.id == itemId }
                if (foundItem != null) {
                    checkInType = type // 使用循环中的type，而不是foundItem.item.type
                    targetValue = foundItem.item.targetValue
                    unit = foundItem.item.unit
                    break
                }
            }
            
            if (checkInType == null) {
                return "打卡项目不存在"
            }
            
            // 计算完成度和经验值
            val completionRatio = (actualValue.toFloat() / targetValue).coerceAtMost(1.0f)
            val baseExp = when (checkInType!!) {
                CheckInType.STUDY -> 30    // 学习类基础经验
                CheckInType.EXERCISE -> 40 // 运动类基础经验  
                CheckInType.MONEY -> 20    // 理财类基础经验
            }
            
            val adjustedExp = achievementUseCase.calculateExperience(completionRatio, baseExp)
            
            // 处理经验值增加
            val levelUpHappened = achievementUseCase.processExperienceGain(checkInType!!, adjustedExp)
            
            // 更新统计数据（累计时间、金额等）
            updateStatisticsData(checkInType!!, actualValue, unit)
            
            Log.d(TAG, "成功处理成就数据: 类型=$checkInType, 经验值=$adjustedExp, 升级=$levelUpHappened")
            
            return if (levelUpHappened) {
                "🎉 恭喜升级！获得 $adjustedExp 经验值！"
            } else {
                "获得 $adjustedExp 经验值！"
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "处理成就和经验值时出错: ${e.message}", e)
            return ""
        }
    }
    
    /**
     * 更新统计数据（累计时间、金额等）
     */
    private suspend fun updateStatisticsData(type: CheckInType, actualValue: Int, unit: String) {
        try {
            val userId = preferencesRepository.getUserId()
            
            when (type) {
                CheckInType.STUDY -> {
                    if (unit.contains("分钟") || unit.contains("小时")) {
                        val minutes = if (unit.contains("小时")) actualValue * 60 else actualValue
                        achievementRepository.updateStudyTime(userId, minutes)
                    }
                }
                CheckInType.EXERCISE -> {
                    if (unit.contains("分钟") || unit.contains("小时")) {
                        val minutes = if (unit.contains("小时")) actualValue * 60 else actualValue
                        achievementRepository.updateExerciseTime(userId, minutes)
                    }
                }
                CheckInType.MONEY -> {
                    if (unit.contains("元")) {
                        achievementRepository.updateMoney(userId, actualValue * 100) // 转换为分存储
                    }
                }
            }
            Log.d(TAG, "更新统计数据成功: $type, $actualValue $unit")
        } catch (e: Exception) {
            Log.e(TAG, "更新统计数据失败: ${e.message}", e)
        }
    }
    
    /**
     * 获取当前日期字符串
     */
    private fun getCurrentDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
} 