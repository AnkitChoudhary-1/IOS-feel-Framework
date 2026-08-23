package dev.iosfeel.sonora.core.model

import androidx.media3.common.Player

enum class RepeatMode {
    Off,
    All,
    One
}

fun RepeatMode.toMedia3(): Int {
    return when (this) {
        RepeatMode.Off -> Player.REPEAT_MODE_OFF
        RepeatMode.All -> Player.REPEAT_MODE_ALL
        RepeatMode.One -> Player.REPEAT_MODE_ONE
    }
}

fun Int.toDomainRepeatMode(): RepeatMode {
    return when (this) {
        Player.REPEAT_MODE_ONE -> RepeatMode.One
        Player.REPEAT_MODE_ALL -> RepeatMode.All
        else -> RepeatMode.Off
    }
}
