package jnu.kulipai.exam.ui.screens.welcome

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import jnu.kulipai.exam.data.datastore.AppPreferences
import jnu.kulipai.exam.data.repository.SourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class WelcomeViewModel(
    val sourceRepository: SourceRepository,
    val application: Application, // 最好别context，内存问题，用application
    val appPre: AppPreferences
) : ViewModel() {
    // 数据状态
    private val _data = MutableStateFlow<String?>(null) // null 表示未加载
    val data: StateFlow<String?> = _data

    // 模拟加载
    fun getSourceJson(url: String="https://www.gubaiovo.com/jnu-exam/source_list.json") {






        api.downloadFileToInternal(
            application, url, "source.json",
            false,
            {
                _data.value = it
            }, {
                Toast.makeText(application, "网络错误:(", Toast.LENGTH_SHORT).show()
            })


    }



}

