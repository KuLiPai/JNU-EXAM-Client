package jnu.kulipai.exam.components

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs

////stackoverflow 好用！！
interface TextShapeCorners {
    fun calculateRadius(density: Density, textStyle: TextStyle): Float

    data class Fixed(private val radius: Dp) : TextShapeCorners {
        override fun calculateRadius(density: Density, textStyle: TextStyle): Float =
            with(density) {
                radius.toPx()
            }
    }

    data class Flexible(
        private val fraction: Float = 0.45f
    ) : TextShapeCorners {
        override fun calculateRadius(density: Density, textStyle: TextStyle): Float =
            with(density) {
                textStyle.lineHeight.toPx() * fraction
            }
    }
}

interface TextShapePadding {
    fun calculatePadding(density: Density, textStyle: TextStyle): Float

    data class Fixed(private val padding: Dp) : TextShapePadding {
        override fun calculatePadding(density: Density, textStyle: TextStyle): Float =
            with(density) {
                padding.toPx()
            }
    }

    object Flexible : TextShapePadding {
        override fun calculatePadding(density: Density, textStyle: TextStyle): Float =
            with(density) {
                textStyle.lineHeight.toPx() - textStyle.fontSize.toPx()
            }
    }
}

fun TextLayoutResult.getLineRect(lineIndex: Int): Rect {
    return Rect(
        left = getLineLeft(lineIndex),
        top = getLineTop(lineIndex),
        right = getLineRight(lineIndex),
        bottom = getLineBottom(lineIndex)
    )
}

fun Rect.addHorizontalPadding(padding: Float): Rect {
    return Rect(left - padding, top, right + padding, bottom)
}

