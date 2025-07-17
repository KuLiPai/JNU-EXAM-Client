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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jnu.kulipai.exam.R
import jnu.kulipai.exam.data.model.DownLoadState
import jnu.kulipai.exam.data.model.FileItem
import jnu.kulipai.exam.util.Api
import jnu.kulipai.exam.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileCard(item: FileItem, homeViewModel: HomeViewModel) { // 接收 ViewModel
    var downloadState by remember { mutableStateOf(DownLoadState.None) }
    var expanded by remember { mutableStateOf(false) }

    val angle by animateFloatAsState(targetValue = if (expanded) 180f else 0f, animationSpec = tween(durationMillis = 300))

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
                enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)),
                exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)) + fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
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
                            ElevatedButton(
                                onClick = { /* 预览逻辑 */ },
                                contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 6.dp)
                            ) {
                                Icon(painter = painterResource(R.drawable.visibility_24px), contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("预览")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    // 调用 ViewModel 中的下载方法
                                    homeViewModel.downloadFile(item) { state ->
                                        downloadState = state // 更新 Composable 内部的下载状态
                                    }
                                },
                                contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 6.dp)
                            ) {
                                when (downloadState) {
                                    DownLoadState.None -> {
                                        Icon(painter = painterResource(R.drawable.download_24px), contentDescription = null)
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
                            //                                    Icon(painter = painterResource(R.drawable.download_done_24px), contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("已下载")
                                    }
                                    DownLoadState.Err -> {
                            //                                    Icon(painter = painterResource(R.drawable.error_24px), contentDescription = null)
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
