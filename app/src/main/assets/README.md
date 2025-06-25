# Assets 资源文件夹

此文件夹用于存放应用的大型资源文件，包括AI角色资源、动画文件等。

## 文件夹结构

```
assets/
├── ai_characters/           # AI角色资源文件
│   ├── xiaocherry/         
│   │   ├── character.json   # 角色配置文件
│   │   ├── dialogues/       # 对话文本文件
│   │   └── models/          # 3D模型文件(可选)
│   ├── leiming/            
│   └── mengmeng/           
├── animations/             # 动画文件
│   ├── lottie/            # Lottie动画文件
│   │   ├── loading.json
│   │   ├── success.json
│   │   └── character_intro.json
│   └── gif/               # GIF动画文件
└── data/                  # 数据文件
    ├── character_data.json # 角色数据配置
    ├── dialogue_templates.json # 对话模板
    └── personality_traits.json # 性格特征定义
```

## Assets vs Drawable vs Raw

### Assets 文件夹用途
- 大型配置文件 (JSON, XML)
- 动画文件 (Lottie JSON, GIF)
- 文本资源文件
- 可以通过AssetManager动态读取
- 不会被压缩或优化

### Drawable 文件夹用途
- 图片资源 (PNG, JPG, WebP)
- 矢量图形 (SVG转换的Vector Drawable)
- 会被系统自动优化和压缩
- 支持不同密度适配

### Raw 文件夹用途
- 音频文件 (MP3, AAC, WAV)
- 视频文件 (MP4)
- 不会被压缩，保持原始格式

## 访问方法

```kotlin
// 读取Assets文件
fun readAssetFile(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}

// 读取JSON配置
fun loadCharacterConfig(context: Context, characterId: String): CharacterConfig {
    val json = readAssetFile(context, "ai_characters/$characterId/character.json")
    return Gson().fromJson(json, CharacterConfig::class.java)
}

// 加载Lottie动画
@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("animations/lottie/loading.json")
    )
    LottieAnimation(composition = composition)
}
```

## 角色配置文件示例

### character.json 结构
```json
{
    "id": "xiaocherry",
    "name": "小樱",
    "personality": {
        "traits": ["温柔", "体贴", "鼓励性"],
        "communication_style": "温暖友好",
        "voice_characteristics": {
            "tone": "甜美",
            "speed": "中等偏慢",
            "volume": "柔和"
        }
    },
    "capabilities": [
        "学习计划制定",
        "情绪引导", 
        "动机激励"
    ],
    "dialogue_templates": [
        {
            "context": "greeting",
            "templates": [
                "你好~我是小樱，很高兴认识你呢！💕",
                "嗨！今天想要一起学习什么呢？"
            ]
        }
    ],
    "visual_config": {
        "primary_color": "#FFB6C1",
        "secondary_color": "#FFC0CB",
        "emoji": "🌸",
        "avatar_path": "xiaocherry/avatar.png"
    }
}
```

## 最佳实践

1. **文件命名**：使用小写+下划线格式
2. **JSON格式**：确保格式正确，使用UTF-8编码
3. **文件大小**：单个文件建议不超过5MB
4. **版本控制**：重要配置文件要做版本管理
5. **本地化**：支持多语言的文本资源要分文件夹存放
6. **缓存策略**：频繁读取的文件考虑缓存到内存

## 注意事项

- Assets文件夹中的文件会被打包到APK中
- 所有文件都会增加APK大小，注意控制总体积
- 文件路径区分大小写
- 不支持文件夹嵌套过深（建议不超过3层） 