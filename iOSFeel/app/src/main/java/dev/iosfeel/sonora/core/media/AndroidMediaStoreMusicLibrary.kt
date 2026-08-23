package dev.iosfeel.sonora.core.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidMediaStoreMusicLibrary(
    private val context: Context
) : MusicLibrarySource {

    private val resolver = context.contentResolver

    private val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.IS_MUSIC
    )

    private val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"

    private val selectionArgs = arrayOf(MINIMUM_MUSIC_DURATION_MS.toString())

    override suspend fun getSongs(): List<Song> = withContext(Dispatchers.IO) {
        querySongs()
    }

    private fun querySongs(): List<Song> {
        val songs = mutableListOf<Song>()

        resolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                val song = Song(
                    id = id,
                    title = cursor.getString(titleColumn).cleanMetadata(fallback = "Unknown Song"),
                    artistId = cursor.nullableLong(artistIdColumn),
                    artist = cursor.getString(artistColumn).cleanMetadata(fallback = "Unknown Artist"),
                    albumId = cursor.nullableLong(albumIdColumn),
                    album = cursor.nullableString(albumColumn)?.cleanMetadata(fallback = "Unknown Album"),
                    durationMs = cursor.getLong(durationColumn),
                    trackNumber = cursor.nullableInt(trackColumn)?.normalizeTrackNumber(),
                    year = cursor.nullableInt(yearColumn)?.takeIf { it > 0 },
                    dateAddedSeconds = cursor.getLong(dateAddedColumn),
                    contentUri = ContentUris.withAppendedId(collection, id)
                )

                songs += song
            }
        }

        return songs
    }

    companion object {
        private const val MINIMUM_MUSIC_DURATION_MS = 20_000L // 20 seconds
    }
}

fun Cursor.nullableString(index: Int): String? {
    return if (isNull(index)) null else getString(index)
}

fun Cursor.nullableLong(index: Int): Long? {
    return if (isNull(index)) null else getLong(index)
}

fun Cursor.nullableInt(index: Int): Int? {
    return if (isNull(index)) null else getInt(index)
}

fun String?.cleanMetadata(fallback: String): String {
    val value = this?.trim()
    if (value.isNullOrBlank()) return fallback
    if (value.startsWith("<") && value.endsWith(">")) return fallback
    return value
}

fun Int.normalizeTrackNumber(): Int {
    if (this <= 0) return 0
    return this % 1000
}
