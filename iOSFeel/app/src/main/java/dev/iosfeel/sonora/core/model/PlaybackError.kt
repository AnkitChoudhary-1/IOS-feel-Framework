package dev.iosfeel.sonora.core.model

import androidx.media3.common.PlaybackException

sealed interface PlaybackError {
    data object FileUnavailable : PlaybackError
    data object UnsupportedFormat : PlaybackError
    data class Unknown(val message: String?) : PlaybackError
}

fun PlaybackException.toDomainError(): PlaybackError {
    return when (errorCode) {
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> PlaybackError.FileUnavailable
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> PlaybackError.UnsupportedFormat
        else -> PlaybackError.Unknown(message)
    }
}
