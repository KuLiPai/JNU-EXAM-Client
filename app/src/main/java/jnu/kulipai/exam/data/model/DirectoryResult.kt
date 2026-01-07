package jnu.kulipai.exam.data.model

sealed class DirectoryResult {
    data class Success(val tree: DirNode) : DirectoryResult()
    data class Error(val reason: DirectoryError) : DirectoryResult()
}

sealed class DirectoryError {
    object EmptyJson : DirectoryError()
    object NetworkFailed : DirectoryError()
    object InvalidJson : DirectoryError()
    object BuildFailed : DirectoryError()
}
