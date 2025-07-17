package jnu.kulipai.exam.data.model

data class DirNode(
    val name: String,
    val path: String,
    val files: MutableList<FileItem> = mutableListOf(),
    val children: MutableMap<String, DirNode> = mutableMapOf()
)