package jnu.kulipai.exam.ui.anim
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.hypot
import androidx.core.graphics.createBitmap

data class AnimConfig(
    val center: Offset,
    val isExpand: Boolean
)

@Composable
fun ScreenshotThemeTransition(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    content: @Composable (startAnim: (Offset, Boolean, () -> Unit) -> Unit) -> Unit
) {
    val view = LocalView.current
    val window = LocalActivity.current?.window

    // 截图状态
    var screenshot by remember { mutableStateOf<Bitmap?>(null) }
    // 动画进度
    val animatable = remember { Animatable(0f) }
    // 动画配置
    var animConfig by remember { mutableStateOf(AnimConfig(Offset.Zero, true)) }

    // 协程作用域
    val scope = rememberCoroutineScope()
    // 保存当前的动画 Job，用于取消
    var currentAnimJob by remember { mutableStateOf<Job?>(null) }
    // 防止截图过程中的极速连点（截图需要几十毫秒，期间不应重入，否则闪烁）
    var isCapturing by remember { mutableStateOf(false) }

    // 触发动画的函数
    // offset: 点击位置
    // isExpand: 扩张还是收缩
    // onThemeChange: 真正切换数据的回调（必须等截图完了再执行）
    val startAnimation: (Offset, Boolean, () -> Unit) -> Unit = { offset, isExpand, onThemeChange ->
        if (!isCapturing) {
            isCapturing = true

            // 1. 如果有正在运行的动画，立刻取消它
            // 此时界面会停留在上一帧的样子（因为 screenshot 还没变，或者 content 还没变）
            currentAnimJob?.cancel()

            // 2. 截图当前屏幕
            // 这一步会把“当前的动画状态”或者“静止状态”截下来作为背景
            captureBitmap(view, window) { bitmap ->
                screenshot = bitmap
                animConfig = AnimConfig(offset, isExpand)

                // 3. 截图完成，现在可以安全地切换底层的主题数据了
                onThemeChange()

                // 4. 开始新的动画
                currentAnimJob = scope.launch {
                    // 重置动画进度
                    animatable.snapTo(0f)
                    isCapturing = false // 解除截图锁

                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 800) // 慢速动画
                    )
                    // 动画自然结束
                    screenshot = null
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 底层：新主题
        content(startAnimation)

        // 顶层：旧主题截图（被裁剪）
        if (screenshot != null) {
            val bitmap = screenshot!!.asImageBitmap()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val maxRadius = hypot(size.width, size.height)
                        val currentRadius = if (animConfig.isExpand) {
                            maxRadius * animatable.value
                        } else {
                            maxRadius * (1f - animatable.value)
                        }

                        clipPath(
                            path = Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(animConfig.center, currentRadius))
                            },
                            clipOp = if (animConfig.isExpand) ClipOp.Difference else ClipOp.Intersect
                        ) {
                            drawImage(bitmap)
                        }
                    }
            )
        }
    }
}

// 辅助函数保持不变
private fun captureBitmap(view: View, window: Window?, onCaptured: (Bitmap) -> Unit) {
    try {
        if (window != null) {
            val bitmap = createBitmap(view.width, view.height)
            val location = IntArray(2)
            view.getLocationInWindow(location)

            PixelCopy.request(
                window,
                Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
                bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        onCaptured(bitmap)
                    } else {
                        // 失败处理，防止卡死锁
                        onCaptured(bitmap)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}