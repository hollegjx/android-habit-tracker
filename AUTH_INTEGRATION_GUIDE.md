# 认证系统集成指南

## 概述

我已经为您的习惯追踪应用设计并实现了一套完整的现代化认证系统，包括登录、注册、忘记密码等功能，并预留了远程服务器接口和测试模式。

## 已实现功能

### 1. 用户界面 (UI)
- ✅ **LoginScreen**: 现代化登录界面，支持用户名/邮箱登录
- ✅ **RegisterScreen**: 注册界面，包含邮箱验证
- ✅ **ForgotPasswordScreen**: 密码重置界面，支持邮箱验证码
- ✅ **测试模式**: 免登录按钮，方便开发调试

### 2. 业务逻辑 (ViewModel)
- ✅ **AuthViewModel**: 处理所有认证相关的业务逻辑
- ✅ 表单验证和错误处理
- ✅ 状态管理和UI反馈
- ✅ 测试模式支持

### 3. 数据层 (Repository & API)
- ✅ **AuthRepository**: 认证数据仓库
- ✅ **AuthApiService**: 远程API接口定义
- ✅ **DTO模型**: 完整的数据传输对象
- ✅ **DataStore**: 本地数据持久化

### 4. 网络层 (Network)
- ✅ **NetworkModule**: 依赖注入配置
- ✅ **Token管理**: 自动刷新和认证拦截器
- ✅ **错误处理**: 网络请求和响应处理

### 5. 导航系统 (Navigation)
- ✅ **AppNavigation**: 应用主导航
- ✅ **AuthNavigation**: 认证流程导航
- ✅ **AuthGuard**: 权限控制组件

## 文件结构

```
app/src/main/java/com/example/cur_app/
├── data/
│   ├── remote/
│   │   ├── AuthApiService.kt              # 远程API接口
│   │   └── dto/
│   │       ├── AuthDto.kt                 # 认证相关DTO
│   │       ├── FriendDto.kt               # 好友管理DTO
│   │       └── ChatDto.kt                 # 聊天相关DTO
│   └── repository/
│       └── AuthRepository.kt              # 认证数据仓库
├── di/
│   └── NetworkModule.kt                   # 网络依赖注入
├── presentation/
│   ├── navigation/
│   │   ├── AppNavigation.kt               # 应用主导航
│   │   └── AuthNavigation.kt              # 认证导航
│   ├── screens/auth/
│   │   ├── LoginScreen.kt                 # 登录界面
│   │   ├── RegisterScreen.kt              # 注册界面
│   │   └── ForgotPasswordScreen.kt        # 忘记密码界面
│   └── viewmodel/
│       └── AuthViewModel.kt               # 认证ViewModel
```

## 使用方法

### 1. 更新MainActivity

在您的`MainActivity`中集成认证系统：

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAppTheme {
                AppNavigation()
            }
        }
    }
}
```

### 2. 配置服务器URL

在`NetworkModule.kt`中更新您的服务器地址：

```kotlin
@Provides
@Named("BASE_URL")
fun provideBaseUrl(): String {
    return "https://your-actual-server.com/api/v1/"
}
```

### 3. 测试模式使用

在开发阶段，您可以使用测试模式免登录：

1. 在登录界面点击"免登录进入（测试）"按钮
2. 系统会自动创建测试用户并进入主应用
3. 正式发布时记得移除或隐藏测试模式

### 4. 远程API集成

当您的后端服务器准备好时，所有API接口都已定义完成：

#### 认证接口
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `POST /auth/refresh` - 刷新Token
- `POST /auth/logout` - 用户登出

#### 用户管理接口
- `GET /user/profile` - 获取用户信息
- `PUT /user/profile` - 更新用户信息
- `POST /user/avatar` - 上传头像

#### 好友管理接口
- `GET /user/search` - 搜索用户
- `POST /friend/request` - 发送好友请求
- `GET /friend/list` - 获取好友列表

#### 聊天接口
- `GET /chat/conversations` - 获取对话列表
- `POST /chat/message` - 发送消息
- `GET /chat/messages/{conversationId}` - 获取聊天记录

## 主要特性

### 🎨 现代化UI设计
- 渐变背景和玻璃拟态效果
- 响应式动画和交互反馈
- 支持深色/浅色主题
- Material Design 3规范

### 🔐 安全特性
- JWT Token自动管理
- Token自动刷新机制
- 安全的密码存储
- 请求头自动添加认证信息

### 🚀 开发友好
- Hilt依赖注入
- 完整的错误处理
- 详细的日志记录
- 测试模式支持

### 📱 用户体验
- 表单验证和实时反馈
- 加载状态和错误提示
- 记住登录状态
- 邮箱验证流程

## 后续集成步骤

1. **设置服务器**: 部署后端API服务器
2. **更新URL**: 在`NetworkModule`中配置实际的服务器地址
3. **测试API**: 验证所有接口的正确性
4. **移除测试模式**: 在正式版本中隐藏测试按钮
5. **添加推送通知**: 集成FCM用于聊天消息推送

## 注意事项

⚠️ **重要提醒**:
- 测试模式仅用于开发调试，正式发布前请移除
- 请及时更新服务器URL配置
- 确保API接口的安全性和数据验证
- 定期更新依赖库版本

## 技术栈

- **UI**: Jetpack Compose + Material Design 3
- **架构**: MVVM + Repository Pattern
- **依赖注入**: Hilt
- **网络**: Retrofit + OkHttp + Gson
- **本地存储**: DataStore Preferences
- **导航**: Navigation Compose
- **异步处理**: Kotlin Coroutines + Flow

现在您的应用已经具备了完整的用户认证系统，可以开始集成您的后端服务器了！