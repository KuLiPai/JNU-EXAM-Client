package jnu.kulipai.exam.platform

interface PlatformService {
    fun showToast(message: String)

    // For opening files (e.g. via external apps)
    fun openFile(relativePath: String, absolutePath: String = "")

    // For file export dialogs
    fun prepareExport(path: String, onLaunch: (String) -> Unit)

    // For URI-based export (Android specific mostly but abstracted)
    fun exportFileToUri(sourcePath: String, targetUriStr: String): Result<Unit>

    // Helper to get cache/files dir path if needed for non-Android logic, or just handle inside impl
}