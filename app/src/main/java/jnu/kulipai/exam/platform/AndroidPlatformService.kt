package jnu.kulipai.exam.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import jnu.kulipai.exam.platform.PlatformService
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import androidx.core.net.toUri

class AndroidPlatformService(
    private val context: Context
) : PlatformService {

    private var exportLauncher: ActivityResultLauncher<String>? = null
    
    // This needs to be set from the Activity/Fragment
    fun setExportLauncher(launcher: ActivityResultLauncher<String>) {
        this.exportLauncher = launcher
    }

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun openFile(relativePath: String, absolutePath: String) {
        val file = if (absolutePath.isNotEmpty()) {
            File(absolutePath)
        } else {
            File(context.getExternalFilesDir(""), relativePath)
        }

        if (!file.exists()) {
            // Alternatively, could define a custom exception
            throw IllegalArgumentException("File does not exist: $relativePath")
        }

        val extension = file.extension.lowercase()
        val fallbackMimeTypes = mapOf(
            "md" to "text/plain",
            "markdown" to "text/plain",
            "lua" to "text/plain",
            "log" to "text/plain",
            "json" to "application/json",
            "xml" to "text/xml",
            "csv" to "text/csv"
        )

        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
            ?: fallbackMimeTypes[extension]
            ?: "application/octet-stream"

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "选择应用打开文件").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
             showToast("找不到应用打开此文件，路径: $relativePath，类型: $mimeType")
        }
    }

    override fun prepareExport(path: String, onLaunch: (String) -> Unit) {
         val file = File(path)
         // Logic that was previously in ViewModel to guess mime or just launch
         // We'll rely on the caller or just launch simply.
         // If specific mime logic was needed for the launcher, it might need to be passed or handled here if we had the launcher config access.
         // For now, assuming standard string contract for launcher.
         
         // In original code: exportLauncher?.launch(file.name)
         // We can use the callback to notify ViewModel if needed, or if we hold the launcher here:
         exportLauncher?.launch(file.name)
    }

    override fun exportFileToUri(sourcePath: String, targetUriStr: String): Result<Unit> {
        return try {
             val file = File(context.getExternalFilesDir(""), sourcePath)
             if (!file.exists()) {
                 return Result.failure(Exception("源文件不存在: $sourcePath"))
             }
             
             val targetUri = targetUriStr.toUri()
             val inputStream = FileInputStream(file)
             val outputStream: OutputStream? = context.contentResolver.openOutputStream(targetUri)

             if (outputStream == null) {
                 return Result.failure(Exception("无法打开导出目标"))
             }

             inputStream.use { input ->
                 outputStream.use { output ->
                     input.copyTo(output)
                 }
             }
             Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
