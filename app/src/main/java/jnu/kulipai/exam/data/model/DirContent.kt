package jnu.kulipai.exam.data.model

data class DirContent(
    val files: List<FileItem>,
    val subDirs: List<DirNode>
)