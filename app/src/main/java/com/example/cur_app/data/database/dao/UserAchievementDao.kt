package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户成就数据访问对象
 */
@Dao
interface UserAchievementDao {
    
    // ============ 查询操作 ============
    
    /**
     * 获取用户在特定类别的成就信息
     */
    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND category = :category LIMIT 1")
    suspend fun getUserAchievement(userId: String, category: String): UserAchievementEntity?
    
    /**
     * 获取用户所有成就信息
     */
    @Query("SELECT * FROM user_achievements WHERE user_id = :userId ORDER BY category")
    fun getUserAchievements(userId: String): Flow<List<UserAchievementEntity>>
    
    /**
     * 获取所有用户成就信息（管理用）
     */
    @Query("SELECT * FROM user_achievements ORDER BY user_id, category")
    fun getAllUserAchievements(): Flow<List<UserAchievementEntity>>
    
    // ============ 插入操作 ============
    
    /**
     * 插入用户成就
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievement(achievement: UserAchievementEntity): Long
    
    /**
     * 批量插入用户成就
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievements(achievements: List<UserAchievementEntity>)
    
    // ============ 更新操作 ============
    
    /**
     * 更新经验值
     */
    @Query("UPDATE user_achievements SET current_exp = :newExp, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun updateExperience(userId: String, category: String, newExp: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新等级
     */
    @Query("UPDATE user_achievements SET current_level = :newLevel, level_index = :levelIndex, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun updateLevel(userId: String, category: String, newLevel: String, levelIndex: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 增加经验值
     */
    @Query("UPDATE user_achievements SET current_exp = current_exp + :expGain, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun addExperience(userId: String, category: String, expGain: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新学习时间
     */
    @Query("UPDATE user_achievements SET total_study_time = total_study_time + :studyTime, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun updateStudyTime(userId: String, category: String, studyTime: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新运动时间
     */
    @Query("UPDATE user_achievements SET total_exercise_time = total_exercise_time + :exerciseTime, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun updateExerciseTime(userId: String, category: String, exerciseTime: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新理财金额
     */
    @Query("UPDATE user_achievements SET total_money = total_money + :money, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun updateMoney(userId: String, category: String, money: Double, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新打卡天数
     */
    @Query("UPDATE user_achievements SET total_checkin_days = total_checkin_days + :days, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun updateCheckInDays(userId: String, category: String, days: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新连续打卡记录
     */
    @Query("UPDATE user_achievements SET current_streak = :currentStreak, max_streak = :maxStreak, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun updateStreak(userId: String, category: String, currentStreak: Int, maxStreak: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 重置连续打卡
     */
    @Query("UPDATE user_achievements SET current_streak = 0, updated_at = :updateTime WHERE user_id = :userId AND category = :category")
    suspend fun resetStreak(userId: String, category: String, updateTime: Long = System.currentTimeMillis())
    
    // ============ 删除操作 ============
    
    /**
     * 删除用户所有成就数据
     */
    @Query("DELETE FROM user_achievements WHERE user_id = :userId")
    suspend fun deleteUserAchievements(userId: String)
    
    /**
     * 删除特定类别的成就
     */
    @Query("DELETE FROM user_achievements WHERE user_id = :userId AND category = :category")
    suspend fun deleteUserAchievement(userId: String, category: String)
    
    /**
     * 清空所有成就数据
     */
    @Query("DELETE FROM user_achievements")
    suspend fun deleteAllAchievements()
}