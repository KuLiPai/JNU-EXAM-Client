package jnu.kulipai.exam.data.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//封装就封装，第一次封装Preferences，感觉完全不用吧
//或许能提高代码可读性，或许并没有
//完全需要，当时做的真对，代码干净了许多
//当时真离谱，竟然即用dataStore又用古老的preference，现在全用DataStore。可以多平台，可以Flow！！！
class AppPreferences(
    context: Context
) {

    private val Context.createDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "conf")

    private val dataStore = context.createDataStore

    /* ===================== Keys ===================== */

    private val firstLaunchKey = booleanPreferencesKey("first_launch")
    private val repoKey = stringPreferencesKey("repo")
    private val repoUrlKey = stringPreferencesKey("repoUrl")
    private val repoAuthKey = stringPreferencesKey("repoKey")
    private val sourceUrlKey = stringPreferencesKey("sourceUrl")
    private val dayKey = longPreferencesKey("day")
    private val updateKey = intPreferencesKey("update")
    private val cooldownKey = longPreferencesKey("cooldown")

    /* ===================== First Launch ===================== */

    suspend fun setFirstLaunch(value: Boolean) {
        dataStore.edit { it[firstLaunchKey] = value }
    }

    val isFirstLaunch: Flow<Boolean> = dataStore.data.map {
        it[firstLaunchKey] ?: true
    }

    /* ===================== Repo ===================== */

    suspend fun setRepo(value: String) {
        dataStore.edit { it[repoKey] = value }
    }

    val repo: Flow<String> = dataStore.data.map {
        it[repoKey] ?: "Github"
    }

    /* ===================== Repo URL ===================== */

    suspend fun setRepoUrl(value: String) {
        dataStore.edit { it[repoUrlKey] = value }
    }

    val repoUrl: Flow<String> = dataStore.data.map {
        it[repoUrlKey] ?: ""
    }

    /* ===================== Repo Key ===================== */

    suspend fun setRepoKey(value: String) {
        dataStore.edit { it[repoAuthKey] = value }
    }

    val repoKeyFlow: Flow<String> = dataStore.data.map {
        it[repoAuthKey] ?: ""
    }

    /* ===================== Source URL ===================== */

    suspend fun setSourceUrl(value: String) {
        dataStore.edit { it[sourceUrlKey] = value }
    }

    val sourceUrl: Flow<String> = dataStore.data.map {
        it[sourceUrlKey]
            ?: "https://www.gubaiovo.com/jnu-exam/source_list.json"
    }

    /* ===================== Day ===================== */

    suspend fun setDay(value: Long) {
        dataStore.edit { it[dayKey] = value }
    }

    val day: Flow<Long> = dataStore.data.map {
        it[dayKey] ?: -1L
    }

    /* ===================== Update Interval ===================== */

    suspend fun setUpdate(value: Int) {
        dataStore.edit { it[updateKey] = value }
    }

    val update: Flow<Int> = dataStore.data.map {
        it[updateKey] ?: 0
    }

    /* ===================== Cooldown ===================== */

    suspend fun setCooldown(value: Long) {
        dataStore.edit { it[cooldownKey] = value }
    }

    val cooldown: Flow<Long> = dataStore.data.map {
        it[cooldownKey] ?: 0L
    }

}
