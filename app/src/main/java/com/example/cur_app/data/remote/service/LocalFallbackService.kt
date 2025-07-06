package com.example.cur_app.data.remote.service

import com.example.cur_app.data.remote.error.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 本地降级服务
 * 当网络不可用或AI服务不可达时，提供预设的本地响应
 */
@Singleton
class LocalFallbackService @Inject constructor() {
    
    /**
     * 生成本地鼓励消息
     */
    fun generateLocalEncouragement(
        characterType: String,
        habitName: String,
        currentStreak: Int,
        completionRate: Float
    ): NetworkResult<String> {
        val templates = getEncouragementTemplates(characterType)
        val selectedTemplate = templates.random()
        
        val message = selectedTemplate
            .replace("{habitName}", habitName)
            .replace("{streak}", currentStreak.toString())
            .replace("{rate}", "${(completionRate * 100).toInt()}%")
        
        return NetworkResult.Success(message)
    }
    
    /**
     * 生成本地提醒消息
     */
    fun generateLocalReminder(
        characterType: String,
        habitName: String,
        missedDays: Int
    ): NetworkResult<String> {
        val templates = getReminderTemplates(characterType, missedDays)
        val selectedTemplate = templates.random()
        
        val message = selectedTemplate
            .replace("{habitName}", habitName)
            .replace("{missedDays}", missedDays.toString())
        
        return NetworkResult.Success(message)
    }
    
    /**
     * 生成本地聊天回复
     */
    fun generateLocalChat(
        characterType: String,
        userMessage: String
    ): NetworkResult<String> {
        val responses = getChatResponses(characterType, userMessage)
        val selectedResponse = responses.random()
        
        return NetworkResult.Success(selectedResponse)
    }
    
    /**
     * 生成庆祝消息
     */
    fun generateLocalCelebration(
        characterType: String,
        habitName: String,
        achievement: String
    ): NetworkResult<String> {
        val templates = getCelebrationTemplates(characterType)
        val selectedTemplate = templates.random()
        
        val message = selectedTemplate
            .replace("{habitName}", habitName)
            .replace("{achievement}", achievement)
        
        return NetworkResult.Success(message)
    }
    
    /**
     * 获取鼓励消息模板
     */
    private fun getEncouragementTemplates(characterType: String): List<String> {
        return when (characterType) {
            "encourager" -> listOf(
                "太棒了！你在{habitName}上已经坚持{streak}天了，完成率达到{rate}！",
                "你的坚持让我感到骄傲！{habitName}完成率{rate}，继续加油！",
                "每一天的努力都在积累，{habitName}已经{streak}天了，你做得很好！",
                "看到你在{habitName}上的坚持，我为你感到高兴！",
                "你的努力有目共睹，{habitName}完成率{rate}，继续保持！"
            )
            "strict" -> listOf(
                "数据显示你在{habitName}上完成率{rate}，还有提升空间。",
                "{habitName}坚持{streak}天，但要保持稳定性才能形成真正的习惯。",
                "完成率{rate}是个不错的开始，但要达到90%以上才算真正养成习惯。",
                "坚持{streak}天值得肯定，现在要关注质量而不仅仅是数量。",
                "{habitName}的进展可以，但需要更严格的执行标准。"
            )
            "friend" -> listOf(
                "哇！{habitName}已经{streak}天了，你简直是我见过最厉害的！",
                "看看这个完成率{rate}，明天我们一起庆祝一下吧！",
                "朋友，你在{habitName}上的表现让我刮目相看！",
                "你知道吗？坚持{streak}天{habitName}已经超过很多人了！",
                "完成率{rate}？这简直太棒了，我要向你学习！"
            )
            "mentor" -> listOf(
                "在{habitName}上坚持{streak}天体现了你的自律能力，这是成功的基石。",
                "完成率{rate}反映了你的执行力，持续改进将带来质的飞跃。",
                "每个习惯的养成都是性格的塑造，{habitName}让你变得更好。",
                "坚持{streak}天的过程中，你收获的不仅是习惯，更是内心的力量。",
                "{habitName}完成率{rate}，这正是通往卓越的道路。"
            )
            else -> listOf(
                "你在{habitName}上的坚持很不错，继续加油！",
                "完成率{rate}，你的努力值得肯定！",
                "坚持{streak}天了，你做得很好！"
            )
        }
    }
    
