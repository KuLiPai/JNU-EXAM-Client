package jnu.kulipai.exam.ui.screens.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import jnu.kulipai.exam.R
import jnu.kulipai.exam.ui.screens.pdf.PdfScreen
import jnu.kulipai.exam.util.Cache
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ManagerTab : Tab {
    private fun readResolve(): Any = ManagerTab

    override val options: TabOptions
        @Composable
        get() {
            val title = "管理"
            // 这里假设你有这个图标，如果没有可以用 Icons.Default.Folder
            val icon = painterResource(R.drawable.folder_open_24px)

            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val viewModel: HomeViewModel = koinViewModel()
        FileManagerScreen(
            viewModel,
            onPdfPreview = { file ->
                Cache.currentFile = file
                Cache.currentName = file.name
                navigator.push(PdfScreen())
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    viewModel: HomeViewModel,
    onPdfPreview: (File) -> Unit
) {
    val context = LocalContext.current
    // 获取根目录: /Android/data/包名/files
    val rootDir = remember { context.getExternalFilesDir(null) } ?: return

    // 当前显示的目录状态
    var currentDir by remember { mutableStateOf(rootDir) }
    // 强制刷新 UI 的 key
    var refreshTrigger by remember { mutableStateOf(0) }

    // 获取当前目录下的文件列表，并排序（文件夹在前，文件在后）
    val fileList = remember(currentDir, refreshTrigger) {
        currentDir.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?.toList() ?: emptyList()
    }

    // 处理系统返回键：如果在子目录，则返回上一级
    BackHandler(enabled = currentDir != rootDir) {
        currentDir.parentFile?.let { currentDir = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentDir == rootDir) "本地文档管理" else currentDir.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (currentDir != rootDir) {
                        IconButton(onClick = { currentDir.parentFile?.let { currentDir = it } }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (fileList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无文件", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(fileList) { file ->
                    FileRowItem(
                        viewModel,
                        context = context,
                        file = file,
                        onFolderClick = { currentDir = it },
                        onDelete = {
                            if (it.deleteRecursively()) {
                                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                                refreshTrigger++
                            } else {
                                Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onPdfPreview = onPdfPreview
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun FileRowItem(
    viewModel: HomeViewModel,
    context: Context,
    file: File,
    onFolderClick: (File) -> Unit,
    onDelete: (File) -> Unit,
    onPdfPreview: (File) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isDir = file.isDirectory
    val lastModified = remember(file) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
    }
    val fileSize = remember(file) {
        if (isDir) "" else formatFileSize(file.length())
    }
    val isPdf = file.extension.equals("pdf", ignoreCase = true)

    ListItem(
        modifier = Modifier.clickable {
            if (isDir) {
                onFolderClick(file)
            } else {
                openFileWithOtherApp(viewModel, file)
            }
        },
        leadingContent = {
            Icon(
                imageVector = when {
                    isDir -> Icons.Default.Folder
                    isPdf -> Icons.Default.PictureAsPdf
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        },
        headlineContent = {
            Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text("$lastModified  $fileSize", style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // PDF 预览选项
                    if (isPdf) {
                        DropdownMenuItem(
                            text = { Text("App内预览") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) },
                            onClick = {
                                expanded = false
                                onPdfPreview(file)
                            }
                        )
                    }

                    if (!isDir) {
                        DropdownMenuItem(
                            text = { Text("用其他应用打开") },
                            leadingIcon = { Icon(Icons.Default.OpenInNew, null) },
                            onClick = {
                                expanded = false
                                openFileWithOtherApp(viewModel, file)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("导出") },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                            onClick = {
                                expanded = false
                                exportFile(viewModel, file)
                            }
                        )
                        HorizontalDivider()

                    }


                    // 删除
                    DropdownMenuItem(
                        text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded = false
                            onDelete(file)
                        }
                    )
                }
            }
        }
    )
}

// --- 工具函数 ---

/**
 * 格式化文件大小
 */
fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

/**
 * 调用系统Intent打开文件
 * 注意：需要在 AndroidManifest.xml 配置 FileProvider
 */
fun openFileWithOtherApp(viewModel: HomeViewModel, file: File) {
    viewModel.openFileWithOtherApp(absolutePath = file.path)
}

/**
 * 分享/导出文件
 */
fun exportFile(viewModel: HomeViewModel, file: File) {
    viewModel.prepareExport(file.path)
}
