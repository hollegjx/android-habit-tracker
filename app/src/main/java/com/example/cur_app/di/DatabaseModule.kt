package com.example.cur_app.di

import android.content.Context
import com.example.cur_app.data.database.HabitTrackerDatabase
import com.example.cur_app.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 * 负责配置Room数据库和相关DAO的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * 提供HabitTrackerDatabase实例
     */
    @Provides
    @Singleton
    fun provideHabitTrackerDatabase(
        @ApplicationContext context: Context
    ): HabitTrackerDatabase {
        return HabitTrackerDatabase.getDatabase(context)
    }
    
    /**
     * 提供AiCharacterDao
     */
    @Provides
    fun provideAiCharacterDao(database: HabitTrackerDatabase): AiCharacterDao {
        return database.aiCharacterDao()
    }
    
    /**
     * 提供AiConversationDao
     */
    @Provides
    fun provideAiConversationDao(database: HabitTrackerDatabase): AiConversationDao {
        return database.aiConversationDao()
    }
    
    /**
     * 提供ChatConversationDao
     */
    @Provides
    fun provideChatConversationDao(database: HabitTrackerDatabase): ChatConversationDao {
        return database.chatConversationDao()
    }
    
    /**
     * 提供ChatMessageDao
     */
    @Provides
    fun provideChatMessageDao(database: HabitTrackerDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }
    
    /**
     * 提供ChatUserDao
     */
    @Provides
    fun provideChatUserDao(database: HabitTrackerDatabase): ChatUserDao {
        return database.chatUserDao()
    }
    
    /**
     * 提供CheckInItemDao
     */
    @Provides
    fun provideCheckInItemDao(database: HabitTrackerDatabase): CheckInItemDao {
        return database.checkInItemDao()
    }
    
    /**
     * 提供CheckInRecordDao
     */
    @Provides
    fun provideCheckInRecordDao(database: HabitTrackerDatabase): CheckInRecordDao {
        return database.checkInRecordDao()
    }
    
    /**
     * 提供AchievementProgressDao
     */
    @Provides
    fun provideAchievementProgressDao(database: HabitTrackerDatabase): AchievementProgressDao {
        return database.achievementProgressDao()
    }
    
    /**
     * 提供UserAchievementDao
     */
    @Provides
    fun provideUserAchievementDao(database: HabitTrackerDatabase): UserAchievementDao {
        return database.userAchievementDao()
    }
    
    /**
     * 提供LevelDefinitionDao
     */
    @Provides
    fun provideLevelDefinitionDao(database: HabitTrackerDatabase): LevelDefinitionDao {
        return database.levelDefinitionDao()
    }
} 