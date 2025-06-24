package jnu.kulipai.exam.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import okhttp3.Callback
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

//data class Link(
//    val self: String,
//    val git: String,
//    val html: String
//)
//
////原本想用kotlin强大json反序列化，但是要导包
////不对我喜欢org.json
//data class FileItem(
//    val name: String,
//    val path: String,
//    val sha: String,
//    val size: Int,
//    val url: String,
//    @SerialName("html_url")
//    val htmlUrl: String,
//    @SerialName("git_url")
//    val gitUrl: String,
//    @SerialName("download_url")
//    val downloadUrl: String? = null,
//    val type: String,
//    @SerialName("_links")
//    val links: Link
//)


object Api {
//    fun parseFileItems(jsonString: String): List<FileItem> {
//        val list = mutableListOf<FileItem>()
//        val jsonArray = JSONArray(jsonString)
//        for (i in 0 until jsonArray.length()) {
//            val obj = jsonArray.getJSONObject(i)
//            val linksObj = obj.getJSONObject("_links")
//            val links = Link(
//                self = linksObj.getString("self"),
//                git = linksObj.getString("git"),
//                html = linksObj.getString("html")
//            )
//            val item = FileItem(
//                name = obj.getString("name"),
//                path = obj.getString("path"),
//                sha = obj.getString("sha"),
//                size = obj.getInt("size"),
//                url = obj.getString("url"),
//                htmlUrl = obj.getString("html_url"),
//                gitUrl = obj.getString("git_url"),
//                downloadUrl = if (obj.isNull("download_url")) null else obj.getString("download_url"),
//                type = obj.getString("type"),
//                links = links
//            )
//            list.add(item)
//        }
//        return list
//    }
//
//    //ai很好用
//    fun FileSort(rawFileItems: List<FileItem>): List<FileItem> {
//        // 排序逻辑
//        return rawFileItems.sortedWith(
//            compareBy<FileItem> { item ->
//                // 主要排序：dir 在 file 之前
//                when (item.type) {
//                    "dir" -> 0 // dir 类型排在前面
//                    "file" -> 1 // file 类型排在后面
//                    else -> 2 // 其他未知类型排在最后
//                }
//            }.thenBy { item ->
//                // 次要排序：在同类型中按 name 字母顺序排序
//                item.name
//            }
//        )
//    }




    //没活了就..一下吧.............
    fun DotDot(path: String): String {
        val trimmed = path.trimEnd('/')
        val lastSlashIndex = trimmed.lastIndexOf('/')
        if (lastSlashIndex <= 0) return "/"

        return trimmed.substring(0, lastSlashIndex + 1)
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB", "PB", "EB")
        var size = bytes.toDouble()
        var unitIndex = -1
        do {
            size /= 1024
            unitIndex++
        } while (size >= 1024 && unitIndex < units.size - 1)
        return "%.1f %s".format(size, units[unitIndex])
    }


