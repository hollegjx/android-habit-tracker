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
         * 获取本地备用语录（正能量名人名言）
         */
        fun getLocalQuotes(): List<DailyQuote> {
            return listOf(
                DailyQuote("做人就像蜡烛一样，有一分热，发一分光，给人以光明，给以温暖。", "肖楚女"),
                DailyQuote("最可怕的敌人，就是没有坚强的信念。", "罗曼·罗兰"),
                DailyQuote("一堆沙子是松散的，可是它和水泥、石子、水混合后，比花岗岩还坚韧。", "王杰"),
                DailyQuote("只要能培一朵花，就不妨做做会朽的腐草。", "鲁迅"),
                DailyQuote("有教养的头脑的第一个标志就是善于提问。", "普列汉诺夫"),
                DailyQuote("有百折不挠的信念的所支持的人的意志，比那些似乎是无敌的物质力量有更强大的威力。", "爱因斯坦"),
                DailyQuote("游手好闲的学习并不比学习游手好闲好。", "约·贝勒斯"),
                DailyQuote("永没没有人力可以击退一个坚决强毅的希望。", "金斯莱"),
                DailyQuote("意志坚强的人能把世界放在手中像泥块一样任意揉捏。", "歌德"),
                DailyQuote("艺术的大道上荆棘丛生，这也是好事，常人望而却步，只有意志坚强的人例外。", "雨果"),
                DailyQuote("一生奉献于两个神明，即荣誉与英勇。", "蒙森"),
                DailyQuote("只要持续地努力，不懈地奋斗，就没有征服不了的东西。", "塞内加"),
                DailyQuote("知不足者好学，耻下问者自满。", "林逋《省心录》"),
                DailyQuote("在政治中我们需要能有所奉献的人，而不是想有所收获的人。", "（美国）巴鲁克"),
                DailyQuote("在人生的路上，将血一滴一滴地滴过去，去饲别人。虽自觉渐渐瘦弱，也以为快活。", "鲁迅"),
                DailyQuote("欲穷千里目，更上一层楼。", "唐·王之涣《登颧雀楼》"),
                DailyQuote("玉不琢，不成器；人不学，不知道。", "《礼记·学记》"),
                DailyQuote("一滴水只有放进大海里才永远不会干涸，一个人只有当他把自己和集体事业融合在一起的时候才能最有力量。", "雷锋"),
                DailyQuote("只要有坚强的意志力，就自然而然地会有能耐、机灵和知识。", "陀思妥耶夫斯基"),
                DailyQuote("只有为别人而活的生命才是值得的。", "爱因斯坦"),
                DailyQuote("自己活着，就是为了使别人过得更美好。", "雷锋"),
                DailyQuote("一次失败，只是证明我们成功的决心还够坚强。", "博维"),
                DailyQuote("业精于勤而荒于嬉，行成于思而毁于随。", "韩愈"),
                DailyQuote("要知父母恩，怀里抱儿孙。", "日本谚语"),
                DailyQuote("养儿方知娘艰辛，养女方知谢娘恩。", "日本谚语"),
                DailyQuote("孝子之至，莫大乎尊亲；尊亲之至，莫大乎以天下养。", "孟子"),
                DailyQuote("无知的人本想做点好事，结果却害人不轻；小喜鹊拔出妈妈的羽毛，还以为报答了养育之恩。", "藏族谚语"),
                DailyQuote("人是要有帮助的。荷花虽好，也要绿叶扶持。一个篱笆打三个桩，一个好汉要有三个帮。", "毛泽东"),
                DailyQuote("人只有献身于社会，才能找出那短暂而有风险的生命的意义。", "爱因斯坦"),
                DailyQuote("如果有一天，我能够对我们的公共利益有所贡献，我就会认为自己是世界上最幸福的人了。", "果戈理"),
                DailyQuote("上天赋予的生命，就是要为人类的繁荣和平和幸福而奉献。", "松下幸之助"),
                DailyQuote("生活的道路一旦选定，就要勇敢地走到底，决不回头。", "左拉"),
                DailyQuote("忘恩的人落在困难之中，是不能得救的。", "希腊谚语"),
                DailyQuote("忘恩比之说谎、虚荣、饰舌、酗酒或其他存在于脆弱的人心中的恶德还要厉害。", "英国谚语"),
                DailyQuote("天才并不是自生自长在深林荒野里的怪物，是由可以使天才生长的民众产生、长育出来的，所以没有这种民众，就没有天才。", "鲁迅"),
                DailyQuote("谁有历经千辛万苦的意志，谁就能达到任何目的。", "米南德"),
                DailyQuote("生使一切的人站在一条水平线上，死使卓越的人露出头角来。", "萧伯纳"),
                DailyQuote("生命赐给了我们，我们必须奉献于生命，才能获得生命。", "泰戈尔"),
                DailyQuote("为伟大的事业捐躯，从来就不能算作是失败。", "乔·拜伦"),
                DailyQuote("我是炎黄子孙，理所当然地要把学到的知识全部奉献给我亲爱的祖国。", "李四光"),
                DailyQuote("我主要关心的，不是你是不是失败了，而是你对失败是不是甘心。", "林肯"),
                DailyQuote("人生价值的大小是按人们对社会贡献的大小来衡量的。", "向警予"),
                DailyQuote("人民是土壤，它含有一切事物发展所必须的生命汁液；而个人则是这土壤上的花朵与果实。", "别林斯基"),
                DailyQuote("切莫垂头丧气，即使失去了一切，你还握有未来。", "奥丅斯卡·王尔德"),
                DailyQuote("蜜蜂从花中啜蜜，离开时营营的道谢。浮夸的蝴蝶却相信花是应该向他道谢的。", "泰戈尔"),
                DailyQuote("成功的人是跟别人学习经验，失败的人只跟自己学习经验。", "佚名"),
                DailyQuote("成功与失败都是一种表现方式，是一种对结果的评价，可以从中得到经验。", "佚名"),
                DailyQuote("个人之于社会等于身体的细胞，要一个人身体健全，不用说必须每个细胞都健全。", "闻一多"),
                DailyQuote("活着，为的是替整体做点事，滴水是有沾润作用，但滴水必加入河海，才能成为波涛。", "谢觉哉"),
                DailyQuote("不要垂头丧气，即使失去一切，明天仍在你的手里。", "王尔德"),
                DailyQuote("沉沉的黑夜都是白天的前奏。", "郭小川"),
                DailyQuote("成功＝艰苦的劳动＋正确的方法＋少说空话。", "爱因斯坦"),
                DailyQuote("既然我已经踏上这条道路，那么，任何东西都不应妨碍我沿着这条路走下去。", "康德")
            )
        }
    }
} 