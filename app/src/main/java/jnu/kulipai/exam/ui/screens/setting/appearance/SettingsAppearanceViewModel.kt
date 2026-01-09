package jnu.kulipai.exam.ui.screens.setting.appearance

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jnu.kulipai.exam.data.repository.SettingsRepository
import jnu.kulipai.exam.data.datastore.ThemeSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsAppearanceViewModel (
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val darkTheme by lazy {
        settingsRepository.darkTheme
    }

    fun updateDarkTheme(value: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setDarkTheme(value)
        }

    val dynamicColors by lazy {
        settingsRepository.dynamicColors
    }

    fun updateDynamicColors(enabled: Boolean) =
        viewModelScope.launch {
            settingsRepository.setDynamicColors(enabled)
        }

    val amoledBlack by lazy {
        settingsRepository.amoledBlack
    }

    fun updateAmoledBlack(enabled: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setAmoledBlack(enabled)
        }



    val paletteStyle by lazy { settingsRepository.themePaletteStyle }
    fun updatePaletteStyle(index: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            // Need to access paletteStyles companion
            settingsRepository.setPaletteStyle(ThemeSettingsManager.paletteStyles[index].first)
        }

    val isUserDefinedSeedColor by lazy { settingsRepository.isUserDefinedSeedColor }
    fun updateIsUserDefinedSeedColor(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setIsUserDefinedSeedColor(value)
        }
    }

    val seedColor by lazy { settingsRepository.themeColorSeed }
    fun updateCurrentSeedColor(seedColor: Color) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setCurrentThemeColor(seedColor)
        }
    }


}