package dev.iosfeel.sonora.core.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long = 0,
    val artworkUri: Uri? = null,
    val songCount: Int = 0,
    val year: Int = 0
)

data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int = 0,
    val songCount: Int = 0
)

data class Genre(
    val id: Long,
    val name: String,
    val songCount: Int = 0
)
