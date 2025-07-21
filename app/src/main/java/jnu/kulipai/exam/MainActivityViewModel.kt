package jnu.kulipai.exam

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jnu.kulipai.exam.ui.theme.ThemeSettingsManager
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
@Inject constructor(
    themeSettingsManager: ThemeSettingsManager,
//    appSettingsManager: AppSettingsManager
) : ViewModel() {
    val dc = themeSettingsManager.dynamicColors
    val darkTheme = themeSettingsManager.darkTheme
    val amoledBlack = themeSettingsManager.amoledBlack
//    val firstLaunch = appSettingsManager.firstLaunch
    val monetSudokuBoard = themeSettingsManager.monetSudokuBoard
    val colorSeed = themeSettingsManager.themeColorSeed
    val paletteStyle = themeSettingsManager.themePaletteStyle
//    val autoUpdateChannel = appSettingsManager.autoUpdateChannel
//    val updateDismissedName = appSettingsManager.updateDismissedName
}