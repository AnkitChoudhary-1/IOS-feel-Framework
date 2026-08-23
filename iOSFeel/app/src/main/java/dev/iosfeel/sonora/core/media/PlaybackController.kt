package dev.iosfeel.sonora.core.media

import dev.iosfeel.sonora.core.model.PlaybackState
import dev.iosfeel.sonora.core.model.RepeatMode
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlaybackController {
    val state: StateFlow<PlaybackState>

    fun play()
    fun pause()
    fun togglePlayPause()
    fun playSong(song: Song, queue: List<Song>)
    fun playQueue(songs: List<Song>, startIndex: Int = 0)
    fun seekTo(positionMs: Long)
    fun seekToNext()
    fun seekToPrevious()
    fun setShuffle(enabled: Boolean)
    fun setRepeatMode(mode: RepeatMode)
    fun skipToQueueItem(index: Int)
}
