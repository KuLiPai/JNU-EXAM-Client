package jnu.kulipai.exam.core.file

class PathUtil {
    fun dotDot(path: String): String {
        val trimmed = path.trimEnd('/')
        val lastSlashIndex = trimmed.lastIndexOf('/')
        if (lastSlashIndex <= 0) return "/"

        return trimmed.take(lastSlashIndex + 1)
    }
}