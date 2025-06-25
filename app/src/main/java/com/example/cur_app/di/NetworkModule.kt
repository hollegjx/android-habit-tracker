package com.example.cur_app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 网络依赖注入模块
 * 负责配置Retrofit、API服务和网络相关的依赖
 * 
 * 注意：由于API接口还未实现，这个模块暂时为空
 * 待网络层实现后再完善此模块
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // 网络相关依赖将在后续步骤中实现
    
} 