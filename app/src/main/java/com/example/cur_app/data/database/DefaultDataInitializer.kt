package com.example.cur_app.data.database

import android.content.Context
import android.util.Log
import com.example.cur_app.data.database.entities.CheckInItemEntity
import com.example.cur_app.data.database.entities.LevelDefinitionEntity
import com.example.cur_app.data.local.entity.CheckInType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * 默认数据初始化器
 * 负责从JSON文件读取默认打卡项目并插入到数据库
 */
object DefaultDataInitializer {

    private const val TAG = "DefaultDataInitializer"
    private const val DEFAULT_ITEMS_FILE = "default_checkin_items.json"

    /**
     * 初始化默认打卡项目
     */
    suspend fun initializeDefaultItems(context: Context, database: HabitTrackerDatabase) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始初始化默认打卡项目")
                
                // 检查是否已有数据
                val existingCount = database.checkInItemDao().getActiveItemCount()
                Log.d(TAG, "现有项目数量: $existingCount")
                
                if (existingCount > 0) {
                    Log.d(TAG, "已有数据，跳过初始化")
                    return@withContext // 已有数据，不需要初始化
                }
                
                                // 从assets读取JSON文件
                Log.d(TAG, "尝试读取JSON文件: $DEFAULT_ITEMS_FILE")
                val jsonString = context.assets.open(DEFAULT_ITEMS_FILE).bufferedReader().use { it.readText() }
                Log.d(TAG, "JSON文件读取成功，长度: ${jsonString.length}")
                
                val defaultData = Json.decodeFromString<DefaultCheckInData>(jsonString)
                Log.d(TAG, "JSON解析成功，项目数量: ${defaultData.defaultCheckInItems.size}")

                // 转换为实体对象
                val items = defaultData.defaultCheckInItems.map { item ->
                    CheckInItemEntity(
                        type = item.type,
                        title = item.title,
                        description = item.description,
                        targetValue = item.targetValue,
                        unit = item.unit,
                        icon = item.icon,
                        color = item.color,
                        experienceValue = item.experienceValue,
                        isActive = item.isActive,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                }
                Log.d(TAG, "实体对象转换完成，准备插入${items.size}个项目")

                // 批量插入到数据库
                database.checkInItemDao().insertItems(items)
                Log.d(TAG, "默认数据插入成功")
                
                // 初始化等级定义
                initializeLevelDefinitions(database)
                
            } catch (e: IOException) {
                // JSON文件读取失败，使用代码中的默认数据
                Log.e(TAG, "JSON文件读取失败: ${e.message}", e)
                initializeFallbackData(database)
            } catch (e: Exception) {
                // 其他异常，使用代码中的默认数据
                Log.e(TAG, "数据初始化异常: ${e.message}", e)
                initializeFallbackData(database)
            }
        }
    }
    
    /**
     * 使用代码中的备用默认数据
     */
    private suspend fun initializeFallbackData(database: HabitTrackerDatabase) {
        Log.d(TAG, "使用备用数据进行初始化")
        val fallbackItems = listOf(
            CheckInItemEntity(
                type = "STUDY",
                title = "英语单词背诵", 
                description = "每天背诵新单词，提升词汇量",
                targetValue = 30,
                unit = "分钟",
                icon = "📚",
                color = "#667EEA",
                experienceValue = 30,
                isActive = true
            ),
            CheckInItemEntity(
                type = "EXERCISE",
                title = "晨跑",
                description = "每天早晨跑步锻炼，保持身体健康",
                targetValue = 300,
                unit = "千卡", 
                icon = "🏃",
                color = "#FF7043",
                experienceValue = 60,
                isActive = true
            ),
            CheckInItemEntity(
                type = "MONEY",
                title = "每日储蓄",
                description = "每天存一点钱，积少成多",
                targetValue = 50,
                unit = "元",
                icon = "💰",
                color = "#43A047",
                experienceValue = 25,
                isActive = true
            )
                )

        Log.d(TAG, "准备插入${fallbackItems.size}个备用项目")
        database.checkInItemDao().insertItems(fallbackItems)
        Log.d(TAG, "备用数据插入成功")
        
        // 初始化等级定义
        initializeLevelDefinitions(database)
    }
    
    /**
     * 初始化等级定义数据
     */
    private suspend fun initializeLevelDefinitions(database: HabitTrackerDatabase) {
        try {
            Log.d(TAG, "开始初始化等级定义")
            
            // 检查是否已有等级定义
            val existingCount = database.levelDefinitionDao().hasDefaultLevels()
            if (existingCount > 0) {
                Log.d(TAG, "已有等级定义，跳过初始化")
                return
            }
            
            val defaultLevels = mutableListOf<LevelDefinitionEntity>()
            
            // 学习类型等级
            val studyLevels = listOf(
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 0, "学习新手", 0, "🌱", "刚开始学习之旅"),
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 1, "学习达人", 500, "📚", "坚持学习，持续进步"),
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 2, "学霸", 1500, "🎓", "学习成果显著"),
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 3, "知识大师", 3000, "🧠", "知识渊博，学习高手"),
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 4, "智慧导师", 5000, "👨‍🏫", "智慧如海，引领他人")
            )
            
            // 运动类型等级
            val exerciseLevels = listOf(
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 0, "运动新手", 0, "🚶", "开始健康生活"),
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 1, "健身爱好者", 500, "🏃", "坚持运动，活力满满"),
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 2, "运动达人", 1500, "💪", "运动成效显著"),
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 3, "健身大师", 3000, "🏆", "身体素质超群"),
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 4, "运动导师", 5000, "🥇", "健康典范，激励他人")
            )
            
            // 理财类型等级
            val moneyLevels = listOf(
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 0, "储蓄新手", 0, "🐷", "开始理财规划"),
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 1, "理财爱好者", 500, "💰", "培养理财习惯"),
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 2, "投资达人", 1500, "📈", "理财观念成熟"),
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 3, "财富大师", 3000, "💎", "财富管理高手"),
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 4, "理财导师", 5000, "👑", "财富自由，指导他人")
            )
            
            defaultLevels.addAll(studyLevels)
            defaultLevels.addAll(exerciseLevels)
            defaultLevels.addAll(moneyLevels)
            
            // 批量插入等级定义
            database.levelDefinitionDao().insertLevels(defaultLevels)
            Log.d(TAG, "等级定义初始化完成，共插入${defaultLevels.size}个等级")
            
        } catch (e: Exception) {
            Log.e(TAG, "等级定义初始化失败: ${e.message}", e)
        }
    }
}

/**
 * JSON数据结构类
 */
@Serializable
data class DefaultCheckInData(
    val defaultCheckInItems: List<DefaultCheckInItem>
)

@Serializable
data class DefaultCheckInItem(
    val type: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val unit: String,
    val icon: String,
    val color: String,
    val experienceValue: Int,
    val isActive: Boolean
) 