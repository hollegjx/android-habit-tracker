# 习惯追踪应用资源管理指南

此文档说明了应用中各种资源文件夹的用途和使用方法。

## 项目资源结构

```
cur_app/
├── app/src/main/
│   ├── res/                    # Android资源文件夹
│   │   ├── drawable/           # 图片资源
│   │   │   ├── ai_characters/  # AI角色图片
│   │   │   └── icons/          # 自定义图标
│   │   ├── raw/                # 音频资源
│   │   └── values/             # 配置资源
│   └── assets/                 # 大型资源文件
│       ├── ai_characters/      # AI角色配置
│       └── animations/         # 动画文件
└── docs/                       # 项目文档
    └── resources/              # 资源使用说明
```

## 资源文件夹详解

### 1. AI角色图片资源 (`app/src/main/res/drawable/ai_characters/`)

**用途**：存放AI角色的头像图片和相关视觉资源

**文件结构建议**：
```
ai_characters/
├── xiaocherry_avatar.png       # 小樱头像
├── xiaocherry_background.png   # 小樱背景
├── leiming_avatar.png          # 雷鸣头像
├── leiming_background.png      # 雷鸣背景
├── mengmeng_avatar.png         # 萌萌头像
└── mengmeng_background.png     # 萌萌背景
```

**要求**：
- 文件名必须使用小写字母、数字和下划线
- 头像尺寸：512x512 pixels
- 格式：PNG（支持透明背景）
- 文件大小：< 100KB

**代码引用示例**：
```kotlin
Image(
    painter = painterResource(id = R.drawable.xiaocherry_avatar),
    contentDescription = "小樱头像"
)
```

### 2. 自定义图标 (`app/src/main/res/drawable/icons/`)

**用途**：存放应用内使用的自定义图标

**文件命名**：
- `icon_message.xml` - 消息图标
- `icon_ai_assistant.xml` - AI助手图标
- `icon_voice_play.xml` - 语音播放图标

### 3. 音频资源 (`app/src/main/res/raw/`)

**用途**：存放AI角色语音文件和音效

**文件结构建议**：
```
raw/
├── xiaocherry_greeting.mp3     # 小樱问候语音
├── xiaocherry_encourage.mp3    # 小樱鼓励语音
├── leiming_greeting.mp3        # 雷鸣问候语音
├── leiming_encourage.mp3       # 雷鸣鼓励语音
├── mengmeng_greeting.mp3       # 萌萌问候语音
├── mengmeng_encourage.mp3      # 萌萌鼓励语音
├── notification_sound.mp3      # 通知音效
└── button_click.mp3            # 按钮点击音效
```

**要求**：
- 文件名必须使用小写字母、数字和下划线
- 格式：MP3 或 AAC
- 采样率：44.1kHz
- 比特率：128kbps
- 文件大小：< 500KB

**代码引用示例**：
```kotlin
val mediaPlayer = MediaPlayer.create(context, R.raw.xiaocherry_greeting)
mediaPlayer.start()
```

### 4. Assets文件夹 (`app/src/main/assets/`)

**用途**：存放大型配置文件、动画文件等

这个文件夹中的文件不会被Android资源系统处理，可以使用任意文件名。

详细说明请参考：`app/src/main/assets/README.md`

## Android资源命名规则

**重要提醒**：
- `res/` 文件夹下的所有文件名必须遵循Android资源命名规则
- 只能使用：小写字母 (a-z)、数字 (0-9)、下划线 (_)
- 不能使用：大写字母、中划线、空格、特殊字符
- 不能以数字开头

**正确示例**：
- ✅ `xiaocherry_avatar.png`
- ✅ `background_gradient.xml`
- ✅ `sound_notification.mp3`

**错误示例**：
- ❌ `XiaoCherry_Avatar.png` (包含大写字母)
- ❌ `background-gradient.xml` (包含中划线)
- ❌ `1_sound.mp3` (以数字开头)
- ❌ `README.md` (包含大写字母)

## 使用建议

1. **图片优化**：使用WebP格式可以减小文件大小
2. **多密度支持**：为不同屏幕密度提供对应图片
3. **矢量图标**：优先使用Vector Drawable
4. **音频压缩**：确保音频文件大小合理
5. **版本控制**：重要资源文件要做版本管理

## 文件添加步骤

1. 准备符合要求的资源文件
2. 重命名为符合Android规则的文件名
3. 复制到对应的资源文件夹
4. 在代码中通过R.drawable.xxx或R.raw.xxx引用
5. 重新编译应用

## 注意事项

- 所有添加到`res/`文件夹的文件都会增加APK大小
- `assets/`文件夹适合存放大型文件和配置文件
- 删除不用的资源文件以保持APK精简
- 测试不同设备上的资源加载效果 