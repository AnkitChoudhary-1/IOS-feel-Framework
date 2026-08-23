package dev.iosfeel.sonora.core.repository

import android.content.ContentResolver
import dev.iosfeel.sonora.core.media.MusicLibrarySource
import dev.iosfeel.sonora.core.media.observeMusicChanges
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class DefaultMusicLibraryRepository(
    private val source: MusicLibrarySource,
    private val contentResolver: ContentResolver? = null
) : MusicLibraryRepository {

    override suspend fun loadLibrary(): MusicLibrary {
        val songs = source.getSongs()
        val albums = buildAlbums(songs)
        val artists = buildArtists(albums)

        return MusicLibrary(
            songs = songs,
            albums = albums,
            artists = artists
        )
    }

    override fun observeLibrary(): Flow<MusicLibrary> = flow {
        emit(loadLibrary())
        if (contentResolver != null) {
            contentResolver.observeMusicChanges()
                .debounce(750)
                .collect {
                    emit(loadLibrary())
                }
        }
    }

    fun buildAlbums(songs: List<Song>): List<Album> {
        return songs
            .filter { it.albumId != null }
            .groupBy { it.albumId!! }
            .map { (albumId, albumSongs) ->
                val first = albumSongs.first()

                Album(
                    id = albumId,
                    title = first.album ?: "Unknown Album",
                    artist = first.artist,
                    artistId = first.artistId,
                    songs = albumSongs.sortedBy { it.trackNumber ?: Int.MAX_VALUE },
                    year = albumSongs.mapNotNull { it.year }.minOrNull()
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    fun buildArtists(albums: List<Album>): List<Artist> {
        return albums
            .filter { it.artistId != null }
            .groupBy { it.artistId!! }
            .map { (artistId, artistAlbums) ->
                Artist(
                    id = artistId,
                    name = artistAlbums.first().artist,
                    albums = artistAlbums,
                    songCount = artistAlbums.sumOf { it.songCount }
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}
