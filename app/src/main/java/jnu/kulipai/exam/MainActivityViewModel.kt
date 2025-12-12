package jnu.kulipai.exam

import androidx.lifecycle.ViewModel
import jnu.kulipai.exam.ui.theme.ThemeSettingsManager


class MainActivityViewModel
    (
    themeSettingsManager: ThemeSettingsManager,
    appPreferences: AppPreferences,
) : ViewModel() {

    val appPre = appPreferences


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