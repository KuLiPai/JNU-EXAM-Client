package jnu.kulipai.exam.ui.screens.pdf

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.MutableState
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
import jnu.kulipai.exam.data.model.MaskAnimActive
import dev.zt64.compose.pdf.PdfState
import dev.zt64.compose.pdf.component.PdfPage
import dev.zt64.compose.pdf.rememberLocalPdfState
import jnu.kulipai.exam.ui.components.ThemeToggleButton
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import jnu.kulipai.exam.ui.anim.ScreenshotThemeTransition
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
        val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(0)
        var isAnimating by remember { mutableStateOf(false) }
        val isDark = remember { mutableStateOf(false) }
        isDark.value = when (darkTheme) {
            0 -> isSystemInDarkTheme()
            1 -> false
            2 -> true
            else -> false
        }


        ScreenshotThemeTransition(
            isDarkTheme = isDark.value,
            modifier = Modifier
        ) { startAnim ->
            PdfScaffold(
                isDarkTheme = isDark,
                isAnimating = isAnimating,
                homeViewModel = viewModel,
                onThemeToggle = { animModel, x, y ->

                    val isExpand = isDark.value
                    startAnim(Offset(x, y), isExpand) {
                        // 这个代码块会在截图完成后执行
                        isDark.value=!isDark.value
                        viewModel.updateDarkTheme(if (isDark.value) 2 else 1)
                    }



                },
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
//标题栏
@Composable
fun PdfScaffold(
    isDarkTheme: MutableState<Boolean>,
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

                key(isDarkTheme.value) {

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
