package jnu.kulipai.exam.data.model

sealed interface ChangeSourceEvent {
    data class SourceChangeSuccess(val message: String) : ChangeSourceEvent
    data class SourceChangeFailed(val message: String) : ChangeSourceEvent
}
