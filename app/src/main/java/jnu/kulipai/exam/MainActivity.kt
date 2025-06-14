package jnu.kulipai.exam

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.setruth.themechange.components.MaskAnimActive
import com.setruth.themechange.components.MaskBox
import com.setruth.themechange.model.MaskAnimModel
import jnu.kulipai.exam.ui.theme.期末无挂Theme
import jnu.kulipai.exam.util.Api
import jnu.kulipai.exam.util.FileManager
import jnu.kulipai.exam.util.FileManager.DirNode
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs


val pwd = mutableStateOf("/")
var loadingState = mutableStateOf(LoadingState.Loading)
lateinit var appPrefs: AppPreferences

class MainActivity : ComponentActivity() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appPrefs = AppPreferences(applicationContext)

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
                MainApp(appPrefs, this)
            }
        }

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


@Composable
fun MainApp(appPrefs: AppPreferences, context: Context) {

    var isDarkTheme by remember { mutableStateOf(appPrefs.isNight) }
    var isAnimating by remember { mutableStateOf(false) }
    var pendingThemeChange by remember { mutableStateOf<Boolean?>(null) }
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(isDarkTheme) {
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = !isDarkTheme)
    }

    //mask的动画很强，贵州kug的人就是强
    MaskBox(
        animTime = 1500L,
        maskComplete = {
            pendingThemeChange?.let { newTheme ->
                isDarkTheme = newTheme
                appPrefs.isNight = isDarkTheme
                pendingThemeChange = null
            }
        },
        animFinish = {
            isAnimating = false
        }
    ) { maskAnimActiveEvent ->
        //有点无语，中文名，但是自动生成的懒得改
        期末无挂Theme(darkTheme = isDarkTheme) {
            MainScaffold(
                isDarkTheme = isDarkTheme,
                isAnimating = isAnimating,
                context = context,
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


//非常棒的Material 3 Experiment
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    isDarkTheme: Boolean,
    isAnimating: Boolean,
    onThemeToggle: MaskAnimActive,
    context: Context
) {


//    //pwd没错就是pwd
//    var pwd = remember { mutableStateOf("/") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            key(isDarkTheme) {
                TopAppBar(
                    title = {
                        Text(

                            if (pwd.value == "/") "期末无挂" else if (pwd.value.length >= 8) ".." + pwd.value.substring(
                                pwd.value.length - 8
                            ) else pwd.value,
                            modifier = Modifier.animateContentSize(),
                        )
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
                        //别忘了路径过程隐藏一些按钮，
                        //好吧忘了，不对懒了
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
        MainContent(modifier = Modifier.padding(innerPadding), pwd, context)
    }
}

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

enum class LoadingState { Loading, Loaded, Step }

lateinit var root: DirNode

@Composable
fun MainContent(modifier: Modifier = Modifier, pwd: MutableState<String>, context: Context) {

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.app_list_loading))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = loadingState.value == LoadingState.Loading,
        iterations = LottieConstants.IterateForever,
        speed = 1f,
    )

    LaunchedEffect(Unit) {
        if (FileManager.exists(context, "cache.json")) {
            val json = FileManager.read(context, "cache.json")
            root = FileManager.buildDirectoryTree(json.toString())
            loadingState.value = LoadingState.Loaded
        } else {

            try {
                if (appPre.Repo == "gitee") {
                    val json =
                        Api.performGetRequest("https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/directory_structure.json")
                    if (json != "err") {
                        FileManager.write(context, "cache.json", json)
                        root = FileManager.buildDirectoryTree(json)
                        loadingState.value = LoadingState.Loaded
                    } else {
                        Toast.makeText(context, "超时力~", Toast.LENGTH_SHORT).show()
                    }
                } else if (appPre.Repo == "github") {
                    val json =
                        Api.performGetRequest("https://gitee.com/gubaiovo/jnu-exam/raw/main/directory_structure.json")
                    if (json != "err") {
                        FileManager.write(context, "cache.json", json)
                        root = FileManager.buildDirectoryTree(json)
                        loadingState.value = LoadingState.Loaded
                    } else {
                        Toast.makeText(context, "超时力~", Toast.LENGTH_SHORT).show()
                    }

                }
            } catch (e: Exception) {

            }


        }

    }


    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        AnimatedVisibility(
            visible = loadingState.value == LoadingState.Loading,
            enter = fadeIn(animationSpec = tween(durationMillis = 100)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)),
        ) {

            LottieAnimation(composition = composition, progress = { progress })

        }

        AnimatedVisibility(
            visible = loadingState.value == LoadingState.Loaded,
            enter = fadeIn(animationSpec = tween(durationMillis = 400)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)),


            ) {


            lateinit var newdata: List<Any>



            if (pwd.value != "/") {
                val targetContent = FileManager.getDirContent(root, pwd.value)
                targetContent?.let {
                    newdata = listOf(
                        DirNode(
                            name = "..",
                            path = "",
                        )
                    ) + it.subDirs + it.files
                }
            } else {
                val targetContent = FileManager.getDirContent(root, pwd.value)
                targetContent?.let {
                    newdata = it.subDirs + it.files
                }
            }

            Box {
                LazyColumn(
                    modifier = Modifier.animateContentSize(),
                ) {
                    items(newdata) { item ->
                        if (item is DirNode) {
                            FolderCard(name = item.name, pwd, loadingState)
                        } else if (item is FileManager.FileItem) {
                            FileCard(item)
                        }

                    }
                }
                if (appPrefs.Day != LocalDate.now().dayOfMonth) {
                    BounceUpButton(context)
                }
            }

        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
@Composable
fun FolderCard(
    name: String = "[object Object]",
    pwd: MutableState<String>,
    loadingState: MutableState<LoadingState>
) {
    Card(
        onClick = {
            if (name == "..") {
                GlobalScope.launch {
                    loadingState.value = LoadingState.Step
                    delay(250)
                    pwd.value = Api.DotDot(pwd.value)
                    loadingState.value = LoadingState.Loaded
                }
            } else {

                GlobalScope.launch {
                    loadingState.value = LoadingState.Step
                    delay(250)
                    pwd.value += "$name/"
                    loadingState.value = LoadingState.Loaded
                }

            }

        },
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 6.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp, 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(R.drawable.folder_24px),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
            Text(
                name,
                modifier = Modifier.padding(12.dp, 0.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

enum class DownLoadState { DownLoading, Downloaded, Err, None }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileCard(item: FileManager.FileItem) {

    var downloadState by remember { mutableStateOf(DownLoadState.None) }
    var expanded by remember { mutableStateOf(false) }
    item.name.substringAfterLast(".")

    // 根据rotated状态，目标角度为0或180度，动画时长300毫秒
    val angle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    OutlinedCard(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(24.dp, 6.dp),
        onClick = { expanded = !expanded },

        shape = RoundedCornerShape(24.dp),

        ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.file_24px),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f) // 占满剩余空间
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            modifier = Modifier.rotate(angle),
                            painter = painterResource(R.drawable.keyboard_arrow_down_24px),
                            contentDescription = null
                        )
                    }
                }
            }


            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(24.dp, 0.dp))
                Column(
                    modifier = Modifier.padding(24.dp, 16.dp)
                ) {
                    Row {
                        Text("路径: ")
                        Text(item.path)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("大小: ${Api.formatFileSize(item.size)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("直链: ")
                        CopyableTextWithShape("Github", item.github_raw_url)
                        Spacer(modifier = Modifier.width(8.dp))
                        CopyableTextWithShape(
                            "Gitee", item.gitee_raw_url
                        )
//                        CopyableTextWithShape(
//                            if(appPre.Repo=="github")item.github_raw_url.toString()else item.gitee_raw_url,
//                            backgroundColor = MaterialTheme.colorScheme.primary
//                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 16.dp, 0.dp, 0.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ElevatedButton(
                            onClick = {

                            },
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 6.dp,
                                end = 16.dp,
                                bottom = 6.dp
                            )

                        ) {
                            Icon(
                                painter = painterResource(R.drawable.visibility_24px),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("预览")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(//暂时的下载
                            onClick = {
                                downloadState = DownLoadState.DownLoading
                            },
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 6.dp,
                                end = 16.dp,
                                bottom = 6.dp
                            )
                        ) {

                            if (downloadState == DownLoadState.None) {
                                Icon(
                                    painter = painterResource(R.drawable.download_24px),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("下载")
                            } else if (downloadState == DownLoadState.DownLoading) {
                                LoadingIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("下载中")
                            }


                        }
                    }
                }

            }


        }
    }
}


