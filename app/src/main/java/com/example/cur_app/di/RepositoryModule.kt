package com.example.cur_app.di

import com.example.cur_app.data.repository.CheckInRepository
import com.example.cur_app.data.repository.AchievementRepository
import com.example.cur_app.data.database.HabitTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓库层依赖注入模块
 * 负责配置数据仓库相关的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * 提供CheckInRepository实例
     */
    @Provides
    @Singleton
    fun provideCheckInRepository(
        database: HabitTrackerDatabase
    ): CheckInRepository {
        return CheckInRepository(
            itemDao = database.checkInItemDao(),
            recordDao = database.checkInRecordDao()
        )
    }
    
    /**
     * 提供AchievementRepository实例
     */
    @Provides
    @Singleton
    fun provideAchievementRepository(
        database: HabitTrackerDatabase
    ): AchievementRepository {
        return AchievementRepository(database)
    }
} 