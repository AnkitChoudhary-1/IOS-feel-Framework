package dev.iosfeel.sonora.core.network.ytmusic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeMusicClientTest {

    @Test
    fun testSongItemToDomainConversion() {
        val ytSong = YTSongItem(
            videoId = "dQw4w9WgXcQ",
            title = "Never Gonna Give You Up",
            artist = "Rick Astley",
            album = "Whenever You Need Somebody",
            durationSeconds = 213,
            thumbnailUrl = "https://lh3.googleusercontent.com/test=w544-h544"
        )

        val domainSong = ytSong.toDomainSong()

        assertEquals("Never Gonna Give You Up", domainSong.title)
        assertEquals("Rick Astley", domainSong.artist)
        assertEquals("Whenever You Need Somebody", domainSong.album)
        assertEquals(213000L, domainSong.durationMs)
        assertEquals("dQw4w9WgXcQ", domainSong.remoteId)
        assertEquals("https://lh3.googleusercontent.com/test=w544-h544", domainSong.artworkUrl)
        assertTrue(domainSong.isOnline)
    }

    @Test
    fun testAlbumItemToDomainConversion() {
        val ytAlbum = YTAlbumItem(
            browseId = "MPREb_test123",
            title = "Hit Me Hard and Soft",
            artist = "Billie Eilish",
            year = 2024,
            thumbnailUrl = "https://lh3.googleusercontent.com/album"
        )

        val domainAlbum = ytAlbum.toDomainAlbum()
        assertEquals("Hit Me Hard and Soft", domainAlbum.title)
        assertEquals("Billie Eilish", domainAlbum.artist)
        assertEquals(2024, domainAlbum.year)
    }
}
