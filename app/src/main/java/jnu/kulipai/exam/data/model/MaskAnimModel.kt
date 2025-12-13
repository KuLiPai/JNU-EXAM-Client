package jnu.kulipai.exam.data.model

enum class MaskAnimModel {
    EXPEND,
    SHRINK,
}
typealias MaskAnimActive = (MaskAnimModel, Float, Float) -> Unit