pluginManagement {
    repositories {
        gradlePluginPortal()   // 解析 gradle 插件
        google()               // 关键：AGP 在这里
        mavenCentral()         // 备用
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android") {
                useModule("com.android.tools.build:gradle:8.3.0")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HookLogModule"
include(":app")