package dev.iosfeel.sonora.core.media.controller

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import dev.iosfeel.sonora.core.database.dao.HistoryDao
import dev.iosfeel.sonora.core.database.entity.PlaybackHistoryEntity
import dev.iosfeel.sonora.core.media.PlaybackController
import dev.iosfeel.sonora.core.media.history.PlaybackHistoryTracker
import dev.iosfeel.sonora.core.media.mapper.toMediaItem
import dev.iosfeel.sonora.core.model.PlaybackState
import dev.iosfeel.sonora.core.model.RepeatMode
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.toDomainError
import dev.iosfeel.sonora.core.model.toDomainRepeatMode
import dev.iosfeel.sonora.core.model.toMedia3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SonoraPlaybackController(
    private val connection: MediaControllerConnection,
    private val historyDao: HistoryDao? = null,
    private val historyTracker: PlaybackHistoryTracker? = null
) : PlaybackController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var currentQueue: List<Song> = emptyList()
    private var positionJob: Job? = null
    private var activeSongId: Long? = null

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            synchronizeState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startPositionUpdates()
            } else {
                stopPositionUpdates()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.update { it.copy(error = error.toDomainError()) }
        }
    }

    suspend fun connect() {
        if (controller != null) return

        val mediaController = connection.connect()
        controller = mediaController
        mediaController.addListener(playerListener)
        synchronizeState()
    }

    override fun play() {
        controller?.play()
    }

    override fun pause() {
        controller?.pause()
    }

    override fun togglePlayPause() {
        controller?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    override fun playSong(song: Song, queue: List<Song>) {
        val player = controller ?: return
        val targetQueue = if (queue.isNotEmpty()) queue else listOf(song)
        val index = targetQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        currentQueue = targetQueue
        val mediaItems = targetQueue.map { it.toMediaItem() }

        player.setMediaItems(mediaItems, index, 0L)
        player.prepare()
        player.play()

        activeSongId = song.id
        recordHistory(song.id)
        scope.launch {
            historyTracker?.onSongStarted(song)
        }
    }

    override fun playQueue(songs: List<Song>, startIndex: Int) {
        val player = controller ?: return
        if (songs.isEmpty()) return

        val safeIndex = startIndex.coerceIn(songs.indices)
        currentQueue = songs
        val mediaItems = songs.map { it.toMediaItem() }

        player.setMediaItems(mediaItems, safeIndex, 0L)
        player.prepare()
        player.play()

        val startingSong = songs.getOrNull(safeIndex)
        activeSongId = startingSong?.id
        startingSong?.let { song ->
            recordHistory(song.id)
            scope.launch {
                historyTracker?.onSongStarted(song)
            }
        }
    }

    override fun seekTo(positionMs: Long) {
        val player = controller ?: return
        val duration = player.duration
        val safePosition = if (duration > 0) {
            positionMs.coerceIn(0L, duration)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        player.seekTo(safePosition)
    }

    override fun seekToNext() {
        controller?.let { player ->
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
            }
        }
    }

    override fun seekToPrevious() {
        controller?.let { player ->
            // If more than 3 seconds in, restart current track
            if (player.currentPosition > 3000L) {
                player.seekTo(0L)
            } else if (player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem()
            } else {
                player.seekTo(0L)
            }
        }
    }

    override fun setShuffle(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
        _state.update { it.copy(shuffleEnabled = enabled) }
    }

    override fun setRepeatMode(mode: RepeatMode) {
        controller?.repeatMode = mode.toMedia3()
        _state.update { it.copy(repeatMode = mode) }
    }

    override fun skipToQueueItem(index: Int) {
        val player = controller ?: return
        if (index in 0 until player.mediaItemCount) {
            player.seekTo(index, 0L)
        }
    }

    private fun synchronizeState() {
        val player = controller ?: return

        val currentIndex = player.currentMediaItemIndex
        val currentSong = currentQueue.getOrNull(currentIndex)

        if (currentSong?.id != activeSongId && currentSong != null) {
            activeSongId = currentSong.id
            recordHistory(currentSong.id)
            scope.launch {
                historyTracker?.onSongStarted(currentSong)
            }
        }

        _state.update {
            it.copy(
                currentSong = currentSong,
                isPlaying = player.isPlaying,
                isLoading = player.playbackState == Player.STATE_BUFFERING,
                positionMs = player.currentPosition,
                durationMs = if (player.duration > 0) player.duration else (currentSong?.durationMs ?: 0L),
                bufferedPositionMs = player.bufferedPosition,
                queue = currentQueue,
                currentQueueIndex = currentIndex,
                shuffleEnabled = player.shuffleModeEnabled,
                repeatMode = player.repeatMode.toDomainRepeatMode(),
                playbackSpeed = player.playbackParameters.speed,
                error = null
            )
        }
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                controller?.let { player ->
                    val pos = player.currentPosition
                    val dur = player.duration
                    _state.update {
                        it.copy(
                            positionMs = pos,
                            bufferedPositionMs = player.bufferedPosition
                        )
                    }
                    activeSongId?.let { songId ->
                        historyTracker?.onPositionUpdated(songId, pos, dur)
                    }
                }
                delay(250L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun recordHistory(songId: Long) {
        if (historyDao == null) return
        scope.launch(Dispatchers.IO) {
            val existing = historyDao.getHistoryForSong(songId)
            val count = (existing?.playCount ?: 0) + 1
            historyDao.recordPlay(
                PlaybackHistoryEntity(
                    songId = songId,
                    playedAt = System.currentTimeMillis(),
                    playCount = count
                )
            )
        }
    }
}
