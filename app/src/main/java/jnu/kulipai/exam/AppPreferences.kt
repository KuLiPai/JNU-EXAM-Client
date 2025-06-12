// file: AppPreferences.kt
package jnu.kulipai.exam

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    companion object {
        private const val PREFS_NAME = "jnu.kulipai.exam.prefs"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
    }
}