    /**
     * 获取提醒消息模板
     */
    private fun getReminderTemplates(characterType: String, missedDays: Int): List<String> {
        return when (characterType) {
            "encourager" -> when {
                missedDays == 0 -> listOf(
                    "新的一天开始了，记得完成今天的{habitName}哦！",
                    "相信你今天也能很好地完成{habitName}！",
                    "今天也要记得{habitName}，你一定可以的！"
                )
                missedDays <= 2 -> listOf(
                    "没关系，大家都有忙碌的时候，今天重新开始{habitName}吧！",
                    "短暂的休息后，现在是重新开始{habitName}的好时机！",
                    "不要气馁，今天就让我们重新回到{habitName}的轨道上！"
                )
                else -> listOf(
                    "我知道最近可能很忙，但{habitName}可以帮你重新找回节奏！",
                    "虽然已经{missedDays}天了，但现在开始永远不晚！",
                    "每个人都会有低潮期，今天开始重新建立{habitName}的习惯吧！"
                )
            }
            "strict" -> when {
                missedDays == 0 -> listOf(
                    "按计划执行{habitName}，保持你的承诺。",
                    "今天的{habitName}任务不容拖延。",
                    "坚持就是胜利，完成今天的{habitName}。"
                )
                missedDays <= 2 -> listOf(
                    "已经{missedDays}天没有{habitName}了，需要立即行动。",
                    "拖延只会让习惯更难养成，马上完成{habitName}。",
                    "失误{missedDays}天，现在必须重新严格执行{habitName}。"
                )
                else -> listOf(
                    "{missedDays}天的间断已经严重影响了习惯养成，必须立即重启。",
                    "长时间的中断（{missedDays}天）需要重新制定{habitName}计划。",
                    "已经{missedDays}天了，如果再不开始{habitName}，之前的努力就白费了。"
                )
            }
            "friend" -> when {
                missedDays == 0 -> listOf(
                    "嘿朋友，今天的{habitName}别忘了哦！",
                    "今天也要记得{habitName}，我们一起加油！",
                    "新的一天，新的开始，{habitName}走起！"
                )
                missedDays <= 2 -> listOf(
                    "没事没事，谁还没个忙的时候，{habitName}继续！",
                    "休息了{missedDays}天，现在重新开始{habitName}吧！",
                    "朋友，{habitName}等你呢，一起重新开始！"
                )
                else -> listOf(
                    "我知道你最近很忙，但{habitName}真的很重要呢！",
                    "好久没{habitName}了（{missedDays}天），我都想你了！",
                    "朋友，{habitName}是个好习惯，我们重新开始吧！"
                )
            }
            "mentor" -> when {
                missedDays == 0 -> listOf(
                    "今日事今日毕，记得完成{habitName}。",
                    "每一天的{habitName}都是对自己的投资。",
                    "保持节奏，今天的{habitName}很重要。"
                )
                missedDays <= 2 -> listOf(
                    "短暂的中断并不意味着失败，重新开始{habitName}。",
                    "从{missedDays}天的停顿中总结经验，重新投入{habitName}。",
                    "暂停{missedDays}天后，现在是重建{habitName}习惯的最佳时机。"
                )
                else -> listOf(
                    "{missedDays}天的间隔让我们反思{habitName}在生活中的位置。",
                    "长时间的中断往往反映了优先级的问题，重新审视{habitName}的重要性。",
                    "经过{missedDays}天，是时候重新建立{habitName}的习惯了。"
                )
            }
            else -> listOf(
                "记得完成今天的{habitName}！",
                "今天也要坚持{habitName}哦！",
                "不要忘记{habitName}！"
            )
        }
    }
    
    /**
     * 获取庆祝消息模板
     */
    private fun getCelebrationTemplates(characterType: String): List<String> {
        return when (characterType) {
            "encourager" -> listOf(
                "恭喜你达成了{achievement}！你的坚持真的很棒！",
                "太令人激动了！{achievement}的成就让我为你骄傲！",
                "你成功了！{achievement}是你努力的最好证明！"
            )
            "strict" -> listOf(
                "达成{achievement}，这是严格执行的结果。",
                "数据证明了你的努力，{achievement}值得肯定。",
                "{achievement}的成果体现了你的自律性。"
            )
            "friend" -> listOf(
                "哇！{achievement}！太厉害了，我们一起庆祝吧！",
                "朋友你太棒了！{achievement}简直不敢相信！",
                "看看这个{achievement}，你简直是我的榜样！"
            )
            "mentor" -> listOf(
                "{achievement}标志着你在自我管理上的重要进步。",
                "达成{achievement}的过程比结果更珍贵。",
                "{achievement}是你成长路上的重要里程碑。"
            )
            else -> listOf(
                "恭喜你达成{achievement}！",
                "太棒了，{achievement}！",
                "你成功了！{achievement}！"
            )
        }
    }
    