    // 使用协程在IO线程执行网络请求
    suspend fun performGetRequest(url: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                response.body?.string() ?: "err"
            }
        } catch (e: Exception) {
            "err"
        }
    }

    fun downloadFileToInternal(
        context: Context,
        url: String,
        relativePath: String, // 修改参数名，更清晰地表示相对路径
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e) // 网络请求失败
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    // HTTP 状态码不是 2xx 的情况
                    onFailure(IOException("HTTP Error: ${response.code} - ${response.message}"))
                    return
                }

                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    inputStream = response.body?.byteStream()
                    if (inputStream == null) {
                        onFailure(IOException("Response body is null."))
                        return
                    }

                    // 构建目标文件对象
                    val destinationFile = File(context.filesDir, relativePath)

                    // 确保父目录存在
                    val parentDir = destinationFile.parentFile
                    if (parentDir != null && !parentDir.exists()) {
                        if (!parentDir.mkdirs()) { // 创建多级目录
                            onFailure(IOException("Failed to create parent directories: ${parentDir.absolutePath}"))
                            return
                        }
                    }

                    outputStream = FileOutputStream(destinationFile)

                    // 使用 Kotlin 的 copyTo 扩展函数进行文件拷贝，更简洁高效
                    inputStream.copyTo(outputStream)

                    onSuccess() // 下载并写入成功
                } catch (e: Exception) {
                    onFailure(e) // 捕获文件IO、JSON解析等所有可能异常
                } finally {
                    // 确保流被关闭
                    try {
                        inputStream?.close()
                        outputStream?.close()
                        response.body?.close() // 关闭响应体，释放资源
                    } catch (e: IOException) {
                        e.printStackTrace() // 打印关闭流时的异常，但不影响主流程
                    }
                }
            }
        })
    }
    //例子
    /*
    downloadFileToInternal(
    context = this,
    url = "http://example.com/a.zip",
    filename = "a.zip",
    onSuccess = {
        runOnUiThread {
            Toast.makeText(this, "下载成功！", Toast.LENGTH_SHORT).show()
        }
    },
    onFailure = {
        runOnUiThread {
            Toast.makeText(this, "下载失败：${it.message}", Toast.LENGTH_LONG).show()
        }
    }
)

     */




    val exampleData = """
        [
          {
            "name": "LICENSE",
            "path": "LICENSE",
            "sha": "2712c90913cb840d09abf53334cc9d8b0ccabd65",
            "size": 1062,
            "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/LICENSE?ref=main",
            "html_url": "https://github.com/gubaiovo/JNU-EXAM/blob/main/LICENSE",
            "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/2712c90913cb840d09abf53334cc9d8b0ccabd65",
            "download_url": "https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/LICENSE",
            "type": "file",
            "_links": {
              "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/LICENSE?ref=main",
              "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/2712c90913cb840d09abf53334cc9d8b0ccabd65",
              "html": "https://github.com/gubaiovo/JNU-EXAM/blob/main/LICENSE"
            }
          },
          {
            "name": "README.md",
            "path": "README.md",
            "sha": "aa5ef0112c30f3c34fc436764beeb51efcad1b4b",
            "size": 820,
            "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/README.md?ref=main",
            "html_url": "https://github.com/gubaiovo/JNU-EXAM/blob/main/README.md",
            "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/aa5ef0112c30f3c34fc436764beeb51efcad1b4b",
            "download_url": "https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/README.md",
            "type": "file",
            "_links": {
              "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/README.md?ref=main",
              "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/aa5ef0112c30f3c34fc436764beeb51efcad1b4b",
              "html": "https://github.com/gubaiovo/JNU-EXAM/blob/main/README.md"
            }
          },
          {
            "name": "乱七八糟的资料",
            "path": "乱七八糟的资料",
            "sha": "ceb76f9013842d51640fc84a2b0d430971e4a09e",
            "size": 0,
            "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E4%B9%B1%E4%B8%83%E5%85%AB%E7%B3%9F%E7%9A%84%E8%B5%84%E6%96%99?ref=main",
            "html_url": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E4%B9%B1%E4%B8%83%E5%85%AB%E7%B3%9F%E7%9A%84%E8%B5%84%E6%96%99",
            "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/ceb76f9013842d51640fc84a2b0d430971e4a09e",
            "download_url": null,
            "type": "dir",
            "_links": {
              "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E4%B9%B1%E4%B8%83%E5%85%AB%E7%B3%9F%E7%9A%84%E8%B5%84%E6%96%99?ref=main",
              "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/ceb76f9013842d51640fc84a2b0d430971e4a09e",
              "html": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E4%B9%B1%E4%B8%83%E5%85%AB%E7%B3%9F%E7%9A%84%E8%B5%84%E6%96%99"
            }
          },
          {
            "name": "其他学校",
            "path": "其他学校",
            "sha": "42b628041bbbab925bbcfffefe6799a4807026d4",
            "size": 0,
            "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%85%B6%E4%BB%96%E5%AD%A6%E6%A0%A1?ref=main",
            "html_url": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%85%B6%E4%BB%96%E5%AD%A6%E6%A0%A1",
            "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/42b628041bbbab925bbcfffefe6799a4807026d4",
            "download_url": null,
            "type": "dir",
            "_links": {
              "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%85%B6%E4%BB%96%E5%AD%A6%E6%A0%A1?ref=main",
              "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/42b628041bbbab925bbcfffefe6799a4807026d4",
              "html": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%85%B6%E4%BB%96%E5%AD%A6%E6%A0%A1"
            }
          },
          {
            "name": "大一上",
            "path": "大一上",
            "sha": "0ec2c86e73790e87126707c2251fe75dcc94927a",
            "size": 0,
            "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8A?ref=main",
            "html_url": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%A4%A7%E4%B8%80%E4%B8%8A",
            "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/0ec2c86e73790e87126707c2251fe75dcc94927a",
            "download_url": null,
            "type": "dir",
            "_links": {
              "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8A?ref=main",
              "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/0ec2c86e73790e87126707c2251fe75dcc94927a",
              "html": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%A4%A7%E4%B8%80%E4%B8%8A"
            }
          },
          {
            "name": "大一下",
            "path": "大一下",
            "sha": "9119480582fc6709140f2774de4611b6246129b7",
            "size": 0,
            "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B?ref=main",
            "html_url": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%A4%A7%E4%B8%80%E4%B8%8B",
            "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/9119480582fc6709140f2774de4611b6246129b7",
            "download_url": null,
            "type": "dir",
            "_links": {
              "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B?ref=main",
              "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/9119480582fc6709140f2774de4611b6246129b7",
              "html": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%A4%A7%E4%B8%80%E4%B8%8B"
            }
          }
        ]
    """.trimIndent()

}


