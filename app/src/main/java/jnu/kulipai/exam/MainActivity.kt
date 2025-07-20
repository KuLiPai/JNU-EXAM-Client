package jnu.kulipai.exam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.setruth.themechange.components.MaskAnimActive
import com.setruth.themechange.model.MaskAnimModel
import dagger.hilt.android.AndroidEntryPoint
import jnu.kulipai.exam.data.model.ThemeState
import jnu.kulipai.exam.ui.screens.MainApp
import jnu.kulipai.exam.ui.screens.SettingScreen
import jnu.kulipai.exam.ui.theme.期末无挂Theme
import jnu.kulipai.exam.viewmodel.HomeViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Inject


//val pwd = mutableStateOf("/")
//var loadingState = mutableStateOf(LoadingState.Loading)

@AndroidEntryPoint // Hilt 入口点
class MainActivity : ComponentActivity() {


    // 通过 Hilt 注入 HomeViewModel

    @Inject
    lateinit var appPrefs: AppPreferences

    private val homeViewModel: HomeViewModel by viewModels()


    private val exportLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            uri?.let {
                homeViewModel.exportFileToUri(homeViewModel.exportPath, it)
            }
        }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        homeViewModel.exportLauncher = exportLauncher


        setContent {
            val isDarkTheme = ThemeState.isDark
            期末无挂Theme(darkTheme = isDarkTheme) {
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
                        MyApp()
                    }
                }
            }
        }

        //拦截返回，细节但很实用，（可能不算细节）
        //有bug，如果用户退出在打开，就无法拦截了
        //todo 加那个第一次启动监听，或者其他方法
        //7.18号，看不懂to do在说什么，而且发现功能失效了，无语了
        //7.18感谢ai，竟然之前悄悄的在我homeViewModel里加了方法
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (homeViewModel.currentPath.value != "/") {
                    homeViewModel.handleBackPress()
                } else {
                    isEnabled = false // 禁用当前的 OnBackPressedCallback
                    onBackPressedDispatcher.onBackPressed() // 触发默认的返回行为
                }
            }
        })


    }


    @Composable
    fun MyApp() {
        val navController = rememberNavController()

        NavHost(
            navController = navController, startDestination = "main",
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + scaleIn(
                    animationSpec = tween(500),
                    initialScale = 0.9f,
                    transformOrigin = TransformOrigin.Center
                )
            },

            exitTransition = {
                fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 500),
                    initialScale = 0.9f,
                    transformOrigin = TransformOrigin.Center
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 500),
                    initialAlpha = 0.0f // 一开始完全透明
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 300))
            }


        ) {
            composable("main") {
                MainApp(appPrefs, homeViewModel = homeViewModel, navController = navController)
            }
            composable("set") {
                SettingScreen(appPrefs, navController = navController)
            }

            // 定义带参数的路由
//            composable(
//                "second_screen_with_param/{message}",
//                arguments = listOf(navArgument("message") { type = NavType.StringType })
//            ) { backStackEntry ->
//                val message = backStackEntry.arguments?.getString("message")
//                SettingScreen(message = message,navController = navController)
//            }
        }
    }


}


//夜间切换按钮
@Composable
fun ThemeToggleButton(
    isAnimating: Boolean,
    onThemeToggle: MaskAnimActive
) {
    var buttonPosition by remember { mutableStateOf(Offset.Zero) }
    val isDarkTheme = ThemeState.isDark

    IconButton(
        onClick = {
            if (isAnimating) return@IconButton
            onThemeToggle(MaskAnimModel.EXPEND, buttonPosition.x, buttonPosition.y)
        },
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            buttonPosition = Offset(
                x = position.x + coordinates.size.width / 2f,
                y = position.y + coordinates.size.height / 2f
            )
        }
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
            contentDescription = if (isDarkTheme) "切换到亮色模式" else "切换到暗色模式",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
