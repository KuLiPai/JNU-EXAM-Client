package jnu.kulipai.exam.ui.screens.welcome

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jnu.kulipai.exam.core.common.d
import jnu.kulipai.exam.data.datastore.AppPreferences
import jnu.kulipai.exam.data.repository.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


class WelcomeViewModel(
    val sourceRepository: SourceRepository,
    val application: Application, // 最好别context，内存问题，用application
    val appPreferences: AppPreferences,

    ) : ViewModel() {


    val repo: Flow<String> = appPreferences.repo
    val sourceUrl = appPreferences.sourceUrl
    val update = appPreferences.update
    val cooldown = appPreferences.cooldown
    val repoKey = appPreferences.repoKeyFlow
    val repoUrl = appPreferences.repoUrl

    fun updateSourceUrl(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setSourceUrl(value)
        }
    }


    fun updateRepo(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setRepo(value)
        }
    }

    fun updateRepoKey(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setRepoKey(value)
        }
    }

    fun updateIsFirstLaunch(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setFirstLaunch(value)
        }
    }


    fun updateRepoUrl(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setRepoUrl(value)
        }
    }


    // 数据状态
    private val _data = MutableStateFlow<String?>(null) // null 表示未加载
    val data: StateFlow<String?> = _data

    // 模拟加载
    fun getSourceJson(url: String = "https://www.gubaiovo.com/jnu-exam/source_list.json") {


        viewModelScope.launch {
            sourceRepository.updateSourceFile(url).onSuccess {
                _data.value = it.readText()
            }.onFailure {
                Toast.makeText(application, "网络错误:(", Toast.LENGTH_SHORT).show()
            }
        }
    }


}

