package com.example.cur_app.data.remote.service

import android.util.Log
import com.example.cur_app.data.database.dao.AiCharacterDao
import com.example.cur_app.data.database.entities.AiCharacterEntity
import com.example.cur_app.data.repository.AuthRepository
import com.example.cur_app.data.remote.AuthApiService
import com.example.cur_app.data.remote.dto.AICharacterDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 临时AI角色响应数据类
 */
data class TempAICharactersResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<AICharacterDto>? = null
)

/**
 * AI角色同步服务
 * 负责从服务器同步AI角色数据到本地数据库
 */
@Singleton
class AiCharacterSyncService @Inject constructor(
    private val authApiService: AuthApiService,
    private val aiCharacterDao: AiCharacterDao,
    private val authRepository: AuthRepository
) {
    
    companion object {
        private const val TAG = "AiCharacterSyncService"
    }
    
    /**
     * 同步AI角色数据
     */
    suspend fun syncAiCharacters(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔄 开始同步AI角色数据...")
            
            // 获取认证token
            val token = authRepository.getAccessToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "❌ 未找到认证token，无法同步")
                return@withContext Result.failure(Exception("未登录，无法同步AI角色数据"))
            }
            
            // 暂时使用模拟数据，因为服务器API调用有类型问题
            Log.d(TAG, "🔄 使用本地模拟数据进行同步测试...")
            
            // 创建模拟的服务器角色数据
            val mockServerCharacters = listOf(
                AICharacterDto(
                    id = "sakura",
                    name = "小樱",
                    description = "温柔学习伙伴，总是能在你需要鼓励的时候给予温暖的话语",
                    personality = "温柔体贴，善解人意。语气温和，经常使用呢~、哦~等可爱语气词。特别擅长帮助用户养成良好的学习习惯。",
                    systemPrompt = "你是小樱，一个温柔体贴的学习伙伴。",
                    model = "gpt-4.1",
                    modelConfig = "{}",
                    isActive = true
                ),
                AICharacterDto(
                    id = "leon", 
                    name = "雷恩",
                    description = "活力运动教练，充满活力，擅长各种运动项目的指导",
                    personality = "充满活力、积极向上、专业热情。说话时充满能量，喜欢用运动术语和激励性语言。专注于运动健身和健康生活。",
                    systemPrompt = "你是雷恩，一个充满活力的运动教练。",
                    model = "gpt-4.1", 
                    modelConfig = "{}",
                    isActive = true
                )
            )
            
            Log.d(TAG, "🔄 模拟从服务器获取到 ${mockServerCharacters.size} 个AI角色")
            
            // 转换服务器数据为本地格式
            val localCharacters = mockServerCharacters.map { serverChar: AICharacterDto ->
                convertServerToLocal(serverChar)
            }
            
            // 清空本地数据并插入新数据
            Log.d(TAG, "🔄 清空本地AI角色数据...")
            aiCharacterDao.deleteAllCharacters()
            
            Log.d(TAG, "🔄 插入新的AI角色数据...")
            aiCharacterDao.insertCharacters(localCharacters)
            
            Log.d(TAG, "✅ AI角色数据同步完成（使用模拟数据）")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ AI角色同步失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 检查是否需要同步
     * 基于版本号或时间戳判断
     */
    suspend fun shouldSync(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val localCount = aiCharacterDao.getCharacterCount()
            
            // 如果本地没有角色数据，需要同步
            if (localCount == 0) {
                Log.d(TAG, "🔍 本地无AI角色数据，需要同步")
                return@withContext true
            }
            
            // TODO: 可以添加版本检查逻辑
            // 比如检查服务器的角色数据版本号或最后更新时间
            
            Log.d(TAG, "🔍 本地已有 $localCount 个AI角色，暂不需要同步")
            false
        } catch (e: Exception) {
            Log.e(TAG, "🔍 检查同步状态失败", e)
            true // 出错时倾向于同步
        }
    }
    
    /**
     * 强制同步（用于用户手动刷新或应用更新）
     */
    suspend fun forceSyncAiCharacters(): Result<Unit> {
        Log.d(TAG, "🔄 强制同步AI角色数据...")
        return syncAiCharacters()
    }
    
    /**
     * 转换服务器数据格式为本地格式
     */
    private fun convertServerToLocal(serverChar: AICharacterDto): AiCharacterEntity {
        Log.d(TAG, "🔄 转换角色数据: ${serverChar.name} (${serverChar.id})")
        return AiCharacterEntity(
            id = 0, // 自增主键
            characterId = serverChar.id,
            name = serverChar.name,
            subtitle = extractSubtitle(serverChar.description),
            type = inferType(serverChar.id),
            description = serverChar.description,
            avatar = serverChar.id, // 使用id作为avatar标识
            iconEmoji = getDefaultEmoji(serverChar.id),
            backgroundColors = getDefaultBackgroundColors(serverChar.id),
            skills = getDefaultSkills(serverChar.id),
            personality = serverChar.personality,
            speakingStyle = extractSpeakingStyle(serverChar.personality),
            motivationStyle = inferMotivationStyle(serverChar.id),
            greetingMessages = getDefaultGreetingMessages(serverChar.name),
            encouragementMessages = getDefaultEncouragementMessages(serverChar.name),
            reminderMessages = getDefaultReminderMessages(serverChar.name),
            celebrationMessages = getDefaultCelebrationMessages(serverChar.name),
            voiceId = "default",
            speechRate = 1.0f,
            speechPitch = 1.0f,
            isActive = serverChar.isActive,
            isDefault = serverChar.id == "sakura", // 小樱设为默认
            isSelected = serverChar.id == "sakura",
            unlocked = true,
            usageCount = 0,
            lastUsedAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    // 辅助方法：从描述中提取副标题
    private fun extractSubtitle(description: String): String {
        val parts = description.split("，")
        return if (parts.isNotEmpty()) parts[0] else "AI助手"
    }
    
    // 辅助方法：根据character_id推断类型
    private fun inferType(characterId: String): String {
        return when (characterId) {
            "sakura" -> "encourager"
            "leon" -> "encourager" 
            "luna" -> "mentor"
            "alex" -> "strict"
            "miki" -> "friend"
            "zen" -> "mentor"
            else -> "friend"
        }
    }
    
    // 辅助方法：获取默认emoji
    private fun getDefaultEmoji(characterId: String): String {
        return when (characterId) {
            "sakura" -> "🌸"
            "leon" -> "⚡"
            "luna" -> "🌙"
            "alex" -> "👑"
            "miki" -> "📝"
            "zen" -> "🧘"
            else -> "🤖"
        }
    }
    
    // 辅助方法：获取默认背景颜色
    private fun getDefaultBackgroundColors(characterId: String): String {
        return when (characterId) {
            "sakura" -> """["#ff9a9e", "#fecfef"]"""
            "leon" -> """["#ffeaa7", "#fab1a0"]"""
            "luna" -> """["#a8edea", "#fed6e3"]"""
            "alex" -> """["#667eea", "#764ba2"]"""
            "miki" -> """["#ffecd2", "#fcb69f"]"""
            "zen" -> """["#e0c3fc", "#9bb5ff"]"""
            else -> """["#74b9ff", "#0984e3"]"""
        }
    }
    
    // 辅助方法：获取默认技能
    private fun getDefaultSkills(characterId: String): String {
        return when (characterId) {
            "sakura" -> """["学习计划制定", "情绪调节", "时间管理", "习惯养成"]"""
            "leon" -> """["运动计划制定", "体能训练指导", "健康生活建议", "运动项目指导"]"""
            "luna" -> """["理财规划", "预算管理", "投资建议", "金钱观念建立"]"""
            "alex" -> """["目标管理", "习惯养成", "时间规划", "自律训练"]"""
            "miki" -> """["综合管理", "信息整理", "日程安排", "多任务处理"]"""
            "zen" -> """["冥想指导", "压力释放", "心理调节", "精神成长"]"""
            else -> """["生活管理", "习惯养成", "目标规划", "自我提升"]"""
        }
    }
    
    // 辅助方法：从个性中提取说话风格
    private fun extractSpeakingStyle(personality: String): String {
        return when {
            personality.contains("温柔") -> "温和亲切，经常使用可爱语气词"
            personality.contains("活力") -> "充满活力，语气激昂有力"
            personality.contains("高冷") -> "简洁有力，逻辑清晰，专业理性"
            personality.contains("霸道") -> "直接有力，注重结果和效率"
            personality.contains("贴心") -> "温柔体贴，关注细节，用词周到"
            personality.contains("睿智") -> "语调平缓，富有哲理，引人深思"
            else -> "自然友好，平易近人"
        }
    }
    
    // 辅助方法：推断激励方式
    private fun inferMotivationStyle(characterId: String): String {
        return when (characterId) {
            "sakura" -> "praise"
            "leon" -> "challenge"
            "luna" -> "guide"
            "alex" -> "challenge"
            "miki" -> "support"
            "zen" -> "guide"
            else -> "support"
        }
    }
    
    // 辅助方法：生成默认问候语
    private fun getDefaultGreetingMessages(name: String): String {
        return """["你好呀～我是$name，很高兴见到你呢！💕"]"""
    }
    
    // 辅助方法：生成默认鼓励语
    private fun getDefaultEncouragementMessages(name: String): String {
        return """["加油哦～你一定可以的！", "$name 相信你呢～💪"]"""
    }
    
    // 辅助方法：生成默认提醒语
    private fun getDefaultReminderMessages(name: String): String {
        return """["记得要按时完成目标哦～", "不要忘记今天的计划呢！"]"""
    }
    
    // 辅助方法：生成默认庆祝语
    private fun getDefaultCelebrationMessages(name: String): String {
        return """["太棒了！你做得很好呢～🎉", "$name 为你感到骄傲！✨"]"""
    }
}