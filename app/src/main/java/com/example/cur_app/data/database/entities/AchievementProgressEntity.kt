package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * 成就进度实体
 * 存储用户各种成就的完成进度
 */
@Entity(tableName = "achievement_progress")
data class AchievementProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "achievement_id")
    val achievementId: String, // 成就唯一标识
    
    @ColumnInfo(name = "category")
    val category: String, // STUDY, EXERCISE, MONEY
    
    @ColumnInfo(name = "progress")
    val progress: Float, // 进度百分比 0-100
    
    @ColumnInfo(name = "current_value")
    val currentValue: Int, // 当前数值
    
    @ColumnInfo(name = "target_value")
    val targetValue: Int, // 目标数值
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false, // 是否已完成
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null, // 完成时间
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)