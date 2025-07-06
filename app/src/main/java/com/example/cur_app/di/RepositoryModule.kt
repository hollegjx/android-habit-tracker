package com.example.cur_app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.cur_app.data.repository.CheckInRepository
import com.example.cur_app.data.repository.AchievementRepository
import com.example.cur_app.data.repository.AiCharacterRepository
import com.example.cur_app.data.repository.AiRepository
import com.example.cur_app.data.repository.AuthRepository
import com.example.cur_app.data.repository.ChatRepository
import com.example.cur_app.data.repository.FriendRepository
import com.example.cur_app.data.repository.PreferencesRepository
import com.example.cur_app.data.database.HabitTrackerDatabase
import com.example.cur_app.data.ai.AiService
import com.example.cur_app.data.database.dao.AiCharacterDao
import com.example.cur_app.data.database.dao.AiConversationDao
import com.example.cur_app.data.remote.AuthApiService
import com.example.cur_app.data.remote.socket.SocketService
import com.example.cur_app.data.remote.datasource.FriendRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    
    /**
     * 提供AiCharacterRepository实例
     */
    @Provides
    @Singleton
    fun provideAiCharacterRepository(
        database: HabitTrackerDatabase
    ): AiCharacterRepository {
        return AiCharacterRepository(database.aiCharacterDao())
    }
    
    /**
     * 提供DataStore实例 (用于认证相关数据存储)
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.authDataStore
    }
    
    /**
     * 提供ChatRepository实例
     */
    @Provides
    @Singleton
    fun provideChatRepository(
        database: HabitTrackerDatabase,
        socketService: SocketService,
        authApiService: AuthApiService
    ): ChatRepository {
        return ChatRepository(database, socketService, authApiService)
    }
    
    /**
     * 提供AuthRepository实例
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        authApiService: AuthApiService,
        chatRepositoryProvider: dagger.Lazy<ChatRepository>,
        preferencesRepository: PreferencesRepository
    ): AuthRepository {
        return AuthRepository(context, authApiService, chatRepositoryProvider, preferencesRepository)
    }
    
    /**
     * 提供AiRepository实例
     */
    @Provides
    @Singleton
    fun provideAiRepository(
        aiService: AiService,
        aiCharacterDao: AiCharacterDao,
        aiConversationDao: AiConversationDao
    ): AiRepository {
        return AiRepository(aiService, aiCharacterDao, aiConversationDao)
    }
    
    /**
     * 提供FriendRepository实例
     */
    @Provides
    @Singleton
    fun provideFriendRepository(
        friendRemoteDataSource: FriendRemoteDataSource
    ): FriendRepository {
        return FriendRepository(friendRemoteDataSource)
    }
}

// DataStore extension for Context (same as AuthRepository uses)
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs") 