package jnu.kulipai.exam.app

import androidx.lifecycle.ViewModel
import jnu.kulipai.exam.data.datastore.AppPreferences
import jnu.kulipai.exam.data.repository.SourceRepository
import jnu.kulipai.exam.data.datastore.ThemeSettingsManager

class MainActivityViewModel
    (
    themeSettingsManager: ThemeSettingsManager,
    private val appPreferences: AppPreferences,
    private val sourceRepository: SourceRepository
) : ViewModel() {


    val dc = themeSettingsManager.dynamicColors
    val darkTheme = themeSettingsManager.darkTheme
    val amoledBlack = themeSettingsManager.amoledBlack
    val isFirstLaunch = appPreferences.isFirstLaunch
    val monetSudokuBoard = themeSettingsManager.monetSudokuBoard
    val colorSeed = themeSettingsManager.themeColorSeed
    val paletteStyle = themeSettingsManager.themePaletteStyle
//    val autoUpdateChannel = appSettingsManager.autoUpdateChannel
//    val updateDismissedName = appSettingsManager.updateDismissedName


    // 更新源文件source.json
    suspend fun updateSourceFile() {
        sourceRepository.updateSourceFile(appPreferences.sourceUrl)
    }

}