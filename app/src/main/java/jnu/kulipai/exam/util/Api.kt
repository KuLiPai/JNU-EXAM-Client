package jnu.kulipai.exam.util

import android.content.Context
import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

//object Api {
//
//
//    lateinit var client: HttpClient
//
//    fun init(client: HttpClient) {
//        this.client = client
//    }
//



    //没活了就..一下吧.............
//    fun dotDot(path: String): String {
//        val trimmed = path.trimEnd('/')
//        val lastSlashIndex = trimmed.lastIndexOf('/')
//        if (lastSlashIndex <= 0) return "/"
//
//        return trimmed.take(lastSlashIndex + 1)
//    }
//
//    fun formatFileSize(bytes: Long): String {
//        if (bytes < 1024) return "$bytes B"
//        val units = arrayOf("KB", "MB", "GB", "TB", "PB", "EB")
//        var size = bytes.toDouble()
//        var unitIndex = -1
//        do {
//            size /= 1024
//            unitIndex++
//        } while (size >= 1024 && unitIndex < units.size - 1)
//        return "%.1f %s".format(size, units[unitIndex])
//    }
//
//
//    // 使用协程在IO线程执行网络请求
//    suspend fun performGetRequest(url: String): String {
//        return try {
//            client.get(url).bodyAsText()
//        } catch (e: Exception) {
//            "err"
//        }
//    }
//
//    // 获取源
//    fun getSourceJson(application: Context,url: String,onSuccess: (String) -> Unit={}) {
//
//
//        downloadFileToInternal(
//            application, url, "source.json",
//            false,
//            onSuccess, {
//                Toast.makeText(application, "网络错误:(", Toast.LENGTH_SHORT).show()
//            })
//
//
//    }
//
//    // 解析源
//    fun parseSources(jsonString: String): List<SourceItem> {
//        val result = mutableListOf<SourceItem>()
//        try {
//            val jsonObject = JSONObject(jsonString)
//            jsonObject.keys().forEach { key ->
//
//                val itemObj = jsonObject.getJSONObject(key)
//                val jsonUrl = itemObj.getString("json_url")
//                val fileKey = itemObj.getString("file_key")
//                result.add(SourceItem(name = key, jsonUrl = jsonUrl, fileKey = fileKey))
//
//            }
//            return result
//
//        }catch (e: Exception) {
//            return result
//        }
//
//
//    }
//
//    fun downloadFileToInternal(
//        context: Context,
//        url: String,
//        relativePath: String,
//        isExternal: Boolean = false,
//        onSuccess: (String) -> Unit = {},
//        onFailure: (Exception) -> Unit = {}
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//
//            try {
//                val response: HttpResponse = client.get(url)
//
//                if (!response.status.isSuccess()) {
//                    onFailure(Exception("HTTP ${response.status.value}"))
//                    return@launch
//                }
//
//                val parent = if (isExternal) context.getExternalFilesDir("") else context.filesDir
//                // 2. 确保路径处理正确
//                // File(parent, child) 会自动处理路径分隔符
//                val destinationFile = File(parent, relativePath)
//                // 3. 写入文件逻辑 (推荐使用 Ktor 的 readBytes 或 ByteReadChannel 直接写入，效率更高)
//                destinationFile.parentFile?.mkdirs()
//
//                // 使用 Ktor 的 copyTo 直接对接 File流，减少内存占用
//                val channel = response.bodyAsChannel()
//                val fileStream = FileOutputStream(destinationFile)
//
//                fileStream.use { output ->
//                    channel.copyTo(output)
//                }
//
//                withContext(Dispatchers.Main) { onSuccess(response.body()) }
//
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) { onFailure(e) }
//            }
//        }
//    }
//}