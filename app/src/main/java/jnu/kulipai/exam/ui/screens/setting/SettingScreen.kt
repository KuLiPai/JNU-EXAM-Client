package jnu.kulipai.exam.ui.screens.setting

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kaajjo.libresudoku.ui.more.about.AboutScreen
import jnu.kulipai.exam.R
import jnu.kulipai.exam.ui.components.PreferenceRow
import jnu.kulipai.exam.ui.components.ScrollbarLazyColumn
import jnu.kulipai.exam.ui.components.collapsing_topappbar.CollapsingTitle
import jnu.kulipai.exam.ui.components.collapsing_topappbar.CollapsingTopAppBar
import jnu.kulipai.exam.ui.components.collapsing_topappbar.rememberTopAppBarScrollBehavior
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import jnu.kulipai.exam.ui.screens.setting.appearance.SettingsAppearanceScreen
import jnu.kulipai.exam.ui.screens.setting.components.AppThemePreviewItem
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.net.URL


// 4. 将 UI 逻辑抽离出来，方便管理
@Composable
fun SettingsTabContent() {
    val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
    val viewModel: HomeViewModel = koinViewModel()
    val context = LocalContext.current
    val appPre = viewModel.appPre

    // 注意：在 UI 线程读文件可能会卡顿，建议以后放入 IO 线程或 ViewModel
    // 这里为了不破坏你原有逻辑，暂时保留
    val repoListStr = try {
        File(context.filesDir, "source.json").readText()
    } catch (e: Exception) {
        "[]"
    }
    val repoList = Api.parseSources(repoListStr)

    var repoModeDialog by rememberSaveable { mutableStateOf(false) }
    var updateModeDialog by rememberSaveable { mutableStateOf(false) }
    var sourceModeDialog by rememberSaveable { mutableStateOf(false) }
    var currentRepo by rememberSaveable { mutableStateOf(viewModel.appPre.repo) }
    var currentSource by rememberSaveable { mutableStateOf(viewModel.appPre.sourceUrl) }
    var currentUpdate by rememberSaveable { mutableStateOf(viewModel.appPre.update) }
    var cooldown by rememberSaveable { mutableStateOf(viewModel.appPre.cooldown) }

    var progress by remember { mutableStateOf((System.currentTimeMillis() - cooldown) / 100000f) }

    if (progress > 1f) progress = 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900),
        label = "progressAnimation"
    )

    LaunchedEffect(Unit) {
        while (true) {
            if (progress < 1f) {
                progress += 0.01f
            }
            delay(1000)
        }
    }

    rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    SettingsScaffoldLazyColumn(
        titleText = stringResource(R.string.settings_title),
        navigator = navigator,
        snackbarHostState = snackbarHostState,
        // 5. 设置为 false，不显示返回箭头
//        showBackArrow = false
    ) { paddingValues ->
        ScrollbarLazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            item {
                PreferenceRow(
                    title = stringResource(R.string.pref_appearance),
                    subtitle = stringResource(R.string.perf_appearance_summary),
                    onClick = { navigator.push(SettingsAppearanceScreen()) },
                    painter = rememberVectorPainter(Icons.Outlined.Palette)
                )
            }

            item {
                PreferenceRow(
                    title = "更换源",
                    subtitle = "当前${
                        try {
                            URL(currentSource).host
                        } catch (e: Exception) {
                            currentSource
                        }
                    }",
                    onClick = { sourceModeDialog = true },
                    painter = painterResource(
                        R.drawable.database_24px
                    )
                )
            }

            item {
                PreferenceRow(
                    title = "更换仓库",
                    subtitle = "当前$currentRepo",
                    onClick = { repoModeDialog = true },
                    painter = painterResource(
                        if (viewModel.appPre.repo.contains("github", ignoreCase = true)) {
                            R.drawable.github_142_svgrepo_com // 请确保你有这个资源
                        } else if (viewModel.appPre.repo.contains(
                                "Cloudflare",
                                ignoreCase = true
                            )
                        ) {
                            R.drawable.cloudflare // 请确保你有这个资源
                        } else {
                            R.drawable.cloud_24px // 请确保你有这个资源
                        }
                    )
                )
            }


            item {
                PreferenceRow(
                    title = "立即更新",
                    subtitle = if (100 - (progress * 100).toInt() <= 0) "冷却完成" else "冷却剩余${100 - (progress * 100).toInt()}秒",
                    diy = {
                        LinearProgressIndicator(progress = { animatedProgress })
                    },
                    onClick = {
                        if (progress >= 1f) {
                            progress = 0f
                            viewModel.appPre.cooldown = System.currentTimeMillis()
                            viewModel.updateRepositoryData({
                                viewModel.appPre.cooldown -= 100000
                                progress = 1f
                            })
                        } else {
                            Toast.makeText(context, "冷却中", Toast.LENGTH_SHORT).show()
                        }
                    },
                    painter = rememberVectorPainter(Icons.Default.Download)
                )
            }

            item {
                PreferenceRow(
                    title = "自动更新",
                    subtitle = if (currentUpdate == 0) "从不" else "每${currentUpdate}天更新",
                    onClick = { updateModeDialog = true },
                    painter = rememberVectorPainter(
                        if (currentUpdate == 0) Icons.Default.UpdateDisabled
                        else if (currentUpdate == 36500) Icons.Default.QuestionMark
                        else Icons.Default.Update
                    )
                )
            }

            item {
                PreferenceRow(
                    title = "关于",
                    onClick = { navigator.push(AboutScreen()) },
                    painter = rememberVectorPainter(Icons.Outlined.Info)
                )
            }
        }
    }

    if (repoModeDialog) {
        // ... (保持你原来的 Dialog 逻辑不变) ...
        // 为节省篇幅，这里简写，请将原来的 SelectionDialog 代码复制回来
        SelectionDialog(
            title = "选择仓库",
            selections = repoList.map { it.name },
            selected = repoList.map { it.name }.indexOf(viewModel.appPre.repo).coerceAtLeast(0),
            onSelect = { index ->
                if (index in repoList.indices) {
                    currentRepo = repoList[index].name
                    appPre.repoKey = repoList[index].fileKey
                    appPre.repoUrl = repoList[index].jsonUrl
                    viewModel.appPre.repo = currentRepo
                }
            },
            onDismiss = { repoModeDialog = false }
        )
    } else if (updateModeDialog) {
        // ... (保持你原来的 Dialog 逻辑不变) ...
        SelectionDialog(
            title = "设置更新间隔(天)",
            selections = listOf(
                "从不",
                "1天",
                "3天",
                "一周",
                "一个月",
                "一个季",
                "一年",
                "一个世纪"
            ),
            selected = when (currentUpdate) {
                0 -> 0; 1 -> 1; 3 -> 2; 7 -> 3; 30 -> 4; 90 -> 5; 365 -> 6; 36500 -> 7; else -> 0
            },
            onSelect = { index ->
                currentUpdate = when (index) {
                    0 -> 0; 1 -> 1; 2 -> 3; 3 -> 7; 4 -> 30; 5 -> 90; 6 -> 365; 7 -> 36500; else -> 0
                }
                viewModel.appPre.update = currentUpdate
            },
            onDismiss = { updateModeDialog = false }
        )
    } else if (sourceModeDialog) {
        InputDialog(
            title = "输入源URL",
            hint = "源列表配置URL",
            onConfirm = {
                appPre.sourceUrl = it
                currentSource = it
//                Toast.makeText(context, "更换成功", Toast.LENGTH_SHORT).show()
                Api.getSourceJson(context, it, {
                    Toast.makeText(context, "更换成功", Toast.LENGTH_SHORT).show()

                })
            },
            onNeutral = {
                // 把这里写一个常量
                appPre.sourceUrl = "https://www.gubaiovo.com/jnu-exam/source_list.json"
                currentSource = it
                Api.getSourceJson(context, "https://www.gubaiovo.com/jnu-exam/source_list.json", {
                    Toast.makeText(context, "已恢复默认", Toast.LENGTH_SHORT).show()
                })

//                Toast.makeText(context, "已恢复默认", Toast.LENGTH_SHORT).show()

            },
            onDismiss = { sourceModeDialog = false }
        )
    }
}


@Composable
fun AppThemeItem(
    title: String,
    colorScheme: ColorScheme,
    amoledBlack: Boolean,
    darkTheme: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(115.dp)
            .padding(start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AppThemePreviewItem(
            selected = selected,
            onClick = onClick,
            colorScheme = colorScheme.copy(
                background =
                    if (amoledBlack && (darkTheme == 0 && isSystemInDarkTheme() || darkTheme == 2)) {
                        Color.Black
                    } else {
                        colorScheme.background
                    }
            ),
            shapes = MaterialTheme.shapes
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall
        )
    }
}


@Composable
fun SettingsScaffoldLazyColumn(
    navigator: Navigator,
    titleText: String,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = rememberTopAppBarScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            snackbarHostState?.let {
                SnackbarHost(it)
            }
        },
        topBar = {
            CollapsingTopAppBar(
                collapsingTitle = CollapsingTitle.medium(titleText = titleText),
//                navigationIcon = {
//                    IconButton(onClick = { navigator.pop() }) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = null
//                        )
//                    }
//                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}