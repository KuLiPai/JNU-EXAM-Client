package jnu.kulipai.exam.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jnu.kulipai.exam.core.common.d
import jnu.kulipai.exam.core.file.FileManager
import jnu.kulipai.exam.core.file.PathUtil
import jnu.kulipai.exam.core.network.DownloadDataSource
import jnu.kulipai.exam.data.model.ChangeSourceEvent
import jnu.kulipai.exam.data.model.DirNode
import jnu.kulipai.exam.data.model.DirectoryResult
import jnu.kulipai.exam.data.model.DownLoadState
import jnu.kulipai.exam.data.model.FileItem
import jnu.kulipai.exam.data.model.LoadingState
import jnu.kulipai.exam.data.model.SourceItem
import jnu.kulipai.exam.data.repository.FileRepository
import jnu.kulipai.exam.data.repository.SettingsRepository
import jnu.kulipai.exam.data.repository.SourceRepository
import jnu.kulipai.exam.platform.PlatformService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

//哇好像很方便，在一个地方统一管理需要context的函数
//用流来管理全局变量，神奇的感觉:))))))
// TODO)) 只注入Repository
class HomeViewModel(
    private val fileRepository: FileRepository,
    private val settingsRepository: SettingsRepository,
    private val platformService: PlatformService,
    private val downloadDataSource: DownloadDataSource,
    private val sourceRepository: SourceRepository,
    private val fileManager: FileManager,

    ) : ViewModel() {

    var repo = settingsRepository.repo
    val sourceUrl = settingsRepository.sourceUrl
    val update = settingsRepository.update
    val cooldown = settingsRepository.cooldown
    val day = settingsRepository.day

    fun updateSourceUrl(value: String) {
        viewModelScope.launch {
            settingsRepository.setSourceUrl(value)
        }
    }


    fun updateUpdate(value: Int) {
        viewModelScope.launch {
            settingsRepository.setUpdate(value)
        }
    }


    fun updateCooldown(value: Long) {
        viewModelScope.launch {
            settingsRepository.setCooldown(value)
        }
    }


    fun updateRepo(value: String) {
        viewModelScope.launch {
            settingsRepository.setRepo(value)
        }
    }

    fun updateDay(value: Long) {
        viewModelScope.launch {
            settingsRepository.setDay(value)
        }
    }

    fun updateRepoKey(value: String) {
        viewModelScope.launch {
            settingsRepository.setRepoKey(value)
        }
    }

    fun updateRepoUrl(value: String) {
        viewModelScope.launch {
            settingsRepository.setRepoUrl(value)
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
//            updateRepoUrl(sourceUrl)

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

    val darkTheme = settingsRepository.darkTheme

    fun updateDarkTheme(value: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setDarkTheme(value)
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
                        platformService.showToast("网络异常")
                        _loadingState.value = LoadingState.Err
                    }

                    is DirectoryResult.Success -> {
                        navigateTo("")
                        _root.value = result.tree
                        _loadingState.value = LoadingState.Loaded
                    }
                }
            } catch (e: Exception) {
                platformService.showToast("加载失败: ${e.message}")
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
                // fileRepository.clearCache() // Ideal: add clearCache to interface, or rely on impl details if needed, but here we just used to delete file directly.
                // Since removing direct file access, let's assume we can re-fetch or we need a clearCache mehod.
                // For now, in FileRepositoryImpl logic, if we call it again, it might use cache?
                // The original code was: File(application.filesDir, "cache.json").delete()
                // We should add clearCache to FileRepository or just direct FileManager.
                // Let's rely on standard logic or add clearCache later.
                // Actually, let's just use fileManager via Platform/Repository if exposed, or add clearCache to Repo interface.
                // To keep it simple and consistent with previous "trick", we might need to expose a clear logic.
                // For this refactor, I will add a clear function to FileRepositoryImpl or FileManager usage if possible.
                // BUT, since we passed FileManager to ViewModel, we can still use it for non-android file ops?
                // FileManager uses Context. So we should probably avoid direct FileManager usage if we want pure KMP ViewModel.
                // But header says: private val fileManager: FileManager. FileManager is in core, does it have android deps? Yes context.
                // So FileManager also needs refactoring for KMP. But step by step.
                // For now, let's assume FileRepository has forceUpdate param or a clearCache method.
                // I'll add `clearCache` to the interface later if needed or just skip explicit delete if Repo handles it on "force refresh".
                // Original used: File(...).delete().
                // Let's assume we just call getDirectoryTree again? No, it caches.

                // Hack: We can ask FileManager to delete it if we still have access to it (we do).
                fileManager.delete("cache.json")

                // 这里直接调用 repository 的逻辑
                when (val result = fileRepository.getDirectoryTree()) {
                    is DirectoryResult.Error -> {
                        platformService.showToast("网络异常")

                        _loadingState.value = LoadingState.Err
                    }

                    is DirectoryResult.Success -> {
                        _root.value = result.tree
                        _loadingState.value = LoadingState.Loaded
                        settingsRepository.setDay(System.currentTimeMillis())
                    }
                }
                // 假设 repository 有一个 forceUpdate 参数


                platformService.showToast("数据已更新！")
            } catch (e: Exception) {
                platformService.showToast("更新失败: ${e.message}")
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
                platformService.showToast("网络错误")
                downloadCallback(DownLoadState.Err)
            }
        }
    }


    fun openFileWithOtherApp(relativePath: String = "", absolutePath: String = "") {
        try {
            platformService.openFile(relativePath, absolutePath)
        } catch (e: Exception) {
            platformService.showToast("找不到应用打开此文件")
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
        // Mime guessing moved to PlatformService or can be kept here if pure kotlin logic.
        // But launcher is in PlatformService.
        platformService.prepareExport(path) {
            // Optional callback if needed
        }
    }

    fun exportFileToUri(targetUriStr: String) { // Changed Uri to String to avoid Android dependency in VM signature if possible, but standard is Uri.
        // For partial refactor, passing String uri might be better or keep Uri if use generic wrapper.
        // But let's assume we pass string or wrapper. 
        // Original: exportFileToUri(targetUri: Uri)
        // Let's keep it clean: pass String or generic type.
        // Actually, let's allow `Any` or specific platform wrapper? 
        // For now, I'll use String for URI to be safe, caller converts.

        val path = exportPath ?: return
        platformService.exportFileToUri(path, targetUriStr)
            .onSuccess {
                platformService.showToast("文件已成功导出")
            }
            .onFailure { e ->
                platformService.showToast("导出失败: ${e.message}")
            }
    }


    fun getFileByPath(path: String): File {
        return fileManager.getFileByPath(path)
    }

}
