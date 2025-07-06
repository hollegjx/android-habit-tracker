# 服务器后端与客户端接口对应文档

## 服务器信息
- **域名**: https://dkht.gjxlsy.top
- **API基础路径**: https://dkht.gjxlsy.top/api
- **管理员账号**: 872886699@qq.com / 123456

## 1. 用户认证接口

### 1.1 用户注册
- **客户端使用**: `UserRepository.registerUser()`
- **服务器接口**: `POST /api/auth/register`
- **请求体**:
```json
{
  "username": "string",
  "email": "string", 
  "password": "string",
  "nickname": "string",
  "phone": "string",
  "verificationCode": "string"
}
```

### 1.2 用户登录
- **客户端使用**: `UserRepository.loginUser()`
- **服务器接口**: `POST /api/auth/login`
- **请求体**:
```json
{
  "username": "string", // 可以是用户名或邮箱
  "password": "string",
  "deviceInfo": {
    "deviceId": "string",
    "deviceName": "string",
    "platform": "android"
  }
}
```

### 1.3 刷新Token
- **客户端使用**: `UserRepository.refreshToken()`
- **服务器接口**: `POST /api/auth/refresh`
- **请求体**:
```json
{
  "refreshToken": "string"
}
```

### 1.4 用户登出
- **客户端使用**: `UserRepository.logoutUser()`
- **服务器接口**: `POST /api/auth/logout`
- **Headers**: `Authorization: Bearer {token}`

## 2. 用户信息接口

### 2.1 获取用户信息
- **客户端使用**: `UserRepository.getCurrentUser()`
- **服务器接口**: `GET /api/users/profile`
- **Headers**: `Authorization: Bearer {token}`

### 2.2 更新用户信息
- **客户端使用**: `UserRepository.updateUserProfile()`
- **服务器接口**: `PUT /api/users/profile`
- **Headers**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "nickname": "string",
  "phone": "string",
  "avatarUrl": "string"
}
```

## 3. AI角色接口

### 3.1 获取AI角色列表
- **客户端使用**: `AiCharacterRepository.getAllCharacters()`
- **服务器接口**: `GET /api/ai-characters`
- **响应体**:
```json
{
  "success": true,
  "data": [
    {
      "character_id": "sakura",
      "name": "小樱",
      "description": "温柔学习伙伴",
      "avatar_url": "/images/avatars/sakura.png",
      "personality": "温柔体贴，善解人意"
    }
  ]
}
```

### 3.2 获取AI角色详情
- **客户端使用**: `AiCharacterRepository.getCharacterById()`
- **服务器接口**: `GET /api/ai-characters/{character_id}`

### 3.3 与AI角色聊天
- **客户端使用**: `AiService.sendMessage()`
- **服务器接口**: `POST /api/ai-characters/{character_id}/chat`
- **Headers**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "message": "string",
  "conversationId": "string", // 可选，用于维持对话上下文
  "context": {
    "habitData": {}, // 可选，当前习惯数据
    "userMood": "string" // 可选，用户当前情绪
  }
}
```

## 4. 习惯管理接口

### 4.1 获取用户习惯列表
- **客户端使用**: `HabitRepository.getAllHabits()`
- **服务器接口**: `GET /api/habits`
- **Headers**: `Authorization: Bearer {token}`

### 4.2 创建新习惯
- **客户端使用**: `HabitRepository.createHabit()`
- **服务器接口**: `POST /api/habits`
- **Headers**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "name": "string",
  "description": "string",
  "category": "string",
  "frequency": "daily|weekly|monthly",
  "targetValue": "number",
  "unit": "string",
  "reminderTime": "HH:mm",
  "isActive": true
}
```

### 4.3 更新习惯
- **客户端使用**: `HabitRepository.updateHabit()`
- **服务器接口**: `PUT /api/habits/{habit_id}`
- **Headers**: `Authorization: Bearer {token}`

### 4.4 删除习惯
- **客户端使用**: `HabitRepository.deleteHabit()`
- **服务器接口**: `DELETE /api/habits/{habit_id}`
- **Headers**: `Authorization: Bearer {token}`

### 4.5 记录习惯完成
- **客户端使用**: `HabitRepository.recordHabitCompletion()`
- **服务器接口**: `POST /api/habits/{habit_id}/complete`
- **Headers**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "completedAt": "ISO8601 timestamp",
  "value": "number", // 完成的量
  "notes": "string" // 可选
}
```

## 5. 聊天和社交接口

### 5.1 发送好友请求
- **客户端使用**: `SocialRepository.sendFriendRequest()`
- **服务器接口**: `POST /api/users/friends/request`
- **Headers**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "targetUserId": "string",
  "message": "string"
}
```

### 5.2 获取好友列表
- **客户端使用**: `SocialRepository.getFriends()`
- **服务器接口**: `GET /api/users/friends`
- **Headers**: `Authorization: Bearer {token}`

### 5.3 发送消息
- **客户端使用**: `ChatRepository.sendMessage()`
- **服务器接口**: `POST /api/chat/messages`
- **Headers**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "conversationId": "string",
  "recipientId": "string",
  "messageType": "text|image|file",
  "content": "string",
  "metadata": {}
}
```

### 5.4 获取对话列表
- **客户端使用**: `ChatRepository.getConversations()`
- **服务器接口**: `GET /api/chat/conversations`
- **Headers**: `Authorization: Bearer {token}`

### 5.5 获取对话消息
- **客户端使用**: `ChatRepository.getMessages()`
- **服务器接口**: `GET /api/chat/conversations/{conversation_id}/messages`
- **Headers**: `Authorization: Bearer {token}`

## 6. 服务器连接检测

### 6.1 健康检查接口
- **客户端使用**: `NetworkManager.checkServerHealth()`
- **服务器接口**: `GET /health`
- **用途**: 检测服务器是否在线

### 6.2 API连通性测试
- **客户端使用**: `NetworkManager.testApiConnection()`
- **服务器接口**: `GET /api/ping`
- **用途**: 测试API服务是否正常

## 7. 客户端配置建议

### 7.1 网络配置
```kotlin
object ServerConfig {
    const val BASE_URL = "https://dkht.gjxlsy.top"
    const val API_BASE_URL = "$BASE_URL/api"
    const val HEALTH_CHECK_URL = "$BASE_URL/health"
    
    // 连接超时配置
    const val CONNECT_TIMEOUT = 10_000L // 10秒
    const val READ_TIMEOUT = 30_000L    // 30秒
    const val WRITE_TIMEOUT = 30_000L   // 30秒
}
```

### 7.2 错误处理
```kotlin
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : NetworkResult<T>()
    data class NetworkError<T>(val exception: Throwable) : NetworkResult<T>()
}
```

### 7.3 服务器连接检测逻辑
```kotlin
class NetworkManager {
    suspend fun checkServerConnection(): Boolean {
        return try {
            val response = healthCheckApi.checkHealth()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun showConnectionErrorDialog() {
        // 显示连接错误对话框
        // "无法连接到服务器，你可能无法正常使用AI功能"
    }
}
```

## 8. 数据同步策略

### 8.1 AI角色同步
1. 应用启动时检查服务器连接
2. 如果连接成功，同步最新AI角色数据
3. 如果连接失败，使用本地缓存数据
4. 定期尝试重新连接并同步

### 8.2 用户数据同步
1. 优先使用服务器数据
2. 离线时使用本地数据
3. 网络恢复后自动同步
4. 冲突处理：服务器数据优先

### 8.3 聊天数据同步
1. 实时同步聊天消息
2. 离线消息排队
3. 网络恢复后批量发送
4. 支持消息状态（发送中、已发送、已读）