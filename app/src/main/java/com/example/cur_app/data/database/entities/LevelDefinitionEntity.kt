 package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * 等级定义实体
 * 用于存储可配置的等级系统数据
 */
@Entity(tableName = "level_definitions")
data class LevelDefinitionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "category")
    val category: String, // STUDY, EXERCISE, MONEY
    
    @ColumnInfo(name = "level_index")
    val levelIndex: Int, // 等级索引 0,1,2,3,4...
    
    @ColumnInfo(name = "title")
    val title: String, // 等级称号，如"学习新手"、"学习达人"
    
    @ColumnInfo(name = "exp_threshold")
    val expThreshold: Int, // 达到此等级所需的经验值
    
    @ColumnInfo(name = "icon")
    val icon: String, // 等级图标，如"🌱"、"📚"
    
    @ColumnInfo(name = "description")
    val description: String, // 等级描述，如"刚开始学习之旅"
    
    @ColumnInfo(name = "color")
    val color: String? = null, // 等级颜色（可选），用于UI显示
    
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = true, // 是否为默认配置
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)