package jnu.kulipai.exam.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.copyTo
import jnu.kulipai.exam.core.file.FileManager
import java.io.File
import java.io.FileOutputStream

class DownloadDataSource(
    private val client: HttpClient,
    private val fileManager: FileManager
) {

    suspend fun download(
        url: String,
        relativePath: String,
        external: Boolean
    ): Result<File> = runCatching {

        val response = client.get(url)
        if (!response.status.isSuccess()) {
            error("HTTP ${response.status.value}")
        }

        val file = fileManager.getFile(relativePath, external)

        response.bodyAsChannel().copyTo(FileOutputStream(file))
        file
    }
}
