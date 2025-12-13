package jnu.kulipai.exam.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import jnu.kulipai.exam.ui.screens.setting.SettingsTabContent


object SettingsTab : Tab {

    // 2. 定义底栏的标题和图标
    override val options: TabOptions
        @Composable
        get() {
            val title = "设置"
            val icon = rememberVectorPainter(Icons.Default.Settings)

            return remember {
                TabOptions(
                    index = 2u, // 你的 Settings 在第几个位置，假设是第3个
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // 3. 将原来的内容放入 Content
        SettingsTabContent()
    }
}