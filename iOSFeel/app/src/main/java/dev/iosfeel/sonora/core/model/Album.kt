package dev.iosfeel.sonora.core.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Immutable

@Immutable
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long? = null,
    val songs: List<Song> = emptyList(),
    val year: Int? = null
) {
    val songCount: Int
        get() = songs.size

    fun contentUri(): Uri {
        return ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            id
        )
    }
}
