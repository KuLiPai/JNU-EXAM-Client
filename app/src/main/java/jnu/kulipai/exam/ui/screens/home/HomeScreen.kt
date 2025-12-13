package jnu.kulipai.exam.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import jnu.kulipai.exam.components.HomeTopBar
import jnu.kulipai.exam.components.LiquidBottomTab
import jnu.kulipai.exam.components.LiquidBottomTabs
import jnu.kulipai.exam.data.model.MaskAnimActive
import jnu.kulipai.exam.ui.util.ScreenshotThemeTransition
import jnu.kulipai.exam.util.Api.getSourceJson
import org.koin.androidx.compose.koinViewModel

class MainScreen : Screen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow

        val viewModel: HomeViewModel = koinViewModel()

        val context = LocalContext.current


        // 每次打开加载一次源
        LaunchedEffect(Unit) {
            getSourceJson(context, viewModel.appPre.sourceUrl)
        }


        // 创建一个用于导出文件的回调
        // 1. 创建 Launcher
        val createDocumentLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/octet-stream") // 默认使用通用流，防止乱加 .txt
        ) { uri ->
            // 3. 用户选完路径后的回调
            if (uri != null) {
                viewModel.exportFileToUri(uri) // 调用 ViewModel 里的实际写入逻辑
            }
        }

        // 2. 将 Launcher 传递给 ViewModel (这一步非常重要，否则 vm 里的 exportLauncher 是 null)
        LaunchedEffect(Unit) {
            viewModel.setExportLauncher(createDocumentLauncher)
        }


        val appPrefs = viewModel.appPre
        // 初始化一次

        // 这个好乱，我问ai怎么优化代码，他让我再写一个类就写一个配置一行代码，我说能不能写进viewModel
        // ai说x，要写进一个新文件，
        // 我说f**k(
        // 已经优化掉了，当我没说


        val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(0)
        if (darkTheme == 0) {
            viewModel.updateDarkTheme(if (isSystemInDarkTheme()) 2 else 1)
        }


        ScreenshotThemeTransition(
            isDarkTheme = (darkTheme == 2),
            modifier = Modifier
        ) { startAnim ->

            MainScaffold(
                isDarkTheme = (darkTheme == 2),
                isAnimating = false, // 这个参数现在不重要了
                homeViewModel = viewModel,

                onThemeToggle = { _, x, y ->

                    val isExpand = (darkTheme == 2)

                    // 调用 startAnim
                    // 参数1: 位置
                    // 参数2: 扩张还是收缩
                    // 参数3: 【核心】真正切换数据的操作，放在这里面
                    startAnim(Offset(x, y), isExpand) {
                        // 这个代码块会在截图完成后执行
                        val newTheme = when (darkTheme) {
                            1 -> true
                            2 -> false
                            else -> true
                        }
                        viewModel.updateDarkTheme(if (newTheme) 2 else 1)
                        appPrefs.isNight = newTheme

                    }
                },
                navController = navigator,
            )
        }
    }
}


val LocalScaffoldPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    isDarkTheme: Boolean,
    isAnimating: Boolean,
    homeViewModel: HomeViewModel,
    onThemeToggle: MaskAnimActive,
    navController: Navigator
) {
    // 1. 定义 Tab 列表
    val tabs = remember { listOf(HomeTab, ManagerTab, SettingsTab) }


    // 2. 启动 Voyager 的 TabNavigator
    TabNavigator(HomeTab) { tabNavigator ->

        // 3. 计算当前 Index 给底栏用
        var currentTabIndex by rememberSaveable { mutableIntStateOf(tabs.indexOf(tabNavigator.current)) }


        // 4. 初始化 Backdrop (用于底栏模糊效果)
        val backgroundColor = MaterialTheme.colorScheme.background
        val backdrop = rememberLayerBackdrop {
            drawRect(backgroundColor) // 绘制背景防重影
            drawContent()             // 绘制下方的内容（即 CurrentTab）
        }



        Scaffold(
            contentWindowInsets = WindowInsets(bottom = 0),
            modifier = Modifier.fillMaxSize(),
            // TopBar 逻辑略有调整，见下文解释
            topBar = {
                // 只有在 HomeTab 时才显示这个复杂的 TopBar，或者根据 tabNavigator.current 切换不同的 TopBar
                if (tabNavigator.current == HomeTab) {
                    HomeTopBar(
                        homeViewModel,
                        isDarkTheme,
                        isAnimating,
                        onThemeToggle,
                        navController
                    )
                }
            },
            bottomBar = {
                // 5. 将底栏放在 Scaffold 的 bottomBar 中
                key(isDarkTheme) { // 保留你的 key 逻辑
                    val contentColor = if (isDarkTheme) Color.White else Color.Black
                    val iconColorFilter = ColorFilter.tint(contentColor)

                    LiquidBottomTabs(
                        myaccentColor = MaterialTheme.colorScheme.primary,
                        selectedTabIndex = { currentTabIndex }, // 读：当前 index
                        onTabSelected = { index ->
                            // 写：切换 Tab
                            currentTabIndex = index
                            tabNavigator.current = tabs[index]
                        },
                        backdrop = backdrop,  // 传入 backdrop
                        tabsCount = tabs.size,
                        modifier = Modifier
                            .padding(horizontal = 36.dp, vertical = 16.dp)
                            .navigationBarsPadding()
//                            .align(Alignment.BottomCenter)
                    ) {
                        repeat(tabs.size) { index ->
                            LiquidBottomTab(
                                // 点击逻辑其实在 onTabSelected 处理了，这里可能是多余的，看你组件具体实现
                                // 如果需要手动处理：onClick = { tabNavigator.current = tabs[index] }
                                {
                                    currentTabIndex = index

                                    tabNavigator.current = tabs[index]
                                }
                            ) {
                                Box(
                                    Modifier
                                        .size(28.dp)
                                        .paint(
                                            // 这里可以根据 index 获取不同图标
                                            tabs[index].options.icon!!,
                                            colorFilter = iconColorFilter
                                        )
                                )
                                BasicText(
                                    tabs[index].options.title, // 动态获取标题
                                    style = TextStyle(contentColor, 12.sp)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind { drawRect(backgroundColor) }
                    .layerBackdrop(backdrop)
            ) {
                // 使用 AnimatedContent 包裹内容
                AnimatedContent(
                    targetState = tabNavigator.current,
                    transitionSpec = {
                        val fromIndex = tabs.indexOf(initialState)
                        val toIndex = tabs.indexOf(targetState)

                        // 1. 【修改】位移专用 Spec (IntOffset)
                        val slideSpec = tween<IntOffset>(durationMillis = 300)

                        // 2. 【修改】透明度专用 Spec (Float)
                        val fadeSpec = tween<Float>(durationMillis = 300)

                        if (toIndex > fromIndex) {
                            // 向右切换
                            (slideInHorizontally(slideSpec) { it } + fadeIn(fadeSpec))
                                .togetherWith(slideOutHorizontally(slideSpec) { -it } + fadeOut(
                                    fadeSpec
                                ))
                        } else {
                            // 向左切换
                            (slideInHorizontally(slideSpec) { -it } + fadeIn(fadeSpec))
                                .togetherWith(slideOutHorizontally(slideSpec) { it } + fadeOut(
                                    fadeSpec
                                ))
                        }
                    },
                    label = "TabAnimation"
                ) { targetTab ->
                    // 这里必须使用 targetTab 来调用 Content，否则动画过程中显示的内容会一样
                    CompositionLocalProvider(LocalScaffoldPadding provides innerPadding) {
                        targetTab.Content()
                    }
                }
            }
        }
    }
}
