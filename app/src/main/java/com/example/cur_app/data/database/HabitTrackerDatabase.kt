package com.example.cur_app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import android.util.Log
import com.example.cur_app.data.database.entities.*
import com.example.cur_app.data.database.dao.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * AI打卡追踪应用主数据库
 * 使用Room持久化框架管理本地数据
 */
@Database(
    entities = [
        CheckInItemEntity::class,
        CheckInRecordEntity::class,
        AiCharacterEntity::class,
        AiConversationEntity::class,
        UserAchievementEntity::class,
        AchievementProgressEntity::class,
        LevelDefinitionEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class HabitTrackerDatabase : RoomDatabase() {
    
    // DAO访问接口
    abstract fun checkInItemDao(): CheckInItemDao
    abstract fun checkInRecordDao(): CheckInRecordDao
    abstract fun aiCharacterDao(): AiCharacterDao
    abstract fun aiConversationDao(): AiConversationDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun achievementProgressDao(): AchievementProgressDao
    abstract fun levelDefinitionDao(): LevelDefinitionDao
    
    companion object {
        @Volatile
        private var INSTANCE: HabitTrackerDatabase? = null
        
        private const val DATABASE_NAME = "habit_tracker_database"
        
        /**
         * 获取数据库实例（单例模式）
         */
        fun getDatabase(context: Context): HabitTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitTrackerDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 清理数据库实例（主要用于测试）
         */
        fun clearInstance() {
            INSTANCE = null
        }
        
        /**
         * 强制重建数据库（删除现有数据库文件）
         * 
         * ⚠️ 警告：此方法仅用于开发和调试阶段！
         * 调用此方法会永久删除所有用户数据，正常使用时不应调用此方法。
         * 
         * @param context 应用上下文
         */
        fun forceRebuildDatabase(context: Context) {
            Log.d("HabitTrackerDatabase", "强制重建数据库")
            try {
                // 关闭现有连接
                INSTANCE?.close()
                INSTANCE = null
                
                // 删除数据库文件
                val dbFile = context.getDatabasePath(DATABASE_NAME)
                if (dbFile.exists()) {
                    val deleted = dbFile.delete()
                    Log.d("HabitTrackerDatabase", "数据库文件删除${if (deleted) "成功" else "失败"}")
                }
                
                // 删除相关的journal文件
                val journalFile = context.getDatabasePath("$DATABASE_NAME-journal")
                if (journalFile.exists()) {
                    journalFile.delete()
                }
                
                val walFile = context.getDatabasePath("$DATABASE_NAME-wal")
                if (walFile.exists()) {
                    walFile.delete()
                }
                
                val shmFile = context.getDatabasePath("$DATABASE_NAME-shm")
                if (shmFile.exists()) {
                    shmFile.delete()
                }
                
                Log.d("HabitTrackerDatabase", "数据库重建完成，下次访问将重新创建")
            } catch (e: Exception) {
                Log.e("HabitTrackerDatabase", "数据库重建失败: ${e.message}", e)
            }
        }
    }
    
    /**
     * 数据库回调，处理创建和打开事件
     */
    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        
        companion object {
            private const val TAG = "HabitTrackerDatabase"
        }
        
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d(TAG, "数据库onCreate被调用，开始初始化")
            // 数据库首次创建时的初始化操作
            INSTANCE?.let { database ->
                Log.d(TAG, "数据库实例存在，启动初始化协程")
                CoroutineScope(Dispatchers.IO).launch {
                    // 延迟一小段时间，确保数据库完全初始化
                    delay(100)
                    try {
                        Log.d(TAG, "尝试使用DefaultDataInitializer初始化")
                        // 使用DefaultDataInitializer进行完整的初始化
                        DefaultDataInitializer.initializeDefaultItems(context, database)
                        Log.d(TAG, "DefaultDataInitializer初始化完成")
                    } catch (e: Exception) {
                        Log.e(TAG, "DefaultDataInitializer初始化失败: ${e.message}", e)
                        // 如果DefaultDataInitializer失败，使用fallback
                        initializeFallbackData(database)
                    }
                }
            } ?: run {
                Log.e(TAG, "数据库实例为null，无法初始化")
            }
        }
        
        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onOpen(db)
            // 每次打开数据库时的操作
        }
        
        /**
         * 使用代码中的备用默认数据
         */
        private suspend fun initializeFallbackData(database: HabitTrackerDatabase) {
            Log.d(TAG, "开始使用fallback数据初始化")
            try {
                val existingCount = database.checkInItemDao().getActiveItemCount()
                Log.d(TAG, "现有项目数量: $existingCount")
                if (existingCount > 0) {
                    Log.d(TAG, "已有数据，跳过fallback初始化")
                    return // 已有数据
                }
                
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
                        type = "STUDY",
                        title = "编程练习",
                        description = "坚持每日编程，提高技术水平",
                        targetValue = 90,
                        unit = "分钟",
                        icon = "💻",
                        color = "#667EEA",
                        experienceValue = 50,
                        isActive = true
                    ),
                    CheckInItemEntity(
                        type = "STUDY",
                        title = "阅读专业书籍",
                        description = "每天阅读专业书籍，丰富知识储备",
                        targetValue = 45,
                        unit = "分钟",
                        icon = "📖",
                        color = "#667EEA",
                        experienceValue = 40,
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
                        type = "EXERCISE",
                        title = "力量训练",
                        description = "进行力量训练，增强肌肉力量",
                        targetValue = 150,
                        unit = "千卡",
                        icon = "💪",
                        color = "#FF7043",
                        experienceValue = 50,
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
                    ),
                    CheckInItemEntity(
                        type = "MONEY",
                        title = "理财投资",
                        description = "定期进行理财投资，实现财富增长",
                        targetValue = 200,
                        unit = "元",
                        icon = "📈",
                        color = "#43A047",
                        experienceValue = 35,
                        isActive = true
                    )
                )
                
                Log.d(TAG, "准备插入${fallbackItems.size}个fallback项目")
                database.checkInItemDao().insertItems(fallbackItems)
                Log.d(TAG, "Fallback数据插入成功")
            } catch (e: Exception) {
                Log.e(TAG, "Fallback数据插入失败: ${e.message}", e)
            }
        }
    }
} 