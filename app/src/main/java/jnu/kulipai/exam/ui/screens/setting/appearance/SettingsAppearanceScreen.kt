package jnu.kulipai.exam.ui.screens.setting.appearance

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import jnu.kulipai.exam.R
import jnu.kulipai.exam.components.PreferenceRow
import jnu.kulipai.exam.components.PreferenceRowSwitch
import jnu.kulipai.exam.components.ScrollbarLazyColumn
import jnu.kulipai.exam.data.constants.PreferencesConstants
import jnu.kulipai.exam.ui.screens.setting.AppThemeItem
import jnu.kulipai.exam.ui.screens.setting.SelectionDialog
import jnu.kulipai.exam.ui.screens.setting.SettingsScaffoldLazyColumn
import jnu.kulipai.exam.ui.screens.setting.components.AppThemePreviewItem
import jnu.kulipai.exam.ui.screens.setting.components.ColorPickerDialog
import jnu.kulipai.exam.ui.theme.ThemeSettingsManager
import jnu.kulipai.exam.ui.theme.期末无挂Theme
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalStdlibApi::class)
class SettingsAppearanceScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SettingsAppearanceViewModel = koinViewModel()
        val context = LocalContext.current

        var darkModeDialog by rememberSaveable { mutableStateOf(false) }
        var paletteStyleDialog by rememberSaveable { mutableStateOf(false) }
        var colorPickerDialog by rememberSaveable { mutableStateOf(false) }

        val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(initialValue = PreferencesConstants.DEFAULT_DARK_THEME)
        val dynamicColors by viewModel.dynamicColors.collectAsStateWithLifecycle(initialValue = PreferencesConstants.DEFAULT_DYNAMIC_COLORS)
        val amoledBlack by viewModel.amoledBlack.collectAsStateWithLifecycle(initialValue = PreferencesConstants.DEFAULT_AMOLED_BLACK)

        val currentPaletteStyle by viewModel.paletteStyle.collectAsStateWithLifecycle(initialValue = PaletteStyle.TonalSpot)
        val currentSeedColor by viewModel.seedColor.collectAsStateWithLifecycle(
            initialValue = Color(
                PreferencesConstants.DEFAULT_THEME_SEED_COLOR
            )
        )
        val isUserDefinedSeedColor by viewModel.isUserDefinedSeedColor.collectAsStateWithLifecycle(
            initialValue = false
        )

        SettingsScaffoldLazyColumn(
            titleText = stringResource(R.string.pref_appearance),
            navigator = navigator
        ) { paddingValues ->
            ScrollbarLazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
            ) {
                item {
                    PreferenceRow(
                        title = stringResource(R.string.pref_dark_theme),
                        subtitle = when (darkTheme) {
                            0 -> stringResource(R.string.pref_dark_theme_follow)
                            1 -> stringResource(R.string.pref_dark_theme_off)
                            2 -> stringResource(R.string.pref_dark_theme_on)
                            else -> ""
                        },
                        onClick = { darkModeDialog = true },
                        painter = rememberVectorPainter(Icons.Outlined.DarkMode)
                    )
                }

                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.pref_app_theme)
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp)
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            item {
                                期末无挂Theme(
                                    dynamicColor = true,
                                    darkTheme = when (darkTheme) {
                                        0 -> isSystemInDarkTheme()
                                        1 -> false
                                        else -> true
                                    },
                                    amoled = amoledBlack
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(115.dp)
                                            .padding(start = 8.dp, end = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AppThemePreviewItem(
                                            selected = dynamicColors,
                                            onClick = {
                                                viewModel.updateDynamicColors(true)
                                                viewModel.updateIsUserDefinedSeedColor(false)
                                            },
                                            colorScheme = MaterialTheme.colorScheme,
                                            shapes = MaterialTheme.shapes
                                        )
                                        Text(
                                            text = stringResource(R.string.theme_dynamic),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                        items(
                            listOf(
                                Color.Green to context.getString(R.string.theme_green),
                                Color.Red to context.getString(R.string.theme_peach),
                                Color.Yellow to context.getString(R.string.theme_yellow),
                                Color.Blue to context.getString(R.string.theme_blue),
                                Color(0xFFC97820) to context.getString(R.string.theme_orange),
                                Color.Cyan to context.getString(R.string.theme_cyan),
                                Color.Magenta to context.getString(R.string.theme_lavender)
                            )
                        ) {
                            AppThemeItem(
                                title = it.second,
                                colorScheme = rememberDynamicColorScheme(
                                    seedColor = it.first,
                                    isDark = when (darkTheme) {
                                        0 -> isSystemInDarkTheme()
                                        1 -> false
                                        else -> true
                                    },
                                    style = currentPaletteStyle,
                                    isAmoled = amoledBlack
                                ),
                                onClick = {
                                    viewModel.updateDynamicColors(false)
                                    viewModel.updateCurrentSeedColor(it.first)
                                    viewModel.updateIsUserDefinedSeedColor(false)
                                },
                                selected = currentSeedColor == it.first && !dynamicColors && !isUserDefinedSeedColor,
                                amoledBlack = amoledBlack,
                                darkTheme = darkTheme,
                            )
                        }

                        item {
                            Box {
                                AppThemeItem(
                                    title = stringResource(R.string.theme_custom),
                                    colorScheme = rememberDynamicColorScheme(
                                        seedColor = currentSeedColor,
                                        isDark = when (darkTheme) {
                                            0 -> isSystemInDarkTheme()
                                            1 -> false
                                            else -> true
                                        },
                                        style = currentPaletteStyle,
                                        isAmoled = amoledBlack
                                    ),
                                    onClick = {
                                        viewModel.updateDynamicColors(false)
                                        viewModel.updateIsUserDefinedSeedColor(true)
                                        colorPickerDialog = true
                                    },
                                    selected = isUserDefinedSeedColor,
                                    amoledBlack = amoledBlack,
                                    darkTheme = darkTheme,
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(top = 8.dp, end = 16.dp)
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    PreferenceRow(
                        title = stringResource(R.string.pref_monet_style),
                        subtitle = when (currentPaletteStyle) {
                            PaletteStyle.TonalSpot -> stringResource(R.string.monet_tonalspot)
                            PaletteStyle.Neutral -> stringResource(R.string.monet_neutral)
                            PaletteStyle.Vibrant -> stringResource(R.string.monet_vibrant)
                            PaletteStyle.Expressive -> stringResource(R.string.monet_expressive)
                            PaletteStyle.Rainbow -> stringResource(R.string.monet_rainbow)
                            PaletteStyle.FruitSalad -> stringResource(R.string.monet_fruitsalad)
                            PaletteStyle.Monochrome -> stringResource(R.string.monet_monochrome)
                            PaletteStyle.Fidelity -> stringResource(R.string.monet_fidelity)
                            PaletteStyle.Content -> stringResource(R.string.monet_content)
                        },
                        onClick = { paletteStyleDialog = true },
                        painter = rememberVectorPainter(Icons.Outlined.Palette)
                    )
                }
                item {
                    PreferenceRowSwitch(
                        title = stringResource(R.string.pref_pure_black),
                        checked = amoledBlack,
                        onClick = {
                            viewModel.updateAmoledBlack(!amoledBlack)
                        },
                        painter = rememberVectorPainter(Icons.Outlined.Contrast)
                    )
                }
            }
        }

        if (darkModeDialog) {
            SelectionDialog(
                title = stringResource(R.string.pref_dark_theme),
                selections = listOf(
                    stringResource(R.string.pref_dark_theme_follow),
                    stringResource(R.string.pref_dark_theme_off),
                    stringResource(R.string.pref_dark_theme_on)
                ),
                selected = darkTheme,
                onSelect = { index ->
                    viewModel.updateDarkTheme(index)
                },
                onDismiss = { darkModeDialog = false }
            )
        } else if (paletteStyleDialog) {
            SelectionDialog(
                title = stringResource(R.string.pref_monet_style),
                selections = listOf(
                    stringResource(R.string.monet_tonalspot),
                    stringResource(R.string.monet_neutral),
                    stringResource(R.string.monet_vibrant),
                    stringResource(R.string.monet_expressive),
                    stringResource(R.string.monet_rainbow),
                    stringResource(R.string.monet_fruitsalad),
                    stringResource(R.string.monet_monochrome),
                    stringResource(R.string.monet_fidelity),
                    stringResource(R.string.monet_content)
                ),
                selected = ThemeSettingsManager.paletteStyles.find { it.first == currentPaletteStyle }?.second
                    ?: 0,
                onSelect = { index ->
                    viewModel.updatePaletteStyle(index)
                },
                onDismiss = { paletteStyleDialog = false }
            )
        } else if (colorPickerDialog) {
            val clipboardManager = LocalClipboardManager.current
            var currentColor by remember {
                mutableIntStateOf(currentSeedColor.toArgb())
            }
            ColorPickerDialog(
                currentColor = currentColor,
                onConfirm = {
                    viewModel.updateCurrentSeedColor(Color(currentColor))
                    colorPickerDialog = false
                },
                onDismiss = {
                    colorPickerDialog = false
                },
                onHexColorClick = {
                    clipboardManager.setText(
                        AnnotatedString(
                            "#" + currentColor.toHexString(
                                HexFormat.UpperCase
                            )
                        )
                    )
                },
                onRandomColorClick = {
                    currentColor = (Math.random() * 16777215).toInt() or (0xFF shl 24)
                },
                onColorChange = {
                    currentColor = it
                },
                onPaste = {
                    val clipboardContent = clipboardManager.getText()
                    var parsedColor: Int? = null
                    if (clipboardContent != null) {
                        try {
                            parsedColor = clipboardContent.text.toColorInt()
                        } catch (_: Exception) {

                        }
                    }
                    if (parsedColor != null) {
                        currentColor = parsedColor
                    } else {
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.parse_color_fail),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            )
        }

    }
}
