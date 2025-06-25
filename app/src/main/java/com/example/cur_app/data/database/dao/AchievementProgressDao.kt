package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.AchievementProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * 成就进度数据访问对象
 */
@Dao
interface AchievementProgressDao {
    
    // ============ 查询操作 ============
    
    /**
     * 获取用户特定类别的成就进度
     */
    @Query("SELECT * FROM achievement_progress WHERE user_id = :userId AND category = :category ORDER BY achievement_id")
    fun getAchievementProgress(userId: String, category: String): Flow<List<AchievementProgressEntity>>
    
    /**
     * 获取用户所有成就进度
     */
    @Query("SELECT * FROM achievement_progress WHERE user_id = :userId ORDER BY category, achievement_id")
    fun getAllUserProgress(userId: String): Flow<List<AchievementProgressEntity>>
    
    /**
     * 获取特定成就进度
     */
    @Query("SELECT * FROM achievement_progress WHERE user_id = :userId AND achievement_id = :achievementId LIMIT 1")
    suspend fun getAchievementProgressById(userId: String, achievementId: String): AchievementProgressEntity?
    
    /**
     * 获取用户已完成的成就
     */
    @Query("SELECT * FROM achievement_progress WHERE user_id = :userId AND is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedAchievements(userId: String): Flow<List<AchievementProgressEntity>>
    
    /**
     * 获取用户特定类别已完成的成就
     */
    @Query("SELECT * FROM achievement_progress WHERE user_id = :userId AND category = :category AND is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedAchievementsByCategory(userId: String, category: String): Flow<List<AchievementProgressEntity>>
    
    /**
     * 获取用户成就完成统计
     */
    @Query("SELECT COUNT(*) FROM achievement_progress WHERE user_id = :userId AND is_completed = 1")
    suspend fun getCompletedAchievementCount(userId: String): Int
    
    /**
     * 获取用户特定类别成就完成统计
     */
    @Query("SELECT COUNT(*) FROM achievement_progress WHERE user_id = :userId AND category = :category AND is_completed = 1")
    suspend fun getCompletedAchievementCountByCategory(userId: String, category: String): Int
    
    // ============ 插入操作 ============
    
    /**
     * 插入成就进度
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: AchievementProgressEntity): Long
    
    /**
     * 批量插入成就进度
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressList(progressList: List<AchievementProgressEntity>)
    
    // ============ 更新操作 ============
    
    /**
     * 更新成就进度
     */
    @Query("UPDATE achievement_progress SET progress = :progress, current_value = :currentValue, is_completed = :isCompleted, updated_at = :updateTime WHERE achievement_id = :achievementId AND user_id = :userId")
    suspend fun updateProgress(achievementId: String, userId: String, progress: Float, currentValue: Int, isCompleted: Boolean, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 标记成就为完成
     */
    @Query("UPDATE achievement_progress SET is_completed = 1, progress = 100.0, completed_at = :completedTime, updated_at = :updateTime WHERE achievement_id = :achievementId AND user_id = :userId")
    suspend fun markAchievementCompleted(achievementId: String, userId: String, completedTime: Long = System.currentTimeMillis(), updateTime: Long = System.currentTimeMillis())
    
    /**
     * 增加成就进度值
     */
    @Query("UPDATE achievement_progress SET current_value = current_value + :increment, updated_at = :updateTime WHERE achievement_id = :achievementId AND user_id = :userId")
    suspend fun incrementProgress(achievementId: String, userId: String, increment: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 设置成就当前值
     */
    @Query("UPDATE achievement_progress SET current_value = :currentValue, progress = CASE WHEN target_value > 0 THEN MIN(100.0, (:currentValue * 100.0 / target_value)) ELSE 0.0 END, is_completed = CASE WHEN :currentValue >= target_value THEN 1 ELSE 0 END, updated_at = :updateTime WHERE achievement_id = :achievementId AND user_id = :userId")
    suspend fun setCurrentValue(achievementId: String, userId: String, currentValue: Int, updateTime: Long = System.currentTimeMillis())
    
    // ============ 删除操作 ============
    
    /**
     * 删除用户所有成就进度
     */
    @Query("DELETE FROM achievement_progress WHERE user_id = :userId")
    suspend fun deleteUserProgress(userId: String)
    
    /**
     * 删除用户特定类别的成就进度
     */
    @Query("DELETE FROM achievement_progress WHERE user_id = :userId AND category = :category")
    suspend fun deleteUserProgressByCategory(userId: String, category: String)
    
    /**
     * 删除特定成就进度
     */
    @Query("DELETE FROM achievement_progress WHERE user_id = :userId AND achievement_id = :achievementId")
    suspend fun deleteAchievementProgress(userId: String, achievementId: String)
    
    /**
     * 清空所有成就进度数据
     */
    @Query("DELETE FROM achievement_progress")
    suspend fun deleteAllProgress()
}