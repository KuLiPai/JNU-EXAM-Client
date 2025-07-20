package jnu.kulipai.exam.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import jnu.kulipai.exam.AppPreferences
import jnu.kulipai.exam.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(appPrefs: AppPreferences, navController: NavController) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 👈 这里只调用独立的设置组件
            item { SourceSettingsGroup(navController) }
            item { UpdateSettingsGroup() } // 更新设置不需要 NavController
            item { AboutSettingsGroup(navController) }
            // 后面扩展内容，只需在这里添加新的设置组 Composable 即可
        }
    }
}


// 源设置组
@Composable
fun SourceSettingsGroup(navController: NavController) {
    Column {
        Text(
            text = stringResource(R.string.settings_source_category),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.settings_source_title)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Source,
                    contentDescription = null
                )
            },
            modifier = Modifier.clickable {
                // TODO: 处理源设置点击事件，例如导航到子设置页面
                // navController.navigate("source_settings_route")
                println("点击了源设置")
            }
        )
        Divider()
    }
}

// 更新设置组
@Composable
fun UpdateSettingsGroup() {
    Column {
        Text(
            text = stringResource(R.string.settings_update_category),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.settings_update_title)) },
            supportingContent = { Text(text = stringResource(R.string.settings_update_summary)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            },
            modifier = Modifier.clickable {
                // TODO: 处理更新时间点击事件，例如触发检查更新或显示上次更新时间
                println("点击了检查更新")
            }
        )
        Divider()
    }
}

// 关于应用设置组
@Composable
fun AboutSettingsGroup(navController: NavController) {
    Column {
        Text(
            text = stringResource(R.string.settings_about_category),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.settings_about_title)) },
            supportingContent = { Text(text = stringResource(R.string.settings_about_summary)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null
                )
            },
            modifier = Modifier.clickable {
                // TODO: 处理关于点击事件，例如导航到关于页面（显示版本信息、隐私政策等）
                // navController.navigate("about_screen_route")
                println("点击了关于")
            }
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
    }
}