    /**
     * 获取聊天回复模板
     */
    private fun getChatResponses(characterType: String, userMessage: String): List<String> {
        // 简单的关键词匹配，实际中可以使用更复杂的NLP处理
        val lowerMessage = userMessage.lowercase()
        
        return when {
            // 问候类
            lowerMessage.contains("你好") || lowerMessage.contains("在吗") || lowerMessage.contains("hi") || lowerMessage.contains("hello") -> {
                getGreetingResponses(characterType)
            }
            // 情绪类
            lowerMessage.contains("累") || lowerMessage.contains("焦虑") || lowerMessage.contains("压力") || lowerMessage.contains("难过") -> {
                getEmotionalSupportResponses(characterType)
            }
            // 习惯相关
            lowerMessage.contains("习惯") || lowerMessage.contains("坚持") || lowerMessage.contains("打卡") || lowerMessage.contains("运动") -> {
                getHabitRelatedResponses(characterType)
            }
            // 心情好
            lowerMessage.contains("开心") || lowerMessage.contains("高兴") || lowerMessage.contains("好") -> {
                getPositiveResponses(characterType)
            }
            // 感谢类
            lowerMessage.contains("谢谢") || lowerMessage.contains("感谢") -> {
                getThankResponses(characterType)
            }
            // 默认通用回复
            else -> {
                getGeneralResponses(characterType)
            }
        }
    }
    
    private fun getGreetingResponses(characterType: String): List<String> {
        return when (characterType) {
            "encourager" -> listOf(
                "你好！很高兴见到你！今天的你看起来充满活力呢！",
                "夥上好！希望你今天也能充满正能量！",
                "嗨，我在呢！随时为你加油鼓劲！"
            )
            "strict" -> listOf(
                "你好。希望你今天能按计划执行你的任务。",
                "早上好。记住，时间很宝贵，不要浪费。",
                "我在。你今天的目标完成情况如何？"
            )
            "friend" -> listOf(
                "嗨嗨！小伙伴！怎么样，今天心情好吗？",
                "哦哦，你来啦！有什么有趣的事情要分享吗？",
                "在在在！哈喽，找我聊天呢！"
            )
            "mentor" -> listOf(
                "你好。很高兴你主动来交流，这说明你有成长的意愿。",
                "欢迎。你的每一次主动交流都是对自我的投资。",
                "你好。今天有什么想要探讨的吗？"
            )
            else -> listOf(
                "你好！很高兴见到你！",
                "嗨，我在呢！有什么可以帮你的吗？"
            )
        }
    }
    
    private fun getEmotionalSupportResponses(characterType: String): List<String> {
        return when (characterType) {
            "encourager" -> listOf(
                "我理解你的感受。每个人都会有累的时候，但你已经做得很好了。",
                "辛苦了，但这些都会过去的。你比想象中更坚强。",
                "没关系，允许自己有低潮的时候。休息一下，明天又是新的开始。"
            )
            "strict" -> listOf(
                "困难是成长的必经之路。现在的痛苦将成为未来的财富。",
                "压力是成功的伴侣。关键是如何管理和利用它。",
                "这是测试你意志力的时刻。坚持下去，你会变得更强。"
            )
            "friend" -> listOf(
                "嗨，别太为难自己了。我们可以一起找些轻松的事情做做。",
                "累的时候就好好休息，没必要逗自己。我陈你聊天！",
                "没事的，每个人都有不在状态的时候。我支持你！"
            )
            "mentor" -> listOf(
                "困难时刻往往是最好的学习机会。问问自己能从中学到什么。",
                "情绪是信号，不是指令。允许自己感受，但不要被它控制。",
                "这些感受都是成长过程的一部分。记住，你比问题更强大。"
            )
            else -> listOf(
                "理解你的感受。每个人都会有这样的时候。",
                "辛苦了。休息一下，给自己一些时间。"
            )
        }
    }
    
