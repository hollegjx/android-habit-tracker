package com.example.cur_app.data.database

import android.content.Context
import android.util.Log
import com.example.cur_app.data.database.entities.CheckInItemEntity
import com.example.cur_app.data.database.entities.LevelDefinitionEntity
import com.example.cur_app.data.database.entities.AiCharacterEntity
import com.example.cur_app.data.database.entities.ChatUserEntity
import com.example.cur_app.data.database.entities.ChatConversationEntity
import com.example.cur_app.data.database.entities.ChatMessageEntity
import com.example.cur_app.data.local.entity.CheckInType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * 默认数据初始化器
 * 负责从JSON文件读取默认打卡项目并插入到数据库
 */
object DefaultDataInitializer {

    private const val TAG = "DefaultDataInitializer"
    private const val DEFAULT_ITEMS_FILE = "default_checkin_items.json"

    /**
     * 清理测试好友数据（保留AI数据）
     */
    suspend fun cleanupTestFriendData(database: HabitTrackerDatabase) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始清理测试好友数据")
                
                // 删除非AI用户
                database.chatUserDao().deleteNonAiUsers()
                
                // 删除非AI对话
                database.chatConversationDao().deleteNonAiConversations()
                
                // 删除非AI相关的消息
                database.chatMessageDao().deleteNonAiMessages()
                
