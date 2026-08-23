package dev.iosfeel.sonora.core.media.library

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

object SonoraLibraryTree {

    fun rootMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(MediaLibraryIds.Root)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Sonora")
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }

    fun songsCategory(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(MediaLibraryIds.Songs)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Songs")
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }
}
