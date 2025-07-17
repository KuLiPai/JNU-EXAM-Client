package jnu.kulipai.exam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jnu.kulipai.exam.util.Api
import jnu.kulipai.exam.util.FileManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 这个 Module 的生命周期与应用的 SingletonComponent 相同
class AppModule {
    // ✨ 提供 Api 单例实例 ✨
    @Provides
    @Singleton
    fun provideApiService(): Api {
        // 因为 Api 是一个 object，所以直接返回它的实例
        // 如果 Api object 内部依赖 Retrofit，你可以在这里传入
        return Api // 如果 Api object 内部通过构造函数或其他方式接收 Retrofit，你需要调整这里
        // 如果你的 Api object 内部不直接接收 Retrofit 实例，而是有一个方法来设置它，
        // 或者它是一个 Retrofit 接口实例，你可能需要这样：
        // return retrofit.create(Api::class.java) // 假设 Api 是一个 Retrofit 接口
    }

    // ✨ 提供 FileManager 单例实例 ✨
    @Provides
    @Singleton
    fun provideFileManager(): FileManager {
        // 因为 FileManager 是一个 object，所以直接返回它的实例
        return FileManager
    }

}