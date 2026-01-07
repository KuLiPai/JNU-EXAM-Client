package jnu.kulipai.exam.core.common

fun String.isBlankJson(): Boolean =
    this.isBlank() || this == "null"
