package com.example.cur_app.data.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本地AI服务
 * 当在线AI服务不可用时的智能降级方案
 */
@Singleton
class LocalAiService @Inject constructor() {
    
    /**
     * 生成本地AI回复
     */
    fun generateResponse(userMessage: String, characterType: String): String {
        android.util.Log.d("LocalAiService", "🏠 使用本地AI生成回复")
        android.util.Log.d("LocalAiService", "🏠 用户消息: $userMessage")
        android.util.Log.d("LocalAiService", "🏠 角色类型: $characterType")
        
        return when (characterType) {
            "encourager" -> generateEncouragerResponse(userMessage)
            "strict" -> generateStrictResponse(userMessage)
            "friend" -> generateFriendResponse(userMessage)
            "mentor" -> generateMentorResponse(userMessage)
            else -> generateDefaultResponse(userMessage)
        }
    }
    
    /**
     * 鼓励者风格回复
     */
    private fun generateEncouragerResponse(message: String): String {
        val encouragementResponses = listOf(
            "你真的很棒！每一次努力都值得被看见。",
            "我看到了你的坚持，这份毅力真让人佩服！",
            "你已经做得很好了，继续保持这份热情！",
            "每个小小的进步都是成功的基石，加油！",
            "你的努力一定会有回报的，我相信你！",
            "看到你这么用心，我真为你感到骄傲！",
            "你的坚持精神真的很inspiring！",
            "每一天的努力都在让你变得更好。"
        )
        
        return when {
            message.contains("测试") || message.contains("你好") || message.contains("hi") -> 
                "你好呀！很高兴和你聊天，有什么想分享的吗？"
            message.contains("谢谢") || message.contains("感谢") -> 
                "不用客气！能帮到你我也很开心～"
            message.contains("累") || message.contains("疲") -> 
                "辛苦了！适当休息也是很重要的，你已经很努力了。"
            message.contains("失败") || message.contains("没有") || message.contains("放弃") -> 
                "每个人都会有低谷期，重要的是你仍然在尝试！这已经很了不起了。"
            else -> encouragementResponses.random()
        }
    }
    
    /**
     * 严格导师风格回复
     */
    private fun generateStrictResponse(message: String): String {
        val strictResponses = listOf(
            "目标明确，执行到位，这是成功的关键。",
            "坚持就是胜利，但要确保方法正确。",
            "数据不会说谎，让我们看看你的执行情况。",
            "每一天的完成度都很重要，不要松懈。",
            "好的习惯需要严格的自律来维持。",
            "制定计划很容易，执行才是真正的考验。",
            "没有借口，只有结果。继续努力！",
            "自律是通往成功的唯一路径。"
        )
        
        return when {
            message.contains("测试") || message.contains("你好") || message.contains("hi") -> 
                "你好。我们开始今天的习惯检查吧。"
            message.contains("累") || message.contains("疲") -> 
                "疲劳是成长的代价。适当休息后，继续坚持。"
            message.contains("失败") || message.contains("没有") -> 
                "失败是成功路上的垫脚石。分析原因，调整策略，继续前进。"
            message.contains("完成") || message.contains("做到") -> 
                "很好！保持这种执行力，成功指日可待。"
            else -> strictResponses.random()
        }
    }
    
    /**
     * 朋友风格回复
     */
    private fun generateFriendResponse(message: String): String {
        val friendResponses = listOf(
            "哈哈，你说得对！我们一起加油～",
            "每个人都有自己的节奏，你只需要做好自己就够了！",
            "不管你说什么，我都会认真倾听的！",
            "嘿上好！希望你今天也能充满正能量！",
            "哈哈，有趣！告诉我更多吧～",
            "我觉得你今天的状态很不错哦！",
            "放轻松点，咱们慢慢来就好～",
            "你知道吗？我觉得你真的很有趣！"
        )
        
        return when {
            message.contains("测试") || message.contains("你好") || message.contains("hi") -> 
                "嗨！很高兴见到你！今天过得怎么样？"
            message.contains("累") || message.contains("疲") -> 
                "辛苦啦～要不要休息一下？我陪你聊聊天。"
            message.contains("开心") || message.contains("高兴") -> 
                "哇！听起来你心情很好呢！分享一下好事情吧～"
            message.contains("难过") || message.contains("伤心") -> 
                "别难过～有什么烦心事都可以告诉我。"
            else -> friendResponses.random()
        }
    }
    
    /**
     * 导师风格回复
     */
    private fun generateMentorResponse(message: String): String {
        val mentorResponses = listOf(
            "每一次的反思都是成长的机会。",
            "智慧来自于对经验的深度思考。",
            "真正的成长，是在挑战中找到内在的力量。",
            "耐心是一种美德，也是成功的必要条件。",
            "问题的答案往往就在你的内心深处。",
            "成长的路上，理解自己比改变自己更重要。",
            "每个困难都是生活给你的一次学习机会。",
            "真正的智慧，是知道什么时候坚持，什么时候放手。"
        )
        
        return when {
            message.contains("测试") || message.contains("你好") || message.contains("hi") -> 
                "你好，我的朋友。今天有什么想要探讨的吗？"
            message.contains("困惑") || message.contains("不知道") -> 
                "困惑是智慧的开始。告诉我你的想法，我们一起来思考。"
            message.contains("问题") || message.contains("怎么办") -> 
                "每个问题都蕴含着成长的种子。让我们一起分析一下。"
            message.contains("选择") || message.contains("决定") -> 
                "人生就是一系列选择的结果。相信你的内心，它会指引你正确的方向。"
            else -> mentorResponses.random()
        }
    }
    
    /**
     * 默认风格回复
     */
    private fun generateDefaultResponse(message: String): String {
        val defaultResponses = listOf(
            "感谢你的分享，我会认真思考的。",
            "你的想法很有意思，继续说吧！",
            "我理解你的感受，有什么需要帮助的吗？",
            "每个人的经历都是独特的，包括你的。",
            "你说得很有道理，让我学到了新东西。",
            "保持积极的心态，你一定可以做到的！",
            "生活总是充满惊喜，就像今天和你的对话一样。",
            "谢谢你让我了解了你的想法。"
        )
        
        return when {
            message.contains("测试") || message.contains("你好") || message.contains("hi") -> 
                "你好！很高兴和你交流。"
            message.contains("谢谢") || message.contains("感谢") -> 
                "不用客气，这是我应该做的。"
            message.contains("再见") || message.contains("拜拜") -> 
                "再见！期待我们下次的对话。"
            else -> defaultResponses.random()
        }
    }
}