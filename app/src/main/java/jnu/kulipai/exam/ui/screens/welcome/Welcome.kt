package jnu.kulipai.exam.ui.screens.welcome

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.compose.waveloading.DrawType
import com.github.compose.waveloading.WaveLoading
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import jnu.kulipai.exam.AppPreferences
import jnu.kulipai.exam.R
import jnu.kulipai.exam.ui.anim.SlideAnimationScreen
import jnu.kulipai.exam.ui.screens.home.MainScreen
import jnu.kulipai.exam.util.Api
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


//究极烂的赋值
//light
var one_background = Color(0xfff9f9ff)
var two_background = Color(0xffeee2bc)
var three_background = Color(0xffffede8)

var one_containerColor = Color(0xffd6e3ff)
var one_contentColor = Color(0xff284777)

var two_contentColor = Color(0xff534600)
var two_containerColor = Color(0xfff8e287)
var two_mainText = Color(0xff4e472a)
var two_secondText = Color(0xff817C7C)

var three_text = Color(0xff5d4037)
var three_smallText = Color(0xff8f4c38)
var three_contentColor = Color(0xff723523)
var three_containerColor = Color(0xffffdbd1)

var chipcolor = Color(0xffffdbd1)
var chipborderColor = Color(0xffd8c2bc)


lateinit var appPre: AppPreferences

// 路由WelcomeScreen页面
class WelcomeScreen : Screen {
    @Composable
    override fun Content() {
        appPre = koinInject()
        val viewModel: WelcomeViewModel = koinViewModel()
        val isSystemDark = isSystemInDarkTheme()
        var isDarkTheme by remember { mutableStateOf(isSystemDark) }
        val systemUiController = rememberSystemUiController()

        LaunchedEffect(isDarkTheme) {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = !isDarkTheme
            )
        }

        LaunchedEffect(Unit) {
            viewModel.getSourceJson()
        }
        if (isDarkTheme) {
            //night
            one_background = Color(0xff111318)
            two_background = Color(0xff4e472a)
            three_background = Color(0xff392e2b)

            one_containerColor = Color(0xff284777)
            one_contentColor = Color(0xffd6e3ff)

            two_contentColor = Color(0xfff8e287)
            two_containerColor = Color(0xff6d5e0f)
            two_mainText = Color(0xffeee2bc)
            two_secondText = Color(0xffcdc6b4)

            three_text = Color(0xffffdbd1)
            three_smallText = Color(0xffffffff)
            three_contentColor = Color(0xffffdbd1)
            three_containerColor = Color(0xff723523)

            chipcolor = Color(0xff5d4037)
            chipborderColor = Color(0xff53433f)

        }
        OneScreen(viewModel).Content()

    }
}

