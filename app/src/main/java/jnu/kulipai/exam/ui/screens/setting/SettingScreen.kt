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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import jnu.kulipai.exam.ui.screens.setting.components.AppThemePreviewItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AboutScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsAppearanceScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import jnu.kulipai.exam.R
import jnu.kulipai.exam.components.PreferenceRow
import jnu.kulipai.exam.components.ScrollbarLazyColumn
import jnu.kulipai.exam.components.collapsing_topappbar.CollapsingTitle
import jnu.kulipai.exam.components.collapsing_topappbar.CollapsingTopAppBar
import jnu.kulipai.exam.components.collapsing_topappbar.rememberTopAppBarScrollBehavior
import jnu.kulipai.exam.ui.anim.AnimatedNavigation
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>(style = AnimatedNavigation::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navigator: DestinationsNavigator
) {


    val context = LocalContext.current
    var repoModeDialog by rememberSaveable { mutableStateOf(false) }
    var updateModeDialog by rememberSaveable { mutableStateOf(false) }
    var currentRepo by rememberSaveable { mutableStateOf(viewModel.appPre.repo) }
    var currentUpdate by rememberSaveable { mutableStateOf(viewModel.appPre.update) }
    // 一个100秒后的时间
    var cooldown by rememberSaveable { mutableStateOf(viewModel.appPre.cooldown) }

    var progress by remember { mutableStateOf((System.currentTimeMillis() - cooldown) / 100000f) }

    if (progress > 1f) {
        progress = 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900),
        label = "progressAnimation"
    )

    // 每秒更新进度
    LaunchedEffect(Unit) {
        while (
            true
        ) {
            if (progress < 1f) {
                progress += 0.01f

            }
            delay(1000)

        }
    }

    SettingsScaffoldLazyColumn(
        titleText = stringResource(R.string.settings_title),
        navigator = navigator
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
                    onClick = {
                        navigator.navigate(SettingsAppearanceScreenDestination())
                    },
                    painter = rememberVectorPainter(Icons.Outlined.Palette)
                )
            }

            item {
                PreferenceRow(
                    title = "更换仓库",
                    subtitle = "当前$currentRepo",
                    onClick = { repoModeDialog = true },
                    painter = painterResource(
                        when (viewModel.appPre.repo) {
                            "github" -> R.drawable.github_142_svgrepo_com
                            "gitee" -> R.drawable.gitee_svgrepo_com
                            "cloudflare" -> R.drawable.cloudflare
                            else -> R.drawable.gitee_svgrepo_com
                        }
                    )
                )
            }

            item {
                PreferenceRow(
                    title = "立即更新",
                    subtitle = if (100 - (progress * 100).toInt() == 0) "冷却完成" else "冷却剩余${100 - (progress * 100).toInt()}秒",
                    diy = {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                        )
                    },
                    onClick = {
                        if (progress == 1f) {
                            progress = 0f
                            viewModel.appPre.cooldown = System.currentTimeMillis()
                            viewModel.updateRepositoryData()
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
                    painter = rememberVectorPainter(if (currentUpdate == 0) Icons.Default.UpdateDisabled else if (currentUpdate == 36500) Icons.Default.QuestionMark else Icons.Default.Update)
                )
            }

            item {
                PreferenceRow(
                    title = "关于",
//                  subtitle = "",
                    onClick = {
                        navigator.navigate(AboutScreenDestination)
                    },
                    painter = rememberVectorPainter(Icons.Outlined.Info)
                )
            }
        }

    }

    if (repoModeDialog) {
        SelectionDialog(
            title = "选择仓库",
            selections = listOf(
                "Github", "Gitee","Cloudflare"
            ),
            selected = when (viewModel.appPre.repo) {
                "github" -> 0
                "gitee" -> 1
                "cloudflare" -> 2
                else -> 1
            },
            onSelect = { index ->
                currentRepo = when (index) {
                    0 -> "github"
                    1 -> "gitee"
                    2 -> "cloudflare"
                    else -> "gitee"
                }
                viewModel.appPre.repo = currentRepo

            },
            onDismiss = { repoModeDialog = false }
        )
    } else if (updateModeDialog) {
        SelectionDialog(
            title = "设置更新间隔(天)",
            selections = listOf(
                "从不", "1天", "3天", "一周", "一个月", "一个季", "一年", "一个世纪"
            ),
            selected = when (currentUpdate) {
                0 -> 0
                1 -> 1
                3 -> 2
                7 -> 3
                30 -> 4
                90 -> 5
                365 -> 6
                36500 -> 7
                else -> 0
            },
            onSelect = { index ->
                currentUpdate = when (index) {
                    0 -> 0
                    1 -> 1
                    2 -> 3
                    3 -> 7
                    4 -> 30
                    5 -> 90
                    6 -> 365
                    7 -> 36500
                    else -> 0
                }
                viewModel.appPre.update = currentUpdate

            },
            onDismiss = { updateModeDialog = false }
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
    navigator: DestinationsNavigator,
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
                navigationIcon = {
                    IconButton(onClick = { navigator.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}