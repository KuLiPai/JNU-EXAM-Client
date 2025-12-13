package jnu.kulipai.exam.di


import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import jnu.kulipai.exam.AppPreferences
import jnu.kulipai.exam.MainActivityViewModel
import jnu.kulipai.exam.data.repository.FileRepository
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import jnu.kulipai.exam.ui.screens.setting.appearance.SettingsAppearanceViewModel
import jnu.kulipai.exam.ui.screens.welcome.WelcomeViewModel
import jnu.kulipai.exam.ui.theme.ThemeSettingsManager
import jnu.kulipai.exam.util.Api
import jnu.kulipai.exam.util.FileManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

//
//// Koin的module
//// 优雅简洁又强大
//// 需要什么参数直接get()，自动找需要的参数填进去
//// 美妙

// 网络模块
val networkModule = module {
    single { HttpClient(CIO) }
}

// ViewModel 模块
val viewModelModule = module {
    viewModel { MainActivityViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { SettingsAppearanceViewModel(get()) }
    viewModel { WelcomeViewModel(get(),get(),get()) }
}

// App 主模块
val appModule = module {

    // Api
    single {
        Api.apply { init(get()) }
    }

    // FileManager
    single { FileManager }

    // Repository
    single { FileRepository(get(), get(), get()) }

    // 主题单例
    single { ThemeSettingsManager(get()) }

    // 配置信息
    single { AppPreferences(get()) }

    // 引入子模块
    includes(networkModule)

    includes(viewModelModule)
}



/////////////////////////下面的代码是历史了,从前谷歌有个儿子叫hilt,然而
/////////////////////////KMP兴起的时代,开发者不得不跟进时代
/////////////////////////社区的力量和kotlin的优雅+不依赖平台的产物诞生了
/////////////////////////这是我正真懂了依赖注入的一次,谢谢Koin

////di是什么意思dididi？
////哈哈ai写的，我还是第一次用这个hilt管理context，然后在单例中注入这个hilt呢
//@Module
//@InstallIn(SingletonComponent::class) // 这个 Module 的生命周期与应用的 SingletonComponent 相同
//class AppModule {
//    // ✨ 提供 Api 单例实例 ✨
//    @Provides
//    @Singleton
//    fun provideApiService(): Api {
//        // 因为 Api 是一个 object，所以直接返回它的实例
//        // 如果 Api object 内部依赖 Retrofit，你可以在这里传入
//        return Api // 如果 Api object 内部通过构造函数或其他方式接收 Retrofit，你需要调整这里
//        // 如果你的 Api object 内部不直接接收 Retrofit 实例，而是有一个方法来设置它，
//        // 或者它是一个 Retrofit 接口实例，你可能需要这样：
//        // return retrofit.create(Api::class.java) // 假设 Api 是一个 Retrofit 接口
//    }
//
//    // ✨ 提供 FileManager 单例实例 ✨
//    @Provides
//    @Singleton
//    fun provideFileManager(): FileManager {
//        // 因为 FileManager 是一个 object，所以直接返回它的实例
//        return FileManager
//    }
//
//}