class OneScreen(
    private val viewModel: WelcomeViewModel
) : Screen, SlideAnimationScreen {

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(one_background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Text(
                    "Welcome To\n期末无挂",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 64.sp,//默认的有点小
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
                    containerColor = one_containerColor,
                    contentColor = one_contentColor,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(0.dp, 0.dp, 0.dp, 64.dp),
                    onClick = { navigator.push(TwoScreen(viewModel)) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //全新加载变形等待
                        LoadingIndicator(
                            color = one_contentColor,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
class TwoScreen(
    private val viewModel: WelcomeViewModel
) : Screen, SlideAnimationScreen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(two_background)
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
                style = MaterialTheme.typography.displayMedium.copy(color = two_mainText),
            )

            Text(
                "过过过", // 难蚌，就这样吧
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(0.dp, 164.dp)
                    .padding(32.dp),
                style = MaterialTheme.typography.displaySmall.copy(color = two_secondText),
            )
            FloatingActionButton(
                contentColor = two_contentColor,
                containerColor = two_containerColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(0.dp, 0.dp, 0.dp, 64.dp),
                onClick = { navigator.push(ThreeScreen(viewModel)) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoadingIndicator(color = two_contentColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Next")
                }
            }
        }
    }
}

// 为 ThreeScreen 添加 onFinish 回调
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class ThreeScreen(
    private val viewModel: WelcomeViewModel
) : Screen, SlideAnimationScreen {
    @Composable
    override fun Content() {

        val context = LocalContext.current

        val repository by viewModel.data.collectAsState() // 监听数据变化

        val navigator = LocalNavigator.currentOrThrow

        var sources: List<SourceItem?>? = null
        var selectedItem by remember { mutableStateOf<SourceItem?>(null) }

        var sourceText by rememberSaveable { mutableStateOf("") }



        Box(

            modifier = Modifier
                .fillMaxSize()
                .background(three_background),

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
                style = MaterialTheme.typography.displaySmall.copy(
                    color = three_text
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .offset(0.dp, 166.dp),
            ) {
                if (repository.isNullOrEmpty()) {

//                    LoadingIndicator(
//                        modifier = Modifier
//                            .size(188.dp)
//                            .align(Alignment.Center),
//                        color = three_containerColor
//                    )

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "无法获取源信息，请手动输入",

                            )
//

                        OutlinedTextField(
                            value = sourceText,
                            onValueChange = { sourceText = it },
                            label = { Text("源地址") },
                            placeholder = { Text("请输入源地址") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }

                } else {
                    sources = Api.parseSources(repository!!)
                    FlowRow(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        sources.forEach { item ->
                            FilterChip(
                                modifier = Modifier.padding(
                                    end = 8.dp,
                                ),
                                onClick = {
                                    selectedItem = if (selectedItem == item) null else item
                                },
                                label = {
                                    Text(item.name, color = three_text)
                                },
                                selected = selectedItem == item,
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedItem == item,
                                    borderColor = chipborderColor, // 未选中时边框颜色
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipcolor, // 选中时背景色
                                ),
                                leadingIcon = if (selectedItem == item) {
                                    {
                                        Icon(
                                            tint = three_contentColor,
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Done icon",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    {}
                                },
                            )
                        }

                    }
                }


            }


            FloatingActionButton(
                contentColor = three_contentColor,
                containerColor = three_containerColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(0.dp, 0.dp, 0.dp, 64.dp),
                onClick = {
                    if (selectedItem != null) {
                        sources?.let {

                            appPre.repo = selectedItem?.name.toString()
                            appPre.repoUrl = selectedItem?.jsonUrl.toString()
                            appPre.repoKey = selectedItem?.fileKey.toString()
                            appPre.isFirstLaunch = false
                            navigator.replaceAll(MainScreen())
                        }
                    } else {
                        if (sourceText.isEmpty()) {
                            Toast.makeText(context, "请选择一个源", Toast.LENGTH_SHORT).show()

                        } else {
                            viewModel.getSourceJson(sourceText)
//                            appPre.repo = selectedItem?.name.toString()
//                            appPre.repoUrl = selectedItem?.jsonUrl.toString()
//                            appPre.repoKey = selectedItem?.fileKey.toString()
//                            appPre.isFirstLaunch = false
//                            navigator.replaceAll(MainScreen())
                        }
                    }

                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoadingIndicator(
                        color = three_contentColor,
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (repository.isNullOrEmpty()) {
                        Text("加载源")
                    } else {
                        Text("Welcome")
                    }
                }
            }
        }
    }
}


//@Composable
//fun PingText(
//    host: String,
//    port: Int = 80,
//    timeoutMillis: Int = 3000
//) {
//    var resultText by remember { mutableStateOf("检测中...") }
//    val scope = rememberCoroutineScope()
//
//    LaunchedEffect(host, port) {
//        resultText = "检测中..."
//        scope.launch(Dispatchers.IO) {
//            val start = System.currentTimeMillis()
//            val reachable = try {
//                Socket().use { socket ->
//                    socket.connect(InetSocketAddress(host, port), timeoutMillis)
//                    true
//                }
//            } catch (_: Exception) {
//                false
//            }
//            val duration = System.currentTimeMillis() - start
//            withContext(Dispatchers.Main) {
//                resultText = if (reachable) "${duration}ms" else "超时"
//            }
//        }
//    }
//
//
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp),
//    ) {
//        Box(
//            modifier = Modifier
//                .size(9.dp)
//                .background(
//                    color = if (resultText == "超时") Color.Red else Color.Green,
//                    shape = CircleShape
//                )
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = resultText,
//            style = MaterialTheme.typography.bodySmall,
//            color = three_smallText
//        )
//    }
//
//}
//
