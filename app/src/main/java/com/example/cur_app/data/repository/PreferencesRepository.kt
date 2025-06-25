package com.example.cur_app.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 偏好设置数据仓库
 * 管理应用配置、用户偏好和敏感数据存储
 */
@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        // 普通偏好设置文件名
        private const val PREFS_NAME = "habit_tracker_prefs"
        
        // 偏好设置键名
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SELECTED_CHARACTER_ID = "selected_character_id"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_TTS_VOICE_SPEED = "tts_voice_speed"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_STREAK_GOAL = "streak_goal"
        private const val KEY_WEEKLY_GOAL = "weekly_goal"
        private const val KEY_STATS_PRIVACY = "stats_privacy"
        private const val KEY_DATA_SYNC_ENABLED = "data_sync_enabled"
        private const val KEY_OFFLINE_MODE = "offline_mode"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        
        // 用户信息相关键名
        private const val KEY_USER_NICKNAME = "user_nickname"
        private const val KEY_USER_SIGNATURE = "user_signature"
        private const val KEY_USER_AVATAR_TYPE = "user_avatar_type" // "emoji" 或 "image"
        private const val KEY_USER_AVATAR_VALUE = "user_avatar_value" // emoji字符或图片路径
        private const val KEY_USER_ID = "user_id"
    }
    
    // SharedPreferences
    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // StateFlow用于响应式更新
    private val _selectedCharacterIdFlow = MutableStateFlow(getSelectedCharacterId())
    val selectedCharacterIdFlow: Flow<Long> = _selectedCharacterIdFlow.asStateFlow()
    
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: Flow<String> = _themeMode.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(isNotificationsEnabled())
    val notificationsEnabled: Flow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _userProfileFlow = MutableStateFlow(getUserProfile())
    val userProfileFlow: Flow<UserProfile> = _userProfileFlow.asStateFlow()
    
    // ========== API配置管理 ==========
    
    /**
     * 保存API密钥（普通存储，生产环境建议使用加密存储）
     */
    fun saveApiKey(apiKey: String) {
        sharedPrefs.edit()
            .putString(KEY_API_KEY, apiKey)
            .apply()
    }
    
    /**
     * 获取API密钥
     */
    fun getApiKey(): String? {
        return sharedPrefs.getString(KEY_API_KEY, null)
    }
    
    /**
     * 检查是否已配置API密钥
     */
    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }
    
    /**
     * 清除API密钥
     */
    fun clearApiKey() {
        sharedPrefs.edit()
            .remove(KEY_API_KEY)
            .apply()
    }
    
    // ========== AI角色管理 ==========
    
    /**
     * 保存选中的AI角色ID
     */
    fun saveSelectedCharacterId(characterId: Long) {
        sharedPrefs.edit()
            .putLong(KEY_SELECTED_CHARACTER_ID, characterId)
            .apply()
        _selectedCharacterIdFlow.value = characterId
    }
    
    /**
     * 获取选中的AI角色ID
     */
    fun getSelectedCharacterId(): Long {
        return sharedPrefs.getLong(KEY_SELECTED_CHARACTER_ID, -1L)
    }
    
    // ========== 通知设置 ==========
    
    /**
     * 设置通知开关
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
        _notificationsEnabled.value = enabled
    }
    
    /**
     * 获取通知开关状态
     */
    fun isNotificationsEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    /**
     * 保存提醒时间
     */
    fun saveReminderTime(hour: Int, minute: Int) {
        val timeString = "${hour}:${minute}"
        sharedPrefs.edit()
            .putString(KEY_REMINDER_TIME, timeString)
            .apply()
    }
    
    /**
     * 获取提醒时间
     */
    fun getReminderTime(): Pair<Int, Int> {
        val timeString = sharedPrefs.getString(KEY_REMINDER_TIME, "20:00") ?: "20:00"
        val parts = timeString.split(":")
        return if (parts.size == 2) {
            try {
                Pair(parts[0].toInt(), parts[1].toInt())
            } catch (e: NumberFormatException) {
                Pair(20, 0) // 默认晚上8点
            }
        } else {
            Pair(20, 0)
        }
    }
    
    // ========== 主题设置 ==========
    
    /**
     * 保存主题模式
     */
    fun saveThemeMode(mode: String) {
        sharedPrefs.edit()
            .putString(KEY_THEME_MODE, mode)
            .apply()
        _themeMode.value = mode
    }
    
    /**
     * 获取主题模式
     */
    fun getThemeMode(): String {
        return sharedPrefs.getString(KEY_THEME_MODE, "system") ?: "system"
    }
    
    /**
     * 保存语言设置
     */
    fun saveLanguage(language: String) {
        sharedPrefs.edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }
    
    /**
     * 获取语言设置
     */
    fun getLanguage(): String {
        return sharedPrefs.getString(KEY_LANGUAGE, "zh") ?: "zh"
    }
    
    // ========== TTS设置 ==========
    
    /**
     * 设置TTS开关
     */
    fun setTtsEnabled(enabled: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_TTS_ENABLED, enabled)
            .apply()
    }
    
    /**
     * 获取TTS开关状态
     */
    fun isTtsEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_TTS_ENABLED, true)
    }
    
    /**
     * 保存TTS语音速度
     */
    fun saveTtsVoiceSpeed(speed: Float) {
        sharedPrefs.edit()
            .putFloat(KEY_TTS_VOICE_SPEED, speed)
            .apply()
    }
    
    /**
     * 获取TTS语音速度
     */
    fun getTtsVoiceSpeed(): Float {
        return sharedPrefs.getFloat(KEY_TTS_VOICE_SPEED, 1.0f)
    }
    
    // ========== 应用状态管理 ==========
    
    /**
     * 设置首次启动标志
     */
    fun setFirstLaunch(isFirst: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_FIRST_LAUNCH, isFirst)
            .apply()
    }
    
    /**
     * 检查是否首次启动
     */
    fun isFirstLaunch(): Boolean {
        return sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    /**
     * 设置引导流程完成标志
     */
    fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
            .apply()
    }
    
    /**
     * 检查引导流程是否完成
     */
    fun isOnboardingCompleted(): Boolean {
        return sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    // ========== 目标设置 ==========
    
    /**
     * 保存连续天数目标
     */
    fun saveStreakGoal(goal: Int) {
        sharedPrefs.edit()
            .putInt(KEY_STREAK_GOAL, goal)
            .apply()
    }
    
    /**
     * 获取连续天数目标
     */
    fun getStreakGoal(): Int {
        return sharedPrefs.getInt(KEY_STREAK_GOAL, 21) // 默认21天
    }
    
    /**
     * 保存每周目标
     */
    fun saveWeeklyGoal(goal: Int) {
        sharedPrefs.edit()
            .putInt(KEY_WEEKLY_GOAL, goal)
            .apply()
    }
    
    /**
     * 获取每周目标
     */
    fun getWeeklyGoal(): Int {
        return sharedPrefs.getInt(KEY_WEEKLY_GOAL, 5) // 默认每周5天
    }
    
    // ========== 隐私和数据管理 ==========
    
    /**
     * 设置统计隐私开关
     */
    fun setStatsPrivacy(private: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_STATS_PRIVACY, private)
            .apply()
    }
    
    /**
     * 获取统计隐私设置
     */
    fun isStatsPrivate(): Boolean {
        return sharedPrefs.getBoolean(KEY_STATS_PRIVACY, false)
    }
    
    /**
     * 设置数据同步开关
     */
    fun setDataSyncEnabled(enabled: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_DATA_SYNC_ENABLED, enabled)
            .apply()
    }
    
    /**
     * 获取数据同步开关状态
     */
    fun isDataSyncEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_DATA_SYNC_ENABLED, false)
    }
    
    /**
     * 设置离线模式
     */
    fun setOfflineMode(offline: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_OFFLINE_MODE, offline)
            .apply()
    }
    
    /**
     * 获取离线模式状态
     */
    fun isOfflineMode(): Boolean {
        return sharedPrefs.getBoolean(KEY_OFFLINE_MODE, false)
    }
    
    /**
     * 设置自动备份开关
     */
    fun setAutoBackup(enabled: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_AUTO_BACKUP, enabled)
            .apply()
    }
    
    /**
     * 获取自动备份开关状态
     */
    fun isAutoBackupEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_AUTO_BACKUP, true)
    }
    
    /**
     * 保存最后备份时间
     */
    fun saveLastBackupTime(timestamp: Long) {
        sharedPrefs.edit()
            .putLong(KEY_LAST_BACKUP_TIME, timestamp)
            .apply()
    }
    
    /**
     * 获取最后备份时间
     */
    fun getLastBackupTime(): Long {
        return sharedPrefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
    }
    
    // ========== 数据清理 ==========
    
    /**
     * 重置所有偏好设置
     */
    fun resetAllPreferences() {
        sharedPrefs.edit().clear().apply()
        
        // 重置StateFlow
        _selectedCharacterIdFlow.value = -1L
        _themeMode.value = "system"
        _notificationsEnabled.value = true
    }
    
    /**
     * 仅清除用户数据，保留应用设置
     */
    fun clearUserData() {
        sharedPrefs.edit()
            .remove(KEY_SELECTED_CHARACTER_ID)
            .remove(KEY_STREAK_GOAL)
            .remove(KEY_WEEKLY_GOAL)
            .remove(KEY_LAST_BACKUP_TIME)
            .remove(KEY_API_KEY)
            .remove(KEY_USER_NICKNAME)
            .remove(KEY_USER_SIGNATURE)
            .remove(KEY_USER_AVATAR_TYPE)
            .remove(KEY_USER_AVATAR_VALUE)
            .remove(KEY_USER_ID)
            .apply()
    }
    
    // ========== 用户信息管理 ==========
    
    /**
     * 保存用户昵称
     */
    fun saveUserNickname(nickname: String) {
        sharedPrefs.edit()
            .putString(KEY_USER_NICKNAME, nickname)
            .apply()
    }
    
    /**
     * 获取用户昵称
     */
    fun getUserNickname(): String {
        return sharedPrefs.getString(KEY_USER_NICKNAME, "小明") ?: "小明"
    }
    
    /**
     * 保存用户个性签名
     */
    fun saveUserSignature(signature: String) {
        sharedPrefs.edit()
            .putString(KEY_USER_SIGNATURE, signature)
            .apply()
    }
    
    /**
     * 获取用户个性签名
     */
    fun getUserSignature(): String {
        return sharedPrefs.getString(KEY_USER_SIGNATURE, "习惯成就卓越，坚持成就梦想！") ?: "习惯成就卓越，坚持成就梦想！"
    }
    
    /**
     * 保存用户头像信息
     */
    fun saveUserAvatar(type: String, value: String) {
        sharedPrefs.edit()
            .putString(KEY_USER_AVATAR_TYPE, type)
            .putString(KEY_USER_AVATAR_VALUE, value)
            .apply()
    }
    
    /**
     * 获取用户头像类型
     */
    fun getUserAvatarType(): String {
        return sharedPrefs.getString(KEY_USER_AVATAR_TYPE, "emoji") ?: "emoji"
    }
    
    /**
     * 获取用户头像值
     */
    fun getUserAvatarValue(): String {
        return sharedPrefs.getString(KEY_USER_AVATAR_VALUE, "😊") ?: "😊"
    }
    
    /**
     * 保存用户ID
     */
    fun saveUserId(userId: String) {
        sharedPrefs.edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    /**
     * 获取用户ID
     */
    fun getUserId(): String {
        return sharedPrefs.getString(KEY_USER_ID, "HT2024001") ?: "HT2024001"
    }
    
    /**
     * 获取完整用户信息
     */
    fun getUserProfile(): UserProfile {
        return UserProfile(
            nickname = getUserNickname(),
            signature = getUserSignature(),
            avatarType = getUserAvatarType(),
            avatarValue = getUserAvatarValue(),
            userId = getUserId()
        )
    }
    
    /**
     * 保存完整用户信息
     */
    fun saveUserProfile(profile: UserProfile) {
        sharedPrefs.edit()
            .putString(KEY_USER_NICKNAME, profile.nickname)
            .putString(KEY_USER_SIGNATURE, profile.signature)
            .putString(KEY_USER_AVATAR_TYPE, profile.avatarType)
            .putString(KEY_USER_AVATAR_VALUE, profile.avatarValue)
            .putString(KEY_USER_ID, profile.userId)
            .apply()
        
        // 更新Flow以通知所有监听者
        _userProfileFlow.value = profile
    }
}

/**
 * 用户信息数据类
 */
data class UserProfile(
    val nickname: String,
    val signature: String,
    val avatarType: String, // "emoji" 或 "image"
    val avatarValue: String, // emoji字符或图片路径
    val userId: String
) 