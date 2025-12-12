package jnu.kulipai.exam.ui.anim

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator

interface SlideAnimationScreen : Screen

object AppTransition {
    // 统一配置动画参数
    private const val DURATION = 300
    private val animSpec = tween<IntOffset>(durationMillis = DURATION, easing = FastOutSlowInEasing)
    private val fadeSpec = tween<Float>(durationMillis = DURATION)

    /**
     * 核心逻辑函数
     * @param navigator 用于获取当前的 StackEvent (Push/Pop)
     * @param screen 当前正要显示的页面
     */
    fun animate(navigator: Navigator, enterScreen: Screen, exitScreen: Screen): ContentTransform {
        return when (navigator.lastEvent) {
            StackEvent.Push -> {
                if (enterScreen is SlideAnimationScreen) slidePush()
                else fadeScaleTransition()
            }
            StackEvent.Pop -> {
                if (exitScreen is SlideAnimationScreen) slidePop()
                else fadeScaleTransition()
            }
            else -> defaultFade()
        }
    }


    // --- 具体动画实现细节 (私有，让外部调用更干净) ---

    // 前进：新页从右入，旧页向左出
    private fun slidePush() =
        slideInHorizontally(animSpec) { it } togetherWith
                slideOutHorizontally(animSpec) { -it }

    // 后退：旧页从左入，当前页向右出
    private fun slidePop() =
        slideInHorizontally(animSpec) { -it } togetherWith
                slideOutHorizontally(animSpec) { it }

    private fun fadeScaleTransition() =
        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90))) togetherWith
                fadeOut(animationSpec = tween(90))

    // 默认淡入淡出
    private fun defaultFade() =
        fadeIn(fadeSpec) togetherWith fadeOut(fadeSpec)
}