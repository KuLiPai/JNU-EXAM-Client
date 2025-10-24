package jnu.kulipai.exam

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


//封装就封装，第一次封装Preferences，感觉完全不用吧
//或许能提高代码可读性，或许并没有
//完全需要，当时做的真对，代码干净了许多
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context // Hilt 可以提供 ApplicationContext
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit { putBoolean(KEY_FIRST_LAUNCH, value) }
    var isNight: Boolean
        get() = prefs.getBoolean(NIGHT, false)
        set(value) = prefs.edit { putBoolean(NIGHT, value) }
    var repo: String
        get() = prefs.getString(REPO, "gitee").toString()
        set(value) = prefs.edit { putString(REPO, value) }
    var day: Long
        get() = prefs.getLong(DAY, -1)
        set(value) = prefs.edit { putLong(DAY, value) }

    // 间隔更新天数
    var update: Int
        get() = prefs.getInt(UPDATE, 0)
        set(value) = prefs.edit { putInt(UPDATE, value) }

    // 严谨的100秒
    var cooldown: Long
        get() = prefs.getLong(COOLDOWN, 0L)
        set(value) = prefs.edit { putLong(COOLDOWN, value) }




    companion object {
        private const val PREFS_NAME = "conf"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val NIGHT = "night"
        private const val REPO = "repo"
        private const val DAY = "day"
        private const val UPDATE = "update"
        private const val COOLDOWN = "cooldown"
    }
}