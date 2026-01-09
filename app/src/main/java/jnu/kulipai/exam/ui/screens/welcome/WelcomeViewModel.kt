package jnu.kulipai.exam.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jnu.kulipai.exam.data.repository.SettingsRepository
import jnu.kulipai.exam.data.repository.SourceRepository
import jnu.kulipai.exam.platform.PlatformService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class WelcomeViewModel(
    val sourceRepository: SourceRepository,
    val platformService: PlatformService,
    val settingsRepository: SettingsRepository,

    ) : ViewModel() {


    val repo: Flow<String> = settingsRepository.repo
    val sourceUrl = settingsRepository.sourceUrl
    val update = settingsRepository.update
    val cooldown = settingsRepository.cooldown
    val repoKey = settingsRepository.repoKeyFlow

    fun updateSourceUrl(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setSourceUrl(value)
        }
    }


    fun updateRepo(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setRepo(value)
        }
    }

    fun updateRepoKey(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setRepoKey(value)
        }
    }

    fun updateIsFirstLaunch(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setFirstLaunch(value)
        }
    }


    fun updateRepoUrl(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setRepoUrl(value)
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
                platformService.showToast("网络错误:(")
            }
        }
    }


}
