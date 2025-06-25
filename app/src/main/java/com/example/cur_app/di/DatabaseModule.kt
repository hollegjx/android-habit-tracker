package com.example.cur_app.di

import android.content.Context
import com.example.cur_app.data.database.HabitTrackerDatabase
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
} 