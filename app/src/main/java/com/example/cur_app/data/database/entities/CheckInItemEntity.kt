package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 打卡项目实体类
 * 存储用户创建的打卡项目模板信息，支持学习、运动、攒钱三种类型
 */
@Entity(tableName = "checkin_items")
@Serializable
data class CheckInItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本信息
    val type: String,                      // 打卡类型：STUDY, EXERCISE, MONEY
    val title: String,                     // 打卡项目标题
    val description: String = "",          // 项目描述
    val icon: String = "⭐",              // 图标emoji
    val color: String = "#6650a4",         // 主题色
    
    // 目标设置
    val targetValue: Int = 1,              // 目标数值
    val unit: String = "次",               // 单位：分钟、千卡、元、次等
    val targetFrequency: String = "daily", // 频率：daily, weekly, monthly
    val experienceValue: Int = 30,         // 完成后获得的经验值
    
    // 状态信息
    val isActive: Boolean = true,          // 是否激活
    
    // 时间信息
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    val updatedAt: Long = System.currentTimeMillis()   // 更新时间
) 