////stackoverflow 好用！！
interface TextShapeCorners {
    fun calculateRadius(density: Density, textStyle: TextStyle): Float

    data class Fixed(private val radius: Dp) : TextShapeCorners {
        override fun calculateRadius(density: Density, textStyle: TextStyle): Float =
            with(density) {
                radius.toPx()
            }
    }

    data class Flexible(
        private val fraction: Float = 0.45f
    ) : TextShapeCorners {
        override fun calculateRadius(density: Density, textStyle: TextStyle): Float =
            with(density) {
                textStyle.lineHeight.toPx() * fraction
            }
    }
}

interface TextShapePadding {
    fun calculatePadding(density: Density, textStyle: TextStyle): Float

    data class Fixed(private val padding: Dp) : TextShapePadding {
        override fun calculatePadding(density: Density, textStyle: TextStyle): Float =
            with(density) {
                padding.toPx()
            }
    }

    object Flexible : TextShapePadding {
        override fun calculatePadding(density: Density, textStyle: TextStyle): Float =
            with(density) {
                textStyle.lineHeight.toPx() - textStyle.fontSize.toPx()
            }
    }
}

fun TextLayoutResult.getLineRect(lineIndex: Int): Rect {
    return Rect(
        left = getLineLeft(lineIndex),
        top = getLineTop(lineIndex),
        right = getLineRight(lineIndex),
        bottom = getLineBottom(lineIndex)
    )
}

