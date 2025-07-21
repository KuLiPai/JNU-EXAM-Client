package jnu.kulipai.exam.ui.theme

import androidx.annotation.FloatRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

var Green10 = Color(0x666c6167)
var Green20 = Color(0x7b755f73)
var Green30 = Color(0x68303164)
var Green40 = Color(0x5f757365)
var Green50 = Color(0x5f64796e)
var Green60 = Color(0x406d3163)
var Green70 = Color(0x5f636f6c)
var Green80 = Color(0x6f72737d)


object ColorUtils {

    fun Color.blend(
        color: Color,
        @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.2f
    ): Color = ColorUtils.blendARGB(this.toArgb(), color.toArgb(), fraction).toColor()

    fun Color.darken(
        @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.2f
    ): Color = blend(color = Color.Black, fraction = fraction)

    fun Color.lighten(
        @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.2f
    ): Color = blend(color = Color.White, fraction = fraction)

    fun Int.toColor(): Color = Color(color = this)

    @Composable
    fun Color.harmonizeWithPrimary(
        @FloatRange(
            from = 0.0,
            to = 1.0
        ) fraction: Float = 0.2f
    ): Color = blend(MaterialTheme.colorScheme.primary, fraction)

}