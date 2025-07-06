package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 打卡记录实体类
 * 专门记录每日打卡完成情况，每天每个项目一条记录
 */
@Entity(
    tableName = "checkin_records",
    foreignKeys = [
        ForeignKey(
            entity = CheckInItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["date"]),
        Index(value = ["itemId", "date"], unique = true)  // 确保每天每项目只有一条记录
    ]
)
@Serializable
data class CheckInRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 关联信息
    val itemId: Long,                      // 关联的打卡项目ID
    
    // 日期信息
    val date: String,                      // 完成日期 "yyyy-MM-dd" 格式
    
    // 完成状态
    val isCompleted: Boolean = false,      // 今日是否已完成
    val actualValue: Int = 0,              // 实际完成数值（如学习30分钟、跑步5公里）
    val completedAt: Long? = null,         // 完成时间戳（毫秒）
    
    // 可选信息
    val note: String = "",                 // 完成备注
    
    // 系统信息
    val createdAt: Long = System.currentTimeMillis(),  // 记录创建时间
    val updatedAt: Long = System.currentTimeMillis()   // 最后更新时间
) 