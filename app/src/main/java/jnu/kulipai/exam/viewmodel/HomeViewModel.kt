package jnu.kulipai.exam.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jnu.kulipai.exam.AppPreferences
import jnu.kulipai.exam.data.model.DirNode
import jnu.kulipai.exam.data.model.DownLoadState
import jnu.kulipai.exam.data.model.FileItem
import jnu.kulipai.exam.data.model.LoadingState
import jnu.kulipai.exam.data.repository.FileRepository
import jnu.kulipai.exam.util.Api
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val appPreferences: AppPreferences,
    private val application: Application // Hilt 可以注入 Application Context
) : ViewModel() {


    private var _isSearch = MutableStateFlow(false)
    var isSearch = _isSearch.asStateFlow()

    fun setisSearch(bool: Boolean) {
        _isSearch.value = bool
    }

    private var _searchText = MutableStateFlow("")
    var searchText = _searchText.asStateFlow()

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    private val _loadingState = MutableStateFlow(LoadingState.Loading)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()


    fun setLoadingState(newState: LoadingState) {
        _loadingState.value = newState // 只有在 ViewModel 内部才能修改 _loadingState.value
    }


    private val _currentPath = MutableStateFlow("/")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private var _root = MutableStateFlow(DirNode("root", "/")) // ViewModel 持有 root 节点
    val root: StateFlow<DirNode> = _root


    init {
        loadDirectoryTree() // ViewModel 初始化时加载数据
    }

    private fun loadDirectoryTree() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                Toast.makeText(application, "加载", Toast.LENGTH_SHORT).show()

                _root.value = fileRepository.getDirectoryTree(application) // 传入 application context
                _loadingState.value = LoadingState.Loaded
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
                Api.DotDot(_currentPath.value)
            } else {
                _currentPath.value + "$folderName/"
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
                _currentPath.value = Api.DotDot(_currentPath.value)
                _loadingState.value = LoadingState.Loaded
            }
        } else {
            // 如果在根目录，可以考虑关闭应用或者不作处理，具体取决于需求
            // 注意：在 ViewModel 中不应该直接调用 finish()，而是通过事件回调通知 UI
            Toast.makeText(application, "已经在根目录啦", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateRepositoryData() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                // 这里直接调用 repository 的逻辑
                val newRoot =
                    fileRepository.getDirectoryTree(application) // 假设 repository 有一个 forceUpdate 参数
                _root.value = newRoot
                _loadingState.value = LoadingState.Loaded
                appPreferences.day = LocalDate.now().dayOfMonth
                Toast.makeText(application, "数据已更新！", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(application, "更新失败: ${e.message}", Toast.LENGTH_SHORT).show()
                _loadingState.value = LoadingState.Loaded // 即使失败也要回到 Loaded 状态
            }
        }
    }

    // 可以添加文件下载的逻辑到 ViewModel
    fun downloadFile(fileItem: FileItem, downloadCallback: (DownLoadState) -> Unit) {
        viewModelScope.launch {
            downloadCallback(DownLoadState.DownLoading)
            try {
                val url = if (appPreferences.repo == "gitee") {
                    fileItem.gitee_raw_url
                } else {
                    fileItem.github_raw_url
                }
                Api.downloadFileToInternal(
                    application, // 使用 application context
                    url,
                    fileItem.path,
                    { downloadCallback(DownLoadState.Downloaded) },
                    { downloadCallback(DownLoadState.Err) }
                )
            } catch (e: Exception) {
                Toast.makeText(application, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                downloadCallback(DownLoadState.Err)
            }
        }
    }
}