class TextShape(
    private val textLayoutResult: TextLayoutResult,
    private val padding: TextShapePadding = TextShapePadding.Flexible,
    private val corners: TextShapeCorners = TextShapeCorners.Flexible()
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val lineCount = textLayoutResult.lineCount
        val textStyle = textLayoutResult.layoutInput.style
        val lineHeight = with(density) { textStyle.lineHeight.toPx() }

        val lineRects = mutableMapOf<Int, Rect>()

        val paddingPx = padding.calculatePadding(density, textStyle)
        val curveRadiusPx =
            corners.calculateRadius(density, textStyle).coerceIn(0f, lineHeight / 2)

        val path = Path()

        // Step 1: Draw top line and top corners
        var previousLine: Rect = lineRects.getOrPut(0) {
            textLayoutResult.getLineRect(0).addHorizontalPadding(paddingPx)
        }
        path.moveTo(previousLine.left, previousLine.top + curveRadiusPx)
        path.quadraticTo(
            x1 = previousLine.left, y1 = previousLine.top,
            x2 = previousLine.left + curveRadiusPx, y2 = previousLine.top
        )
        path.lineTo(previousLine.right - curveRadiusPx, previousLine.top)
        path.quadraticTo(
            x1 = previousLine.right, y1 = previousLine.top,
            x2 = previousLine.right, y2 = previousLine.top + curveRadiusPx
        )
        path.lineTo(previousLine.right, previousLine.bottom - curveRadiusPx)

        // Step 2: Draw right sides of lines
        for (i in 1 until lineCount) {
            val currentLine = lineRects.getOrPut(i) {
                textLayoutResult.getLineRect(i).addHorizontalPadding(paddingPx)
            }
            if (abs(currentLine.right - previousLine.right) > curveRadiusPx) {
                val normalizedCurveRadius =
                    if (currentLine.right > previousLine.right) curveRadiusPx else -curveRadiusPx
                path.quadraticTo(
                    x1 = previousLine.right, y1 = previousLine.bottom,
                    x2 = previousLine.right + normalizedCurveRadius, y2 = currentLine.top
                )
                path.lineTo(currentLine.right - normalizedCurveRadius, currentLine.top)
                path.quadraticTo(
                    x1 = currentLine.right, y1 = currentLine.top,
                    x2 = currentLine.right, y2 = currentLine.top + curveRadiusPx
                )
            } else {
                path.cubicTo(
                    x1 = previousLine.right, y1 = previousLine.bottom,
                    x2 = currentLine.right, y2 = currentLine.top,
                    x3 = currentLine.right, y3 = currentLine.top + curveRadiusPx
                )
            }
            path.lineTo(currentLine.right, currentLine.bottom - curveRadiusPx)
            previousLine = currentLine
        }

        // Step 3: Draw bottom line and bottom corners
        path.quadraticTo(
            x1 = previousLine.right, y1 = previousLine.bottom,
            x2 = previousLine.right - curveRadiusPx, y2 = previousLine.bottom
        )
        path.lineTo(previousLine.left + curveRadiusPx, previousLine.bottom)
        path.quadraticTo(
            x1 = previousLine.left, y1 = previousLine.bottom,
            x2 = previousLine.left, y2 = previousLine.bottom - curveRadiusPx
        )
        path.lineTo(previousLine.left, previousLine.top + curveRadiusPx)

        // Step 4: Draw left sides of lines in reverse order
        for (i in lineCount - 2 downTo 0) {
            val currentLine = lineRects.getOrPut(i) {
                textLayoutResult.getLineRect(i).addHorizontalPadding(paddingPx)
            }
            if (abs(previousLine.left - currentLine.left) > curveRadiusPx) {
                val normalizedCurveRadius =
                    if (previousLine.left > currentLine.left) -curveRadiusPx else curveRadiusPx
                path.quadraticTo(
                    x1 = previousLine.left, y1 = previousLine.top,
                    x2 = previousLine.left + normalizedCurveRadius, y2 = currentLine.bottom
                )
                path.lineTo(currentLine.left - normalizedCurveRadius, currentLine.bottom)
                path.quadraticTo(
                    x1 = currentLine.left, y1 = currentLine.bottom,
                    x2 = currentLine.left, y2 = currentLine.bottom - curveRadiusPx
                )
            } else {
                path.cubicTo(
                    x1 = previousLine.left, y1 = previousLine.top,
                    x2 = currentLine.left, y2 = currentLine.bottom,
                    x3 = currentLine.left, y3 = currentLine.bottom - curveRadiusPx
                )
            }
            path.lineTo(currentLine.left, currentLine.top + curveRadiusPx)
            previousLine = currentLine
        }

        path.close()

        return Outline.Generic(path)
    }
}

// 点击复制文字，仿TG的，
@Composable
fun CopyableTextWithShape(
    text: String,
    copy: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val textShape by remember {
        derivedStateOf {
            textLayoutResult?.let {
                TextShape(
                    textLayoutResult = it,
                    padding = TextShapePadding.Fixed(4.dp), // <-- 指定较小 padding
                    corners = TextShapeCorners.Flexible(0.45f)
                )
            }
        }
    }


    var isPressed by remember { mutableStateOf(false) }
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.4f else 0f,
        label = "BackgroundAlpha"
    )

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Text(
        text = text,
        modifier = modifier
            .pointerInput(text) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val released = try {
                            awaitRelease()
                            true
                        } catch (e: Exception) {
                            false
                        }
                        // 延迟让按压效果显得更自然
                        if (released) {
                            delay(350)
                        }
                        isPressed = false
                    },
                    onTap = {
                        clipboardManager.setText(AnnotatedString(copy))
                        Toast.makeText(context, "已复制文本", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .then(
                textShape?.let {
                    Modifier
                        .background(backgroundColor.copy(alpha = backgroundAlpha), it)
//                        .border(0.72.dp, backgroundColor.copy(alpha = borderAlpha), it)
                } ?: Modifier
            )
            .padding(horizontal = 0.dp, vertical = 0.dp),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        onTextLayout = { textLayoutResult = it }
    )
}