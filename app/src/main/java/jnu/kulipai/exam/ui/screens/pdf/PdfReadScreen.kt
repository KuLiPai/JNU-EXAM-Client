package jnu.kulipai.exam.ui.screens.pdf

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import jnu.kulipai.exam.data.model.MaskAnimActive
import dev.zt64.compose.pdf.PdfState
import dev.zt64.compose.pdf.component.PdfPage
import dev.zt64.compose.pdf.rememberLocalPdfState
import jnu.kulipai.exam.components.ThemeToggleButton
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import jnu.kulipai.exam.ui.util.ScreenshotThemeTransition
import jnu.kulipai.exam.util.Cache
import org.koin.androidx.compose.koinViewModel
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
class PdfScreen : Screen {
    @Composable
    override fun Content() {


        val viewModel: HomeViewModel = koinViewModel()
        LocalNavigator.currentOrThrow
        val appPrefs = viewModel.appPre
        val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(0)
        var isAnimating by remember { mutableStateOf(false) }




        ScreenshotThemeTransition(
            isDarkTheme = (darkTheme == 2),
            modifier = Modifier
        ) { startAnim ->
            PdfScaffold(
                isDarkTheme = when (darkTheme) {
                    1 -> false
                    2 -> true
                    else -> false
                },
                isAnimating = isAnimating,
                homeViewModel = viewModel,
                onThemeToggle = { animModel, x, y ->

                    val isExpand = (darkTheme == 2)
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
            )
        }


//        MaskBox(
//            animTime = 1500L,
//            maskComplete = {
//                pendingThemeChange?.let { newTheme ->
//
//                    viewModel.updateDarkTheme(if (newTheme) 2 else 1)
//                    appPrefs.isNight = newTheme
//                    pendingThemeChange = null
//                }
//            },
//            animFinish = {
//                isAnimating = false
//            }
//        ) { maskAnimActiveEvent ->
//            PdfScaffold(
//                isDarkTheme = when (darkTheme) {
//                    1 -> false
//                    2 -> true
//                    else -> false
//                },
//                isAnimating = isAnimating,
//                homeViewModel = viewModel,
//                onThemeToggle = { animModel, x, y ->
//                    if (!isAnimating) {
//                        isAnimating = true
//                        pendingThemeChange = !when (darkTheme) {
//                            1 -> false
//                            2 -> true
//                            else -> false
//                        }
//                        maskAnimActiveEvent(animModel, x, y)
//                    }
//                },
//            )
//        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
//标题栏
@Composable
fun PdfScaffold(
    isDarkTheme: Boolean,
    isAnimating: Boolean,
    homeViewModel: HomeViewModel, // 接收 ViewModel
    onThemeToggle: MaskAnimActive
) {


//
//    var shouldBlockBack by remember { mutableStateOf(false) }
//
//    LaunchedEffect(pwd.value) {
//        if (pwd.value == "/") {
//            shouldBlockBack = false
//        }else{
//            shouldBlockBack = true
//        }
//
//
//    }
//
//    // 拦截系统返回
//    BackHandler(enabled = shouldBlockBack) {
//        // 你可以弹窗确认、执行某些操作等
//        homeViewModel.handleBackPress()
//    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                key(isDarkTheme) {

                    TopAppBar(
                        title = {
                            Text(Cache.currentName.substring(0, 7) + "...")
                        },
                        navigationIcon = {
                        },
                        actions = {
                            //别忘了路径过长隐藏一些按钮，
                            //好吧忘了，不对懒了
                            ThemeToggleButton(
                                isAnimating = isAnimating,
                                onThemeToggle = onThemeToggle,
                                homeViewModel
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    )
                }

            }
        }

    ) { innerPadding ->
        val currentFile = Cache.currentFile




        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            CustomPdfViewer(currentFile)
        }
    }
}


@Composable
fun CustomPdfViewer(
    file: File
) {

    // 创建 PDF 状态
    val pdfState = rememberLocalPdfState(file)


    // 当前显示页码
    var currentPage by remember { mutableStateOf(0) }

    // 最大页码
    val maxPage = pdfState.pageCount - 1

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // PDF 显示区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            ZoomablePdfPage(
                pdfState = pdfState,
                pageNumber = currentPage,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 页码滑动条
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Page: ${currentPage + 1} / ${maxPage + 1}")

            Slider(
                value = currentPage.toFloat(),
                onValueChange = { newValue ->
                    currentPage = newValue.roundToInt()
                },
                valueRange = 0f..maxPage.toFloat(),
                steps = max(maxPage - 1, 0) // 让滑块只停在整数
            )
        }
    }
}

@Composable
fun ZoomablePdfPage(
    pdfState: PdfState,
    pageNumber: Int,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += panChange
    }

    Box(
        modifier = modifier
            .transformable(transformState)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
    ) {
        PdfPage(
            state = pdfState,
            index = pageNumber
        )
    }
}
