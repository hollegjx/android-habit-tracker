# 🎯 Smart Habit Tracker

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room-4285F4?style=for-the-badge&logo=android&logoColor=white)

**一款现代化的Android习惯追踪应用，集成AI智能分析和成就系统**

[功能特性](#-功能特性) • [技术架构](#-技术架构) • [安装指南](#-安装指南) • [开发进度](#-开发进度) • [未来规划](#-未来规划)

</div>

---

## 📱 项目概述

Smart Habit Tracker 是一款基于 Jetpack Compose 构建的现代化习惯追踪应用，专注于帮助用户建立和维持良好的生活习惯。应用支持多类型打卡、智能AI分析、成就系统等功能，采用分层架构设计，具备良好的扩展性和维护性。

## ✨ 功能特性

### 🎯 核心功能
- **📚 多类型打卡**：支持学习、运动、理财三大类型的习惯追踪
- **⏰ 专注模式**：内置番茄钟功能，专注时间自动累计到成就系统
- **🏆 成就系统**：数据库驱动的等级系统，支持动态配置和后台管理
- **🤖 AI智能助手**：集成多个AI角色，提供个性化建议和激励
- **📊 数据统计**：详细的进度追踪和可视化统计

### 🎨 用户体验
- **🌙 深色主题**：完整的Material Design 3主题支持
- **🎭 动画效果**：流畅的转场动画和交互反馈
- **📱 响应式设计**：适配不同屏幕尺寸和方向
- **🗓️ 智能日历**：直观的日历视图显示打卡记录

### 🛡️ 数据管理
- **💾 本地存储**：基于Room的可靠数据持久化
- **👤 用户系统**：完整的用户信息管理
- **🔄 数据迁移**：平滑的数据库版本升级机制

## 🏗️ 技术架构

### 核心技术栈
```
📱 Frontend: Jetpack Compose + Material Design 3
🔧 Language: Kotlin
🗄️ Database: Room + SQLite
🔗 DI: Hilt
🏛️ Architecture: MVVM + Clean Architecture
🌐 Network: Retrofit + OkHttp
```

### 项目结构
```
app/src/main/java/com/example/cur_app/
├── 📁 data/                    # 数据层
│   ├── 📁 database/           # 数据库相关
│   │   ├── 📁 entities/       # 数据实体
│   │   ├── 📁 dao/           # 数据访问对象
│   │   └── HabitTrackerDatabase.kt
│   ├── 📁 repository/         # 仓库实现
│   └── 📁 remote/            # 网络服务
├── 📁 domain/                 # 领域层
│   └── 📁 usecase/           # 业务用例
├── 📁 presentation/           # 表现层
│   ├── 📁 components/        # UI组件
│   ├── 📁 screens/           # 页面
│   ├── 📁 viewmodel/         # 视图模型
│   └── 📁 navigation/        # 导航
├── 📁 di/                    # 依赖注入
└── 📁 ui/theme/              # 主题样式
```

### 设计模式
- **🎯 MVVM**：清晰的数据流和状态管理
- **🧹 Clean Architecture**：分层架构确保代码可维护性
- **🔄 Repository Pattern**：统一的数据访问接口
- **💉 Dependency Injection**：使用Hilt进行依赖管理

## 🚀 安装指南

### 系统要求
- Android 7.0 (API level 24) 或更高版本
- 最小RAM: 2GB
- 存储空间: 100MB

### 开发环境
```bash
# 克隆项目
git clone https://github.com/hollegjx/android-habit-tracker.git
cd android-habit-tracker

# 使用Android Studio打开项目
# 确保安装了以下组件：
# - Android SDK 34
# - Kotlin 1.9.0+
# - Gradle 8.0+
```

### 构建项目
```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease
```

## 📈 开发进度

### ✅ 已完成功能

#### 第一阶段：基础架构 (100%)
- [x] 项目架构搭建（MVVM + Clean Architecture）
- [x] 数据库设计（Room + SQLite）
- [x] 依赖注入配置（Hilt）
- [x] 基础UI框架（Jetpack Compose + Material Design 3）

#### 第二阶段：核心功能 (95%)
- [x] 用户系统与认证
- [x] 三类打卡功能（学习、运动、理财）
- [x] 专注模式与番茄钟
- [x] 基础数据统计
- [x] 日历视图集成
- [x] 数据本地存储

#### 第三阶段：成就系统重构 (100%)
- [x] 数据库驱动的等级系统
- [x] 动态等级配置支持
- [x] 经验值自动累计
- [x] 打卡系统与成就系统统一
- [x] 数据库迁移机制

#### 第四阶段：AI智能功能 (80%)
- [x] AI角色系统
- [x] 基础聊天功能
- [x] 多模型支持框架
- [ ] 智能建议算法优化
- [ ] 个性化推荐系统

### 🚧 开发中功能

#### UI/UX优化 (70%)
- [x] 深色主题适配
- [x] 动画效果优化
- [ ] 无障碍功能支持
- [ ] 多语言国际化

#### 数据分析模块 (30%)
- [x] 基础统计图表
- [ ] 趋势分析算法
- [ ] 数据导出功能
- [ ] 高级可视化组件

## 🔮 未来规划

### 📊 阶段五：大模型API集成与智能分析
**预计时间：2-3个月**

#### 🧠 智能分析引擎
- **📈 习惯模式识别**：基于大模型分析用户打卡数据，识别习惯形成模式
- **🎯 个性化建议**：根据用户行为数据提供精准的习惯改进建议
- **📊 智能报告生成**：自动生成周报、月报，包含深度分析和改进建议
- **🔮 预测性分析**：预测用户可能的习惯中断风险，提前提醒

#### 🤖 AI功能增强
- **💬 智能对话升级**：集成GPT-4、Claude等先进模型
- **🎨 内容个性化**：根据用户偏好定制激励内容和提醒文案
- **🧘 情绪识别**：通过文本分析识别用户情绪状态，提供相应支持

### 📱 阶段六：打卡方式扩展
**预计时间：2-3个月**

#### 📸 视觉打卡系统
- **📷 拍照验证**：支持拍照上传作为打卡凭证
- **🖼️ 图像识别**：AI识别图片内容，自动验证打卡真实性
- **📱 AR集成**：增强现实功能，让打卡更有趣味性
- **🎨 视觉历程**：以图片形式展示习惯养成历程

#### 👥 社交打卡系统
- **🤝 好友系统**：添加好友，查看彼此的打卡进度
- **🏆 团队挑战**：创建或加入习惯养成小组
- **📢 打卡分享**：将成就分享到社交平台
- **🎖️ 排行榜**：好友间的健康竞争机制

#### 🔧 其他创新打卡方式
- **🏃‍♂️ 运动数据集成**：连接健康应用，自动同步运动数据
- **📍 位置打卡**：基于地理位置的自动打卡（如到达健身房）
- **⌚ 可穿戴设备支持**：智能手表快速打卡
- **🎵 语音打卡**：语音指令完成打卡操作

### 🚀 阶段七：平台扩展与生态建设
**预计时间：3-4个月**

#### 🌐 跨平台支持
- **💻 Web端开发**：基于Flutter Web的管理后台
- **🖥️ 桌面应用**：Windows/macOS桌面客户端
- **⌚ 智能手表应用**：WearOS适配

#### 🔧 开放API
- **🔌 第三方集成**：提供API供其他应用集成
- **📊 数据分析平台**：为企业用户提供员工习惯分析
- **🏥 健康管理**：与医疗健康平台数据互通

## 🎨 界面预览

### 🏠 主要页面
- **🏠 首页**：概览今日打卡进度和成就状态
- **📊 统计页**：详细的数据分析和趋势图表
- **🏆 成就页**：等级进度和成就解锁状态
- **🤖 AI助手**：智能对话和个性化建议
- **⚙️ 设置页**：个人信息和应用配置

### 🎯 核心组件
- **📋 打卡卡片**：直观的进度显示和快速操作
- **⏰ 专注模式**：沉浸式的专注计时体验
- **📅 智能日历**：可视化的历史记录查看
- **🏆 成就系统**：游戏化的等级和徽章展示

## 🤝 贡献指南

我们欢迎所有形式的贡献！请查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细信息。

### 如何贡献
1. 🍴 Fork 此仓库
2. 🌿 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 💾 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 📤 推送到分支 (`git push origin feature/AmazingFeature`)
5. 🔀 打开 Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- **开发者**：hollegjx
- **GitHub**：[@hollegjx](https://github.com/hollegjx)
- **项目链接**：[https://github.com/hollegjx/android-habit-tracker](https://github.com/hollegjx/android-habit-tracker)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给我们一个星标！**

![Star History Chart](https://api.star-history.com/svg?repos=hollegjx/android-habit-tracker&type=Date)

</div> 