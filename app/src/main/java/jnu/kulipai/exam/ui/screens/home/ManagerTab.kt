package jnu.kulipai.exam.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import jnu.kulipai.exam.R


object ManagerTab : Tab {
    // 2. 定义底栏的标题和图标
    override val options: TabOptions
        @Composable
        get() {
            val title = "管理"
            val icon = painterResource(R.drawable.folder_open_24px)

            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {

    }
}
