// file: MainActivity.kt
package jnu.kulipai.exam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.compose.waveloading.DrawType
import com.github.compose.waveloading.WaveLoading
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.setruth.themechange.components.MaskAnimActive
import com.setruth.themechange.components.MaskBox
import com.setruth.themechange.model.MaskAnimModel
import jnu.kulipai.exam.ui.theme.期末无挂Theme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var appPrefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appPrefs = AppPreferences(applicationContext)

        setContent {
            // 使用 a mutableState 来控制显示欢迎页还是主页
            val showWelcomeScreen = remember { mutableStateOf(appPrefs.isFirstLaunch) }

            if (showWelcomeScreen.value) {
                WelcomeApp(
                    onFinish = {
                        // 当引导流程结束时，更新 SharedPreferences 并切换到主页
                        appPrefs.isFirstLaunch = false
                        showWelcomeScreen.value = false
                    }
                )
            } else {
                MainApp()
            }
        }
    }
}

// ------------------- 主应用界面 -------------------

@Composable
fun MainApp() {
    var isDarkTheme by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }
    var pendingThemeChange by remember { mutableStateOf<Boolean?>(null) }
    val systemUiController = rememberSystemUiController()
    val initialDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        isDarkTheme = initialDarkTheme
    }

    LaunchedEffect(isDarkTheme) {
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = !isDarkTheme)
    }

    MaskBox(
        animTime = 1000L,
        maskComplete = {
            pendingThemeChange?.let { newTheme ->
                isDarkTheme = newTheme
                pendingThemeChange = null
            }
        },
        animFinish = {
            isAnimating = false
        }
    ) { maskAnimActiveEvent ->
        期末无挂Theme(darkTheme = isDarkTheme) {
            MainScaffold(
                isDarkTheme = isDarkTheme,
                isAnimating = isAnimating,
                onThemeToggle = { animModel, x, y ->
                    if (!isAnimating) {
                        isAnimating = true
                        pendingThemeChange = !isDarkTheme
                        maskAnimActiveEvent(animModel, x, y)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(isDarkTheme: Boolean, isAnimating: Boolean, onThemeToggle: MaskAnimActive) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            key(isDarkTheme) {
                TopAppBar(
                    title = { Text("期末无挂") },
                    actions = {
                        ThemeToggleButton(
                            isDarkTheme = isDarkTheme,
                            isAnimating = isAnimating,
                            onThemeToggle = onThemeToggle
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            }
        }
    ) { innerPadding ->
        MainContent(modifier = Modifier.padding(innerPadding))
    }
}

// ... MainContent 和 ThemeToggleButton 等 Composable 保持不变 ...
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
            contentDescription = if (isDarkTheme) "切换到亮色模式" else "切换到暗色模式",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class LoadingState { Loading, Loaded }

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    var loadingState by remember { mutableStateOf(LoadingState.Loading) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.app_list_loading))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = loadingState == LoadingState.Loading,
        iterations = LottieConstants.IterateForever,
        speed = 1f
    )

    LaunchedEffect(Unit) {
        delay(3000)
        loadingState = LoadingState.Loaded
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = loadingState == LoadingState.Loading,
            enter = fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)),
        ) {
            LottieAnimation(composition = composition, progress = { progress })
        }

        AnimatedVisibility(
            visible = loadingState == LoadingState.Loaded,
            enter = fadeIn(animationSpec = tween(durationMillis = 50)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = "数据已加载完成！", modifier = Modifier.padding(16.dp))
                Button(onClick = { /* Do something */ }) {
                    Text("点击这里")
                }
            }
        }
    }
}

// ------------------- 欢迎与引导流程 -------------------

// 为 WelcomeApp 添加 onFinish 回调
@Composable
fun WelcomeApp(onFinish: () -> Unit) {
    val isSystemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(isSystemDark) }
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(isDarkTheme) {
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = !isDarkTheme)
    }

    期末无挂Theme(darkTheme = isDarkTheme) {
        NavigationApp(onFinish = onFinish) // 将回调传递下去
    }
}

// 页面路由定义
sealed class Screen(val route: String) {
    object One : Screen("one_screen")
    object Two : Screen("two_screen")
    object Three : Screen("three_screen")
}

// 动画参数
private const val ANIMATION_DURATION = 400
private val smoothEasing = FastOutSlowInEasing

