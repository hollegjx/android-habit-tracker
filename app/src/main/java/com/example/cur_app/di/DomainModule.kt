package com.example.cur_app.di

import com.example.cur_app.domain.usecase.CheckInUseCase
import com.example.cur_app.domain.usecase.AchievementUseCase
import com.example.cur_app.data.repository.CheckInRepository
import com.example.cur_app.data.repository.AchievementRepository
import com.example.cur_app.data.repository.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Domain层依赖注入模块
 * 负责配置业务逻辑用例相关的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    
    /**
     * 提供AchievementUseCase实例
     */
    @Provides
    @Singleton
    fun provideAchievementUseCase(
        achievementRepository: AchievementRepository,
        preferencesRepository: PreferencesRepository
    ): AchievementUseCase {
        return AchievementUseCase(achievementRepository, preferencesRepository)
    }
    
    /**
     * 提供CheckInUseCase实例
     */
    @Provides
    @Singleton
    fun provideCheckInUseCase(
        checkInRepository: CheckInRepository,
        preferencesRepository: PreferencesRepository,
        achievementRepository: AchievementRepository,
        achievementUseCase: AchievementUseCase
    ): CheckInUseCase {
        return CheckInUseCase(checkInRepository, preferencesRepository, achievementRepository, achievementUseCase)
    }
} 