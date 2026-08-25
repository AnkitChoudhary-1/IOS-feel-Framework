package dev.iosfeel.sonora.core.media.mapper

import android.content.ContentUris
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dev.iosfeel.sonora.core.model.Song

fun Song.toMediaItem(): MediaItem {
    val albumArtUri = if (artworkUrl != null) {
        android.net.Uri.parse(artworkUrl)
    } else {
        albumId?.let {
            ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it)
        }
    }

    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setArtworkUri(albumArtUri)
        .setIsPlayable(true)
        .setIsBrowsable(false)
        .build()

    val uri = contentUri ?: if (isOnline && remoteId != null) {
        android.net.Uri.parse("https://www.youtube.com/watch?v=$remoteId")
    } else {
        ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id
        )
    }

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(metadata)
        .build()
}
