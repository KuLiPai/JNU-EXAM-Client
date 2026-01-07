package jnu.kulipai.exam.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import jnu.kulipai.exam.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun BounceUpButton(onUpdateClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    val offsetY = remember { Animatable(screenHeightPx.toFloat()) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = {
//                loadingState.value = LoadingState.Loading
                scope.launch {
                    offsetY.animateTo(
                        targetValue = screenHeightPx.toFloat(),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                onUpdateClick()

//                appPrefs.day = LocalDate.now().dayOfMonth

                //有点问题好像，协程中不能干什么来的，好久没看这个忘记了
//                GlobalScope.launch {
//                    //http
//                    try {
//                        if (appPre.Repo == "gitee") {
//                            val json =
//                                Api.performGetRequest("https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/directory_structure.json")
//                            if (json != "err") {
//                                FileManager.write(context, "cache.json", json)
//                                root = FileManager.buildDirectoryTree(json)
//                                loadingState.value = LoadingState.Loaded
//                            } else {
//                                Toast.makeText(context, "超时力~", Toast.LENGTH_SHORT).show()
//                            }
//                        } else if (appPre.Repo == "github") {
//                            val json =
//                                Api.performGetRequest("https://gitee.com/gubaiovo/jnu-exam/raw/main/directory_structure.json")
//                            if (json != "err") {
//                                FileManager.write(context, "cache.json", json)
//                                root = FileManager.buildDirectoryTree(json)
//                                loadingState.value = LoadingState.Loaded
//                            } else {
//                                Toast.makeText(context, "超时力~", Toast.LENGTH_SHORT).show()
//                            }
//
//                        }
//                    } catch (e: Exception) {
//                    }
//                }

            },// 点击更新
            modifier = Modifier.offset { IntOffset(0, offsetY.value.toInt()) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.notifications_24px),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update")
            }
        }
    }

    LaunchedEffect(Unit) {
        val targetOffsetPx = screenHeightPx - with(density) { 924.dp.roundToPx() }
        offsetY.animateTo(
            targetValue = targetOffsetPx.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessVeryLow // 低刚性，动画更慢
            )
        )
    }
}