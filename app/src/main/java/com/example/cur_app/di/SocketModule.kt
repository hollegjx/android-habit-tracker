package com.example.cur_app.di

import com.example.cur_app.data.remote.socket.SocketService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Socket.IO依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    fun provideSocketService(gson: Gson): SocketService {
        return SocketService(gson)
    }
}