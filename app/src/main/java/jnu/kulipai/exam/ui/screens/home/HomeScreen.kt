package jnu.kulipai.exam.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.setruth.themechange.components.MaskAnimActive
import jnu.kulipai.exam.R
import jnu.kulipai.exam.components.BounceUpButton
import jnu.kulipai.exam.components.FileCard
import jnu.kulipai.exam.components.FolderCard
import jnu.kulipai.exam.components.LiquidBottomTab
import jnu.kulipai.exam.components.LiquidBottomTabs
import jnu.kulipai.exam.components.ThemeToggleButton
import jnu.kulipai.exam.data.model.DirNode
import jnu.kulipai.exam.data.model.FileItem
import jnu.kulipai.exam.data.model.LoadingState
import jnu.kulipai.exam.ui.screens.setting.SettingScreen
import jnu.kulipai.exam.ui.util.ScreenshotThemeTransition
import jnu.kulipai.exam.util.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


//这里有笼罩动画，状态栏
class MainScreen : Screen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow


        val viewModel: HomeViewModel = koinViewModel()
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
                uri?.let {
                    viewModel.exportFileToUri(viewModel.exportPath, it)
                }
            }

        // 设置 launcher 给 ViewModel
        LaunchedEffect(Unit) {
            viewModel.setExportLauncher(launcher)
        }
        // wow 这个单例原来可以哦哦，对的
        // 单例然后保存到viewModel然后就获取然后就唯一的
        // 全局的!
        val appPrefs = viewModel.appPre
        // 初始化一次

        // 这个好乱，我问ai怎么优化代码，他让我再写一个类就写一个配置一行代码，我说能不能写进viewModel
        // ai说x，要写进一个新文件，
        // 我说f**k(
        // 已经优化掉了，当我没说


        val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(0)




        ScreenshotThemeTransition(
            isDarkTheme = (darkTheme == 2),
            modifier = Modifier
        ) { startAnim ->

            MainScaffold(
                isDarkTheme = (darkTheme == 2),
                isAnimating = false, // 这个参数现在不重要了
                homeViewModel = viewModel,

                // 🔥🔥🔥 重点修改：点击事件 🔥🔥🔥
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

//非常棒的Material 3 Experiment
@OptIn(ExperimentalMaterial3Api::class)
//标题栏
@Composable
fun MainScaffold(
    isDarkTheme: Boolean,
    isAnimating: Boolean,
    homeViewModel: HomeViewModel, // 接收 ViewModel
    onThemeToggle: MaskAnimActive,
    navController: Navigator
) {


    val pwd = homeViewModel.currentPath.collectAsState()
    val searchText = homeViewModel.searchText.collectAsState()
    homeViewModel.isSearch.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var shouldBlockBack by remember { mutableStateOf(false) }

    LaunchedEffect(pwd.value) {
        shouldBlockBack = pwd.value != "/"


    }

    // 拦截系统返回
    BackHandler(enabled = shouldBlockBack) {
        // 你可以弹窗确认、执行某些操作等
        homeViewModel.handleBackPress()
    }



    Scaffold(
        contentWindowInsets = WindowInsets(bottom = 0),

        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus() // 点击外部取消焦点
                })
            },
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                key(isDarkTheme) {

                    TopAppBar(
                        title = {

                            AnimatedContent(
                                targetState = pwd.value, // 监视 pwd 的变化
                                transitionSpec = {
                                    // 定义进入和退出动画
                                    // 旧内容向左滑动并淡出，新内容从右侧滑入并淡入
                                    if (targetState.length > initialState.length) {
                                        (slideInHorizontally { fullWidth -> fullWidth } + fadeIn())
                                            .togetherWith(slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut())
                                            // SizeTransform 会动画 AnimatedContent 容器的尺寸变化
                                            .using(SizeTransform(clip = false)) // clip = false 防止内容在动画过程中被裁剪
                                    } else {
                                        (slideInHorizontally { fullWidth -> -fullWidth } + fadeIn())
                                            .togetherWith(slideOutHorizontally { fullWidth -> fullWidth } + fadeOut())
                                            // SizeTransform 会动画 AnimatedContent 容器的尺寸变化
                                            .using(SizeTransform(clip = false)) // clip = false 防止内容在动画过程中被裁剪
                                    }

                                },
                                modifier = Modifier.animateContentSize(animationSpec = tween(150)), // 仍然保留 animateContentSize 来动画 Text 组件自身的尺寸变化
                                label = "PathTextAnimation"
                            ) { targetPwd -> // targetPwd 是当前动画的目标 pwd 值
                                Text(
                                    if (targetPwd == "/") "期末无挂" else if (targetPwd.length >= 8) ".." + targetPwd.substring(
                                        targetPwd.length - 8
                                    ) else targetPwd,
//                                modifier = Modifier.animateContentSize(),
                                    maxLines = 1, // 确保文本在一行内，方便水平滑动动画
                                    overflow = TextOverflow.Ellipsis
                                )
                            }


//                        Text(
//                            if (pwd.value == "/") "期末无挂" else if (pwd.value.length >= 8) ".." + pwd.value.substring(
//                                pwd.value.length - 8
//                            ) else pwd.value,
//                            modifier = Modifier.animateContentSize(),
//                        )
                        },
                        navigationIcon = {
                            if (pwd.value != "/") {
                                Icon(
                                    modifier = Modifier.padding(16.dp, 0.dp, 4.dp, 0.dp),
                                    painter = painterResource(R.drawable.folder_open_24px),
                                    contentDescription = null
                                )
                            } else {
                                Icon(
                                    modifier = Modifier.padding(16.dp, 0.dp, 4.dp, 0.dp),
                                    painter = painterResource(R.drawable.biglogo),
                                    contentDescription = null
                                )
                            }

                        },
                        actions = {
                            //别忘了路径过长隐藏一些按钮，
                            //好吧忘了，不对懒了
                            ThemeToggleButton(
                                isAnimating = isAnimating,
                                onThemeToggle = onThemeToggle,
                                homeViewModel
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = {
                                navController.push(SettingScreen())
                            }) {
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

                var searchJob: Job? = null

                OutlinedTextField(
                    value = searchText.value,
                    onValueChange = { newText ->
                        homeViewModel.setSearchText(newText)
                        homeViewModel.setLoadingState(LoadingState.Loading)
                        homeViewModel.setisSearch(false)

                        searchJob?.cancel()
                        searchJob = CoroutineScope(Dispatchers.Main).launch {
                            if (newText.isNotEmpty()) {
                                delay(300) // 延迟300ms
                                homeViewModel.setisSearch(true)
                                homeViewModel.setLoadingState(LoadingState.Loaded)

                            }
                        }

                    },
                    placeholder = { Text("搜索") },
                    leadingIcon = {
                        Row {
                            Spacer(Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索图标"
                            )
                        }
                    },
                    trailingIcon = { // 右侧图标 (取消按钮)
                        if (searchText.value.isNotEmpty()) { // 只有当有文字时才显示
                            Row {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清空输入",
                                    modifier = Modifier.clickable { // 添加点击事件
                                        homeViewModel.setSearchText("")
                                        focusManager.clearFocus()
                                    }
                                )
                                Spacer(Modifier.width(8.dp))

                            }
                        }
                    },
                    shape = RoundedCornerShape(percent = 50),
                    singleLine = true,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(26.dp, 8.dp)
                        .focusRequester(focusRequester)
                )


            }
        },

        ) { innerPadding ->

        Column {

            MainContent(
                modifier = Modifier
                    .padding(innerPadding),
                homeViewModel,
                navController
            )


        }
    }
}


