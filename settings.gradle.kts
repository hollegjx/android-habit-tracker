pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        // Fallback repositories
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
    }
}

dependencyResolutionManagement {
    // 改为 PREFER_SETTINGS 而不是 FAIL_ON_PROJECT_REPOS
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        
        // JitPack 仓库 (用于第三方开源库)
        maven { url = uri("https://jitpack.io") }
        
        // Fallback repositories
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
    }
}

rootProject.name = "cur_app"
include(":app")