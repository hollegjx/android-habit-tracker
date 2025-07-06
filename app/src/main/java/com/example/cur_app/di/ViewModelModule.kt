package com.example.cur_app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * ViewModel层依赖注入模块
 * 
 * 注意：由于使用了@HiltViewModel注解，ViewModels会自动注册到Hilt中，
 * 这个模块暂时为空，但保留作为未来可能的扩展点。
 * 
 * 如果需要为ViewModels提供特殊的依赖配置，可以在这里添加。
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    
    // ViewModels 通过 @HiltViewModel 自动注册
    // 如果需要特殊配置，可以在这里添加 @Provides 方法
    
} 