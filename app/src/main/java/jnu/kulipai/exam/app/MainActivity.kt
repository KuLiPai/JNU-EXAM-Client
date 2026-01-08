package jnu.kulipai.exam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import com.materialkolor.PaletteStyle
import jnu.kulipai.exam.ui.anim.AppTransition
import jnu.kulipai.exam.ui.screens.home.MainScreen
import jnu.kulipai.exam.ui.screens.welcome.WelcomeScreen
import jnu.kulipai.exam.ui.theme.期末无挂Theme
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.androidx.compose.koinViewModel

// 7.21 0:30
// 总结一下
// 首先是文件 一个页面一个文件夹，有screen，子页面，viewModel
// 有@hilt的viewModel 在@compose中用hiltviewModel获取
// 有一个全局的主题单例，在其他的viewModel中获取以及修改
// 改主题配置，适配新主题和背景外的背景主题色
// 睡觉

// 12.12 14:08
// 拜拜hilt，你好Koin


class MainActivity : ComponentActivity() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainActivityViewModel = koinViewModel()

            LaunchedEffect(Unit) {
                viewModel.updateSourceFile()
            }

            val dynamicColors by viewModel.dc.collectAsStateWithLifecycle(isSystemInDarkTheme())
            val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(0)
            val amoledBlack by viewModel.amoledBlack.collectAsStateWithLifecycle(false)
            val firstLaunch by viewModel.firstLaunch.collectAsStateWithLifecycle(null)
            val colorSeed by viewModel.colorSeed.collectAsStateWithLifecycle(initialValue = Color.Red)
            val paletteStyle by viewModel.paletteStyle.collectAsStateWithLifecycle(initialValue = PaletteStyle.TonalSpot)

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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.background
                        )
                ) {

                    val screen = when (firstLaunch) {
                        null -> null          // 还没加载完
                        true -> WelcomeScreen()
                        false -> MainScreen()
                    }

                    if (screen != null) {
                        Navigator(screen) { navigator ->
                            ScreenTransition(
                                navigator = navigator,
                                transition = {
                                    AppTransition.animate(
                                        navigator,
                                        targetState,
                                        initialState
                                    )
                                }
                            )
                        }
                    } else {
                        Spacer(Modifier)
                    }


                }
            }
        }
    }
}