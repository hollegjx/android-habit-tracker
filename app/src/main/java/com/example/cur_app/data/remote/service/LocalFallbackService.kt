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
} 