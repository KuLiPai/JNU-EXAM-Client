package jnu.kulipai.exam.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import jnu.kulipai.exam.R
import jnu.kulipai.exam.data.model.LoadingState
import jnu.kulipai.exam.data.model.MaskAnimActive
import jnu.kulipai.exam.ui.screens.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    homeViewModel: HomeViewModel,
    isDarkTheme: Boolean,
    isAnimating: Boolean,
    onThemeToggle: MaskAnimActive,
    navController: Navigator
) {
    val pwd = homeViewModel.currentPath.collectAsState()
    val searchText = homeViewModel.searchText.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        key(isDarkTheme) {

            TopAppBar(
                title = {

                    AnimatedContent(
                        targetState = pwd.value, // 监视 pwd 的变化
                        transitionSpec = {
                            // 定义进入和退出动画
                            // 旧内容向左滑动并淡出，新内容从右侧滑入并淡入
                            if (targetState.length > initialState.length) {
                                (slideInHorizontally { fullWidth -> fullWidth } + fadeIn())
                                    .togetherWith(slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut())
                                    // SizeTransform 会动画 AnimatedContent 容器的尺寸变化
                                    .using(SizeTransform(clip = false)) // clip = false 防止内容在动画过程中被裁剪
                            } else {
                                (slideInHorizontally { fullWidth -> -fullWidth } + fadeIn())
                                    .togetherWith(slideOutHorizontally { fullWidth -> fullWidth } + fadeOut())
                                    // SizeTransform 会动画 AnimatedContent 容器的尺寸变化
                                    .using(SizeTransform(clip = false)) // clip = false 防止内容在动画过程中被裁剪
                            }

                        },
                        modifier = Modifier.animateContentSize(animationSpec = tween(150)), // 仍然保留 animateContentSize 来动画 Text 组件自身的尺寸变化
                        label = "PathTextAnimation"
                    ) { targetPwd -> // targetPwd 是当前动画的目标 pwd 值
                        Text(
                            if (targetPwd == "/") "期末无挂" else if (targetPwd.length >= 8) ".." + targetPwd.substring(
                                targetPwd.length - 8
                            ) else targetPwd,
//                                modifier = Modifier.animateContentSize(),
                            maxLines = 1, // 确保文本在一行内，方便水平滑动动画
                            overflow = TextOverflow.Ellipsis
                        )
                    }


//                        Text(
//                            if (pwd.value == "/") "期末无挂" else if (pwd.value.length >= 8) ".." + pwd.value.substring(
//                                pwd.value.length - 8
//                            ) else pwd.value,
//                            modifier = Modifier.animateContentSize(),
//                        )
                },
                navigationIcon = {
                    if (pwd.value != "/") {
                        Icon(
                            modifier = Modifier.padding(16.dp, 0.dp, 4.dp, 0.dp),
                            painter = painterResource(R.drawable.folder_open_24px),
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            modifier = Modifier.padding(16.dp, 0.dp, 4.dp, 0.dp),
                            painter = painterResource(R.drawable.biglogo),
                            contentDescription = null
                        )
                    }

                },
                actions = {
                    //别忘了路径过长隐藏一些按钮，
                    //好吧忘了，不对懒了
                    ThemeToggleButton(
                        isAnimating = isAnimating,
                        onThemeToggle = onThemeToggle,
                        homeViewModel
                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    IconButton(onClick = {
////                        navController.push(SettingScreen())
//                    }) {
//                        Icon(
//                            imageVector = Icons.Default.Settings,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onSurface
//                        )
//                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }

        var searchJob: Job? = null

        OutlinedTextField(
            value = searchText.value,
            onValueChange = { newText ->
                homeViewModel.setSearchText(newText)
                homeViewModel.setLoadingState(LoadingState.Loading)
                homeViewModel.setisSearch(false)

                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    if (newText.isNotEmpty()) {
                        delay(300) // 延迟300ms
                        homeViewModel.setisSearch(true)
                        homeViewModel.setLoadingState(LoadingState.Loaded)

                    }
                }

            },
            placeholder = { Text("搜索") },
            leadingIcon = {
                Row {
                    Spacer(Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索图标"
                    )
                }
            },
            trailingIcon = { // 右侧图标 (取消按钮)
                if (searchText.value.isNotEmpty()) { // 只有当有文字时才显示
                    Row {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "清空输入",
                            modifier = Modifier.clickable { // 添加点击事件
                                homeViewModel.setSearchText("")
                                focusManager.clearFocus()
                            }
                        )
                        Spacer(Modifier.width(8.dp))

                    }
                }
            },
            shape = RoundedCornerShape(percent = 50),
            singleLine = true,

            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp, 8.dp)
                .focusRequester(focusRequester)
        )


    }
}