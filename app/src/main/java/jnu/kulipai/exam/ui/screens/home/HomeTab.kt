package jnu.kulipai.exam.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import jnu.kulipai.exam.R
import jnu.kulipai.exam.components.BounceUpButton
import jnu.kulipai.exam.components.FileCard
import jnu.kulipai.exam.components.FolderCard
import jnu.kulipai.exam.data.model.DirNode
import jnu.kulipai.exam.data.model.FileItem
import jnu.kulipai.exam.data.model.LoadingState
import jnu.kulipai.exam.util.FileManager
import org.koin.androidx.compose.koinViewModel

// HomeTab.kt
object HomeTab : Tab {

    // 这里的 options 是 Tab 接口强制要求的
    override val options: TabOptions
        @Composable get() {
            val title = "主页"
            val icon = rememberVectorPainter(Icons.Default.Cloud)

            // 因为 options 会被频繁调用，建议用 remember 包裹
            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // 这里获取 ViewModel，Koin 会处理单例，不用担心重复创建
        val viewModel: HomeViewModel = koinViewModel()
        val navigator = LocalNavigator.currentOrThrow

        // 把你原来 MainContent 里的逻辑移到这里
        HomeTabContent(viewModel, navigator)
    }
}

// 抽离出来的纯内容组件，保持逻辑清晰
@Composable
fun HomeTabContent(
    homeViewModel: HomeViewModel,
    navigator: cafe.adriel.voyager.navigator.Navigator
) {
    // 2. 这里的 state 放在 Scaffold 层级
    val scaffoldPadding = LocalScaffoldPadding.current

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
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
            // 1. 先画背景 (解决重影)
//                .drawBehind {
//                    drawRect(backgroundColor)
//                }
//                // 2. 再进行捕捉 (解决 Tab 看不到内容)
//                // 注意：layerBackdrop 必须加在这个包裹了所有内容的 Box 上
//                .layerBackdrop(backdrop)
        ) {
            AnimatedVisibility(
                visible = loadingState.value == LoadingState.Loading,
                enter = fadeIn(animationSpec = tween(durationMillis = 100)),
                exit = fadeOut(animationSpec = tween(durationMillis = 50)),
            ) {

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.offset(y = (-128).dp)
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

                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = scaffoldPadding.calculateTopPadding(),
                            bottom = scaffoldPadding.calculateBottomPadding() + 80.dp // 如果需要额外空隙防止太贴边，可以在这里 + dp
                        )
                    ) {
                        items(newdata) { item ->
                            if (item is DirNode) {
                                FolderCard(name = item.name, { name ->
                                    homeViewModel.navigateTo(name)
                                })
                            } else if (item is FileItem) {
                                FileCard(item, homeViewModel, navigator)
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

    }
}