package jnu.kulipai.exam.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jnu.kulipai.exam.data.repository.SettingsRepository
import jnu.kulipai.exam.data.repository.SourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel
    (
    private val settingsRepository: SettingsRepository,
    private val sourceRepository: SourceRepository
) : ViewModel() {


    val dc = settingsRepository.dynamicColors
    val darkTheme = settingsRepository.darkTheme
    val amoledBlack = settingsRepository.amoledBlack
    val firstLaunch: StateFlow<Boolean?> =
        settingsRepository.firstLaunch
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )

    val colorSeed = settingsRepository.themeColorSeed
    val paletteStyle = settingsRepository.themePaletteStyle


    // 更新源文件source.json
    suspend fun updateSourceFile() {
        sourceRepository.updateSourceFile(settingsRepository.sourceUrl.first())
    }

}