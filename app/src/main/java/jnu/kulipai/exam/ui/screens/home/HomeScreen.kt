package jnu.kulipai.exam.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import jnu.kulipai.exam.R
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
            getSourceJson(context)
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
            // 6. 内容区域
            Box(
                modifier = Modifier
//                    .padding(innerPadding)
                    .fillMaxSize()
                    .drawBehind { drawRect(backgroundColor) } // 防重影
                    .layerBackdrop(backdrop) // ★★★ 关键：让底栏能捕捉到这里面的内容
            ) {
                // 7. 显示当前 Tab
                // Voyager 会自动保存 HomeTab 的状态（滚动位置等）
                CompositionLocalProvider(LocalScaffoldPadding provides innerPadding) {
                    tabNavigator.current.Content()

                }
            }
        }
    }
}

//
////非常棒的Material 3 Experiment
//@OptIn(ExperimentalMaterial3Api::class)
////标题栏
//@Composable
//fun MainScaffold(
//    isDarkTheme: Boolean,
//    isAnimating: Boolean,
//    homeViewModel: HomeViewModel, // 接收 ViewModel
//    onThemeToggle: MaskAnimActive,
//    navController: Navigator
//) {
//
//
//    val pwd = homeViewModel.currentPath.collectAsState()
//    val searchText = homeViewModel.searchText.collectAsState()
//    homeViewModel.isSearch.collectAsState()
//    val focusManager = LocalFocusManager.current
//    val focusRequester = remember { FocusRequester() }
//
//    var shouldBlockBack by remember { mutableStateOf(false) }
//
//    LaunchedEffect(pwd.value) {
//        shouldBlockBack = pwd.value != "/"
//    }
//
//    // 拦截系统返回
//    BackHandler(enabled = shouldBlockBack) {
//        // 你可以弹窗确认、执行某些操作等
//        homeViewModel.handleBackPress()
//    }
//
//
//    //TODO)) 封装控件，不要过多嵌套
//    Scaffold(
//        contentWindowInsets = WindowInsets(bottom = 0),
//
//        modifier = Modifier
//            .fillMaxSize()
//            .pointerInput(Unit) {
//                detectTapGestures(onTap = {
//                    focusManager.clearFocus() // 点击外部取消焦点
//                })
//            },
//        topBar = {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//
//                key(isDarkTheme) {
//
//                    TopAppBar(
//                        title = {
//
//                            AnimatedContent(
//                                targetState = pwd.value, // 监视 pwd 的变化
//                                transitionSpec = {
//                                    // 定义进入和退出动画
//                                    // 旧内容向左滑动并淡出，新内容从右侧滑入并淡入
//                                    if (targetState.length > initialState.length) {
//                                        (slideInHorizontally { fullWidth -> fullWidth } + fadeIn())
//                                            .togetherWith(slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut())
//                                            // SizeTransform 会动画 AnimatedContent 容器的尺寸变化
//                                            .using(SizeTransform(clip = false)) // clip = false 防止内容在动画过程中被裁剪
//                                    } else {
//                                        (slideInHorizontally { fullWidth -> -fullWidth } + fadeIn())
//                                            .togetherWith(slideOutHorizontally { fullWidth -> fullWidth } + fadeOut())
//                                            // SizeTransform 会动画 AnimatedContent 容器的尺寸变化
//                                            .using(SizeTransform(clip = false)) // clip = false 防止内容在动画过程中被裁剪
//                                    }
//
//                                },
//                                modifier = Modifier.animateContentSize(animationSpec = tween(150)), // 仍然保留 animateContentSize 来动画 Text 组件自身的尺寸变化
//                                label = "PathTextAnimation"
//                            ) { targetPwd -> // targetPwd 是当前动画的目标 pwd 值
//                                Text(
//                                    if (targetPwd == "/") "期末无挂" else if (targetPwd.length >= 8) ".." + targetPwd.substring(
//                                        targetPwd.length - 8
//                                    ) else targetPwd,
////                                modifier = Modifier.animateContentSize(),
//                                    maxLines = 1, // 确保文本在一行内，方便水平滑动动画
//                                    overflow = TextOverflow.Ellipsis
//                                )
//                            }
//
//
////                        Text(
////                            if (pwd.value == "/") "期末无挂" else if (pwd.value.length >= 8) ".." + pwd.value.substring(
////                                pwd.value.length - 8
////                            ) else pwd.value,
////                            modifier = Modifier.animateContentSize(),
////                        )
//                        },
//                        navigationIcon = {
//                            if (pwd.value != "/") {
//                                Icon(
//                                    modifier = Modifier.padding(16.dp, 0.dp, 4.dp, 0.dp),
//                                    painter = painterResource(R.drawable.folder_open_24px),
//                                    contentDescription = null
//                                )
//                            } else {
//                                Icon(
//                                    modifier = Modifier.padding(16.dp, 0.dp, 4.dp, 0.dp),
//                                    painter = painterResource(R.drawable.biglogo),
//                                    contentDescription = null
//                                )
//                            }
//
//                        },
//                        actions = {
//                            //别忘了路径过长隐藏一些按钮，
//                            //好吧忘了，不对懒了
//                            ThemeToggleButton(
//                                isAnimating = isAnimating,
//                                onThemeToggle = onThemeToggle,
//                                homeViewModel
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            IconButton(onClick = {
//                                navController.push(SettingScreen())
//                            }) {
//                                Icon(
//                                    imageVector = Icons.Default.Settings,
//                                    contentDescription = null,
//                                    tint = MaterialTheme.colorScheme.onSurface
//                                )
//                            }
//                            Spacer(modifier = Modifier.width(8.dp))
//                        }
//                    )
//                }
//
//                var searchJob: Job? = null
//
//                OutlinedTextField(
//                    value = searchText.value,
//                    onValueChange = { newText ->
//                        homeViewModel.setSearchText(newText)
//                        homeViewModel.setLoadingState(LoadingState.Loading)
//                        homeViewModel.setisSearch(false)
//
//                        searchJob?.cancel()
//                        searchJob = CoroutineScope(Dispatchers.Main).launch {
//                            if (newText.isNotEmpty()) {
//                                delay(300) // 延迟300ms
//                                homeViewModel.setisSearch(true)
//                                homeViewModel.setLoadingState(LoadingState.Loaded)
//
//                            }
//                        }
//
//                    },
//                    placeholder = { Text("搜索") },
//                    leadingIcon = {
//                        Row {
//                            Spacer(Modifier.width(8.dp))
//
//                            Icon(
//                                imageVector = Icons.Default.Search,
//                                contentDescription = "搜索图标"
//                            )
//                        }
//                    },
//                    trailingIcon = { // 右侧图标 (取消按钮)
//                        if (searchText.value.isNotEmpty()) { // 只有当有文字时才显示
//                            Row {
//                                Icon(
//                                    imageVector = Icons.Default.Close,
//                                    contentDescription = "清空输入",
//                                    modifier = Modifier.clickable { // 添加点击事件
//                                        homeViewModel.setSearchText("")
//                                        focusManager.clearFocus()
//                                    }
//                                )
//                                Spacer(Modifier.width(8.dp))
//
//                            }
//                        }
//                    },
//                    shape = RoundedCornerShape(percent = 50),
//                    singleLine = true,
//
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(26.dp, 8.dp)
//                        .focusRequester(focusRequester)
//                )
//
//
//            }
//        },
//
//        ) { innerPadding ->
//
//        Column {
//
//            MainContent(
//                modifier = Modifier
//                    .padding(innerPadding),
//                homeViewModel,
//                navController
//            )
//
//
//        }
//    }
//}

//
////主页
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainContent(
//    modifier: Modifier = Modifier,
//    homeViewModel: HomeViewModel,
//    navController: Navigator
//) {
//    val backgroundColor = MaterialTheme.colorScheme.background
//
//    val darkTheme by homeViewModel.darkTheme.collectAsStateWithLifecycle(0)
//
//    val backdrop = rememberLayerBackdrop {
//        drawRect(backgroundColor) // 绘制背景色，防止透明导致重影
//        drawContent()             // 绘制下方的内容（即 MainContent）
//    }
//
//    // 2. 这里的 state 放在 Scaffold 层级
//    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
//
//
//    val appPrefs = homeViewModel.appPre
//    val loadingState =
//        homeViewModel.loadingState.collectAsState() // 从 ViewModel 收集 loadingState
//    val pwd = homeViewModel.currentPath.collectAsState() // 从 ViewModel 收集 loadingState
//    val root = homeViewModel.root.collectAsState() // 从 ViewModel 收集 loadingState
//    val searchText = homeViewModel.searchText.collectAsState() // 从 ViewModel 收集 loadingState
//    val isSearch = homeViewModel.isSearch.collectAsState()
//
//    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.app_list_loading))
//    val progress by animateLottieCompositionAsState(
//        composition = composition,
//        isPlaying = loadingState.value == LoadingState.Loading,
//        iterations = LottieConstants.IterateForever,
//        speed = 1f,
//    )
//
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                // 1. 先画背景 (解决重影)
//                .drawBehind {
//                    drawRect(backgroundColor)
//                }
//                // 2. 再进行捕捉 (解决 Tab 看不到内容)
//                // 注意：layerBackdrop 必须加在这个包裹了所有内容的 Box 上
//                .layerBackdrop(backdrop)
//        ) {
//            AnimatedVisibility(
//                visible = loadingState.value == LoadingState.Loading,
//                enter = fadeIn(animationSpec = tween(durationMillis = 100)),
//                exit = fadeOut(animationSpec = tween(durationMillis = 50)),
//            ) {
//
//                LottieAnimation(
//                    composition = composition,
//                    progress = { progress },
//                    modifier = modifier.offset(y = (-128).dp)
//                )
//
//            }
//
//            AnimatedVisibility(
//                visible = loadingState.value == LoadingState.Loaded,
//                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
//                exit = fadeOut(animationSpec = tween(durationMillis = 50)),
//            ) {
//
//                lateinit var newdata: List<Any>
//
//
//                if (searchText.value.isNotEmpty() && isSearch.value) {
//                    newdata = FileManager.searchFiles(root.value, searchText.value)
//
//                } else if (pwd.value == "/") {
//                    val targetContent = FileManager.getDirContent(root.value, pwd.value)
//                    targetContent?.let {
//                        newdata = it.subDirs + it.files
//                    }
//                } else {
//                    val targetContent = FileManager.getDirContent(root.value, pwd.value)
//                    targetContent?.let {
//                        newdata = listOf(
//                            DirNode(
//                                name = "..",
//                                path = "",
//                            )
//                        ) + it.subDirs + it.files
//                    }
//                }
//
//
//                Box {
//
//                    LazyColumn {
//                        items(newdata) { item ->
//                            if (item is DirNode) {
//                                FolderCard(name = item.name, { name ->
//                                    homeViewModel.navigateTo(name)
//                                })
//                            } else if (item is FileItem) {
//                                FileCard(item, homeViewModel, navController)
//                            }
//
//                        }
//                        item {
//                            Spacer(Modifier.height(128.dp))
//                        }
//                    }
//                }
//
//
//
//
//                if (appPrefs.update != 0) {
//                    // 第一次打开，记录时间
//                    val firstOpenTime by remember { mutableLongStateOf(appPrefs.day) }
//
//                    if (firstOpenTime == -1L) {
//                        appPrefs.day = System.currentTimeMillis()
//                    } else {
//                        if ((System.currentTimeMillis() - firstOpenTime) / (1000 * 60 * 60 * 24) >= appPrefs.update) {
//                            BounceUpButton({
//                                homeViewModel.updateRepositoryData()
//                            })
//                        }
//                    }
//                }
//            }
//        }
//
//
//        key(darkTheme) {
//            val contentColor = if (darkTheme == 1) Color.Black else Color.White
//            val iconColorFilter = ColorFilter.tint(contentColor)
//
//            // 1. 定义唯一的 backdrop 控制器
//
//            LiquidBottomTabs(
//                myaccentColor = MaterialTheme.colorScheme.primary,
//                selectedTabIndex = { selectedTabIndex },
//                onTabSelected = { selectedTabIndex = it },
//                backdrop = backdrop,  // 传入上面定义的那个 backdrop
//                tabsCount = 3,
//                modifier = Modifier
//                    .padding(horizontal = 36.dp, vertical = 16.dp) // 调整 padding
//                    .navigationBarsPadding() // 适配底部手势条
//                    .align(Alignment.BottomCenter) // 放在底部
//            ) {
//                repeat(3) { index ->
//                    LiquidBottomTab({ selectedTabIndex = index }) {
//                        Box(
//                            Modifier
//                                .size(28.dp)
//                                .paint(
//                                    painterResource(R.drawable.notifications_24px),
//                                    colorFilter = iconColorFilter
//                                )
//                        )
//                        BasicText(
//                            "Tab ${index + 1}",
//                            style = TextStyle(contentColor, 12.sp)
//                        )
//                    }
//                }
//            }
//        }
//
//    }
//}
