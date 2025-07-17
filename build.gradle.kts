plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // 添加 Hilt Gradle 插件的声明和版本，注意 apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
}

subprojects {
    configurations.all {
        resolutionStrategy {
            // 强制所有模块使用指定的 javapoet 版本。
            // Dagger Hilt 2.56.2 通常兼容 javapoet:1.13.0。
            force("com.squareup.javapoet:javapoet:1.13.0")
        }
    }
}