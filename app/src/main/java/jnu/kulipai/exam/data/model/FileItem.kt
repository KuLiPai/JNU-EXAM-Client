package jnu.kulipai.exam.data.model

data class FileItem(
    val name: String,
    val path: String,
    val size: Long,
    val url: String = ""
)
