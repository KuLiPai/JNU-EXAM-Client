package jnu.kulipai.exam.core.file

class FileSizeFormatter {
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
}