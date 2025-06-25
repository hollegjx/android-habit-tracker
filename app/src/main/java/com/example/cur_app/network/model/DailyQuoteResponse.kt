package com.example.cur_app.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * 每日一句API响应数据模型
 * 对应API返回的JSON结构
 */
@Serializable
data class DailyQuoteResponse(
    @SerialName("id")
    val id: Int,
    
    @SerialName("yiyan")
    val content: String,
    
    @SerialName("createTime")
    val createTime: Long,
    
    @SerialName("nick")
    val author: String
)

/**
 * 每日一句业务模型
 * 用于UI展示的简化模型
 */
data class DailyQuote(
    val content: String,
    val author: String,
    val createTime: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 从API响应转换为业务模型
         */
        fun fromResponse(response: DailyQuoteResponse): DailyQuote {
            return DailyQuote(
                content = response.content,
                author = response.author,
                createTime = response.createTime
            )
        }
        
        /**
         * 默认语录（网络请求失败时使用）
         */
        fun getDefault(): DailyQuote {
            return DailyQuote(
                content = "今日努力一分，明日成功十分。",
                author = "励志语录"
            )
        }
        
        /**
         * 获取本地备用语录
         */
        fun getLocalQuotes(): List<DailyQuote> {
            return listOf(
                DailyQuote("成功不是终点，失败也不是终结，唯有勇气才是永恒。", "丘吉尔"),
                DailyQuote("生活不是等待暴风雨过去，而是要学会在雨中跳舞。", "维维安·格林"),
                DailyQuote("今天你不努力，明天努力也晚了。", "佚名"),
                DailyQuote("心中有梦想，脚下有力量。", "佚名"),
                DailyQuote("每一次跌倒都是成长，每一次努力都有意义。", "佚名"),
                DailyQuote("改变自己，从现在开始。", "佚名"),
                DailyQuote("坚持不懈，直至成功。", "佚名"),
                DailyQuote("梦想不会发光，发光的是追梦的你。", "佚名")
            )
        }
    }
} 