//主页

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    navController: Navigator
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val isLightTheme by remember { mutableStateOf(homeViewModel.appPre.isNight) }


    val contentColor = if (isLightTheme) Color.Black else Color.White
    val iconColorFilter = ColorFilter.tint(contentColor)

    // 1. 定义唯一的 backdrop 控制器
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor) // 绘制背景色，防止透明导致重影
        drawContent()             // 绘制下方的内容（即 MainContent）
    }
    // 2. 这里的 state 放在 Scaffold 层级
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }


    val appPrefs = homeViewModel.appPre
    val loadingState =
        homeViewModel.loadingState.collectAsState() // 从 ViewModel 收集 loadingState
    val pwd = homeViewModel.currentPath.collectAsState() // 从 ViewModel 收集 loadingState
    val root = homeViewModel.root.collectAsState() // 从 ViewModel 收集 loadingState
    val searchText = homeViewModel.searchText.collectAsState() // 从 ViewModel 收集 loadingState
    val isSearch = homeViewModel.isSearch.collectAsState()

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.app_list_loading))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = loadingState.value == LoadingState.Loading,
        iterations = LottieConstants.IterateForever,
        speed = 1f,
    )


    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 1. 先画背景 (解决重影)
                .drawBehind {
                    drawRect(backgroundColor)
                }
                // 2. 再进行捕捉 (解决 Tab 看不到内容)
                // 注意：layerBackdrop 必须加在这个包裹了所有内容的 Box 上
                .layerBackdrop(backdrop)
        ) {
            AnimatedVisibility(
                visible = loadingState.value == LoadingState.Loading,
                enter = fadeIn(animationSpec = tween(durationMillis = 100)),
                exit = fadeOut(animationSpec = tween(durationMillis = 50)),
            ) {

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = modifier.offset(y = (-128).dp)
                )

            }

            AnimatedVisibility(
                visible = loadingState.value == LoadingState.Loaded,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 50)),
            ) {

                lateinit var newdata: List<Any>


                if (searchText.value.isNotEmpty() && isSearch.value) {
                    newdata = FileManager.searchFiles(root.value, searchText.value)

                } else if (pwd.value == "/") {
                    val targetContent = FileManager.getDirContent(root.value, pwd.value)
                    targetContent?.let {
                        newdata = it.subDirs + it.files
                    }
                } else {
                    val targetContent = FileManager.getDirContent(root.value, pwd.value)
                    targetContent?.let {
                        newdata = listOf(
                            DirNode(
                                name = "..",
                                path = "",
                            )
                        ) + it.subDirs + it.files
                    }
                }


                Box {

                    LazyColumn {
                        items(newdata) { item ->
                            if (item is DirNode) {
                                FolderCard(name = item.name, { name ->
                                    homeViewModel.navigateTo(name)
                                })
                            } else if (item is FileItem) {
                                FileCard(item, homeViewModel, navController)
                            }

                        }
                        item {
                            Spacer(Modifier.height(128.dp))
                        }
                    }
                }




                if (appPrefs.update != 0) {
                    // 第一次打开，记录时间
                    val firstOpenTime by remember { mutableLongStateOf(appPrefs.day) }

                    if (firstOpenTime == -1L) {
                        appPrefs.day = System.currentTimeMillis()
                    } else {
                        if ((System.currentTimeMillis() - firstOpenTime) / (1000 * 60 * 60 * 24) >= appPrefs.update) {
                            BounceUpButton({
                                homeViewModel.updateRepositoryData()
                            })
                        }
                    }
                }
            }
        }


        LiquidBottomTabs(
            myaccentColor = MaterialTheme.colorScheme.primary,
            selectedTabIndex = { selectedTabIndex },
            onTabSelected = { selectedTabIndex = it },
            backdrop = backdrop,  // 传入上面定义的那个 backdrop
            tabsCount = 3,
            modifier = Modifier
                .padding(horizontal = 36.dp, vertical = 16.dp) // 调整 padding
                .navigationBarsPadding() // 适配底部手势条
                .align(Alignment.BottomCenter) // 放在底部
        ) {
            repeat(3) { index ->
                LiquidBottomTab({ selectedTabIndex = index }) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .paint(
                                painterResource(R.drawable.notifications_24px),
                                colorFilter = iconColorFilter
                            )
                    )
                    BasicText(
                        "Tab ${index + 1}",
                        style = TextStyle(contentColor, 12.sp)
                    )
                }
            }
        }

    }
}
