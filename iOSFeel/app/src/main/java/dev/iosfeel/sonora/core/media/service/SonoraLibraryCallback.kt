package dev.iosfeel.sonora.core.media.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import dev.iosfeel.sonora.core.di.SonoraContainer
import dev.iosfeel.sonora.core.media.library.MediaLibraryIds
import dev.iosfeel.sonora.core.media.library.SonoraLibraryTree
import dev.iosfeel.sonora.core.media.mapper.toMediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SonoraLibraryCallback(
    private val context: Context
) : MediaLibrarySession.Callback {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(
            LibraryResult.ofItem(
                SonoraLibraryTree.rootMediaItem(),
                params
            )
        )
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return when (parentId) {
            MediaLibraryIds.Root -> {
                Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        ImmutableList.of(SonoraLibraryTree.songsCategory()),
                        params
                    )
                )
            }
            MediaLibraryIds.Songs -> {
                val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
                scope.launch(Dispatchers.IO) {
                    try {
                        val container = SonoraContainer.getInstance(context)
                        val songs = container.musicRepository.loadLibrary().songs
                        val mediaItems = songs.map { it.toMediaItem() }
                        future.set(
                            LibraryResult.ofItemList(
                                ImmutableList.copyOf(mediaItems),
                                params
                            )
                        )
                    } catch (e: Exception) {
                        future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                }
                future
            }
            else -> {
                Futures.immediateFuture(
                    LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                )
            }
        }
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return when (mediaId) {
            MediaLibraryIds.Root -> Futures.immediateFuture(
                LibraryResult.ofItem(SonoraLibraryTree.rootMediaItem(), null)
            )
            MediaLibraryIds.Songs -> Futures.immediateFuture(
                LibraryResult.ofItem(SonoraLibraryTree.songsCategory(), null)
            )
            else -> {
                val future = SettableFuture.create<LibraryResult<MediaItem>>()
                scope.launch(Dispatchers.IO) {
                    try {
                        val container = SonoraContainer.getInstance(context)
                        val songId = mediaId.toLongOrNull()
                        val song = container.musicRepository.loadLibrary().songs.firstOrNull { it.id == songId }
                        if (song != null) {
                            future.set(LibraryResult.ofItem(song.toMediaItem(), null))
                        } else {
                            future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                        }
                    } catch (e: Exception) {
                        future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                }
                future
            }
        }
    }
}