fun Rect.addHorizontalPadding(padding: Float): Rect {
    return Rect(left - padding, top, right + padding, bottom)
}

class TextShape(
    private val textLayoutResult: TextLayoutResult,
    private val padding: TextShapePadding = TextShapePadding.Flexible,
    private val corners: TextShapeCorners = TextShapeCorners.Flexible()
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val lineCount = textLayoutResult.lineCount
        val textStyle = textLayoutResult.layoutInput.style
        val lineHeight = with(density) { textStyle.lineHeight.toPx() }

        val lineRects = mutableMapOf<Int, Rect>()

        val paddingPx = padding.calculatePadding(density, textStyle)
        val curveRadiusPx =
            corners.calculateRadius(density, textStyle).coerceIn(0f, lineHeight / 2)

        val path = Path()

        // Step 1: Draw top line and top corners
        var previousLine: Rect = lineRects.getOrPut(0) {
            textLayoutResult.getLineRect(0).addHorizontalPadding(paddingPx)
        }
        path.moveTo(previousLine.left, previousLine.top + curveRadiusPx)
        path.quadraticBezierTo(
            x1 = previousLine.left, y1 = previousLine.top,
            x2 = previousLine.left + curveRadiusPx, y2 = previousLine.top
        )
        path.lineTo(previousLine.right - curveRadiusPx, previousLine.top)
        path.quadraticBezierTo(
            x1 = previousLine.right, y1 = previousLine.top,
            x2 = previousLine.right, y2 = previousLine.top + curveRadiusPx
        )
        path.lineTo(previousLine.right, previousLine.bottom - curveRadiusPx)

        // Step 2: Draw right sides of lines
        for (i in 1 until lineCount) {
            val currentLine = lineRects.getOrPut(i) {
                textLayoutResult.getLineRect(i).addHorizontalPadding(paddingPx)
            }
            if (abs(currentLine.right - previousLine.right) > curveRadiusPx) {
                val normalizedCurveRadius =
                    if (currentLine.right > previousLine.right) curveRadiusPx else -curveRadiusPx
                path.quadraticBezierTo(
                    x1 = previousLine.right, y1 = previousLine.bottom,
                    x2 = previousLine.right + normalizedCurveRadius, y2 = currentLine.top
                )
                path.lineTo(currentLine.right - normalizedCurveRadius, currentLine.top)
                path.quadraticBezierTo(
                    x1 = currentLine.right, y1 = currentLine.top,
                    x2 = currentLine.right, y2 = currentLine.top + curveRadiusPx
                )
            } else {
                path.cubicTo(
                    x1 = previousLine.right, y1 = previousLine.bottom,
                    x2 = currentLine.right, y2 = currentLine.top,
                    x3 = currentLine.right, y3 = currentLine.top + curveRadiusPx
                )
            }
            path.lineTo(currentLine.right, currentLine.bottom - curveRadiusPx)
            previousLine = currentLine
        }

        // Step 3: Draw bottom line and bottom corners
        path.quadraticBezierTo(
            x1 = previousLine.right, y1 = previousLine.bottom,
            x2 = previousLine.right - curveRadiusPx, y2 = previousLine.bottom
        )
        path.lineTo(previousLine.left + curveRadiusPx, previousLine.bottom)
        path.quadraticBezierTo(
            x1 = previousLine.left, y1 = previousLine.bottom,
            x2 = previousLine.left, y2 = previousLine.bottom - curveRadiusPx
        )
        path.lineTo(previousLine.left, previousLine.top + curveRadiusPx)

        // Step 4: Draw left sides of lines in reverse order
        for (i in lineCount - 2 downTo 0) {
            val currentLine = lineRects.getOrPut(i) {
                textLayoutResult.getLineRect(i).addHorizontalPadding(paddingPx)
            }
            if (abs(previousLine.left - currentLine.left) > curveRadiusPx) {
                val normalizedCurveRadius =
                    if (previousLine.left > currentLine.left) -curveRadiusPx else curveRadiusPx
                path.quadraticBezierTo(
                    x1 = previousLine.left, y1 = previousLine.top,
                    x2 = previousLine.left + normalizedCurveRadius, y2 = currentLine.bottom
                )
                path.lineTo(currentLine.left - normalizedCurveRadius, currentLine.bottom)
                path.quadraticBezierTo(
                    x1 = currentLine.left, y1 = currentLine.bottom,
                    x2 = currentLine.left, y2 = currentLine.bottom - curveRadiusPx
                )
            } else {
                path.cubicTo(
                    x1 = previousLine.left, y1 = previousLine.top,
                    x2 = currentLine.left, y2 = currentLine.bottom,
                    x3 = currentLine.left, y3 = currentLine.bottom - curveRadiusPx
                )
            }
            path.lineTo(currentLine.left, currentLine.top + curveRadiusPx)
            previousLine = currentLine
        }

        path.close()

        return Outline.Generic(path)
    }
}

