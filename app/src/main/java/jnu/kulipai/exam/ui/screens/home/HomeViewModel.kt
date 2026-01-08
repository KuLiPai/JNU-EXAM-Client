package jnu.kulipai.exam.ui.screens.home

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jnu.kulipai.exam.core.common.d
import jnu.kulipai.exam.core.file.FileManager
import jnu.kulipai.exam.core.file.PathUtil
import jnu.kulipai.exam.core.network.DownloadDataSource
import jnu.kulipai.exam.data.datastore.AppPreferences
import jnu.kulipai.exam.data.datastore.ThemeSettingsManager
import jnu.kulipai.exam.data.model.ChangeSourceEvent
import jnu.kulipai.exam.data.model.DirNode
import jnu.kulipai.exam.data.model.DirectoryResult
import jnu.kulipai.exam.data.model.DownLoadState
import jnu.kulipai.exam.data.model.FileItem
import jnu.kulipai.exam.data.model.LoadingState
import jnu.kulipai.exam.data.model.SourceItem
import jnu.kulipai.exam.data.repository.FileRepository
import jnu.kulipai.exam.data.repository.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

//哇好像很方便，在一个地方统一管理需要context的函数
//用流来管理全局变量，神奇的感觉:))))))
// TODO)) 只注入Repository
class HomeViewModel(
    private val fileRepository: FileRepository,
    private val appPreferences: AppPreferences,
    private val application: Application,
    private val themeSettingsManager: ThemeSettingsManager,
    private val downloadDataSource: DownloadDataSource,
    private val sourceRepository: SourceRepository,
    private val fileManager: FileManager,

    ) : ViewModel() {

    var repo = appPreferences.repo
    val sourceUrl = appPreferences.sourceUrl
    val update = appPreferences.update
    val cooldown = appPreferences.cooldown
    val repoKey = appPreferences.repoKeyFlow
    val repoUrl = appPreferences.repoUrl
    val day = appPreferences.day

    fun updateSourceUrl(value: String) {
        viewModelScope.launch {
            appPreferences.setSourceUrl(value)
        }
    }


    fun updateUpdate(value: Int) {
        viewModelScope.launch {
            appPreferences.setUpdate(value)
        }
    }


    fun updateCooldown(value: Long) {
        viewModelScope.launch {
            appPreferences.setCooldown(value)
        }
    }


    fun updateRepo(value: String) {
        viewModelScope.launch {
            appPreferences.setRepo(value)
        }
    }

    fun updateDay(value: Long) {
        viewModelScope.launch {
            appPreferences.setDay(value)
        }
    }

    fun updateRepoKey(value: String) {
        viewModelScope.launch {
            appPreferences.setRepoKey(value)
        }
    }

    fun updateRepoUrl(value: String) {
        viewModelScope.launch {
            appPreferences.setRepoUrl(value)
        }
    }

    fun changeSource(url: String) {
        changeSourceInternal(
            sourceUrl = url,
            toastOnSuccess = "更换成功"
        )
    }

    fun resetSourceToDefault() {
        changeSourceInternal(
            sourceUrl = "https://www.gubaiovo.com/jnu-exam/source_list.json",
            toastOnSuccess = "已恢复默认"
        )
    }

    private var _changeSourceEvent = MutableSharedFlow<ChangeSourceEvent>()
    val changeSourceEvent = _changeSourceEvent.asSharedFlow()

    private fun changeSourceInternal(
        sourceUrl: String,
        toastOnSuccess: String
    ) {
        viewModelScope.launch {
            updateSourceUrl(sourceUrl)
            updateRepoUrl(sourceUrl)

            fetchSources(sourceUrl)
                .onSuccess {
                    _changeSourceEvent.emit(
                        ChangeSourceEvent.SourceChangeSuccess(toastOnSuccess)
                    )
                }
                .onFailure { e ->
                    _changeSourceEvent.emit(
                        ChangeSourceEvent.SourceChangeFailed(
                            e.message ?: "源更新失败"
                        )
                    )
                }
        }
    }

    suspend fun fetchSources(url: String): Result<List<SourceItem>> {
        return sourceRepository.fetchSources(url)
    }

    val darkTheme = themeSettingsManager.darkTheme

    fun updateDarkTheme(value: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            themeSettingsManager.setDarkTheme(value)
        }


    private var exportLauncher: ActivityResultLauncher<String>? = null
    fun setExportLauncher(arl: ActivityResultLauncher<String>) {
        exportLauncher = arl
    }


//    private var _isSearch = MutableStateFlow(false)
//    var isSearch = _isSearch.asStateFlow()
//
//    fun setIsSearch(bool: Boolean) {
//        _isSearch.value = bool
//    }

    private var _searchText = MutableStateFlow("")
    var searchText = _searchText.asStateFlow()

    fun setSearchText(text: String) {
        _searchText.value = text
        if (text.isEmpty()) {
            fileManager.getDirContent(root.value, currentPath.value)?.let {
                _fileNodeData.value = it.subDirs + it.files
            }
            return

        }
        if (text.isNotEmpty()) {
            _fileNodeData.value = fileManager.searchFiles(root.value, searchText.value)

        } else if (currentPath.value == "/") {
            fileManager.getDirContent(root.value, currentPath.value)?.let {
                _fileNodeData.value = it.subDirs + it.files
            }
        } else {
            fileManager.getDirContent(root.value, currentPath.value)?.let {
                _fileNodeData.value = listOf(
                    DirNode(
                        name = "..",
                        path = "",
                    )
                ) + it.subDirs + it.files
            }
        }
    }


    private var _fileNodeData = MutableStateFlow<List<Any>>(listOf())
    var fileNodeData = _fileNodeData.asStateFlow()


    private val _loadingState = MutableStateFlow(LoadingState.Loading)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    fun setLoadingState(newState: LoadingState) {
        _loadingState.value = newState // 只有在 ViewModel 内部才能修改 _loadingState.value
    }


    private val _currentPath = MutableStateFlow("")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private var _root = MutableStateFlow(DirNode("root", "/")) // ViewModel 持有 root 节点
    val root: StateFlow<DirNode> = _root.asStateFlow()


    init {
        loadDirectoryTree() // ViewModel 初始化时加载数据
    }

    private fun loadDirectoryTree() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                when (val result = fileRepository.getDirectoryTree()) {
                    is DirectoryResult.Error -> {
                        Toast.makeText(application, "网络异常", Toast.LENGTH_SHORT).show()
                        _loadingState.value = LoadingState.Err
                    }

                    is DirectoryResult.Success -> {
                        "123".d()
                        navigateTo("")
                        _root.value = result.tree
                        _loadingState.value = LoadingState.Loaded
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(application, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                _loadingState.value = LoadingState.Err // 加载失败时进入错误状态
            }
        }
    }

    fun navigateTo(folderName: String) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Step
            delay(80) // 模拟动画或操作延迟
            _currentPath.value = if (folderName == "..") {

                PathUtil.dotDot(_currentPath.value)
            } else {
                _currentPath.value + "$folderName/"
            }
            if (_currentPath.value == "/") {
                fileManager.getDirContent(_root.value, _currentPath.value)?.let {
                    _fileNodeData.value = it.subDirs + it.files
                }
            } else {
                fileManager.getDirContent(_root.value, _currentPath.value)?.let {
                    _fileNodeData.value = listOf(
                        DirNode(
                            name = "..",
                            path = "",
                        )
                    ) + it.subDirs + it.files
                }
            }
//            delay(100) // 模拟动画或操作延迟
            _loadingState.value = LoadingState.Loaded
        }
    }

    fun handleBackPress() {
        if (_currentPath.value != "/") {
            viewModelScope.launch {
                _loadingState.value = LoadingState.Step
                delay(250)
                _currentPath.value = PathUtil.dotDot(_currentPath.value)
                _loadingState.value = LoadingState.Loaded
            }
        } else {
            // 如果在根目录，可以考虑关闭应用或者不作处理，具体取决于需求
            // 注意：在 ViewModel 中不应该直接调用 finish()，而是通过事件回调通知 UI
//            Toast.makeText(application, "已经在根目录啦", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateRepositoryData(callBack: () -> Unit = {}) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                File(application.filesDir, "cache.json").delete()

                // 这里直接调用 repository 的逻辑
                when (val result = fileRepository.getDirectoryTree()) {
                    is DirectoryResult.Error -> {
                        Toast.makeText(application, "网络异常", Toast.LENGTH_SHORT).show()

                        _loadingState.value = LoadingState.Err
                    }

                    is DirectoryResult.Success -> {
                        _root.value = result.tree
                        _loadingState.value = LoadingState.Loaded
                        appPreferences.setDay(System.currentTimeMillis())
                    }
                }
                // 假设 repository 有一个 forceUpdate 参数


                Toast.makeText(application, "数据已更新！", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(application, "更新失败: ${e.message}", Toast.LENGTH_SHORT).show()
                _loadingState.value = LoadingState.Loaded // 即使失败也要回到 Loaded 状态
                callBack()
            }
        }
    }

    // 可以添加文件下载的逻辑到 ViewModel
    fun downloadFile(fileItem: FileItem, downloadCallback: (DownLoadState) -> Unit) {
        viewModelScope.launch {
            downloadCallback(DownLoadState.DownLoading)
            val url = fileItem.url
            downloadDataSource.download(
                url,
                fileItem.path,
                true,
            ).onSuccess {
                downloadCallback(DownLoadState.Downloaded)
            }.onFailure {
                Toast.makeText(application, "网络错误", Toast.LENGTH_SHORT).show()
                downloadCallback(DownLoadState.Err)
            }
        }
    }


    fun openFileWithOtherApp(relativePath: String = "", absolutePath: String = "") {
        var file: File = File("")
        if (absolutePath.isEmpty()) {
            file = File(application.getExternalFilesDir(""), relativePath)
        } else {
            file = File(absolutePath)
        }
//        val file = File(application.getExternalFilesDir(""), relativePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File does not exist: $relativePath")
        }

        val extension = file.extension.lowercase()
        val fallbackMimeTypes = mapOf(
            "md" to "text/plain",
            "markdown" to "text/plain",
            "lua" to "text/plain",
            "log" to "text/plain",
            "json" to "application/json",
            "xml" to "text/xml",
            "csv" to "text/csv"
        )

        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
            ?: fallbackMimeTypes[extension]
            ?: "application/octet-stream"

        val uri: Uri = FileProvider.getUriForFile(
            application,
            "${application.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // ✅ 必须加，因是 application context
        }

        val chooser = Intent.createChooser(intent, "选择应用打开文件").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // ✅ Chooser 也加
        }

        try {
            application.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                application, "找不到应用打开此文件，路径: $relativePath，类型: $mimeType",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
//


    var exportPath: String? = null
        private set
    var exportMime: String? = null
        private set


    // 在 HomeViewModel 中
    fun prepareExport(path: String) {
        exportPath = path
        val file = File(path)

        // 1. 设置 MIME (这是为了解决后缀乱加的问题，结合上一条回答)
        exportMime = guessMimeType(file.name)

        // 2. 【关键修复】 这里必须要调用 launch，弹窗才会出来！
        // 传入文件名作为建议名称
        exportLauncher?.launch(file.name)
    }

    fun exportFileToUri(targetUri: Uri) {
        val path = exportPath ?: return
        val file = File(application.getExternalFilesDir(""), path)
        if (!file.exists()) {
            Toast.makeText(application, "源文件不存在: $path", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val inputStream = FileInputStream(file)
            val outputStream: OutputStream? =
                application.contentResolver.openOutputStream(targetUri)

            if (outputStream == null) {
                Toast.makeText(application, "无法打开导出目标", Toast.LENGTH_SHORT).show()
                return
            }

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(application, "文件已成功导出", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(application, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 在 HomeViewModel 中
    private fun guessMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            // 图片、视频、PDF 等为了方便用户在保存时能看到预览图，可以保留具体类型
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "apk" -> "application/vnd.android.package-archive"

            // 【关键修改】
            // 对于 md, json, lua, log, xml, gradle 等容易被系统自动加 .txt 的文件
            // 全部统一返回 application/octet-stream
            // 这样系统就会完全尊重你传入的 fileName，不会自动加后缀
            "md", "json", "xml", "lua", "log", "gradle", "kts", "cpp", "c", "h", "java", "kt" -> "application/octet-stream"

            // 甚至对于 txt，如果你也不想让系统干预，也可以设为 octet-stream
            // 但保留 text/plain 给 .txt 通常是没问题的
            "txt" -> "text/plain"

            // 压缩包通常没问题，保持原样即可
            "zip" -> "application/zip"
            "rar" -> "application/vnd.rar"
            "7z" -> "application/x-7z-compressed"

            // Office 文档
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

            // 默认兜底
            else -> "application/octet-stream"
        }
    }


}