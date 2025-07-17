buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
    }
}

// 在这里添加统一的依赖解决策略
// 这个块应该放在你的 project-level build.gradle 文件中，与 buildscript 和 allprojects/subprojects 同级
subprojects {
    configurations.all {
        resolutionStrategy {
            // 强制统一 javapoet 的版本。
            // Dagger Hilt 2.56.2 内部依赖的 javapoet 通常是 1.13.0 或 1.12.1。
            force("com.squareup.javapoet:javapoet:1.13.0")
        }
    }
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "jnu.kulipai.exam"
    compileSdk = 36


    defaultConfig {
        applicationId = "jnu.kulipai.exam"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
    buildFeatures {
        compose = true
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    compileOnly(libs.material3.jvmstubs)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //强大的长久存储功能
    implementation(libs.androidx.datastore.preferences)
    //material图标库，貌似用不太到
    implementation(libs.androidx.material.icons.extended)
    //管理状态栏
    implementation(libs.accompanist.systemuicontroller)
    //非常棒的lottie
    implementation(libs.android.lottie.compose)
    //好像是路由
    implementation(libs.androidx.navigation.compose)
    //波浪加载动画
    implementation(libs.composewaveloading)
    //老牌http轻量强大完整库
    implementation(libs.okhttp)
    //Compose pdf库
    implementation(libs.bouquet)

    // Hilt 依赖
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose) // 使用 Compose 导航
}