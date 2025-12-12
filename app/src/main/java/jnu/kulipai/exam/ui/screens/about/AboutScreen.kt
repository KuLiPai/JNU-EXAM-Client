package com.kaajjo.libresudoku.ui.more.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import jnu.kulipai.exam.BuildConfig
import jnu.kulipai.exam.R
import jnu.kulipai.exam.ui.screens.egg.EmojiEasterEggScreen
import jnu.kulipai.exam.ui.screens.about.LazyItem
import jnu.kulipai.exam.ui.screens.about.LazyPersonItem
import jnu.kulipai.exam.ui.theme.ColorUtils.harmonizeWithPrimary


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
class AboutScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.about_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Center)
                            .size(48.dp),
                        painter = painterResource(R.drawable.biglogo),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        stringResource(
                            R.string.app_version,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "开发者",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))


                LazyPersonItem(
                    title = "苦小怕",
//                  subtitle = "",
                    painter = painterResource(R.drawable.kulipai),
                    onClick = {
                        uriHandler.openUri("https://github.com/KuLiPai")
                    }
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    "仓库提供者",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))

                LazyPersonItem(
                    title = "顾白",
//                  subtitle = "",
                    painter = painterResource(R.drawable.gubai),
                    onClick = {
                        uriHandler.openUri("https://github.com/gubaiovo")
                    }
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "其他",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))

                LazyItem(
                    title = "App开源地址",
                    painter = painterResource(R.drawable.github_142_svgrepo_com),
                    onClick = {
                        uriHandler.openUri("https://github.com/KuLiPai/JNU-EXAM-Client")
                    }
                )
                LazyItem(
                    title = "仓库地址",
                    painter = rememberVectorPainter(Icons.AutoMirrored.Filled.LibraryBooks),
                    onClick = {
                        uriHandler.openUri("https://github.com/gubaiovo/JNU-EXAM")

                    }
                )
                LazyItem(
                    title = "开源许可",
                    painter = rememberVectorPainter(Icons.Default.Info),
                    onClick = {
                        navigator.push(AboutLibrariesScreen())
                    }
                )


                LocalContext.current
                val requiredClicks = 3
                val timeLimit = 300L

                var tapCount by remember { mutableIntStateOf(0) }
                var lastTapTime by remember { mutableLongStateOf(0L) }

                LazyItem(
                    title = "版本",
                    subtitle = BuildConfig.VERSION_NAME,
                    painter = rememberVectorPainter(Icons.Default.Android),
                    onClick = {
                        val currentTime = System.currentTimeMillis()

                        // 如果距离上次点击的时间在限制内
                        if (currentTime - lastTapTime < timeLimit) {
                            tapCount++ // 连续点击次数+1
                        } else {
                            tapCount = 1 // 否则重置为1
                        }

                        lastTapTime = currentTime // 更新最后一次点击的时间

                        // 检查是否达到目标次数
                        if (tapCount >= requiredClicks) {
                            navigator.push(EmojiEasterEggScreen())


                            tapCount = 0
                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowScope.AboutSectionBox(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    additionalContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxRowHeight()
            .weight(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.harmonizeWithPrimary(),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = subtitle
                )
            }
            if (additionalContent != null) {
                additionalContent()
            }
        }
    }
}

