package jnu.kulipai.exam.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jnu.kulipai.exam.data.datastore.AppPreferences
import jnu.kulipai.exam.data.repository.SourceRepository
import jnu.kulipai.exam.data.datastore.ThemeSettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel
    (
    themeSettingsManager: ThemeSettingsManager,
    private val appPreferences: AppPreferences,
    private val sourceRepository: SourceRepository
) : ViewModel() {


    val dc = themeSettingsManager.dynamicColors
    val darkTheme = themeSettingsManager.darkTheme
    val amoledBlack = themeSettingsManager.amoledBlack
    val firstLaunch: StateFlow<Boolean?> =
        appPreferences.firstLaunch
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )

    val colorSeed = themeSettingsManager.themeColorSeed
    val paletteStyle = themeSettingsManager.themePaletteStyle


    // 更新源文件source.json
    suspend fun updateSourceFile() {
        sourceRepository.updateSourceFile(appPreferences.sourceUrl.first())
    }

}