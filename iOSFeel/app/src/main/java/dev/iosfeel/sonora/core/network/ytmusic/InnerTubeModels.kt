package dev.iosfeel.sonora.core.network.ytmusic

import android.net.Uri
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song

data class YTSongItem(
    val videoId: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val album: String? = null,
    val albumId: String? = null,
    val durationSeconds: Long = 0,
    val durationText: String = "",
    val thumbnailUrl: String? = null,
    val isExplicit: Boolean = false
) {
    fun toDomainSong(): Song {
        val hashId = (videoId.hashCode().toLong() and 0x7FFFFFFF) or 0x4000000000000000L
        return Song(
            id = hashId,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationSeconds * 1000L,
            contentUri = null,
            remoteId = videoId,
            artworkUrl = thumbnailUrl,
            isOnline = true
        )
    }
}

data class YTArtistItem(
    val browseId: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val subscriberCountText: String? = null
) {
    fun toDomainArtist(): Artist {
        val hashId = (browseId.hashCode().toLong() and 0x7FFFFFFF) or 0x4000000000000000L
        return Artist(
            id = hashId,
            name = name,
            albums = emptyList()
        )
    }
}

data class YTAlbumItem(
    val browseId: String,
    val title: String,
    val artist: String,
    val year: Int? = null,
    val thumbnailUrl: String? = null
) {
    fun toDomainAlbum(): Album {
        val hashId = (browseId.hashCode().toLong() and 0x7FFFFFFF) or 0x4000000000000000L
        return Album(
            id = hashId,
            title = title,
            artist = artist,
            year = year,
            songs = emptyList()
        )
    }
}

data class YTPlaylistItem(
    val browseId: String,
    val title: String,
    val author: String? = null,
    val trackCount: Int = 0,
    val thumbnailUrl: String? = null
) {
    fun toDomainPlaylist(): Playlist {
        val hashId = (browseId.hashCode().toLong() and 0x7FFFFFFF) or 0x4000000000000000L
        return Playlist(
            id = hashId,
            name = title,
            songs = emptyList()
        )
    }
}

data class YTSearchResult(
    val songs: List<YTSongItem> = emptyList(),
    val artists: List<YTArtistItem> = emptyList(),
    val albums: List<YTAlbumItem> = emptyList(),
    val playlists: List<YTPlaylistItem> = emptyList()
)

data class YTExploreFeed(
    val trendingSongs: List<YTSongItem> = emptyList(),
    val newReleases: List<YTAlbumItem> = emptyList(),
    val charts: List<YTSongItem> = emptyList()
)

data class YTAudioStream(
    val url: String,
    val mimeType: String,
    val bitrate: Int,
    val durationMs: Long = 0,
    val contentLength: Long = 0
)
