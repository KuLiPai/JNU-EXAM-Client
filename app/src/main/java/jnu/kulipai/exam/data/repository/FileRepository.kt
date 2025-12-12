package jnu.kulipai.exam.data.repository

import android.content.Context
import jnu.kulipai.exam.AppPreferences
import jnu.kulipai.exam.data.model.DirNode
import jnu.kulipai.exam.util.Api
import jnu.kulipai.exam.util.FileManager

class FileRepository (
    private val fileManager: FileManager,
    private val api: Api,
    private val appPreferences: AppPreferences // 注入 AppPreferences 以获取 repo 设置
) {
    suspend fun getDirectoryTree(context: Context): DirNode {
        // 检查缓存
        if (fileManager.exists(context, "cache.json")) {
            val json = fileManager.read(context, "cache.json")
            return fileManager.buildDirectoryTree(json.toString())
        } else {

            // 从网络获取
            val url = when (appPreferences.repo ) {
                "gitee"-> "https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/directory_structure.json"
                "github" -> "https://gitee.com/gubaiovo/jnu-exam/raw/main/directory_structure.json"
                "cloudflare" -> "https://jnuexam.xyz/directory_structure.json"
                else -> "https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/directory_structure.json"
            }



            val json = api.performGetRequest(url)
            if (json != "err") {
                fileManager.write(context, "cache.json", json)
                return fileManager.buildDirectoryTree(json)
            } else {
                throw Exception("Failed to fetch directory tree")
            }
        }
    }
}