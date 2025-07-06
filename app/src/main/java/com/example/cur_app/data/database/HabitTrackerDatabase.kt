package com.example.cur_app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        LevelDefinitionEntity::class,
        ChatMessageEntity::class,
        ChatConversationEntity::class,
        ChatUserEntity::class,
        FriendshipEntity::class,
        FriendNotificationEntity::class,
        ConversationEntity::class,
        ConversationParticipantEntity::class,
        MessageEntity::class
    ],
    version = 8,
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
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun chatConversationDao(): ChatConversationDao
    abstract fun chatUserDao(): ChatUserDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun friendNotificationDao(): FriendNotificationDao
    abstract fun conversationDao(): ConversationDao
    abstract fun conversationParticipantDao(): ConversationParticipantDao
    abstract fun messageDao(): MessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: HabitTrackerDatabase? = null
        
        private const val DATABASE_NAME = "habit_tracker_database"
        
        /**
         * 数据库迁移：从版本5到版本6
         * 添加AI角色表的新字段
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为AI角色表添加新字段
                database.execSQL("ALTER TABLE ai_characters ADD COLUMN characterId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE ai_characters ADD COLUMN subtitle TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE ai_characters ADD COLUMN iconEmoji TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE ai_characters ADD COLUMN backgroundColors TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE ai_characters ADD COLUMN skills TEXT NOT NULL DEFAULT ''")
                
                // 更新现有数据为默认值
                database.execSQL("""
                    UPDATE ai_characters 
                    SET characterId = CASE 
                        WHEN name = '小鼓励' THEN 'encourager'
                        WHEN name = '严师' THEN 'strict_mentor'  
                        WHEN name = '小伙伴' THEN 'friend'
                        WHEN name = '智者' THEN 'mentor'
                        ELSE lower(replace(name, ' ', '_'))
                    END,
                    subtitle = CASE
                        WHEN name = '小鼓励' THEN '温暖鼓励者'
                        WHEN name = '严师' THEN '严格导师'
                        WHEN name = '小伙伴' THEN '亲切朋友'
                        WHEN name = '智者' THEN '睿智导师'
                        ELSE '个性伙伴'
                    END,
                    iconEmoji = CASE
                        WHEN name = '小鼓励' THEN '😊'
                        WHEN name = '严师' THEN '🎯'
                        WHEN name = '小伙伴' THEN '👥'
                        WHEN name = '智者' THEN '🧙‍♂️'
                        ELSE '🤖'
                    END,
                    backgroundColors = '["#ff9a9e", "#fecfef"]',
                    skills = '["智能助手", "陪伴支持"]'
                """)
            }
        }
        
        /**
         * 数据库迁移：从版本6到版本7
         * 添加聊天相关表
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建聊天用户表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_users` (
                        `userId` TEXT NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `realName` TEXT NOT NULL DEFAULT '',
                        `avatar` TEXT NOT NULL,
                        `bio` TEXT NOT NULL DEFAULT '',
                        `email` TEXT NOT NULL DEFAULT '',
                        `phone` TEXT NOT NULL DEFAULT '',
                        `isAiBot` INTEGER NOT NULL DEFAULT 0,
                        `aiType` TEXT,
                        `aiPersonality` TEXT NOT NULL DEFAULT '',
                        `aiCapabilities` TEXT NOT NULL DEFAULT '',
                        `isOnline` INTEGER NOT NULL DEFAULT 0,
                        `lastSeenTime` INTEGER NOT NULL,
                        `status` TEXT NOT NULL DEFAULT 'available',
                        `statusMessage` TEXT NOT NULL DEFAULT '',
                        `language` TEXT NOT NULL DEFAULT 'zh-CN',
                        `timezone` TEXT NOT NULL DEFAULT 'Asia/Shanghai',
                        `notificationSettings` TEXT NOT NULL DEFAULT '',
                        `totalMessages` INTEGER NOT NULL DEFAULT 0,
                        `totalConversations` INTEGER NOT NULL DEFAULT 0,
                        `averageResponseTime` INTEGER NOT NULL DEFAULT 0,
                        `isVerified` INTEGER NOT NULL DEFAULT 0,
                        `verificationLevel` TEXT NOT NULL DEFAULT 'none',
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`userId`)
                    )
                """)
                
                // 创建聊天对话表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_conversations` (
                        `conversationId` TEXT NOT NULL,
                        `otherUserId` TEXT NOT NULL,
                        `conversationType` TEXT NOT NULL DEFAULT 'PRIVATE',
                        `title` TEXT NOT NULL DEFAULT '',
                        `description` TEXT NOT NULL DEFAULT '',
                        `lastMessage` TEXT NOT NULL DEFAULT '',
                        `lastMessageTime` INTEGER NOT NULL,
                        `lastMessageSenderId` TEXT NOT NULL DEFAULT '',
                        `lastMessageType` TEXT NOT NULL DEFAULT 'TEXT',
                        `unreadCount` INTEGER NOT NULL DEFAULT 0,
                        `isPinned` INTEGER NOT NULL DEFAULT 0,
                        `isArchived` INTEGER NOT NULL DEFAULT 0,
                        `isMuted` INTEGER NOT NULL DEFAULT 0,
                        `isBlocked` INTEGER NOT NULL DEFAULT 0,
                        `customAvatar` TEXT NOT NULL DEFAULT '',
                        `customName` TEXT NOT NULL DEFAULT '',
                        `theme` TEXT NOT NULL DEFAULT 'default',
                        `totalMessages` INTEGER NOT NULL DEFAULT 0,
                        `myMessages` INTEGER NOT NULL DEFAULT 0,
                        `otherMessages` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`conversationId`)
                    )
                """)
                
                // 创建聊天消息表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_messages` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `conversationId` TEXT NOT NULL,
                        `senderId` TEXT NOT NULL,
                        `receiverId` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `messageType` TEXT NOT NULL DEFAULT 'TEXT',
                        `metadata` TEXT NOT NULL DEFAULT '',
                        `isRead` INTEGER NOT NULL DEFAULT 0,
                        `isFromMe` INTEGER NOT NULL DEFAULT 0,
                        `isDeleted` INTEGER NOT NULL DEFAULT 0,
                        `isSent` INTEGER NOT NULL DEFAULT 1,
                        `timestamp` INTEGER NOT NULL,
                        `readTimestamp` INTEGER,
                        `editTimestamp` INTEGER,
                        `replyToMessageId` INTEGER,
                        `forwardFromMessageId` INTEGER,
                        `reactions` TEXT NOT NULL DEFAULT '',
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """)
                
                // 创建索引
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_users_isAiBot` ON `chat_users` (`isAiBot`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_users_isOnline` ON `chat_users` (`isOnline`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_users_lastSeenTime` ON `chat_users` (`lastSeenTime`)")
                
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_conversations_otherUserId` ON `chat_conversations` (`otherUserId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_conversations_lastMessageTime` ON `chat_conversations` (`lastMessageTime`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_conversations_isPinned_lastMessageTime` ON `chat_conversations` (`isPinned`, `lastMessageTime`)")
                
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_conversationId` ON `chat_messages` (`conversationId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_timestamp` ON `chat_messages` (`timestamp`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_senderId` ON `chat_messages` (`senderId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_receiverId` ON `chat_messages` (`receiverId`)")
            }
        }
        
        /**
         * 数据库迁移：从版本7到版本8
         * 添加好友系统相关表
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建好友关系表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `friendships` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `requester_id` TEXT NOT NULL,
                        `addressee_id` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `requester_message` TEXT,
                        `reject_reason` TEXT,
                        `friendship_alias` TEXT,
                        `is_starred` INTEGER NOT NULL DEFAULT 0,
                        `is_muted` INTEGER NOT NULL DEFAULT 0,
                        `is_blocked` INTEGER NOT NULL DEFAULT 0,
                        `conversation_id` TEXT,
                        `unread_count` INTEGER NOT NULL DEFAULT 0,
                        `last_message_at` INTEGER,
                        `last_read_at` INTEGER,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL
                    )
                """)
                
                // 创建好友通知表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `friend_notifications` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `friendship_id` INTEGER NOT NULL,
                        `user_id` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `message` TEXT,
                        `is_read` INTEGER NOT NULL DEFAULT 0,
                        `read_at` INTEGER,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        FOREIGN KEY(`friendship_id`) REFERENCES `friendships`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                // 创建对话表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `conversations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `conversation_id` TEXT NOT NULL,
                        `type` TEXT NOT NULL DEFAULT 'private',
                        `name` TEXT,
                        `description` TEXT,
                        `avatar_url` TEXT,
                        `created_by` TEXT,
                        `is_active` INTEGER NOT NULL DEFAULT 1,
                        `last_message_at` INTEGER,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL
                    )
                """)
                
                // 创建对话参与者表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `conversation_participants` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `conversation_id` INTEGER NOT NULL,
                        `user_id` TEXT NOT NULL,
                        `role` TEXT NOT NULL DEFAULT 'member',
                        `joined_at` INTEGER NOT NULL,
                        `last_read_at` INTEGER,
                        `is_muted` INTEGER NOT NULL DEFAULT 0,
                        `is_pinned` INTEGER NOT NULL DEFAULT 0,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        FOREIGN KEY(`conversation_id`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                // 创建消息表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `messages` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `message_id` TEXT NOT NULL,
                        `conversation_id` INTEGER NOT NULL,
                        `sender_id` TEXT,
                        `content` TEXT NOT NULL,
                        `message_type` TEXT NOT NULL DEFAULT 'text',
                        `media_url` TEXT,
                        `media_metadata` TEXT,
                        `reply_to_id` INTEGER,
                        `is_edited` INTEGER NOT NULL DEFAULT 0,
                        `edited_at` INTEGER,
                        `is_deleted` INTEGER NOT NULL DEFAULT 0,
                        `deleted_at` INTEGER,
                        `is_read` INTEGER NOT NULL DEFAULT 0,
                        `read_at` INTEGER,
                        `is_delivered` INTEGER NOT NULL DEFAULT 0,
                        `delivered_at` INTEGER,
                        `reactions` TEXT,
                        `mentions` TEXT,
                        `sent_at` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        FOREIGN KEY(`conversation_id`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                // 创建索引
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_friendships_requester_id_addressee_id` ON `friendships` (`requester_id`, `addressee_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_friendships_status` ON `friendships` (`status`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_friendships_conversation_id` ON `friendships` (`conversation_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_friendships_last_message_at` ON `friendships` (`last_message_at`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_friendships_is_starred` ON `friendships` (`is_starred`)")
                
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_friend_notifications_user_id_is_read` ON `friend_notifications` (`user_id`, `is_read`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_friend_notifications_type` ON `friend_notifications` (`type`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_friend_notifications_friendship_id` ON `friend_notifications` (`friendship_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_friend_notifications_created_at` ON `friend_notifications` (`created_at`)")
                
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_conversations_conversation_id` ON `conversations` (`conversation_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_type` ON `conversations` (`type`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_last_message_at` ON `conversations` (`last_message_at`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_created_by` ON `conversations` (`created_by`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_is_active` ON `conversations` (`is_active`)")
                
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_conversation_participants_conversation_id_user_id` ON `conversation_participants` (`conversation_id`, `user_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversation_participants_user_id` ON `conversation_participants` (`user_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversation_participants_role` ON `conversation_participants` (`role`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversation_participants_joined_at` ON `conversation_participants` (`joined_at`)")
                
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_messages_message_id` ON `messages` (`message_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversation_id_sent_at` ON `messages` (`conversation_id`, `sent_at`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_sender_id` ON `messages` (`sender_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_message_type` ON `messages` (`message_type`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_is_read` ON `messages` (`is_read`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_reply_to_id` ON `messages` (`reply_to_id`)")
            }
        }
        
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
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
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