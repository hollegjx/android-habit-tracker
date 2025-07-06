package com.example.cur_app.presentation.navigation

/**
 * 应用导航路由定义
 * 定义所有界面的导航路径和参数
 */
object Route {
    
    // 主要界面路由
    const val HOME = "home"
    const val HABIT_LIST = "habit_list"
    const val HABIT_ADD = "habit_add"
    const val HABIT_EDIT = "habit_edit/{habitId}"
    const val TYPE_DETAIL = "type_detail/{typeId}"
    const val SETTINGS = "settings"
    const val CHAT_LIST = "chat_list"
    const val CHAT_DETAIL = "chat_detail/{conversationId}"
    const val AI_CHAT = "ai_chat"
    const val STATISTICS = "statistics"
    
    // 带参数的路由构建器
    fun habitEdit(habitId: Long): String = "habit_edit/$habitId"
    fun typeDetail(typeId: String): String = "type_detail/$typeId"
    fun chatDetail(conversationId: String): String = "chat_detail/$conversationId"
    
    // 参数键名
    object Args {
        const val HABIT_ID = "habitId"
        const val TYPE_ID = "typeId"
        const val CONVERSATION_ID = "conversationId"
    }
    
    // 底部导航栏路由
    val bottomNavRoutes = listOf(
        HOME,
        CHAT_LIST,
        AI_CHAT,
        STATISTICS,
        SETTINGS
    )
} 