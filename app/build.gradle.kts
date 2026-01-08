plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
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
        versionCode = 7
        versionName = "1.3-fix"

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




//// 自动生成开源许可列表
//aboutLibraries {
//    // Remove the "generated" timestamp to allow for reproducible builds
//    excludeFields = arrayOf("generated")
//}

aboutLibraries {
    export {
        // Define the output path for manual generation
        // Adjust the path based on your project structure (e.g., composeResources, Android res/raw)
        outputFile = file("src/main/res/raw/aboutlibraries.json")
        // Optionally specify the variant for export
        // variant = "release"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // boom,神奇的库，用于自动更新其他的库好像，不会用:3
    implementation(platform(libs.androidx.compose.bom))

    // compose
    implementation(libs.bundles.compose)


    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))


    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.ui.tooling.preview)
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

    // kmp的pdf预览，上面那个会复制文件，乱死了
    implementation(libs.compose.pdf)

    // kolor!!
    implementation(libs.materialKolor)

    // 偷的别人的包(
    implementation(project(":Color-Picker"))

    // 开源许可列表无敌方便，我以后每个项目都用这个
    implementation(libs.aboutlibraries.compose.core)
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)

    // Koin BOM
    // 轻量而kotlin友好的现代方便易懂简单的注入
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    // Ktor，现代轻量而kotlin友好的网络库
    implementation(libs.bundles.ktor)

    //kyant超绝液态玻璃!!!
    implementation(libs.bundles.liquid.glass)

    // voyager 不依赖ksp的路由，或许以后能升级KMP。
    implementation(libs.bundles.voyager)
}