    private fun getHabitRelatedResponses(characterType: String): List<String> {
        return when (characterType) {
            "encourager" -> listOf(
                "习惯的力量真的很神奇！你的每一次坚持都在积累力量。",
                "坚持一个习惯真的不容易，但你做得很棒！",
                "每一次打卡都是对未来自己的投资，你在做一件伟大的事！"
            )
            "strict" -> listOf(
                "习惯需要系统性的坚持。偶尔的成功不叫习惯。",
                "真正的习惯形成需要至少21天，但稳固需要66天。",
                "数据显示，稳定的执行比间歇性的大量行动更有效。"
            )
            "friend" -> listOf(
                "哇，看到你这么努力培养习惯，我都想向你学习了！",
                "习惯这东西真的太神奇了，一旦养成就像超能力一样！",
                "我们一起加油！互相监督，一起变更好！"
            )
            "mentor" -> listOf(
                "习惯是复利的力量。小的改变，长期坚持，就能产生巨大的变化。",
                "真正的成长来自于日复一日的小进步。你正在走在正确的道路上。",
                "习惯塑造性格，性格决定命运。你正在控制自己的未来。"
            )
            else -> listOf(
                "习惯的力量很强大，你做得很好！",
                "坚持下去，你一定能看到变化！"
            )
        }
    }
    
    private fun getPositiveResponses(characterType: String): List<String> {
        return when (characterType) {
            "encourager" -> listOf(
                "太棒了！你的好心情让我也感到开心！",
                "哇，你的正能量真的会传染呢！继续保持这种状态！",
                "感受到你的好心情了！这样的你真的很棒！"
            )
            "strict" -> listOf(
                "良好的心态是成功的基础。保持这种状态。",
                "积极的态度会带来积极的结果。继续加油。",
                "好的状态要维持，不要因为一时的顺利而放松警惕。"
            )
            "friend" -> listOf(
                "哈哈，看到你这么开心我也超级开心！",
                "太好了！你的好心情让整个世界都变亮了！",
                "哇哦，这种正能量要持续保持哦！我们一起快乐！"
            )
            "mentor" -> listOf(
                "内心的平静和喜悦是智慧的体现。珍惜这种状态。",
                "正面的情绪能提高我们的思维能力。利用好这个状态。",
                "这种心境很珍贵。记住这种感觉，它会成为你的内在资源。"
            )
            else -> listOf(
                "太好了！你的好心情让我也很开心！",
                "哇，这种正能量真的很棒！"
            )
        }
    }
    
    private fun getThankResponses(characterType: String): List<String> {
        return when (characterType) {
            "encourager" -> listOf(
                "不用谢！能帮到你我就很开心了！",
                "这是我应该做的！能与你一起成长真的很棒！",
                "哈哈，不客气！我们是一个团队呀！"
            )
            "strict" -> listOf(
                "这是我的职责。你的进步才是最好的回报。",
                "认真执行计划比感谢更重要。",
                "不需要言谢，用行动证明你的成长就够了。"
            )
            "friend" -> listOf(
                "哈哈，朋友之间说什么谢谢呀！",
                "嗨嗨，我们是好朋友呀，互相帮助很正常！",
                "不用谢啦！朋友就要互相支持嘉！"
            )
            "mentor" -> listOf(
                "你的感激之情我感受到了。但记住，真正的成长来自你自己。",
                "我只是提供了一些建议，真正的执行者是你。",
                "学会感恩是智慧的表现。但更重要的是把这种好意传递下去。"
            )
            else -> listOf(
                "不用谢！能帮到你我很开心！",
                "不客气，我们一起加油！"
            )
        }
    }
    
    private fun getGeneralResponses(characterType: String): List<String> {
        return when (characterType) {
            "encourager" -> listOf(
                "我一直都在支持你！有什么想说的尽管说吧！",
                "每个人都有自己的节奏，你只需要做好自己就够了！",
                "不管你说什么，我都会认真倾听的！"
            )
            "strict" -> listOf(
                "请明确你的问题或目标，这样我才能给出有效的建议。",
                "时间是最宝贵的资源。请告诉我你需要什么帮助。",
                "有具体的问题吗？我可以给你一些实用的建议。"
            )
            "friend" -> listOf(
                "哈哈，怎么了？有什么想聊的吗？",
                "嗨，我在呢！你说什么我都想听！",
                "你说呀，朋友之间什么都可以谈呀！"
            )
            "mentor" -> listOf(
                "每一次对话都是学习的机会。你想探讨什么问题？",
                "我在这里分享经验，也在学习新知识。请说吧。",
                "思考是成长的起点。你最近在思考什么？"
            )
            else -> listOf(
                "我在听，你想说什么？",
                "有什么可以帮你的吗？",
                "我们可以聊聊任何你想说的。"
            )
        }
    }
} 