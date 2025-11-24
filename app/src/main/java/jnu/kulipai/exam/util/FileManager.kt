package jnu.kulipai.exam.util

import android.content.Context
import jnu.kulipai.exam.data.model.DirNode
import jnu.kulipai.exam.data.model.FileItem
import org.json.JSONObject
import java.io.File

object FileManager {

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
                        gitee_raw_url = fileObj.getString("gitee_raw_url"),
                        cf_url = fileObj.getString("cf_url")
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

    /**
     * 递归搜索文件和文件夹名称中包含查询字符串的文件。
     *
     * @param rootNode 目录树的根节点。
     * @param query 要搜索的字符串。
     * @return 返回一个包含所有匹配文件的列表 (List<FileItem>)。
     */
    fun searchFiles(rootNode: DirNode, query: String): List<FileItem> {
        val matchingFiles = mutableListOf<FileItem>()
        val searchQuery = query.lowercase() // 忽略大小写进行搜索

        // 定义一个内部递归函数
        fun recursiveSearch(node: DirNode) {
            // 1. 检查当前文件夹名称是否匹配
            val isDirNameMatch = node.name.lowercase().contains(searchQuery)

            // 2. 检查当前节点下的文件
            for (file in node.files) {
                // 如果文件夹名称匹配，则该文件夹下所有文件都加入列表
                // 或者文件名自身匹配
                if (isDirNameMatch || file.name.lowercase().contains(searchQuery)) {
                    matchingFiles.add(file)
                }
            }

            // 3. 递归地搜索子文件夹
            for (childNode in node.children.values) {
                recursiveSearch(childNode)
            }
        }

        // 从根节点开始执行搜索
        recursiveSearch(rootNode)

        // 返回去重后的结果列表
        return matchingFiles.distinct()
    }

}