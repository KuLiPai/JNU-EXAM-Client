package jnu.kulipai.exam.ui.screens.setting.appearance

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jnu.kulipai.exam.data.datastore.ThemeSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsAppearanceViewModel (
    private val themeSettings: ThemeSettingsManager
) : ViewModel() {
    val darkTheme by lazy {
        themeSettings.darkTheme
    }

    fun updateDarkTheme(value: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            themeSettings.setDarkTheme(value)
        }

    val dynamicColors by lazy {
        themeSettings.dynamicColors
    }

    fun updateDynamicColors(enabled: Boolean) =
        viewModelScope.launch {
            themeSettings.setDynamicColors(enabled)
        }

    val amoledBlack by lazy {
        themeSettings.amoledBlack
    }

    fun updateAmoledBlack(enabled: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            themeSettings.setAmoledBlack(enabled)
        }



    val paletteStyle by lazy { themeSettings.themePaletteStyle }
    fun updatePaletteStyle(index: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            themeSettings.setPaletteStyle(ThemeSettingsManager.paletteStyles[index].first)
        }

    val isUserDefinedSeedColor by lazy { themeSettings.isUserDefinedSeedColor }
    fun updateIsUserDefinedSeedColor(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            themeSettings.setIsUserDefinedSeedColor(value)
        }
    }

    val seedColor by lazy { themeSettings.themeColorSeed }
    fun updateCurrentSeedColor(seedColor: Color) {
        viewModelScope.launch(Dispatchers.IO) {
            themeSettings.setCurrentThemeColor(seedColor)
        }
    }


}