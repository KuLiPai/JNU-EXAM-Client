package jnu.kulipai.exam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.setruth.themechange.components.MaskAnimActive
import com.setruth.themechange.model.MaskAnimModel
import dagger.hilt.android.AndroidEntryPoint
import jnu.kulipai.exam.data.model.LoadingState
import jnu.kulipai.exam.ui.screens.MainApp
import jnu.kulipai.exam.util.Api
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


val pwd = mutableStateOf("/")
var loadingState = mutableStateOf(LoadingState.Loading)

@AndroidEntryPoint // Hilt 入口点
class MainActivity : ComponentActivity() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appPrefs = AppPreferences(applicationContext)

        setContent {
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
                MainApp(appPrefs)
            }
        }


        //拦截返回，细节但很实用，（可能不算细节）
        //有bug，如果用户退出在打开，就无法拦截了
        //todo 加那个第一次启动监听，或者其他方法
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (pwd.value != "/") {

                    GlobalScope.launch {
                        loadingState.value = LoadingState.Step
                        delay(250)
                        pwd.value = Api.DotDot(pwd.value)
                        loadingState.value = LoadingState.Loaded
                    }
                } else {
                    // 放行
                    isEnabled = false  // 允许默认行为
                    onBackPressedDispatcher.onBackPressed() // 手动触发默认返回
                }
            }
        })


    }
}


//夜间切换按钮
@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onThemeToggle: MaskAnimActive,
    isAnimating: Boolean
) {
    var buttonPosition by remember { mutableStateOf(Offset.Zero) }

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
            contentDescription = if (isDarkTheme) "切换到亮色模式" else "切换到暗色模式",//一眼ai，但是写的好
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}