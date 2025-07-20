package jnu.kulipai.exam.ui.screens

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.setruth.themechange.components.MaskAnimActive
import com.setruth.themechange.components.MaskBox
import jnu.kulipai.exam.AppPreferences
import jnu.kulipai.exam.R
import jnu.kulipai.exam.ThemeToggleButton
import jnu.kulipai.exam.components.FileCard
import jnu.kulipai.exam.components.FolderCard
import jnu.kulipai.exam.data.model.DirNode
import jnu.kulipai.exam.data.model.FileItem
import jnu.kulipai.exam.data.model.LoadingState
import jnu.kulipai.exam.data.model.ThemeState
import jnu.kulipai.exam.ui.theme.期末无挂Theme
import jnu.kulipai.exam.util.FileManager
import jnu.kulipai.exam.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//这里有笼罩动画，状态栏
@Composable
fun MainApp(appPrefs: AppPreferences, homeViewModel: HomeViewModel, navController: NavController) {

    // 初始化一次
    LaunchedEffect(Unit) {
        ThemeState.isDark = appPrefs.isNight
    }

    val isDarkTheme = ThemeState.isDark
    var isAnimating by remember { mutableStateOf(false) }
    var pendingThemeChange by remember { mutableStateOf<Boolean?>(null) }
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(isDarkTheme) {
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = !isDarkTheme)
    }

    MaskBox(
        animTime = 1500L,
        maskComplete = {
            pendingThemeChange?.let { newTheme ->
                ThemeState.isDark = newTheme
                appPrefs.isNight = newTheme
                pendingThemeChange = null
            }
        },
        animFinish = {
            isAnimating = false
        }
    ) { maskAnimActiveEvent ->
            MainScaffold(
                isDarkTheme = isDarkTheme,
                isAnimating = isAnimating,
                homeViewModel = homeViewModel,
                onThemeToggle = { animModel, x, y ->
                    if (!isAnimating) {
                        isAnimating = true
                        pendingThemeChange = !ThemeState.isDark
                        maskAnimActiveEvent(animModel, x, y)
                    }
                },
                navController = navController,
            )
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
    navController: NavController
) {

    val pwd = homeViewModel.currentPath.collectAsState()
    val searchText = homeViewModel.searchText.collectAsState()
    val isSearch = homeViewModel.isSearch.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Scaffold(
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
                                onThemeToggle = onThemeToggle
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = {
                                navController.navigate("set")
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
        }

    ) { innerPadding ->

        Column {
            MainContent(modifier = Modifier.padding(innerPadding), homeViewModel)

        }
    }
}



//主页

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(modifier: Modifier = Modifier, homeViewModel: HomeViewModel) {

    val loadingState = homeViewModel.loadingState.collectAsState() // 从 ViewModel 收集 loadingState
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
            .fillMaxSize(),
    ) {
        AnimatedVisibility(
            visible = loadingState.value == LoadingState.Loading,
            enter = fadeIn(animationSpec = tween(durationMillis = 100)),
            exit = fadeOut(animationSpec = tween(durationMillis = 50)),
        ) {

            LottieAnimation(composition = composition, progress = { progress }, modifier = modifier.offset(y = (-128).dp))

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
                            FileCard(item, homeViewModel)
                        }

                    }
                }
            }
            //更新数据按钮
//                if (appPrefs.day != LocalDate.now().dayOfMonth) {
//                    BounceUpButton({})
//                }
        }
    }
}
