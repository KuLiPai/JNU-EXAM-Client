package jnu.kulipai.exam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.materialkolor.PaletteStyle
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import dagger.hilt.android.AndroidEntryPoint
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import jnu.kulipai.exam.ui.screens.welcome.WelcomeApp
import jnu.kulipai.exam.ui.theme.期末无挂Theme
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Inject

// 7.21 0:30
// 总结一下
// 首先是文件 一个页面一个文件夹，有screen，子页面，viewModel
// 有@hilt的viewModel 在@compose中用hiltviewModel获取
// 有一个全局的主题单例，在其他的viewModel中获取以及修改
// 改主题配置，适配新主题和背景外的背景主题色
// 睡觉

@AndroidEntryPoint // Hilt 入口点
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPrefs: AppPreferences

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            val mainViewModel: MainActivityViewModel = hiltViewModel()

            /// 哈哈，没事就是笑笑
            val dynamicColors by mainViewModel.dc.collectAsStateWithLifecycle(isSystemInDarkTheme())
            val darkTheme by mainViewModel.darkTheme.collectAsStateWithLifecycle(0)
            val amoledBlack by mainViewModel.amoledBlack.collectAsStateWithLifecycle(false)
//            val firstLaunch by mainViewModel.firstLaunch.collectAsStateWithLifecycle(false)
            val colorSeed by mainViewModel.colorSeed.collectAsStateWithLifecycle(initialValue = Color.Red)
            val paletteStyle by mainViewModel.paletteStyle.collectAsStateWithLifecycle(initialValue = PaletteStyle.TonalSpot)
//            val autoUpdateChannel by mainViewModel.autoUpdateChannel.collectAsStateWithLifecycle(UpdateChannel.Disabled)
//            val updateDismissedName by mainViewModel.updateDismissedName.collectAsStateWithLifecycle("")


            //全局主题
            期末无挂Theme(
                darkTheme = when (darkTheme) {
                    1 -> false
                    2 -> true
                    else -> isSystemInDarkTheme()
                },
                dynamicColor = dynamicColors,
                amoled = amoledBlack,
                colorSeed = colorSeed,
                paletteStyle = paletteStyle
            ) {
                // 跳转动画防止白/黑边
                // 应该有更优雅的解决方案，累了，明天看看吧
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.background
                        )
                ) {
                    // 使用 a mutableState 来控制显示欢迎页还是主页
                    val showWelcomeScreen = remember { mutableStateOf(appPrefs.isFirstLaunch) }

                    if (showWelcomeScreen.value) {
                        WelcomeApp(
                            //忘了写路由了，只能简单的finish一下，一下子就没有动画了
                            onFinish = {
                                // 当引导流程结束时，更新 SharedPreferences 并切换到主页
                                appPrefs.isFirstLaunch = false
                                showWelcomeScreen.value = false
                            },
                            appPrefs
                        )
                    } else {
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController,
                        )
                    }
                }
            }
        }

        //拦截返回，细节但很实用，（可能不算细节）
        //有bug，如果用户退出在打开，就无法拦截了
        //todo 加那个第一次启动监听，或者其他方法
        //7.18号，看不懂to do在说什么，而且发现功能失效了，无语了
        //7.18感谢ai，竟然之前悄悄的在我homeViewModel里加了方法
//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (homeViewModel.currentPath.value != "/") {
//                    homeViewModel.handleBackPress()
//                } else {
//                    isEnabled = false // 禁用当前的 OnBackPressedCallback
//                    onBackPressedDispatcher.onBackPressed() // 触发默认的返回行为
//                }
//            }
//        })


    }

}