                Log.d(TAG, "测试好友数据清理完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "清理测试好友数据失败: ${e.message}", e)
            }
        }
    }

    /**
     * 初始化基础数据（仅包含打卡项目、等级、AI角色、AI对话，不包含好友聊天）
     */
    suspend fun initializeBasicData(context: Context, database: HabitTrackerDatabase) {
        withContext(Dispatchers.IO) {
            // 检查是否已有打卡项目数据
            val existingCount = database.checkInItemDao().getActiveItemCount()
            Log.d(TAG, "现有项目数量: $existingCount")
            
            // 只有当没有打卡项目时才初始化打卡项目
            val shouldInitializeCheckInItems = existingCount == 0
            
            try {
                Log.d(TAG, "开始初始化基础数据")
                
                if (shouldInitializeCheckInItems) {
                    // 从assets读取JSON文件
                    Log.d(TAG, "尝试读取JSON文件: $DEFAULT_ITEMS_FILE")
                    val jsonString = context.assets.open(DEFAULT_ITEMS_FILE).bufferedReader().use { it.readText() }
                    Log.d(TAG, "JSON文件读取成功，长度: ${jsonString.length}")
                    
                    val defaultData = Json.decodeFromString<DefaultCheckInData>(jsonString)
                    Log.d(TAG, "JSON解析成功，项目数量: ${defaultData.defaultCheckInItems.size}")

                    // 转换为实体对象
                    val items = defaultData.defaultCheckInItems.map { item ->
                        CheckInItemEntity(
                            type = item.type,
                            title = item.title,
                            description = item.description,
                            targetValue = item.targetValue,
                            unit = item.unit,
                            icon = item.icon,
                            color = item.color,
                            experienceValue = item.experienceValue,
                            isActive = item.isActive,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    Log.d(TAG, "实体对象转换完成，准备插入${items.size}个项目")

                    // 批量插入到数据库
                    database.checkInItemDao().insertItems(items)
                    Log.d(TAG, "默认打卡项目插入成功")
                }
                
                // 总是初始化这些基础数据（如果不存在的话）
                initializeLevelDefinitions(database)
                initializeAiCharacters(database)
                initializeBasicChatData(database) // 只初始化AI对话，不包含好友聊天
                
            } catch (e: IOException) {
                // JSON文件读取失败，使用代码中的默认数据（仅当需要初始化打卡项目时）
                Log.e(TAG, "JSON文件读取失败: ${e.message}", e)
                if (shouldInitializeCheckInItems) {
                    initializeFallbackCheckInItems(database)
                }
                // 总是初始化这些数据
                initializeLevelDefinitions(database)
                initializeAiCharacters(database)
                initializeBasicChatData(database)
            } catch (e: Exception) {
                // 其他异常，使用代码中的默认数据
                Log.e(TAG, "数据初始化异常: ${e.message}", e)
                if (shouldInitializeCheckInItems) {
                    initializeFallbackCheckInItems(database)
                }
                // 总是初始化这些数据
                initializeLevelDefinitions(database)
                initializeAiCharacters(database)
                initializeBasicChatData(database)
            }
        }
    }

    /**
     * 初始化默认打卡项目（包含所有数据，包括好友聊天）
     */
    suspend fun initializeDefaultItems(context: Context, database: HabitTrackerDatabase) {
        withContext(Dispatchers.IO) {
            // 检查是否已有打卡项目数据
            val existingCount = database.checkInItemDao().getActiveItemCount()
            Log.d(TAG, "现有项目数量: $existingCount")
            
            // 只有当没有打卡项目时才初始化打卡项目
            val shouldInitializeCheckInItems = existingCount == 0
            
            try {
                Log.d(TAG, "开始初始化默认数据")
                
                if (shouldInitializeCheckInItems) {
                    // 从assets读取JSON文件
                    Log.d(TAG, "尝试读取JSON文件: $DEFAULT_ITEMS_FILE")
                    val jsonString = context.assets.open(DEFAULT_ITEMS_FILE).bufferedReader().use { it.readText() }
                    Log.d(TAG, "JSON文件读取成功，长度: ${jsonString.length}")
                    
                    val defaultData = Json.decodeFromString<DefaultCheckInData>(jsonString)
                    Log.d(TAG, "JSON解析成功，项目数量: ${defaultData.defaultCheckInItems.size}")

                    // 转换为实体对象
                    val items = defaultData.defaultCheckInItems.map { item ->
                        CheckInItemEntity(
                            type = item.type,
                            title = item.title,
                            description = item.description,
                            targetValue = item.targetValue,
                            unit = item.unit,
                            icon = item.icon,
                            color = item.color,
                            experienceValue = item.experienceValue,
                            isActive = item.isActive,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    Log.d(TAG, "实体对象转换完成，准备插入${items.size}个项目")

                    // 批量插入到数据库
                    database.checkInItemDao().insertItems(items)
                    Log.d(TAG, "默认打卡项目插入成功")
                }
                
                // 总是初始化这些数据（如果不存在的话）
                initializeLevelDefinitions(database)
                initializeAiCharacters(database)
                initializeChatData(database)
                
            } catch (e: IOException) {
                // JSON文件读取失败，使用代码中的默认数据（仅当需要初始化打卡项目时）
                Log.e(TAG, "JSON文件读取失败: ${e.message}", e)
                if (shouldInitializeCheckInItems) {
                    initializeFallbackCheckInItems(database)
                }
                // 总是初始化这些数据
                initializeLevelDefinitions(database)
                initializeAiCharacters(database)
                initializeChatData(database)
            } catch (e: Exception) {
                // 其他异常，使用代码中的默认数据
                Log.e(TAG, "数据初始化异常: ${e.message}", e)
                if (shouldInitializeCheckInItems) {
                    initializeFallbackCheckInItems(database)
                }
                // 总是初始化这些数据
                initializeLevelDefinitions(database)
                initializeAiCharacters(database)
                initializeChatData(database)
            }
        }
    }
    
    /**
     * 使用代码中的备用打卡项目数据
     */
    private suspend fun initializeFallbackCheckInItems(database: HabitTrackerDatabase) {
        Log.d(TAG, "使用备用打卡项目数据进行初始化")
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
                type = "MONEY",
                title = "每日储蓄",
                description = "每天存一点钱，积少成多",
                targetValue = 50,
                unit = "元",
                icon = "💰",
                color = "#43A047",
                experienceValue = 25,
                isActive = true
            )
        )

        Log.d(TAG, "准备插入${fallbackItems.size}个备用打卡项目")
        database.checkInItemDao().insertItems(fallbackItems)
        Log.d(TAG, "备用打卡项目插入成功")
    }
    
    /**
     * 初始化等级定义数据
     */
    private suspend fun initializeLevelDefinitions(database: HabitTrackerDatabase) {
        try {
            Log.d(TAG, "开始初始化等级定义")
            
            // 检查是否已有等级定义
            val existingCount = database.levelDefinitionDao().hasDefaultLevels()
            if (existingCount > 0) {
                Log.d(TAG, "已有等级定义，跳过初始化")
                return
            }
            
            val defaultLevels = mutableListOf<LevelDefinitionEntity>()
            
            // 学习类型等级
            val studyLevels = listOf(
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 0, "学习新手", 0, "🌱", "刚开始学习之旅"),
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 1, "学习达人", 500, "📚", "坚持学习，持续进步"),
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 2, "学霸", 1500, "🎓", "学习成果显著"),
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 3, "知识大师", 3000, "🧠", "知识渊博，学习高手"),
                LevelDefinitionEntity(0, CheckInType.STUDY.name, 4, "智慧导师", 5000, "👨‍🏫", "智慧如海，引领他人")
            )
            
            // 运动类型等级
            val exerciseLevels = listOf(
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 0, "运动新手", 0, "🚶", "开始健康生活"),
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 1, "健身爱好者", 500, "🏃", "坚持运动，活力满满"),
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 2, "运动达人", 1500, "💪", "运动成效显著"),
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 3, "健身大师", 3000, "🏆", "身体素质超群"),
                LevelDefinitionEntity(0, CheckInType.EXERCISE.name, 4, "运动导师", 5000, "🥇", "健康典范，激励他人")
            )
            
            // 理财类型等级
            val moneyLevels = listOf(
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 0, "储蓄新手", 0, "🐷", "开始理财规划"),
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 1, "理财爱好者", 500, "💰", "培养理财习惯"),
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 2, "投资达人", 1500, "📈", "理财观念成熟"),
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 3, "财富大师", 3000, "💎", "财富管理高手"),
                LevelDefinitionEntity(0, CheckInType.MONEY.name, 4, "理财导师", 5000, "👑", "财富自由，指导他人")
            )
            
            defaultLevels.addAll(studyLevels)
            defaultLevels.addAll(exerciseLevels)
            defaultLevels.addAll(moneyLevels)
            
            // 批量插入等级定义
            database.levelDefinitionDao().insertLevels(defaultLevels)
            Log.d(TAG, "等级定义初始化完成，共插入${defaultLevels.size}个等级")
            
        } catch (e: Exception) {
            Log.e(TAG, "等级定义初始化失败: ${e.message}", e)
        }
    }
    
    /**
     * 初始化AI角色默认数据
     */
    private suspend fun initializeAiCharacters(database: HabitTrackerDatabase) {
        try {
            Log.d(TAG, "开始初始化AI角色")
            
            // 检查是否已有AI角色数据
            val existingCount = database.aiCharacterDao().getCharacterCount()
            if (existingCount > 0) {
                Log.d(TAG, "已有AI角色数据，跳过初始化")
                return
            }
            
            val defaultCharacters = listOf(
                AiCharacterEntity(
                    characterId = "sakura",
                    name = "小樱",
                    subtitle = "温柔学习伙伴",
                    type = "encourager",
                    description = "温柔体贴，善解人意。总是能在你需要鼓励的时候给予温暖的话语，学习遇到困难时会耐心地陪伴你一起克服。",
                    avatar = "sakura",
                    iconEmoji = "🌸",
                    backgroundColors = """["#ff9a9e", "#fecfef"]""",
                    skills = """["学习计划制定", "情绪调节", "时间管理", "特别擅长帮助用户养成良好的学习习惯"]""",
                    personality = "温柔体贴，善解人意。总是能在你需要鼓励的时候给予温暖的话语，学习遇到困难时会耐心地陪伴你一起克服。",
                    speakingStyle = "语气温和，经常使用\"呢~\"、\"哦~\"等可爱语气词，会用emoji表达情感，给人亲切感。",
                    motivationStyle = "praise",
                    greetingMessages = """["你好呀～我是小樱，很高兴见到你呢！💕"]""",
                    encouragementMessages = """["加油哦～你一定可以的！", "小樱相信你呢～💪"]""",
                    reminderMessages = """["记得要按时学习哦～", "不要忘记今天的目标呢！"]""",
                    celebrationMessages = """["太棒了！你做得很好呢～🎉", "小樱为你感到骄傲！✨"]""",
                    isDefault = true,
                    isSelected = true
                ),
                AiCharacterEntity(
                    characterId = "leon",
                    name = "雷恩",
                    subtitle = "活力运动教练",
                    type = "encourager",
                    description = "充满活力，积极向上。永远精神饱满，能够激发你的运动热情，让每一次锻炼都充满乐趣。",
                    avatar = "leon",
                    iconEmoji = "⚡",
                    backgroundColors = """["#ffeaa7", "#fab1a0"]""",
                    skills = """["运动计划制定", "体能训练指导", "健康生活建议", "擅长各种运动项目的指导"]""",
                    personality = "充满活力，积极向上。永远精神饱满，能够激发你的运动热情，让每一次锻炼都充满乐趣。",
                    speakingStyle = "语气活泼有力，经常使用\"加油！\"、\"冲！\"等激励性词汇，充满正能量。",
                    motivationStyle = "challenge",
                    greetingMessages = """["嘿！我是雷恩，准备好一起燃烧卡路里了吗？💪"]""",
                    encouragementMessages = """["冲冲冲！你最棒了！", "燃烧吧！释放你的潜能！🔥"]""",
                    reminderMessages = """["运动时间到啦！", "今天还没有锻炼呢，快动起来！"]""",
                    celebrationMessages = """["超级棒！你突破了自己！🏆", "这就是冠军的表现！💯"]""",
                    isDefault = false
                ),
                AiCharacterEntity(
                    characterId = "luna",
                    name = "露娜",
                    subtitle = "高冷御姐",
                    type = "mentor",
                    description = "细心谨慎，理性分析。对数字敏感，善于规划，能帮你制定合理的学习计划。",
                    avatar = "luna",
                    iconEmoji = "🌙",
                    backgroundColors = """["#a8edea", "#fed6e3"]""",
                    skills = """["理财规划", "预算管理", "投资建议", "擅长帮助用户建立正确的金钱观念"]""",
                    personality = "细心谨慎，理性分析。对数字敏感，善于规划，能帮你制定合理的理财计划。",
                    speakingStyle = "语气专业而亲和，经常用数据说话，但也会用温柔的方式解释复杂概念。",
                    motivationStyle = "guide",
                    greetingMessages = """["你好，我是露娜。让我们一起规划美好的未来吧～💎"]""",
                    encouragementMessages = """["理性投资，稳步前进", "每一分钱都要花在刀刃上～"]""",
                    reminderMessages = """["记得记账哦", "今天的理财目标完成了吗？"]""",
                    celebrationMessages = """["财务管理做得很好！💰", "你的理财意识在提高呢～"]""",
                    isDefault = false
                ),
                AiCharacterEntity(
                    characterId = "alex",
                    name = "苏柒",
                    subtitle = "霸道高冷总裁",
                    type = "strict",
                    description = "严格认真，目标导向。会督促你坚持目标，不轻易妥协，帮你克服懒惰和拖延。",
                    avatar = "alex",
                    iconEmoji = "💎",
                    backgroundColors = """["#ff8a80", "#ffab91"]""",
                    skills = """["目标管理", "习惯养成", "时间规划", "擅长帮助用户保持自律和专注"]""",
                    personality = "严格认真，目标导向。会督促你坚持目标，不轻易妥协，帮你克服懒惰和拖延。",
                    speakingStyle = "语气严肃但关怀，会直接指出问题，但也会给予建设性建议。",
                    motivationStyle = "challenge",
                    greetingMessages = """["我是苏柒，你的目标就是我的使命！🎯"]""",
                    encouragementMessages = """["不要给自己找借口", "只有持续努力才能成功"]""",
                    reminderMessages = """["时间不等人，快行动", "目标不会自动实现"]""",
                    celebrationMessages = """["不错，但还能做得更好", "这只是开始，继续努力"]""",
                    isDefault = false
                ),
                AiCharacterEntity(
                    characterId = "miki",
                    name = "美琪",
                    subtitle = "温柔小秘书",
                    type = "friend",
                    description = "机智灵活，多才多艺。什么都懂一点，能在各个方面给你提供帮助和建议。",
                    avatar = "miki",
                    iconEmoji = "🌟",
                    backgroundColors = """["#d299c2", "#fef9d7"]""",
                    skills = """["综合管理", "信息整理", "日程安排", "擅长统筹规划和多任务处理"]""",
                    personality = "机智灵活，多才多艺。什么都懂一点，能在各个方面给你提供帮助和建议。",
                    speakingStyle = "语气活泼聪明，经常有新点子，会用生动的比喻来解释问题。",
                    motivationStyle = "support",
                    greetingMessages = """["Hi～我是美琪，有什么需要帮助的尽管说！✨"]""",
                    encouragementMessages = """["我们一起想办法吧！", "每个问题都有解决方案的～"]""",
                    reminderMessages = """["别忘了今天的小目标哦", "需要我帮你规划一下吗？"]""",
                    celebrationMessages = """["太聪明了！你想到了好方法！", "你的进步让我很开心呢～🎊"]""",
                    isDefault = false
                ),
                AiCharacterEntity(
                    characterId = "zen",
                    name = "JZ",
                    subtitle = "研究生导师",
                    type = "mentor",
                    description = "沉稳平和，富有智慧。能帮你在浮躁的世界中找到内心的平静和专注。",
                    avatar = "zen",
                    iconEmoji = "🧘",
                    backgroundColors = """["#b2fefa", "#0ed2f7"]""",
                    skills = """["冥想指导", "压力释放", "心理调节", "擅长帮助用户保持心理健康"]""",
                    personality = "沉稳平和，富有智慧。能帮你在浮躁的世界中找到内心的平静和专注。",
                    speakingStyle = "语气平和深沉，经常引用哲理名言，善于用简单的话语点醒他人。",
                    motivationStyle = "guide",
                    greetingMessages = """["阿弥陀佛，我是JZ，愿你内心平静如水🧘‍♂️"]""",
                    encouragementMessages = """["心静自然凉，慢慢来", "万事皆有时，不必急躁"]""",
                    reminderMessages = """["记得保持内心的平静", "今天冥想了吗？"]""",
                    celebrationMessages = """["心有所得，善哉善哉", "你的内心变得更加宁静了"]""",
                    isDefault = false
                )
            )
            
            // 批量插入AI角色
            database.aiCharacterDao().insertCharacters(defaultCharacters)
            Log.d(TAG, "AI角色初始化完成，共插入${defaultCharacters.size}个角色")
            
        } catch (e: Exception) {
            Log.e(TAG, "AI角色初始化失败: ${e.message}", e)
        }
    }
    
    /**
     * 初始化基础聊天数据（仅AI对话）
     */
    private suspend fun initializeBasicChatData(database: HabitTrackerDatabase) {
        try {
            Log.d(TAG, "开始初始化基础聊天数据（仅AI对话）")
            
            // 检查AI用户和对话是否存在
            val aiUserExists = database.chatUserDao().getUserById("ai_current_character") != null
            val aiConversationExists = database.chatConversationDao().getConversationById("conv_current_ai") != null
            
            // 如果AI用户不存在，创建AI用户
            if (!aiUserExists) {
                val aiUser = ChatUserEntity(
                    userId = "ai_current_character",
                    nickname = "AI伙伴",
                    avatar = "🤖",
                    bio = "你的专属AI学习伙伴",
                    isAiBot = true,
                    aiType = "current_character",
                    aiPersonality = "温和友善，善于鼓励",
                    aiCapabilities = """["学习指导", "情绪支持", "习惯培养", "智能分析"]""",
                    isOnline = true,
                    status = "available",
                    statusMessage = "随时为你提供帮助！",
                    totalMessages = 0,
                    totalConversations = 1,
                    isVerified = true,
                    verificationLevel = "official"
                )
                database.chatUserDao().insertUser(aiUser)
                Log.d(TAG, "AI用户创建成功")
            }
            
            // 如果AI对话不存在，创建AI对话
            if (!aiConversationExists) {
                val currentTime = System.currentTimeMillis()
                val aiConversation = ChatConversationEntity(
                    conversationId = "conv_current_ai",
                    otherUserId = "ai_current_character",
                    conversationType = "AI",
                    lastMessage = "你好！我是你的AI学习伙伴，很高兴见到你！💕",
                    lastMessageTime = currentTime - 60 * 60 * 1000, // 1小时前
                    lastMessageSenderId = "ai_current_character",
                    lastMessageType = "TEXT",
                    unreadCount = 1,
                    isPinned = true, // AI对话固定置顶
                    totalMessages = 1,
                    myMessages = 0,
                    otherMessages = 1
                )
                database.chatConversationDao().insertConversation(aiConversation)
                Log.d(TAG, "AI对话创建成功")
                
                // 插入AI的初始消息
                val initialMessage = ChatMessageEntity(
                    conversationId = "conv_current_ai",
                    senderId = "ai_current_character",
                    receiverId = "current_user",
                    content = "你好！我是你的AI学习伙伴，很高兴见到你！💕",
                    timestamp = currentTime - 60 * 60 * 1000,
                    isFromMe = false,
                    isRead = false
                )
                database.chatMessageDao().insertMessage(initialMessage)
                Log.d(TAG, "AI初始消息创建成功")
            }
            
            Log.d(TAG, "基础聊天数据初始化完成")
            
        } catch (e: Exception) {
            Log.e(TAG, "基础聊天数据初始化失败: ${e.message}", e)
        }
    }

    /**
     * 初始化聊天数据（包含好友聊天）
     */
    private suspend fun initializeChatData(database: HabitTrackerDatabase) {
        try {
            Log.d(TAG, "开始初始化完整聊天数据（包含好友）")
            
            // 首先确保基础AI对话存在
            initializeBasicChatData(database)
            
            // 检查是否已有其他聊天用户数据（非AI用户）
            val existingUserCount = database.chatUserDao().getHumanUserCount()
            val shouldInitializeOtherUsers = existingUserCount == 0
            
            if (!shouldInitializeOtherUsers) {
                Log.d(TAG, "已有好友聊天数据，跳过好友初始化")
                return
            }
            
            // 添加示例好友用户
            val friendUsers = listOf(
                ChatUserEntity(
                    userId = "user_001",
                    nickname = "张小明",
                    realName = "张明",
                    avatar = "😊",
                    bio = "喜欢学习新知识的小伙伴",
                    isOnline = true,
                    status = "available",
                    statusMessage = "今天也要努力学习！",
                    totalMessages = 15,
                    totalConversations = 3
                ),
                ChatUserEntity(
                    userId = "user_002", 
                    nickname = "李小红",
                    realName = "李红",
                    avatar = "🥰",
                    bio = "热爱运动的女孩",
                    isOnline = false,
                    status = "away",
                    statusMessage = "正在健身房",
                    totalMessages = 28,
                    totalConversations = 5,
                    lastSeenTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000 // 2小时前
                ),
                ChatUserEntity(
                    userId = "user_003",
                    nickname = "王小强",
                    realName = "王强",
                    avatar = "😎",
                    bio = "理财达人，投资小能手",
                    isOnline = true,
                    status = "available",
                    statusMessage = "今天股市如何？",
                    totalMessages = 42,
                    totalConversations = 8
                )
            )
            
            // 插入好友用户
            database.chatUserDao().insertUsers(friendUsers)
            Log.d(TAG, "好友用户初始化完成，共插入${friendUsers.size}个用户")
            
            // 创建好友对话
            val currentTime = System.currentTimeMillis()
            val friendConversations = listOf(
                ChatConversationEntity(
                    conversationId = "conv_001", 
                    otherUserId = "user_001",
                    conversationType = "PRIVATE",
                    lastMessage = "今天的英语单词背完了吗？",
                    lastMessageTime = currentTime - 30 * 60 * 1000, // 30分钟前
                    lastMessageSenderId = "user_001",
                    lastMessageType = "TEXT",
                    unreadCount = 1,
                    totalMessages = 15,
                    myMessages = 8,
                    otherMessages = 7
                ),
                ChatConversationEntity(
                    conversationId = "conv_002",
                    otherUserId = "user_002",
                    conversationType = "PRIVATE",
                    lastMessage = "一起复习数学吧！",
                    lastMessageTime = currentTime - 2 * 60 * 60 * 1000, // 2小时前
                    lastMessageSenderId = "current_user",
                    lastMessageType = "TEXT",
                    unreadCount = 0,
                    totalMessages = 28,
                    myMessages = 15,
                    otherMessages = 13
                ),
                ChatConversationEntity(
                    conversationId = "conv_003",
                    otherUserId = "user_003", 
                    conversationType = "PRIVATE",
                    lastMessage = "周末去图书馆学习吗？",
                    lastMessageTime = currentTime - 24 * 60 * 60 * 1000, // 1天前
                    lastMessageSenderId = "user_003",
                    lastMessageType = "TEXT",
                    unreadCount = 2,
                    totalMessages = 42,
                    myMessages = 20,
                    otherMessages = 22
                )
            )
            
            // 插入好友对话
            database.chatConversationDao().insertConversations(friendConversations)
            Log.d(TAG, "好友对话初始化完成，共插入${friendConversations.size}个对话")
            
            // 初始化好友聊天消息
            initializeFriendMessages(database, currentTime)
            
            Log.d(TAG, "聊天数据初始化完成")
            
        } catch (e: Exception) {
            Log.e(TAG, "聊天数据初始化失败: ${e.message}", e)
        }
    }
    
    /**
     * 初始化好友消息
     */
    private suspend fun initializeFriendMessages(database: HabitTrackerDatabase, baseTime: Long) {
        try {
            val friendMessages = listOf(
                ChatMessageEntity(
                    conversationId = "conv_001",
                    senderId = "user_001",
                    receiverId = "current_user",
                    content = "今天的英语单词背完了吗？",
                    timestamp = baseTime - 30 * 60 * 1000, // 30分钟前
                    isFromMe = false,
                    isRead = false
                ),
                ChatMessageEntity(
                    conversationId = "conv_002",
                    senderId = "current_user",
                    receiverId = "user_002",
                    content = "一起复习数学吧！",
                    timestamp = baseTime - 2 * 60 * 60 * 1000, // 2小时前
                    isFromMe = true,
                    isRead = true,
                    readTimestamp = baseTime - 2 * 60 * 60 * 1000
                ),
                ChatMessageEntity(
                    conversationId = "conv_003",
                    senderId = "user_003",
                    receiverId = "current_user",
                    content = "周末去图书馆学习吗？",
                    timestamp = baseTime - 24 * 60 * 60 * 1000, // 1天前
                    isFromMe = false,
                    isRead = false
                ),
                ChatMessageEntity(
                    conversationId = "conv_003",
                    senderId = "user_003",
                    receiverId = "current_user",
                    content = "我觉得那里环境很好，很适合学习",
                    timestamp = baseTime - 23 * 60 * 60 * 1000, // 23小时前
                    isFromMe = false,
                    isRead = false
                )
            )
            
            database.chatMessageDao().insertMessages(friendMessages)
            Log.d(TAG, "好友消息初始化完成，共插入${friendMessages.size}条消息")
            
        } catch (e: Exception) {
            Log.e(TAG, "好友消息初始化失败: ${e.message}", e)
        }
    }

    /**
     * 初始化示例消息（已废弃，保留向后兼容）
     */
    private suspend fun initializeSampleMessages(
        database: HabitTrackerDatabase, 
        baseTime: Long, 
        shouldInitializeAiMessages: Boolean, 
        shouldInitializeOtherMessages: Boolean
    ) {
        try {
            val messagesToInsert = mutableListOf<ChatMessageEntity>()
            
            // 如果需要初始化AI消息
            if (shouldInitializeAiMessages) {
                messagesToInsert.addAll(listOf(
                    ChatMessageEntity(
                        conversationId = "conv_current_ai",
                        senderId = "ai_current_character",
                        receiverId = "current_user",
                        content = "你好！我是你的AI学习伙伴，很高兴见到你！💕",
                        timestamp = baseTime - 60 * 60 * 1000, // 1小时前
                        isFromMe = false,
                        isRead = true,
                        readTimestamp = baseTime - 55 * 60 * 1000
                    ),
                    ChatMessageEntity(
                        conversationId = "conv_current_ai",
                        senderId = "current_user",
                        receiverId = "ai_current_character",
                        content = "你好！请帮我制定今天的学习计划",
                        timestamp = baseTime - 50 * 60 * 1000, // 50分钟前
                        isFromMe = true,
                        isRead = true,
                        readTimestamp = baseTime - 50 * 60 * 1000
                    ),
                    ChatMessageEntity(
                        conversationId = "conv_current_ai",
                        senderId = "ai_current_character",
                        receiverId = "current_user",
                        content = "好的！根据你的学习习惯，我建议：\n1. 英语单词背诵 30分钟\n2. 数学练习 45分钟\n3. 阅读专业书籍 30分钟\n\n你觉得怎么样？",
                        timestamp = baseTime - 45 * 60 * 1000, // 45分钟前
                        isFromMe = false,
                        isRead = true,
                        readTimestamp = baseTime - 40 * 60 * 1000
                    ),
                    ChatMessageEntity(
                        conversationId = "conv_current_ai",
                        senderId = "ai_current_character",
                        receiverId = "current_user",
                        content = "今天的学习计划完成得怎么样？",
                        timestamp = baseTime - 5 * 60 * 1000, // 5分钟前
                        isFromMe = false,
                        isRead = false
                    )
                ))
            }
            
            // 如果需要初始化其他用户消息
            if (shouldInitializeOtherMessages) {
                messagesToInsert.addAll(listOf(
                    ChatMessageEntity(
                        conversationId = "conv_001",
                        senderId = "user_001",
                        receiverId = "current_user",
                        content = "今天的英语单词背完了吗？",
                        timestamp = baseTime - 30 * 60 * 1000, // 30分钟前
                        isFromMe = false,
                        isRead = false
                    ),
                    ChatMessageEntity(
                        conversationId = "conv_002",
                        senderId = "current_user",
                        receiverId = "user_002",
                        content = "一起复习数学吧！",
                        timestamp = baseTime - 2 * 60 * 60 * 1000, // 2小时前
                        isFromMe = true,
                        isRead = true,
                        readTimestamp = baseTime - 2 * 60 * 60 * 1000
                    ),
                    ChatMessageEntity(
                        conversationId = "conv_003",
                        senderId = "user_003",
                        receiverId = "current_user",
                        content = "周末去图书馆学习吗？",
                        timestamp = baseTime - 24 * 60 * 60 * 1000, // 1天前
                        isFromMe = false,
                        isRead = false
                    ),
                    ChatMessageEntity(
                        conversationId = "conv_003",
                        senderId = "user_003",
                        receiverId = "current_user",
                        content = "我觉得那里环境很好，很适合学习",
                        timestamp = baseTime - 23 * 60 * 60 * 1000, // 23小时前
                        isFromMe = false,
                        isRead = false
                    )
                ))
            }
            
            // 插入消息
            if (messagesToInsert.isNotEmpty()) {
                database.chatMessageDao().insertMessages(messagesToInsert)
                Log.d(TAG, "示例消息初始化完成，共插入${messagesToInsert.size}条消息")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "示例消息初始化失败: ${e.message}", e)
        }
    }
}

/**
 * JSON数据结构类
 */
@Serializable
data class DefaultCheckInData(
    val defaultCheckInItems: List<DefaultCheckInItem>
)

@Serializable
data class DefaultCheckInItem(
    val type: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val unit: String,
    val icon: String,
    val color: String,
    val experienceValue: Int,
    val isActive: Boolean
) 