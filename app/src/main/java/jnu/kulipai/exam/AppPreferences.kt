package jnu.kulipai.exam

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


//封装就封装，第一次封装Preferences，感觉完全不用吧
//或许能提高代码可读性，或许并没有
//完全需要，当时做的真对，代码干净了许多
class AppPreferences (
  context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit { putBoolean(KEY_FIRST_LAUNCH, value) }
//    var isNight: Boolean
//        get() = prefs.getBoolean(NIGHT, false)
//        set(value) = prefs.edit { putBoolean(NIGHT, value) }
    var repo: String
        get() = prefs.getString(REPO, "Github").toString()
        set(value) = prefs.edit { putString(REPO, value) }
    var repoUrl: String
        get() = prefs.getString(REPO_URL, "").toString()
        set(value) = prefs.edit { putString(REPO_URL, value) }
    var repoKey: String
        get() = prefs.getString(REPO_KEY, "").toString()
        set(value) = prefs.edit { putString(REPO_KEY, value) }
    var sourceUrl: String
        get() = prefs.getString(SOURCE_URL, "https://www.gubaiovo.com/jnu-exam/source_list.json").toString()
        set(value) = prefs.edit { putString(SOURCE_URL, value) }
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
        private const val REPO_URL = "repoUrl"
        private const val REPO_KEY = "repoKey"
        private const val SOURCE_URL = "sourceUrl"
        private const val DAY = "day"
        private const val UPDATE = "update"
        private const val COOLDOWN = "cooldown"
    }
}