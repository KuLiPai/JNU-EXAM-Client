package jnu.kulipai.exam.data.repository

import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import jnu.kulipai.exam.data.datastore.AppPreferences
import jnu.kulipai.exam.data.datastore.ThemeSettingsManager
import kotlinx.coroutines.flow.Flow

open class SettingsRepository(
    private val appPreferences: AppPreferences,
    private val themeSettings: ThemeSettingsManager
) {

    open val repo: Flow<String> = appPreferences.repo
    open val sourceUrl: Flow<String> = appPreferences.sourceUrl
    open val update: Flow<Int> = appPreferences.update
    open val cooldown: Flow<Long> = appPreferences.cooldown
    open val day: Flow<Long> = appPreferences.day
    open val repoKeyFlow: Flow<String> = appPreferences.repoKeyFlow
    open val firstLaunch: Flow<Boolean> = appPreferences.firstLaunch
    open val darkTheme: Flow<Int> = themeSettings.darkTheme
    open val dynamicColors: Flow<Boolean> = themeSettings.dynamicColors
    open val amoledBlack: Flow<Boolean> = themeSettings.amoledBlack
    open val themeColorSeed: Flow<Color> = themeSettings.themeColorSeed
    open val themePaletteStyle: Flow<PaletteStyle> = themeSettings.themePaletteStyle
    open val isUserDefinedSeedColor: Flow<Boolean> = themeSettings.isUserDefinedSeedColor


    open suspend fun setSourceUrl(value: String) = appPreferences.setSourceUrl(value)
    open suspend fun setUpdate(value: Int) = appPreferences.setUpdate(value)
    open suspend fun setCooldown(value: Long) = appPreferences.setCooldown(value)
    open suspend fun setRepo(value: String) = appPreferences.setRepo(value)
    open suspend fun setDay(value: Long) = appPreferences.setDay(value)
    open suspend fun setRepoKey(value: String) = appPreferences.setRepoKey(value)
    open suspend fun setRepoUrl(value: String) = appPreferences.setRepoUrl(value)

    open suspend fun setFirstLaunch(value: Boolean) = appPreferences.setFirstLaunch(value)
    open suspend fun setDarkTheme(value: Int) = themeSettings.setDarkTheme(value)
    open suspend fun setDynamicColors(value: Boolean) = themeSettings.setDynamicColors(value)
    open suspend fun setAmoledBlack(value: Boolean) = themeSettings.setAmoledBlack(value)
    open suspend fun setCurrentThemeColor(value: Color) = themeSettings.setCurrentThemeColor(value)
    open suspend fun setPaletteStyle(value: PaletteStyle) = themeSettings.setPaletteStyle(value)
    open suspend fun setIsUserDefinedSeedColor(value: Boolean) = themeSettings.setIsUserDefinedSeedColor(value)
}