//实例数据，记得删了，不要上传到github

/*
https://api.github.com/repos/gubaiovo/JNU-EXAM/contents
[
  {
    "name": "LICENSE",
    "path": "LICENSE",
    "sha": "2712c90913cb840d09abf53334cc9d8b0ccabd65",
    "size": 1062,
    "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/LICENSE?ref=main",
    "html_url": "https://github.com/gubaiovo/JNU-EXAM/blob/main/LICENSE",
    "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/2712c90913cb840d09abf53334cc9d8b0ccabd65",
    "download_url": "https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/LICENSE",
    "type": "file",
    "_links": {
      "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/LICENSE?ref=main",
      "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/2712c90913cb840d09abf53334cc9d8b0ccabd65",
      "html": "https://github.com/gubaiovo/JNU-EXAM/blob/main/LICENSE"
    }
  },
  {
    "name": "README.md",
    "path": "README.md",
    "sha": "aa5ef0112c30f3c34fc436764beeb51efcad1b4b",
    "size": 820,
    "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/README.md?ref=main",
    "html_url": "https://github.com/gubaiovo/JNU-EXAM/blob/main/README.md",
    "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/aa5ef0112c30f3c34fc436764beeb51efcad1b4b",
    "download_url": "https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/README.md",
    "type": "file",
    "_links": {
      "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/README.md?ref=main",
      "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/aa5ef0112c30f3c34fc436764beeb51efcad1b4b",
      "html": "https://github.com/gubaiovo/JNU-EXAM/blob/main/README.md"
    }
  },
  {
    "name": "乱七八糟的资料",
    "path": "乱七八糟的资料",
    "sha": "ceb76f9013842d51640fc84a2b0d430971e4a09e",
    "size": 0,
    "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E4%B9%B1%E4%B8%83%E5%85%AB%E7%B3%9F%E7%9A%84%E8%B5%84%E6%96%99?ref=main",
    "html_url": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E4%B9%B1%E4%B8%83%E5%85%AB%E7%B3%9F%E7%9A%84%E8%B5%84%E6%96%99",
    "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/ceb76f9013842d51640fc84a2b0d430971e4a09e",
    "download_url": null,
    "type": "dir",
    "_links": {
      "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E4%B9%B1%E4%B8%83%E5%85%AB%E7%B3%9F%E7%9A%84%E8%B5%84%E6%96%99?ref=main",
      "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/ceb76f9013842d51640fc84a2b0d430971e4a09e",
      "html": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E4%B9%B1%E4%B8%83%E5%85%AB%E7%B3%9F%E7%9A%84%E8%B5%84%E6%96%99"
    }
  },
  {
    "name": "其他学校",
    "path": "其他学校",
    "sha": "42b628041bbbab925bbcfffefe6799a4807026d4",
    "size": 0,
    "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%85%B6%E4%BB%96%E5%AD%A6%E6%A0%A1?ref=main",
    "html_url": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%85%B6%E4%BB%96%E5%AD%A6%E6%A0%A1",
    "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/42b628041bbbab925bbcfffefe6799a4807026d4",
    "download_url": null,
    "type": "dir",
    "_links": {
      "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%85%B6%E4%BB%96%E5%AD%A6%E6%A0%A1?ref=main",
      "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/42b628041bbbab925bbcfffefe6799a4807026d4",
      "html": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%85%B6%E4%BB%96%E5%AD%A6%E6%A0%A1"
    }
  },
  {
    "name": "大一上",
    "path": "大一上",
    "sha": "0ec2c86e73790e87126707c2251fe75dcc94927a",
    "size": 0,
    "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8A?ref=main",
    "html_url": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%A4%A7%E4%B8%80%E4%B8%8A",
    "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/0ec2c86e73790e87126707c2251fe75dcc94927a",
    "download_url": null,
    "type": "dir",
    "_links": {
      "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8A?ref=main",
      "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/0ec2c86e73790e87126707c2251fe75dcc94927a",
      "html": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%A4%A7%E4%B8%80%E4%B8%8A"
    }
  },
  {
    "name": "大一下",
    "path": "大一下",
    "sha": "9119480582fc6709140f2774de4611b6246129b7",
    "size": 0,
    "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B?ref=main",
    "html_url": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%A4%A7%E4%B8%80%E4%B8%8B",
    "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/9119480582fc6709140f2774de4611b6246129b7",
    "download_url": null,
    "type": "dir",
    "_links": {
      "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B?ref=main",
      "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/trees/9119480582fc6709140f2774de4611b6246129b7",
      "html": "https://github.com/gubaiovo/JNU-EXAM/tree/main/%E5%A4%A7%E4%B8%80%E4%B8%8B"
    }
  }
]






https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/2024%E7%BA%A7C%2B%2B%E5%AE%9E%E9%AA%8C%E6%9C%9F%E6%9C%AB%E8%AF%95%E5%8D%B7.txt?ref=main
{
  "name": "2024级C++实验期末试卷.txt",
  "path": "大一下/C++/2024级C++实验期末试卷.txt",
  "sha": "81f12d359ed37123372d1d872e069ea9085e6415",
  "size": 5085,
  "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/2024%E7%BA%A7C%2B%2B%E5%AE%9E%E9%AA%8C%E6%9C%9F%E6%9C%AB%E8%AF%95%E5%8D%B7.txt?ref=main",
  "html_url": "https://github.com/gubaiovo/JNU-EXAM/blob/main/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/2024%E7%BA%A7C%2B%2B%E5%AE%9E%E9%AA%8C%E6%9C%9F%E6%9C%AB%E8%AF%95%E5%8D%B7.txt",
  "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/81f12d359ed37123372d1d872e069ea9085e6415",
  "download_url": "https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/大一下/C++/2024级C++实验期末试卷.txt",
  "type": "file",
  "content": "MS4J5bey5o+Q5LqkDQrku6XkuIvlk6rkuKrlhbPplK7lrZfnlKjkuo7lo7Dm\nmI7kuIDkuKrnsbvmqKHmnb/vvJ8NCkINCg0KQS4gdmlydHVhbA0KDQpCLiB0\nZW1wbGF0ZQ0KDQpDLiBjbGFzcw0KDQpELiBzdGF0aWMNCg0K5b6X5YiG77ya\nMy4wMA0KMi4J5bey5o+Q5LqkDQrlh73mlbDmqKHmnb/nibnljJbnmoTkuLvo\npoHnm67nmoTmmK/vvJ8NCkINCg0KQS4g5o+Q6auY6L+Q6KGM5pe25oCn6IO9\nDQoNCkIuIOS4uueJueWumuexu+Wei+aPkOS+m+WumuWItuWunueOsA0KDQpD\nLiDpgb/lhY3nvJbor5Hlmajnsbvlnovmjqjlr7wNCg0KRC4g5aKe5Yqg5Luj\n56CB5Y+v6K+75oCnDQoNCg0KDQrlvpfliIbvvJozLjAwDQozLgnlt7Lmj5Dk\nuqQNCuWcqOe7p+aJv+S4re+8jOa0vueUn+exu+WPr+S7pemAmui/h+S7peS4\ni+WTquenjeaWueW8j+iuv+mXruWfuuexu+eahOS/neaKpOaIkOWRmO+8nw0K\nQQ0KDQpBLuebtOaOpeiuv+mXrg0KDQpCLiDku4XpgJrov4fln7rnsbvlr7no\nsaHorr/pl64NCg0KQy4g5LuF6YCa6L+H5Y+L5YWD5Ye95pWw6K6/6ZeuDQoN\nCkQuIOaXoOazleiuv+mXrg0KDQoNCuW+l+WIhu+8mjMuMDANCjQuCeW3suaP\nkOS6pA0K5Lul5LiL5ZOq5Liq6L+Q566X56ym5LiN6IO96KKr6YeN6L2977yf\nDQpDDQoNCkEuICA9PQ0KDQpCLiAqDQoNCkMuIHNpemVvZg0KDQpELiBbXQ0K\nDQrlvpfliIbvvJozLjAwDQo1Lgnlt7Lmj5DkuqQNCuWcqEMrK+S4re+8jOWF\ns+S6juaekOaehOWHveaVsOeahOivtOazle+8jOWTqumhueaYr+mUmeivr+ea\nhO+8nw0KQg0KDQpBLiDmnpDmnoTlh73mlbDlj6/ku6XmmK/omZrlh73mlbAN\nCg0KQi4g5p6Q5p6E5Ye95pWw5Y+v5Lul5pyJ5Y+C5pWwDQoNCkMuIOaekOae\nhOWHveaVsOayoeaciei/lOWbnuWAvA0KDQpELiDmnpDmnoTlh73mlbDlkI3n\np7DkuI7nsbvlkI3nm7jlkIwNCuW+l+WIhu+8mjMuMDANCiANCuWhq+epuumi\nmA0KMS4J5bey5o+Q5LqkDQrlnKjlpJrph43nu6fmib/kuK3vvIzop6PlhrPo\nj7HlvaLnu6fmib/pl67popjnmoTmlrnms5XmmK/kvb/nlKggICAgIOiZmiAg\nICAgIOe7p+aJvw0K5b6X5YiG77yaMy4wMA0KMi4J5bey5o+Q5LqkDQrph43o\nvb3kuozlhYPov5DnrpfnrKbvvIjlpoIr77yJ5L2c5Li65oiQ5ZGY5Ye95pWw\n5pe277yM5Ye95pWw562+5ZCN6YCa5bi45pyJICAgICAxICAgICDkuKrmmL7l\nvI/lj4LmlbANCuW+l+WIhu+8mjMuMDANCjMuCeW3suaPkOS6pA0K57G75qih\n5p2/5Lit77yM6Iul5qih5p2/5Y+C5pWw5Li66Z2e57G75Z6L5Y+C5pWw77yM\n6YCa5bi46ZyA6KaB5oyH5a6a5YW2ICAgICDlhbfkvZMgICAgIOexu+Weiw0K\n5b6X5YiG77yaMy4wMA0KNC4J5bey5o+Q5LqkDQrlnKjnsbvkuK3vvIzoi6Xl\nuIzmnJvmtL7nlJ/nsbvog73ph43lhpnmn5Dlh73mlbDvvIzor6Xlh73mlbDl\nupTlo7DmmI7kuLogICAgICDomZrlh73mlbAgICAgIA0K5b6X5YiG77yaMy4w\nMA0KNS4J5bey5o+Q5LqkDQrlrprkuYnlh73mlbDmqKHmnb/ml7bvvIzpgJrl\nuLjkvb/nlKggICAgICB0ZW1wbGF0ZSAgICAg5YWz6ZSu5a2X77yM5qih5p2/\n5Y+C5pWw5Y+v5Lul5pivICAgICAg57G75Z6L5Y+C5pWwICAgICDmiJbpnZ7n\nsbvlnovlj4LmlbANCg0KDQog566A562U6aKYDQoxLgkNCuivtOaYjkMrK+S4\nreWfuuexu+aekOaehOWHveaVsOS4uuS9lemcgOimgeWjsOaYjuS4uuiZmuWH\nveaVsO+8jOW5tueugOi/sOWFtuS9nOeUqOOAgg0KNS4wMAnlvpfliIbvvJo1\nLjAwDQoxLuWOn+WboO+8muWmguaenOWfuuexu+eahOaekOaehOWHveaVsOS4\njeaYr+iZmuWHveaVsO+8jOmCo+S5iOW9k+mAmui/h+Wfuuexu+aMh+mSiOWI\noOmZpOa0vueUn+exu+WvueixoeaXtu+8jOWPquS8muiwg+eUqOWfuuexu+ea\nhOaekOaehOWHveaVsO+8jOiAjOS4jeS8muiwg+eUqOa0vueUn+exu+eahOae\nkOaehOWHveaVsO+8jOWvvOiHtOi1hOa6kOazhOa8j+OAgg0KMi7kvZznlKjv\nvJrlvZPln7rnsbvmnpDmnoTlh73mlbDlo7DmmI7kuLromZrlh73mlbDlkI7v\nvIzlj6/ku6Xnoa7kv53osIPnlKjmraPnoa7nmoTmnpDmnoTlh73mlbDjgIIN\nCg0KMi4JDQrnroDov7Dlh73mlbDmqKHmnb/kuI7mma7pgJrlh73mlbDnmoTl\njLrliKvvvIzlubbor7TmmI7nvJbor5HlmajlpoLkvZXlpITnkIblh73mlbDm\nqKHmnb/nmoTnsbvlnovmjqjlr7zjgIINCjUuMDAJ5b6X5YiG77yaNS4wMA0K\nMS7kuKTogIXljLrliKvvvJrlh73mlbDmqKHmnb/mmK/ms5vlnovku6PnoIHv\nvIznvJbor5HlmajmoLnmja7lrp7pmYXlj4LmlbDnsbvlnovnlJ/miJDlhbfk\nvZPlh73mlbDvvJvogIzmma7pgJrlh73mlbDmmK/pkojlr7nnibnlrprnsbvl\nnovnmoTlrp7njrDjgIINCjIu57yW6K+R5Zmo5aaC5L2V5aSE55CG5Ye95pWw\n5qih5p2/55qE57G75Z6L5o6o5a+877ya57yW6K+R5Zmo6YCa6L+H5Ye95pWw\n6LCD55So5pe255qE5a6e5Y+C57G75Z6L5o6o5a+85qih5p2/5Y+C5pWw77yM\n55Sf5oiQ5a+55bqU55qE5a6e5L6L5YyW5Ye95pWw44CCDQoNCjEuCQ0K5Yeg\n5L2V5b2i54q26Z2i56evDQoNCuOAkOmXrumimOaPj+i/sOOAkQ0KDQrorr7o\nrqHkuIDkuKrlh6DkvZXlvaLnirbnrqHnkIbnqIvluo/vvIzljIXlkKvmir3o\nsaHln7rnsbsgU2hhcGUg5ZKM5rS+55Sf57G7IENpcmNsZSDlj4ogUmVjdGFu\nZ2xl44CCDQoNClNoYXBlIOWMheWQq+e6r+iZmuWHveaVsCBhcmVhKCnvvIzo\nrqHnrpfpnaLnp6/jgIINCg0KQ2lyY2xlIOWMheWQq+WNiuW+hO+8iGRvdWJs\nZe+8ie+8jOmdouenr+S4uiDPgCAqIHJhZGl1c14y77yI5L2/55SoIDMuMTQx\nNTkyNjUzNTg5Nzkz77yJ44CCDQoNClJlY3RhbmdsZSDljIXlkKvlrr3lkozp\nq5jvvIhkb3VibGXvvInvvIzpnaLnp6/kuLogd2lkdGggKiBoZWlnaHTjgIIN\nCg0KDQrjgJDovpPlhaXlvaLlvI/jgJENCg0K6L6T5YWl5qC85byP77yaDQoN\nCuagvOW8j+S4uiA8dHlwZT4gPHBhcmFtMT4gWzxwYXJhbTI+Xe+8jHR5cGUg\n5Li6IEPvvIjlnIbvvInmiJYgUu+8iOefqeW9ou+8ieOAgg0KDQrlnIbvvJpw\nYXJhbTEg5Li65Y2K5b6E77yb55+p5b2i77yacGFyYW0xIOS4uuWuve+8jHBh\ncmFtMiDkuLrpq5jvvIzlnYfkuLrpnZ7otJ/mta7ngrnmlbDjgIINCg0K56ys\n5LiA6KGM77ya5pW05pWwIG7vvIzooajnpLrlvaLnirbmlbDph4/jgIINCg0K\n5o6l5LiL5p2l55qEIG4NCg0KIOihjO+8jOavj+ihjOaPj+i/sOS4gOS4quW9\noueKtu+8mg0KDQrmnIDlkI7kuIDooYzvvJrmlbTmlbAgce+8jOihqOekuuaf\npeivouasoeaVsO+8m+maj+WQjiBxIOihjO+8jOavj+ihjOS4gOS4quaVtOaV\nsCBpbmRleO+8iDAtYmFzZWTvvIzmn6Xor6LnrKwgaW5kZXgg5Liq5b2i54q2\n55qE6Z2i56ev77yJ44CCDQoNCuOAkOi+k+WHuuW9ouW8j+OAkQ0KDQrmr4/k\nuKrmn6Xor6LovpPlh7rkuIDooYzvvJpBcmVhIG9mIHNoYXBlIDxpbmRleD46\nIDxhcmVhPu+8jGFyZWEg5L+d55WZ5Lik5L2N5bCP5pWw44CCDQoNCuiLpSBp\nbmRleCDml6DmlYjvvIzovpPlh7ogQXJlYSBvZiBzaGFwZSA8aW5kZXg+OiAt\nMS4wMOOAgg0KDQrjgJDmoLfkvovovpPlhaXjgJENCg0KNA0KDQpDIDIuMA0K\nDQpSIDIuMCAzLjANCg0KQyAxLjANCg0KUiA0LjAgNS4wDQoNCjMNCg0KMA0K\nDQoyDQoNCjUNCg0KDQrjgJDmoLfkvovovpPlh7rjgJENCg0KQXJlYSBvZiBz\naGFwZSAwOiAxMi41Nw0KDQpBcmVhIG9mIHNoYXBlIDI6IDMuMTQNCg0KQXJl\nYSBvZiBzaGFwZSA1OiAtMS4wMA0KDQoNCuOAkOagt+S+i+ivtOaYjuOAkQ0K\n44CQ6K+E5YiG5qCH5YeG44CRDQoNCjMwLjAwCeS4i+i9vea6kOaWh+S7tg0K\n5pyA5ZCO5LiA5qyh5o+Q5Lqk5pe26Ze0OiAyMDI1LTA1LTI4IDE2OjQzOjU1\nDQoNCuW+l+WIhu+8mjMwLjAwDQoNCg0K5YWx5pyJ5rWL6K+V5pWw5o2uOjUN\nCuW5s+Wdh+WNoOeUqOWGheWtmDo1LjAxNEsgICAg5bmz5Z2HQ1BV5pe26Ze0\nOjAuMDAzMDVTICAgIOW5s+Wdh+WimemSn+aXtumXtDowLjAxMzM5Uw0KDQrm\ntYvor5XmlbDmja4J6K+E5Yik57uT5p6cDQrmtYvor5XmlbDmja4xCeWujOWF\nqOato+ehrg0K5rWL6K+V5pWw5o2uMgnlrozlhajmraPnoa4NCua1i+ivleaV\nsOaNrjMJ5a6M5YWo5q2j56GuDQrmtYvor5XmlbDmja40CeWujOWFqOato+eh\nrg0K5rWL6K+V5pWw5o2uNQnlrozlhajmraPnoa4NCjIuCQ0K5ZCR6YeP6L+Q\n566XDQoNCuOAkOmXrumimOaPj+i/sOOAkQ0KDQrorr7orqHkuIDkuKrnsbvm\nqKHmnb8gVmVjdG9yMkTvvIzooajnpLrkuoznu7TlkJHph4/vvIzljIXlkKsg\neCDlkowgeSDlnZDmoIfvvIjmlK/mjIHku7vmhI/mlbDlgLznsbvlnovvvInj\ngILlrp7njrDku6XkuIvlip/og73vvJoNCg0KLSDph43ovb0gKyDov5Dnrpfn\nrKbvvIzov5Tlm57kuKTkuKrlkJHph4/lnZDmoIfnm7jliqDnmoTnu5Pmnpzj\ngIINCg0KLSDph43ovb0gPT0g6L+Q566X56ym77yM5q+U6L6D5Lik5Liq5ZCR\n6YeP5piv5ZCm55u4562J77yI5rWu54K55pWw5q+U6L6D5YWB6K646K+v5beu\nIDFlLTbvvInjgIINCg0K44CQ6L6T5YWl5b2i5byP44CRDQoNCuesrOS4gOih\njO+8muaVtOaVsCBu77yM6KGo56S65pON5L2c5qyh5pWw44CCDQoNCuaOpeS4\ni+adpeeahCBuIOihjO+8jOavj+ihjOagvOW8j+S4uiA8b3A+IDx4MT4gPHkx\nPiA8eDI+IDx5Mj7vvJoNCg0Kb3Ag5Li6IGFkZO+8iOebuOWKoO+8ieaIliBl\ncXVhbO+8iOavlOi+g++8ieOAgg0KDQp4MSwgeTEg5Li656ys5LiA5Liq5ZCR\n6YeP5Z2Q5qCH77yMeDIsIHkyIOS4uuesrOS6jOS4quWQkemHj+WdkOagh++8\njOWdh+S4uua1rueCueaVsOOAgg0KDQrjgJDovpPlh7rlvaLlvI/jgJENCg0K\n5a+55LqOIGFkZO+8jOi+k+WHuiBBZGQ6ICg8eD4sIDx5PinvvIx4IOWSjCB5\nIOS/neeVmeS4pOS9jeWwj+aVsOOAgg0KDQrlr7nkuo4gZXF1YWzvvIzovpPl\nh7ogRXF1YWw6IDxyZXN1bHQ+77yMcmVzdWx0IOS4uiB0cnVlIOaIliBmYWxz\nZeOAgg0KDQrjgJDmoLfkvovovpPlhaXjgJENCg0KNA0KDQphZGQgMS41IDIu\nNSAzLjUgNC41DQoNCmVxdWFsIDEuMCAyLjAgMS4wIDIuMA0KDQplcXVhbCAx\nLjAgMi4wIDEuMCAyLjAwMDAwMQ0KDQplcXVhbCAxLjAgMi4wIDIuMCAzLjAN\nCg0KDQrjgJDmoLfkvovovpPlh7rjgJENCg0KQWRkOiAoNS4wMCwgNy4wMCkN\nCg0KRXF1YWw6IHRydWUNCg0KRXF1YWw6IHRydWUNCg0KRXF1YWw6IGZhbHNl\n",
  "encoding": "base64",
  "_links": {
    "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/2024%E7%BA%A7C%2B%2B%E5%AE%9E%E9%AA%8C%E6%9C%9F%E6%9C%AB%E8%AF%95%E5%8D%B7.txt?ref=main",
    "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/81f12d359ed37123372d1d872e069ea9085e6415",
    "html": "https://github.com/gubaiovo/JNU-EXAM/blob/main/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/2024%E7%BA%A7C%2B%2B%E5%AE%9E%E9%AA%8C%E6%9C%9F%E6%9C%AB%E8%AF%95%E5%8D%B7.txt"
  }
}







https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/C%2B%2B%E7%BB%83%E4%B9%A0%E9%A2%98(1)(1)_240112_121403(1).pdf?ref=main
{
  "name": "C++练习题(1)(1)_240112_121403(1).pdf",
  "path": "大一下/C++/C++练习题(1)(1)_240112_121403(1).pdf",
  "sha": "148881804a5633526f6c27cab4a915b43a67e7bb",
  "size": 4966133,
  "url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/C%2B%2B%E7%BB%83%E4%B9%A0%E9%A2%98(1)(1)_240112_121403(1).pdf?ref=main",
  "html_url": "https://github.com/gubaiovo/JNU-EXAM/blob/main/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/C%2B%2B%E7%BB%83%E4%B9%A0%E9%A2%98(1)(1)_240112_121403(1).pdf",
  "git_url": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/148881804a5633526f6c27cab4a915b43a67e7bb",
  "download_url": "https://raw.githubusercontent.com/gubaiovo/JNU-EXAM/main/大一下/C++/C++练习题(1)(1)_240112_121403(1).pdf",
  "type": "file",
  "content": "",
  "encoding": "none",
  "_links": {
    "self": "https://api.github.com/repos/gubaiovo/JNU-EXAM/contents/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/C%2B%2B%E7%BB%83%E4%B9%A0%E9%A2%98(1)(1)_240112_121403(1).pdf?ref=main",
    "git": "https://api.github.com/repos/gubaiovo/JNU-EXAM/git/blobs/148881804a5633526f6c27cab4a915b43a67e7bb",
    "html": "https://github.com/gubaiovo/JNU-EXAM/blob/main/%E5%A4%A7%E4%B8%80%E4%B8%8B/C%2B%2B/C%2B%2B%E7%BB%83%E4%B9%A0%E9%A2%98(1)(1)_240112_121403(1).pdf"
  }
}



 */