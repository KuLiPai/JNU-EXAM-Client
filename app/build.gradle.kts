plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
//    alias(libs.plugins.hilt)
    alias(libs.plugins.aboutLibraries)

}

android {
    namespace = "jnu.kulipai.exam"
    // 安卓16是Baklava，没吃过，看着像切糕？
    compileSdk = 36

    defaultConfig {
        applicationId = "jnu.kulipai.exam"
        // 安卓9，Pie，3.14(
        minSdk = 28
        targetSdk = 36
        // 我喜欢Android13,即内部编号为Tiramisu
        // 正好有个Android12L导致api和版本号对上了12-11，12l-12，13-33
        // 12L是首个为适配安卓平板专门推出的，我手机也刷过，非常棒的底栏和win类似app切换
        // 12L是有无法卸载app漏洞出现的版本，即3700个Activity的应用无法卸载
        versionCode = 4
        versionName = "1.3-Alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }



    buildTypes {
        // 发布编译时候
        release {
            // 代码优化混淆 哈哈，全部代码变成aabbab了吧
            isMinifyEnabled = true
            // 资源优化混淆
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
    // 喂，都过时了还不知道改吗，
    // ...,按照ai的直接项目炸了，不碰了
    // 改好了
//    kotlinOptions {
//        jvmTarget = "17"
//        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
//    }
    // LuaHook的成功适配新版了，这里看到了也复制过来
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true

        // 666,gpa 8开始默认关闭，导致我弄了半天
        buildConfig = true
    }
}




// 自动生成开源许可列表
aboutLibraries {
    // Remove the "generated" timestamp to allow for reproducible builds
    excludeFields = arrayOf("generated")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // boom,神奇的库，用于自动更新其他的库好像，不会用:3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    // m3 i like
    implementation(libs.androidx.material3)
    // 无语，和上面的冲突，还要compileOnly不能implementation，解决了一下午，早上顿悟才解决，真是无语了
    //删了彻底解决问题
//    compileOnly(libs.material3.jvmstubs)

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
    //由于某些原因不用了，取而代之是Ktor，或许以后能升级KMP，总之kotlin万岁
//    implementation(libs.okhttp)

    // 目前我见过最好的pdf预览库了
    implementation(libs.jetpdfvue)
    //Compose pdf库 只能安卓13以上使用
//    implementation(libs.bouquet)
//    implementation(libs.android.pdf.viewer)

    // Hilt 依赖
    // 在compose中感觉能用上，第一次用
    // 由于某些原因不用了，虽然是安卓官方推荐的，取而代之是Koin，或许以后能升级KMP。总之kotlin万岁
    // 可能hilt有点神秘，或许是复杂了
//    implementation(libs.hilt.android)
//    ksp(libs.hilt.android.compiler)
//    implementation(libs.androidx.hilt.navigation.compose)

    // destinations自动路由，路由器（
    //wowwowow 太好用了吧，看文档晕晕的，用下来也太爽了，只能说无敌
    implementation(libs.core)
    ksp(libs.compose.destinations.ksp)


    // kolor!!
    implementation(libs.materialKolor)

    // 偷的别人的包(
    implementation(project(":Color-Picker"))

    // 开源许可列表无敌方便，我以后每个项目都用这个
    implementation(libs.aboutlibraries.compose.core)
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)


    // Koin BOM
    // 轻量而kotlin友好现代方便易懂简单的注入
    implementation(platform(libs.koin.bom))

    // Koin Core
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Koin Compose
    implementation(libs.koin.androidx.compose)

    // Ktor，现代轻量而kotlin友好的网络库
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)


}