// 自己封装
@Composable
fun CopyableTextWithShape(
    text: String,
    copy: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val textShape by remember {
        derivedStateOf {
            textLayoutResult?.let {
                TextShape(
                    textLayoutResult = it,
                    padding = TextShapePadding.Fixed(4.dp), // <-- 指定较小 padding
                    corners = TextShapeCorners.Flexible(0.45f)
                )
            }
        }
    }


    var isPressed by remember { mutableStateOf(false) }
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.4f else 0f,
        label = "BackgroundAlpha"
    )

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Text(
        text = text,
        modifier = modifier
            .pointerInput(text) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val released = try {
                            awaitRelease()
                            true
                        } catch (e: Exception) {
                            false
                        }
                        // 延迟让按压效果显得更自然
                        if (released) {
                            delay(350)
                        }
                        isPressed = false
                    },
                    onTap = {
                        clipboardManager.setText(AnnotatedString(copy))
                        Toast.makeText(context, "已复制文本", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .then(
                textShape?.let {
                    Modifier
                        .background(backgroundColor.copy(alpha = backgroundAlpha), it)
//                        .border(0.72.dp, backgroundColor.copy(alpha = borderAlpha), it)
                } ?: Modifier
            )
            .padding(horizontal = 0.dp, vertical = 0.dp),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        onTextLayout = { textLayoutResult = it }
    )
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun BounceUpButton(context: Context) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    val offsetY = remember { Animatable(screenHeightPx.toFloat()) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = {
//                loadingState.value = LoadingState.Loading
                scope.launch {
                    offsetY.animateTo(
                        targetValue = screenHeightPx.toFloat(),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                appPrefs.Day = LocalDate.now().dayOfMonth

//                GlobalScope.launch {
//                    //http
//                    try {
//                        if (appPre.Repo == "gitee") {
//                            val json =
//                                Api.performGetRequest("https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/directory_structure.json")
//                            if (json != "err") {
//                                FileManager.write(context, "cache.json", json)
//                                root = FileManager.buildDirectoryTree(json)
//                                loadingState.value = LoadingState.Loaded
//                            } else {
//                                Toast.makeText(context, "超时力~", Toast.LENGTH_SHORT).show()
//                            }
//                        } else if (appPre.Repo == "github") {
//                            val json =
//                                Api.performGetRequest("https://gitee.com/gubaiovo/jnu-exam/raw/main/directory_structure.json")
//                            if (json != "err") {
//                                FileManager.write(context, "cache.json", json)
//                                root = FileManager.buildDirectoryTree(json)
//                                loadingState.value = LoadingState.Loaded
//                            } else {
//                                Toast.makeText(context, "超时力~", Toast.LENGTH_SHORT).show()
//                            }
//
//                        }
//                    } catch (e: Exception) {
//                    }
//                }

            },// 点击更新
            modifier = Modifier.offset { IntOffset(0, offsetY.value.toInt()) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.notifications_24px),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update")
            }
        }
    }

    LaunchedEffect(Unit) {
        val targetOffsetPx = screenHeightPx - with(density) { 924.dp.roundToPx() }
        offsetY.animateTo(
            targetValue = targetOffsetPx.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessVeryLow // 低刚性，动画更慢
            )
        )
    }

}