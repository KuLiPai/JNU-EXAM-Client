package jnu.kulipai.exam.ui.screens.pdf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.pratikk.jetpdfvue.state.VueFileType
import com.pratikk.jetpdfvue.state.VueLoadState
import com.pratikk.jetpdfvue.state.VueResourceType
import com.pratikk.jetpdfvue.state.rememberHorizontalVueReaderState
import com.setruth.themechange.components.MaskAnimActive
import com.setruth.themechange.components.MaskBox
import jnu.kulipai.exam.components.ThemeToggleButton
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import jnu.kulipai.exam.util.Cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.androidx.compose.koinViewModel
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
class PdfScreen : Screen {
    @Composable
    override fun Content() {

        val viewModel: HomeViewModel = koinViewModel()
        LocalNavigator.currentOrThrow
        val appPrefs = viewModel.appPre
        val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(0)
        var isAnimating by remember { mutableStateOf(false) }
        var pendingThemeChange by remember { mutableStateOf<Boolean?>(null) }



        MaskBox(
            animTime = 1500L,
            maskComplete = {
                pendingThemeChange?.let { newTheme ->

                    viewModel.updateDarkTheme(if (newTheme) 2 else 1)
                    appPrefs.isNight = newTheme
                    pendingThemeChange = null
                }
            },
            animFinish = {
                isAnimating = false
            }
        ) { maskAnimActiveEvent ->
            PdfScaffold(
                isDarkTheme = when (darkTheme) {
                    1 -> false
                    2 -> true
                    else -> false
                },
                isAnimating = isAnimating,
                homeViewModel = viewModel,
                onThemeToggle = { animModel, x, y ->
                    if (!isAnimating) {
                        isAnimating = true
                        pendingThemeChange = !when (darkTheme) {
                            1 -> false
                            2 -> true
                            else -> false
                        }
                        maskAnimActiveEvent(animModel, x, y)
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

        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            CustomPdfViewer(currentFile)
        }
    }
}

@Composable
fun CustomPdfViewer(
    file: File
) {
    val context = LocalContext.current
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    val windowInfo = LocalWindowInfo.current
    val containerSizePx = windowInfo.containerSize

    val pdfState = rememberHorizontalVueReaderState(
        resource = VueResourceType.Local(
            uri = file.toUri(),
            fileType = VueFileType.PDF
        ),
        cache = 3
    )

    LaunchedEffect(Unit) {
        pdfState.load(
            context = context,
            coroutineScope = coroutineScope,
            containerSize = containerSizePx, // <- 正确的 containerSize
            isPortrait = containerSizePx.height > containerSizePx.width,
            customResource = null
        )
    }

    when (val state = pdfState.vueLoadState) {
        is VueLoadState.DocumentError -> {
            Text("加载 PDF 失败: ${state.getErrorMessage}")
        }

        VueLoadState.DocumentLoaded -> {
            HorizontalSampleB(
                horizontalVueReaderState = pdfState
            )
        }

        VueLoadState.DocumentLoading -> {
            Text("正在加载 PDF...")
        }

        VueLoadState.DocumentImporting -> {
            Text("正在导入 PDF...")
        }

        VueLoadState.NoDocument -> {
            Text("无 PDF 文件")
        }
    }
}
