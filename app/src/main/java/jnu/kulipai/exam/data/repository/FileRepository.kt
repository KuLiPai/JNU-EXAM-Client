package jnu.kulipai.exam.data.repository

import jnu.kulipai.exam.core.common.isBlankJson
import jnu.kulipai.exam.core.file.FileManager
import jnu.kulipai.exam.core.network.NetworkDataSource
import jnu.kulipai.exam.data.datastore.AppPreferences
import jnu.kulipai.exam.data.model.DirectoryError
import jnu.kulipai.exam.data.model.DirectoryResult
import kotlinx.coroutines.flow.first

open class FileRepository(
    private val fileManager: FileManager,
    private val network: NetworkDataSource,
    private val appPreferences: AppPreferences
) {

    open suspend fun getDirectoryTree(): DirectoryResult {
        val cachePath = "cache.json"

        val json = runCatching {
            if (fileManager.exists(cachePath)) {
                fileManager.read(cachePath)
            } else {
                val text = network.getText(appPreferences.repoUrl.first())
                    .getOrElse { return DirectoryResult.Error(DirectoryError.NetworkFailed) }

                if (text.isBlankJson()) {
                    return DirectoryResult.Error(DirectoryError.EmptyJson)
                }

                fileManager.write(cachePath, text)
                text
            }
        }.getOrElse {
            return DirectoryResult.Error(DirectoryError.NetworkFailed)
        }

        // json 为NULL
        if (json.isNullOrEmpty()) {
            return DirectoryResult.Error(DirectoryError.NetworkFailed)
        }

        // json内容为"",或"null"
        if (json.isBlankJson()) {

            fileManager.delete(cachePath) // 💡 关键：防止坏缓存永久污染
            return DirectoryResult.Error(DirectoryError.EmptyJson)
        }


        return runCatching {
            DirectoryResult.Success(
                fileManager.buildDirectoryTree(json, appPreferences.repoKeyFlow.first())
            )
        }.getOrElse {
            fileManager.delete(cachePath)
            DirectoryResult.Error(DirectoryError.BuildFailed)
        }
    }
}