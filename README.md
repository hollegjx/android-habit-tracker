# 🤖 AI习惯追踪器 - 你的智能生活伙伴

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![AI](https://img.shields.io/badge/AI%20Powered-FF6B6B?style=for-the-badge&logo=openai&logoColor=white)

**🎯 让AI陪伴你养成好习惯，与朋友一起成长！**

[📋 打卡系统](#-智能打卡系统) • [🤖 AI分析](#-ai智能分析) • [👥 社交分享](#-好友聊天分享) • [🚀 快速开始](#-快速开始)

</div>

---

## 📱 应用简介

**AI习惯追踪器** 是一款集成AI智能分析的现代化习惯养成应用。通过个性化AI角色陪伴、智能打卡系统和社交分享功能，让用户在轻松愉快的氛围中养成好习惯，与朋友一起成长进步。

### 🌟 为什么选择我们？

- **🤖 AI智能陪伴**：多个性格鲜明的AI角色，提供个性化建议和情感支持
- **📊 科学打卡体系**：学习、运动、理财三大领域，专业的习惯追踪方案
- **👥 社交互动**：与好友分享进度，互相激励，共同成长
- **🎯 成就系统**：完整的等级和奖励机制，让习惯养成充满乐趣

---

## 🎯 三大核心功能

### 📋 智能打卡系统

> **让每一次坚持都被看见，让每一个进步都有意义**

#### 🎯 多类型打卡支持
- **📚 学习打卡**：读书、学习技能、完成课程
- **🏃‍♂️ 运动打卡**：健身、跑步、瑜伽、各类运动
- **💰 理财打卡**：记账、储蓄、投资学习

#### ⚡ 专注模式
- **🍅 番茄钟功能**：25分钟专注工作法
- **⏱️ 自定义计时**：根据个人需求设置专注时长
- **📈 专注统计**：记录每日专注时长，形成专注力报告

#### 📊 智能追踪
- **📅 日历视图**：直观查看打卡历史和连续天数
- **📈 进度统计**：周报、月报自动生成
- **🎯 目标管理**：设置个人目标，追踪完成情况

```kotlin
// 打卡功能示例
class CheckInUseCase {
    suspend fun checkIn(type: CheckInType, duration: Int) {
        // 记录打卡信息
        // 更新成就进度  
        // 触发AI分析
    }
}
```

---

### 🤖 AI智能分析

> **不仅仅是记录，更是你的专属成长顾问**

#### 🎭 多角色AI助手
- **😊 小美（鼓励型）**：温暖贴心，善于发现你的进步亮点
- **🤓 小智（分析型）**：理性客观，提供数据驱动的建议
- **😄 小萌（活泼型）**：活泼有趣，用幽默化解挫折感
- **💪 小强（激励型）**：充满动力，在你想放弃时给予鼓励

#### 🧠 智能分析能力
- **📊 习惯模式识别**：分析你的打卡数据，发现习惯形成规律
- **🎯 个性化建议**：根据你的行为特点，提供定制化改进方案
- **⚠️ 风险预警**：预测可能的习惯中断，提前提醒调整
- **🏆 成就预测**：预测下一个成就达成时间，保持动力

#### 💬 智能对话
- **实时互动**：随时与AI助手聊天，获得即时反馈
- **情感识别**：AI能理解你的情绪状态，提供相应支持
- **学习能力**：AI会记住你的偏好，提供越来越精准的建议

```kotlin
// AI分析功能示例
class AiUseCase {
    suspend fun analyzeHabitProgress(userId: String): AiAnalysisResult {
        val data = checkInRepository.getUserProgress(userId)
        return aiService.analyzePattern(data, selectedCharacter)
    }
}
```

---

### 👥 好友聊天分享

> **一个人走得快，一群人走得远**

#### 🤝 好友系统
- **👫 添加好友**：通过用户名搜索添加朋友
- **📊 进度查看**：查看好友的打卡进度和成就
- **🏆 排行榜**：与好友比较打卡天数和成就等级
- **👋 状态分享**：分享今日心情和打卡感受

#### 💬 实时聊天
- **⚡ 即时通讯**：基于Socket.IO的实时聊天系统
- **🎉 成就分享**：一键分享达成的成就和里程碑
- **📸 图片分享**：分享打卡照片和生活瞬间
- **🎯 目标公布**：向好友公布新目标，获得监督支持

#### 🎊 互动激励
- **👍 点赞支持**：为好友的坚持点赞鼓励
- **💌 私信鼓励**：发送私信互相激励
- **🏁 挑战赛**：创建或参与好友间的习惯挑战
- **🎁 奖励机制**：达成目标后的好友祝贺系统

```kotlin
// 社交功能示例
class FriendRepository {
    suspend fun shareAchievement(achievement: Achievement) {
        // 分享成就到好友动态
        // 发送通知给关注的好友
    }
}
```

---

## 🛠️ 技术架构

### 📱 Android端技术栈
```
🎨 UI框架：Jetpack Compose + Material Design 3
🏗️ 架构：MVVM + Clean Architecture
💾 数据库：Room + SQLite (15个数据实体)
🌐 网络：Retrofit + OkHttp + Socket.IO
💉 依赖注入：Hilt
🔄 协程：Kotlin Coroutines + Flow
```

### 🖥️ 服务器端技术栈
```
⚡ 后端框架：Node.js + Express
🗄️ 数据库：PostgreSQL + Knex.js
🔐 认证：JWT Token
💬 实时通信：Socket.IO
🤖 AI集成：OpenAI API + 本地fallback
📝 日志：Winston Logger
```

### 🏗️ 项目结构
```
android-habit-tracker/
├── 📱 app/                        # Android应用
│   ├── 🎯 presentation/           # UI层 (17个界面)
│   ├── 🏢 domain/                # 业务逻辑层 (4个UseCase)
│   ├── 💾 data/                  # 数据层 (8个Repository)
│   ├── 🔧 di/                    # 依赖注入 (6个模块)
│   └── 🎨 ui/theme/              # UI主题
├── 🖥️ fwq/                       # 服务器端
│   ├── 📡 src/routes/            # API路由 (8个路由)
│   ├── 🎮 src/controllers/       # 业务控制器 (7个控制器)
│   ├── 🔧 src/middleware/        # 中间件
│   ├── 📊 migrations/            # 数据库迁移 (12个迁移)
│   └── 🌱 seeds/                 # 数据种子
└── 📚 docs/                      # 项目文档
```

---

## 🚀 快速开始

### 📋 系统要求
- **Android 7.0+** (API level 26+)
- **2GB RAM** 以上
- **100MB** 存储空间

### 💻 开发环境配置

#### 1️⃣ 克隆项目
```bash
git clone https://github.com/hollegjx/android-habit-tracker.git
cd android-habit-tracker
```

#### 2️⃣ Android端配置
```bash
# 使用Android Studio打开项目
# 确保安装以下组件：
# - Android SDK 35
# - Kotlin 2.0+
# - Gradle 8.10+

# 构建调试版本
./gradlew assembleDebug
```

#### 3️⃣ 服务器端配置
```bash
cd fwq
npm install

# 配置数据库
npm run migrate
npm run seed

# 启动开发服务器
npm run dev
```

### 🔧 配置文件
在 `fwq` 目录下创建 `.env` 文件：
```env
DATABASE_URL=postgresql://username:password@localhost:5432/habit_tracker
JWT_SECRET=your_jwt_secret_key
OPENAI_API_KEY=your_openai_api_key
PORT=3000
```

---

## 📸 应用截图

### 🏠 主界面
- 渐变紫色背景，现代化卡片设计
- 实时显示今日打卡进度和成就
- 快速访问各类打卡功能

### 🤖 AI聊天界面
- 可爱的AI角色选择
- 实时对话气泡设计
- 智能建议和情感支持

### 👥 好友社交界面
- 好友列表和在线状态
- 实时聊天和成就分享
- 进度对比和排行榜

---

## 🎯 核心特色

### ✨ 用户体验
- **🎨 现代化设计**：Material Design 3 + 渐变背景
- **🌙 深色模式**：完整的日夜主题切换
- **🎭 流畅动画**：丰富的转场和交互动画
- **📱 响应式**：适配各种屏幕尺寸

### 🔧 技术特色
- **🏗️ 分层架构**：MVVM + Clean Architecture
- **💉 依赖注入**：Hilt全面管理依赖
- **🔄 响应式编程**：Kotlin Flow + StateFlow
- **💾 离线支持**：Room本地数据库 + 网络同步

### 🤖 AI能力
- **🧠 多模型支持**：OpenAI + Claude + 本地fallback
- **📊 数据驱动**：基于用户行为的智能分析
- **🎭 角色扮演**：不同性格的AI助手
- **💬 自然对话**：流畅的人机交互体验

---

## 📈 开发进度

| 功能模块 | 完成度 | 状态 |
|---------|--------|------|
| 🎯 打卡系统 | 75% | 🚧 开发中 |
| 🤖 AI分析 | 65% | 🚧 开发中 |
| 👥 社交分享 | 70% | 🚧 开发中 |
| 🏆 成就系统 | 80% | 🚧 开发中 |
| 📱 UI界面 | 85% | 🚧 开发中 |
| 🖥️ 服务器端 | 75% | 🚧 开发中 |

### 🎯 即将发布功能
- **📊 高级数据分析**：更详细的习惯趋势分析
- **🎮 游戏化元素**：徽章系统和成就挑战
- **📱 小组件支持**：Android桌面小组件
- **🔔 智能提醒**：基于AI的个性化提醒

---

## 🤝 贡献指南

欢迎参与项目开发！请阅读以下指南：

### 📝 贡献流程
1. **Fork** 项目到你的GitHub账号
2. **创建** 功能分支：`git checkout -b feature/AmazingFeature`
3. **提交** 更改：`git commit -m 'Add some AmazingFeature'`
4. **推送** 分支：`git push origin feature/AmazingFeature`
5. **创建** Pull Request

### 🎯 开发规范
- 遵循 **Kotlin** 编码规范
- 使用 **Clean Architecture** 分层
- 编写 **单元测试** 和 **文档**
- 提交前运行 **代码检查**

---

## 📞 联系我们

- **📧 邮箱**：support@habittracker.com
- **🐙 GitHub**：[hollegjx/android-habit-tracker](https://github.com/hollegjx/android-habit-tracker)
- **📱 QQ群**：123456789

---

## 📄 许可证

本项目采用 **MIT License** 许可证。详见 [LICENSE](LICENSE) 文件。

---

<div align="center">

**🌟 如果这个项目对你有帮助，请给我们一个星标！🌟**

**让AI陪伴你的每一次成长 🤖💪**

</div> 