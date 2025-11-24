package jnu.kulipai.exam.data.model

data class FileItem(
    val name: String,
    val path: String,
    val size: Long,
    val github_raw_url: String,
    val gitee_raw_url: String,
    val cf_url: String
)