// 为 NavigationApp 添加 onFinish 回调
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationApp(onFinish: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.One.route,
        enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = smoothEasing)) },
        exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = smoothEasing)) },
        popEnterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = smoothEasing)) },
        popExitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = smoothEasing)) }
    ) {
        composable(route = Screen.One.route) { OneScreen(navController = navController) }
        composable(route = Screen.Two.route) { TwoScreen(navController = navController) }
        composable(route = Screen.Three.route) {
            // 将 onFinish 回调传递给最终页面
            ThreeScreen(onFinish = onFinish)
        }
    }
}

// 页面 Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OneScreen(navController: NavController) {
    // ... OneScreen 的代码保持不变 ...
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xfff9f9ff)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Text(
                "Welcome To\n期末无挂",
                style = MaterialTheme.typography.displayMedium,
                lineHeight = 64.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp, 88.dp, 0.dp, 0.dp)
            )
            WaveLoading(progress = 0.6f, backDrawType = DrawType.None) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "",
                    modifier = Modifier.scale(1.3f)
                )
            }
            FloatingActionButton(
                containerColor = Color(0xffd6e3ff),
                contentColor = Color(0xff284777),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(0.dp, 0.dp, 0.dp, 64.dp),
                onClick = { navController.navigate(Screen.Two.route) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoadingIndicator(
                        color = Color(0xff284777),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TwoScreen(navController: NavController) {
    // ... TwoScreen 的代码保持不变 ...
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xffeee2bc))
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lib_reference_summer))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
        LottieAnimation(
            modifier = Modifier
                .size(352.dp)
                .align(Alignment.Center)
                .offset(0.dp, -128.dp),
            composition = composition,
            progress = { progress },
        )
        Text(
            "不挂科",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(0.dp, 88.dp),
            style = MaterialTheme.typography.displayMedium.copy(color = Color(0xff4e472a)),
        )
        Text(
            "过过过过过过过过过",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(0.dp, 144.dp)
                .padding(32.dp),
            style = MaterialTheme.typography.displaySmall.copy(color = Color(0xff817C7C)),
        )
        FloatingActionButton(
            contentColor = Color(0xff534600),
            containerColor = Color(0xfff8e287),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(0.dp, 0.dp, 0.dp, 64.dp),
            onClick = { navController.navigate(Screen.Three.route) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingIndicator(color = Color(0xff534600), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Next")
            }
        }
    }
}

// 为 ThreeScreen 添加 onFinish 回调
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThreeScreen(onFinish: () -> Unit) {
    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xffffede8)), // 淡橙色背景

    ) {


        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lib_detail_rocket))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            speed = 1f
        )

        LottieAnimation(
            modifier = Modifier
                .width(352.dp)
                .height(352.dp)
                .align(Alignment.Center)
                .offset(0.dp, -128.dp),
            composition = composition,
            progress = { progress },
        )


        Text(
            "请选择仓库",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(0.dp, 64.dp),
            style = MaterialTheme.typography.displayMedium.copy(
                color = Color(0xff5d4037)
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(64.dp)
                .offset(0.dp, 136.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                var selected by remember { mutableStateOf(false) }

                FilterChip(
                    onClick = { selected = !selected },
                    label = {
                        Text("Gitee")
                    },
                    selected = selected,
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        {

                            Icon(
                                painter = painterResource(id = R.drawable.gitee_svgrepo_com),
                                tint = Color(0xff5d4037),
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }


                    },
                )

                Row(
                    modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = Color.Cyan, shape = CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "999ms",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xff8f4c38)
                        )
                    )
                }


            }


            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                var selected by remember { mutableStateOf(false) }

                FilterChip(
                    onClick = { selected = !selected },
                    label = {
                        Text("Github")
                    },
                    selected = selected,
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.github_142_svgrepo_com),
                                tint = Color(0xff5d4037),
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    },
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color = Color.Green, shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "111ms",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xff8f4c38)
                        )
                    )
                }

            }

        }


        FloatingActionButton(
            contentColor = Color(0xff723523),
            containerColor = Color(0xffffdbd1),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(0.dp, 0.dp, 0.dp, 64.dp),
            onClick = {
                onFinish() // 调用回调，切换到主界面
            }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingIndicator(
                    color = Color(0xff723523),
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Welcome")
            }
        }
    }
}