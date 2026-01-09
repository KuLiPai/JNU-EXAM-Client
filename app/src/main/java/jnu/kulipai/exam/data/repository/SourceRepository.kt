package jnu.kulipai.exam.data.repository

import jnu.kulipai.exam.core.common.d
import jnu.kulipai.exam.core.network.DownloadDataSource
import jnu.kulipai.exam.core.network.NetworkDataSource
import jnu.kulipai.exam.data.model.SourceItem
import jnu.kulipai.exam.data.model.SourceMapper
import java.io.File

class SourceRepository(
    private val network: NetworkDataSource,
    private val downloader: DownloadDataSource
) {

    suspend fun fetchSources(url: String): Result<List<SourceItem>> {
        return network.getText(url).map {
            SourceMapper.fromJson(it)
        }
    }


    suspend fun updateSourceFile(
        url: String
    ): Result<File> =
        downloader.download(
            url = url,
            relativePath = "source.json",
            external = false
        )
}
