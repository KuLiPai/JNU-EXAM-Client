package jnu.kulipai.exam.util

import android.content.Context
import org.json.JSONObject
import java.io.File

object FileManager {

    data class FileItem(
        val name: String,
        val path: String,
        val size: Long,
        val github_raw_url: String,
        val gitee_raw_url: String
    )

    data class DirNode(
        val name: String,
        val path: String,
        val files: MutableList<FileItem> = mutableListOf(),
        val children: MutableMap<String, DirNode> = mutableMapOf()
    )

    fun write(context: Context, filename: String, content: String) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { output ->
            output.write(content.toByteArray())
        }
    }

    fun read(context: Context, filename: String): String? {
        return try {
            context.openFileInput(filename).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // ✅ 判断内部文件是否存在
    fun exists(context: Context, filename: String): Boolean {
        val file = File(context.filesDir, filename)
        return file.exists()
    }

    fun buildDirectoryTree(json: String): DirNode {
        val root = DirNode(name = "/", path = "/")
        val jsonObject = JSONObject(json)
        val dirsArray = jsonObject.getJSONArray("dirs")

        for (i in 0 until dirsArray.length()) {
            val dirObj = dirsArray.getJSONObject(i)
            val dirPath = dirObj.getString("path")
            val dirName = dirObj.getString("name")
            val filesArray = dirObj.getJSONArray("files")

            // 分割路径并插入节点
            val pathSegments = dirPath.split("/")

            var currentNode = root
            for (segment in pathSegments) {
                currentNode = currentNode.children.getOrPut(segment) {
                    DirNode(name = segment, path = if (currentNode.path == "/") segment else "${currentNode.path}/$segment")
                }
            }

            // 添加文件
            for (j in 0 until filesArray.length()) {
                val fileObj = filesArray.getJSONObject(j)
                currentNode.files.add(
                    FileItem(
                        name = fileObj.getString("name"),
                        path = fileObj.getString("path"),
                        size = fileObj.getLong("size"),
                        github_raw_url = fileObj.getString("github_raw_url"),
                        gitee_raw_url = fileObj.getString("gitee_raw_url")
                    )
                )
            }
        }

        return root
    }

    data class DirContent(
        val files: List<FileItem>,
        val subDirs: List<DirNode>
    )

    fun getDirContent(root: DirNode, targetPath: String): DirContent? {
        val normalizedPath = targetPath.trim('/').takeIf { it.isNotEmpty() } ?: "/"
        if (normalizedPath == "/") return DirContent(root.files, root.children.values.toList())

        val segments = normalizedPath.split("/")
        var currentNode = root
        for (segment in segments) {
            currentNode = currentNode.children[segment] ?: return null
        }
        return DirContent(currentNode.files, currentNode.children.values.toList())
    }


}