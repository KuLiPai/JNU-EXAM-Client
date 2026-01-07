package jnu.kulipai.exam.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class NetworkDataSource(
    private val client: HttpClient
) {

    suspend fun getText(url: String): Result<String> =
        runCatching {
            client.get(url).bodyAsText()
        }
}
