package jnu.kulipai.exam.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.generated.destinations.PdfScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import jnu.kulipai.exam.R
import jnu.kulipai.exam.data.model.DownLoadState
import jnu.kulipai.exam.data.model.FileItem
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import jnu.kulipai.exam.util.Api
import jnu.kulipai.exam.util.Cache
import jnu.kulipai.exam.util.FileManager
import java.io.File


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileCard(
    item: FileItem,
    homeViewModel: HomeViewModel,
    navController: DestinationsNavigator
) { // 接收 FileItem 和 HomeViewModel

    val context = LocalContext.current
    var downloadState by remember(item.path) { // key 设为 item.path，确保文件路径改变时重置状态
        mutableStateOf(
            if (FileManager.exists(context, item.path)) { // 使用 LocalContext.current
                DownLoadState.Downloaded
            } else {
                DownLoadState.None
            }
        )
    }

    var expanded by remember { mutableStateOf(false) }

    val angle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    OutlinedCard(
        modifier = Modifier
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(24.dp, 0.dp))
                    Column(modifier = Modifier.padding(24.dp, 16.dp)) {
                        Row {
                            Text("路径: ")
                            Text(item.path)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("大小: ${Api.formatFileSize(item.size)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("直链: ")
                            CopyableTextWithShape("Github", item.github_raw_url)
                            Spacer(modifier = Modifier.width(8.dp))
                            CopyableTextWithShape("Gitee", item.gitee_raw_url)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp, 16.dp, 0.dp, 0.dp),
                            horizontalArrangement = Arrangement.End
                        ) {

                            //导出文件
                            if (downloadState == DownLoadState.Downloaded) {
                                ElevatedButton(
                                    onClick = {
                                        homeViewModel.exportFile(item.path)
                                    },
                                    contentPadding = PaddingValues(
                                        start = 16.dp,
                                        top = 6.dp,
                                        end = 16.dp,
                                        bottom = 6.dp
                                    )
                                ) {
                                    Text("导出文件")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            //pdf预览
                            if (item.name.substringAfterLast(".") == "pdf" && downloadState == DownLoadState.Downloaded) {
                                ElevatedButton(
                                    onClick = {
                                        Cache.currentFile = File(context.filesDir, item.path)
                                        Cache.currentName = item.name
                                        navController.navigate(PdfScreenDestination)
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
                            }


                            Button(
                                onClick = {
                                    if (downloadState == DownLoadState.None) {
                                        // 直接调用 ViewModel 中的下载方法
                                        homeViewModel.downloadFile(item) { state ->
                                            downloadState = state // 更新 Composable 内部的下载状态
                                        }
                                    } else if (downloadState == DownLoadState.Downloaded) {
                                        // 打开
                                        homeViewModel.openFileWithOtherApp(item.path)
                                    }
                                },
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    top = 6.dp,
                                    end = 16.dp,
                                    bottom = 6.dp
                                )
                            ) {
                                when (downloadState) {
                                    DownLoadState.None -> {
                                        Icon(
                                            painter = painterResource(R.drawable.download_24px),
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("下载")
                                    }

                                    DownLoadState.DownLoading -> {
                                        LoadingIndicator(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(24.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("下载中")
                                    }

                                    DownLoadState.Downloaded -> {
                                        Icon(

                                            Icons.Default.Share,
                                            modifier = Modifier.size(20.dp),
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("打开")
                                    }

                                    DownLoadState.Err -> {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("下载失败")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}