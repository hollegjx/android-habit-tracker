package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * 用户成就实体
 * 存储用户在各个类别中的等级、经验值和统计数据
 */
@Entity(tableName = "user_achievements")
data class UserAchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "category")
    val category: String, // STUDY, EXERCISE, MONEY
    
    @ColumnInfo(name = "current_level")
    val currentLevel: String,
    
    @ColumnInfo(name = "current_exp")
    val currentExp: Int,
    
    @ColumnInfo(name = "level_index")
    val levelIndex: Int,
    
    @ColumnInfo(name = "total_study_time")
    val totalStudyTime: Int = 0, // 总学习时间（分钟）
    
    @ColumnInfo(name = "total_exercise_time")
    val totalExerciseTime: Int = 0, // 总运动时间（分钟）
    
    @ColumnInfo(name = "total_money")
    val totalMoney: Double = 0.0, // 总理财金额
    
    @ColumnInfo(name = "total_checkin_days")
    val totalCheckInDays: Int = 0, // 总打卡天数
    
    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0, // 当前连续天数
    
    @ColumnInfo(name = "max_streak")
    val maxStreak: Int = 0, // 最大连续天数
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)