package com.example.cur_app.domain.usecase

import com.example.cur_app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 偏好设置业务逻辑用例
 * 封装应用设置相关的复杂业务逻辑
 */
@Singleton
class PreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    
    // ========== 主题设置 ==========
    
    /**
     * 获取主题模式
     */
    fun getThemeMode(): Flow<String> {
        return preferencesRepository.themeMode
    }
    
    /**
     * 设置主题模式
     */
    suspend fun setThemeMode(mode: String): Result<Unit> {
        return try {
            val validModes = listOf("system", "light", "dark")
            if (mode !in validModes) {
                return Result.failure(IllegalArgumentException("无效的主题模式"))
            }
            
            preferencesRepository.saveThemeMode(mode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取当前主题显示名称
     */
    fun getThemeDisplayName(mode: String): String {
        return when (mode) {
            "system" -> "跟随系统"
            "light" -> "浅色模式"
            "dark" -> "深色模式"
            else -> "跟随系统"
        }
    }
    
    // ========== 通知设置 ==========
    
    /**
     * 获取通知开关状态
     */
    fun getNotificationsEnabled(): Flow<Boolean> {
        return preferencesRepository.notificationsEnabled
    }
    
    /**
     * 设置通知开关
     */
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            preferencesRepository.setNotificationsEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取提醒时间
     */
    suspend fun getReminderTime(): Result<ReminderTime> {
        return try {
            val (hour, minute) = preferencesRepository.getReminderTime()
            Result.success(ReminderTime(hour, minute))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设置提醒时间
     */
    suspend fun setReminderTime(hour: Int, minute: Int): Result<Unit> {
        return try {
            if (hour !in 0..23) {
                return Result.failure(IllegalArgumentException("小时必须在0-23之间"))
            }
            if (minute !in 0..59) {
                return Result.failure(IllegalArgumentException("分钟必须在0-59之间"))
            }
            
            preferencesRepository.saveReminderTime(hour, minute)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取格式化的提醒时间字符串
     */
    suspend fun getFormattedReminderTime(): Result<String> {
        return try {
            val (hour, minute) = preferencesRepository.getReminderTime()
            val timeString = String.format("%02d:%02d", hour, minute)
            Result.success(timeString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== TTS设置 ==========
    
    /**
     * 获取TTS开关状态
     */
    suspend fun getTtsEnabled(): Result<Boolean> {
        return try {
            val enabled = preferencesRepository.isTtsEnabled()
            Result.success(enabled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设置TTS开关
     */
    suspend fun setTtsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            preferencesRepository.setTtsEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取TTS语音速度
     */
    suspend fun getTtsVoiceSpeed(): Result<Float> {
        return try {
            val speed = preferencesRepository.getTtsVoiceSpeed()
            Result.success(speed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设置TTS语音速度
     */
    suspend fun setTtsVoiceSpeed(speed: Float): Result<Unit> {
        return try {
            if (speed !in 0.5f..2.0f) {
                return Result.failure(IllegalArgumentException("语音速度必须在0.5-2.0之间"))
            }
            
            preferencesRepository.saveTtsVoiceSpeed(speed)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取语音速度显示名称
     */
    fun getVoiceSpeedDisplayName(speed: Float): String {
        return when {
            speed <= 0.75f -> "慢速"
            speed <= 1.25f -> "正常"
            else -> "快速"
        }
    }
    
    // ========== 目标设置 ==========
    
    /**
     * 获取连续天数目标
     */
    suspend fun getStreakGoal(): Result<Int> {
        return try {
            val goal = preferencesRepository.getStreakGoal()
            Result.success(goal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设置连续天数目标
     */
    suspend fun setStreakGoal(goal: Int): Result<Unit> {
        return try {
            if (goal !in 1..365) {
                return Result.failure(IllegalArgumentException("连续天数目标必须在1-365之间"))
            }
            
            preferencesRepository.saveStreakGoal(goal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取每周目标
     */
    suspend fun getWeeklyGoal(): Result<Int> {
        return try {
            val goal = preferencesRepository.getWeeklyGoal()
            Result.success(goal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设置每周目标
     */
    suspend fun setWeeklyGoal(goal: Int): Result<Unit> {
        return try {
            if (goal !in 1..7) {
                return Result.failure(IllegalArgumentException("每周目标必须在1-7之间"))
            }
            
            preferencesRepository.saveWeeklyGoal(goal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 隐私设置 ==========
    
    /**
     * 获取统计隐私设置
     */
    suspend fun getStatsPrivacy(): Result<Boolean> {
        return try {
            val isPrivate = preferencesRepository.isStatsPrivate()
            Result.success(isPrivate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设置统计隐私
     */
    suspend fun setStatsPrivacy(private: Boolean): Result<Unit> {
        return try {
            preferencesRepository.setStatsPrivacy(private)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取数据同步设置
     */
    suspend fun getDataSyncEnabled(): Result<Boolean> {
        return try {
            val enabled = preferencesRepository.isDataSyncEnabled()
            Result.success(enabled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设置数据同步
     */
    suspend fun setDataSyncEnabled(enabled: Boolean): Result<Unit> {
        return try {
            preferencesRepository.setDataSyncEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 应用状态管理 ==========
    
    /**
     * 检查是否首次启动
     */
    suspend fun isFirstLaunch(): Result<Boolean> {
        return try {
            val isFirst = preferencesRepository.isFirstLaunch()
            Result.success(isFirst)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 完成首次启动
     */
    suspend fun completeFirstLaunch(): Result<Unit> {
        return try {
            preferencesRepository.setFirstLaunch(false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检查引导流程是否完成
     */
    suspend fun isOnboardingCompleted(): Result<Boolean> {
        return try {
            val completed = preferencesRepository.isOnboardingCompleted()
            Result.success(completed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 完成引导流程
     */
    suspend fun completeOnboarding(): Result<Unit> {
        return try {
            preferencesRepository.setOnboardingCompleted(true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 备份设置 ==========
    
    /**
     * 获取自动备份设置
     */
    suspend fun getAutoBackupEnabled(): Result<Boolean> {
        return try {
            val enabled = preferencesRepository.isAutoBackupEnabled()
            Result.success(enabled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设置自动备份
     */
    suspend fun setAutoBackupEnabled(enabled: Boolean): Result<Unit> {
        return try {
            preferencesRepository.setAutoBackup(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取最后备份时间
     */
    suspend fun getLastBackupTime(): Result<String> {
        return try {
            val timestamp = preferencesRepository.getLastBackupTime()
            val formatted = if (timestamp > 0) {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
            } else {
                "从未备份"
            }
            Result.success(formatted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新最后备份时间
     */
    suspend fun updateLastBackupTime(): Result<Unit> {
        return try {
            preferencesRepository.saveLastBackupTime(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 数据管理 ==========
    
    /**
     * 获取应用设置摘要
     */
    suspend fun getSettingsSummary(): Result<SettingsSummary> {
        return try {
            val themeMode = preferencesRepository.getThemeMode()
            val notificationsEnabled = preferencesRepository.isNotificationsEnabled()
            val (hour, minute) = preferencesRepository.getReminderTime()
            val ttsEnabled = preferencesRepository.isTtsEnabled()
            val streakGoal = preferencesRepository.getStreakGoal()
            val weeklyGoal = preferencesRepository.getWeeklyGoal()
            val hasApiKey = preferencesRepository.hasApiKey()
            
            val summary = SettingsSummary(
                themeMode = themeMode,
                notificationsEnabled = notificationsEnabled,
                reminderTime = "$hour:${String.format("%02d", minute)}",
                ttsEnabled = ttsEnabled,
                streakGoal = streakGoal,
                weeklyGoal = weeklyGoal,
                hasApiKey = hasApiKey
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 重置所有设置
     */
    suspend fun resetAllSettings(): Result<Unit> {
        return try {
            preferencesRepository.resetAllPreferences()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 仅清除用户数据
     */
    suspend fun clearUserData(): Result<Unit> {
        return try {
            preferencesRepository.clearUserData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 验证和实用工具 ==========
    
    /**
     * 验证设置配置的完整性
     */
    suspend fun validateSettings(): Result<SettingsValidation> {
        return try {
            val issues = mutableListOf<String>()
            
            // 检查提醒时间
            val (hour, minute) = preferencesRepository.getReminderTime()
            if (hour !in 0..23 || minute !in 0..59) {
                issues.add("提醒时间设置无效")
            }
            
            // 检查目标设置
            val streakGoal = preferencesRepository.getStreakGoal()
            val weeklyGoal = preferencesRepository.getWeeklyGoal()
            if (streakGoal !in 1..365) {
                issues.add("连续天数目标设置无效")
            }
            if (weeklyGoal !in 1..7) {
                issues.add("每周目标设置无效")
            }
            
            // 检查TTS设置
            val ttsSpeed = preferencesRepository.getTtsVoiceSpeed()
            if (ttsSpeed !in 0.5f..2.0f) {
                issues.add("TTS语音速度设置无效")
            }
            
            val validation = SettingsValidation(
                isValid = issues.isEmpty(),
                issues = issues
            )
            
            Result.success(validation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 提醒时间
 */
data class ReminderTime(
    val hour: Int,
    val minute: Int
) {
    override fun toString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}

/**
 * 设置摘要
 */
data class SettingsSummary(
    val themeMode: String,
    val notificationsEnabled: Boolean,
    val reminderTime: String,
    val ttsEnabled: Boolean,
    val streakGoal: Int,
    val weeklyGoal: Int,
    val hasApiKey: Boolean
)

/**
 * 设置验证结果
 */
data class SettingsValidation(
    val isValid: Boolean,
    val issues: List<String>
) 