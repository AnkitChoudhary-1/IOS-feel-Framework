package dev.iosfeel.sonora.core.network.ytmusic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class YouTubeMusicClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val BASE_URL = "https://music.youtube.com/youtubei/v1"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val WEB_REMIX_VERSION = "1.20240101.01.00"
        private const val USER_AGENT_WEB =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    private fun createWebContextJson(): String {
        return """
            {
                "client": {
                    "clientName": "WEB_REMIX",
                    "clientVersion": "$WEB_REMIX_VERSION",
                    "hl": "en",
                    "gl": "US"
                }
            }
        """.trimIndent()
    }

    suspend fun search(query: String): YTSearchResult = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext YTSearchResult()

        val payload = """
            {
                "context": ${createWebContextJson()},
                "query": ${JSONObject.quote(trimmed)}
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$BASE_URL/search?prettyPrint=false")
            .header("Content-Type", "application/json")
            .header("User-Agent", USER_AGENT_WEB)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com")
            .header("X-YouTube-Client-Name", "67")
            .header("X-YouTube-Client-Version", WEB_REMIX_VERSION)
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext YTSearchResult()
                val bodyStr = response.body?.string() ?: return@withContext YTSearchResult()
                val json = JSONObject(bodyStr)
                return@withContext parseSearchResults(json)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            YTSearchResult()
        }
    }

    suspend fun getSearchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val payload = """
            {
                "context": ${createWebContextJson()},
                "input": ${JSONObject.quote(trimmed)}
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$BASE_URL/music/get_search_suggestions")
            .header("Content-Type", "application/json")
            .header("User-Agent", USER_AGENT_WEB)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com")
            .header("X-YouTube-Client-Name", "67")
            .header("X-YouTube-Client-Version", WEB_REMIX_VERSION)
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(bodyStr)
                val suggestions = mutableListOf<String>()

                val contents = json.optJSONArray("contents") ?: return@withContext emptyList()
                for (i in 0 until contents.length()) {
                    val section = contents.optJSONObject(i)?.optJSONObject("searchSuggestionsSectionRenderer")
                    val items = section?.optJSONArray("contents") ?: continue
                    for (j in 0 until items.length()) {
                        val renderer = items.optJSONObject(j)?.optJSONObject("searchSuggestionRenderer")
                        val runs = renderer?.optJSONObject("suggestion")?.optJSONArray("runs")
                        if (runs != null && runs.length() > 0) {
                            val text = (0 until runs.length()).joinToString("") { runs.optJSONObject(it)?.optString("text", "") ?: "" }
                            if (text.isNotBlank()) suggestions.add(text)
                        }
                    }
                }
                return@withContext suggestions
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getExplore(): YTExploreFeed = withContext(Dispatchers.IO) {
        val payload = """
            {
                "context": ${createWebContextJson()},
                "browseId": "FEmusic_home"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$BASE_URL/browse?prettyPrint=false")
            .header("Content-Type", "application/json")
            .header("User-Agent", USER_AGENT_WEB)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com")
            .header("X-YouTube-Client-Name", "67")
            .header("X-YouTube-Client-Version", WEB_REMIX_VERSION)
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    if (!bodyStr.isNullOrBlank()) {
                        val json = JSONObject(bodyStr)
                        val feed = parseExploreFeed(json)
                        if (feed.trendingSongs.isNotEmpty()) return@withContext feed
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to trending search
        }

        // Fallback to top charts search
        val searchFallback = search("Top Hits")
        return@withContext YTExploreFeed(
            trendingSongs = searchFallback.songs.take(15),
            newReleases = searchFallback.albums.take(10),
            charts = searchFallback.songs.drop(15).take(15)
        )
    }

    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        if (videoId.isBlank()) return@withContext null

        // 1. Direct Invidious/Piped mirrors for high-speed direct Opus/AAC streams
        val pipedMirrors = listOf(
            "https://pipedapi.kavin.rocks/streams/$videoId",
            "https://api.piped.privacy.com.de/streams/$videoId",
            "https://pipedapi.tokhmi.xyz/streams/$videoId"
        )

        for (mirror in pipedMirrors) {
            try {
                val req = Request.Builder()
                    .url(mirror)
                    .header("User-Agent", USER_AGENT_WEB)
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: return@use
                        val json = JSONObject(body)
                        val audioStreams = json.optJSONArray("audioStreams")
                        if (audioStreams != null && audioStreams.length() > 0) {
                            var bestUrl: String? = null
                            var highestBitrate = 0
                            for (i in 0 until audioStreams.length()) {
                                val s = audioStreams.optJSONObject(i) ?: continue
                                val url = s.optString("url", "")
                                val bitrate = s.optInt("bitrate", 0)
                                if (url.isNotBlank() && bitrate > highestBitrate) {
                                    highestBitrate = bitrate
                                    bestUrl = url
                                }
                            }
                            if (bestUrl != null) return@withContext bestUrl
                        }
                    }
                }
            } catch (e: Exception) {
                // Continue to next mirror
            }
        }

        // 2. Direct Web format fallback
        val payload = """
            {
                "context": ${createWebContextJson()},
                "videoId": ${JSONObject.quote(videoId)}
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$BASE_URL/player?prettyPrint=false")
            .header("Content-Type", "application/json")
            .header("User-Agent", USER_AGENT_WEB)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com")
            .header("X-YouTube-Client-Name", "67")
            .header("X-YouTube-Client-Version", WEB_REMIX_VERSION)
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyStr)

                val streamingData = json.optJSONObject("streamingData") ?: return@withContext null
                val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats") ?: return@withContext null

                var bestUrl: String? = null
                var highestBitrate = 0

                for (i in 0 until adaptiveFormats.length()) {
                    val format = adaptiveFormats.optJSONObject(i) ?: continue
                    val mimeType = format.optString("mimeType", "")
                    if (mimeType.startsWith("audio/")) {
                        val bitrate = format.optInt("bitrate", 0)
                        val url = format.optString("url", "")
                        if (url.isNotBlank() && bitrate > highestBitrate) {
                            highestBitrate = bitrate
                            bestUrl = url
                        }
                    }
                }

                return@withContext bestUrl
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseSearchResults(json: JSONObject): YTSearchResult {
        val songs = mutableListOf<YTSongItem>()
        val artists = mutableListOf<YTArtistItem>()
        val albums = mutableListOf<YTAlbumItem>()
        val playlists = mutableListOf<YTPlaylistItem>()

        fun extractFromItem(renderer: JSONObject) {
            // 1. VideoId
            var videoId: String? = renderer.optJSONObject("playlistItemData")?.optString("videoId")
            if (videoId.isNullOrBlank()) {
                videoId = renderer.optJSONObject("navigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")
            }
            if (videoId.isNullOrBlank()) {
                videoId = renderer.optJSONObject("onTap")?.optJSONObject("watchEndpoint")?.optString("videoId")
            }

            // 2. Title
            var title = ""
            val flexColumns = renderer.optJSONArray("flexColumns")
            if (flexColumns != null && flexColumns.length() > 0) {
                val col0 = flexColumns.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                val titleRuns = col0?.optJSONObject("text")?.optJSONArray("runs")
                title = (0 until (titleRuns?.length() ?: 0)).joinToString("") { titleRuns?.optJSONObject(it)?.optString("text", "") ?: "" }
                if (videoId.isNullOrBlank() && titleRuns != null && titleRuns.length() > 0) {
                    videoId = titleRuns.optJSONObject(0)?.optJSONObject("navigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")
                }
            } else {
                val titleObj = renderer.optJSONObject("title")
                val titleRuns = titleObj?.optJSONArray("runs")
                title = (0 until (titleRuns?.length() ?: 0)).joinToString("") { titleRuns?.optJSONObject(it)?.optString("text", "") ?: "" }
                if (videoId.isNullOrBlank() && titleRuns != null && titleRuns.length() > 0) {
                    videoId = titleRuns.optJSONObject(0)?.optJSONObject("navigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")
                }
            }

            // 3. Subtitle / Artist / Duration
            var artist = "Unknown Artist"
            var artistId: String? = null
            var album: String? = null
            var durationSec = 0L

            val subRuns = if (flexColumns != null && flexColumns.length() > 1) {
                flexColumns.optJSONObject(1)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")?.optJSONObject("text")?.optJSONArray("runs")
            } else {
                renderer.optJSONObject("subtitle")?.optJSONArray("runs")
            }

            if (subRuns != null && subRuns.length() > 0) {
                val textParts = mutableListOf<String>()
                for (k in 0 until subRuns.length()) {
                    val r = subRuns.optJSONObject(k) ?: continue
                    val t = r.optString("text", "")
                    val bId = r.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")?.optString("browseId")
                    if (!bId.isNullOrBlank() && (bId.startsWith("UC") || bId.startsWith("FEmusic_library_privately_owned_artist"))) {
                        artistId = bId
                        artist = t
                    }
                    if (t.isNotBlank() && t != " • ") {
                        textParts.add(t)
                    }
                }
                if (artist == "Unknown Artist" && textParts.isNotEmpty()) {
                    val candidate = textParts.firstOrNull { it !in listOf("Song", "Video", "Single", "Album", "EP", "Playlist", "Artist") && !it.contains("views") && !it.contains("plays") && !it.contains(":") }
                    if (candidate != null) artist = candidate
                }
                val albumCandidate = textParts.firstOrNull { it != artist && it !in listOf("Song", "Video", "Single", "Album", "EP", "Playlist", "Artist") && !it.contains("views") && !it.contains("plays") && !it.contains(":") }
                if (albumCandidate != null) album = albumCandidate

                val lastPart = textParts.lastOrNull() ?: ""
                if (lastPart.contains(":")) {
                    durationSec = parseDurationToSeconds(lastPart)
                }
            }

            // 4. Thumbnail
            val thumbObj = renderer.optJSONObject("thumbnail")?.optJSONObject("musicThumbnailRenderer")
                ?: renderer.optJSONObject("thumbnail")
            val thumbs = thumbObj?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                ?: thumbObj?.optJSONArray("thumbnails")
            val thumbnailUrl = if (thumbs != null && thumbs.length() > 0) {
                thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")
            } else null

            if (!videoId.isNullOrBlank() && title.isNotBlank()) {
                songs.add(
                    YTSongItem(
                        videoId = videoId,
                        title = title.trim(),
                        artist = artist.trim(),
                        artistId = artistId,
                        album = album?.trim(),
                        durationSeconds = durationSec,
                        thumbnailUrl = thumbnailUrl
                    )
                )
            }
        }

        fun extractFromCardShelf(renderer: JSONObject) {
            val titleRuns = renderer.optJSONObject("title")?.optJSONArray("runs")
            val title = (0 until (titleRuns?.length() ?: 0)).joinToString("") { titleRuns?.optJSONObject(it)?.optString("text", "") ?: "" }

            var videoId = renderer.optJSONObject("onTap")?.optJSONObject("watchEndpoint")?.optString("videoId")
            if (videoId.isNullOrBlank()) {
                videoId = titleRuns?.optJSONObject(0)?.optJSONObject("navigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")
            }

            var artist = "Unknown Artist"
            var artistId: String? = null
            val subRuns = renderer.optJSONObject("subtitle")?.optJSONArray("runs")
            if (subRuns != null) {
                for (k in 0 until subRuns.length()) {
                    val r = subRuns.optJSONObject(k) ?: continue
                    val t = r.optString("text", "")
                    val bId = r.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")?.optString("browseId")
                    if (!bId.isNullOrBlank() && bId.startsWith("UC")) {
                        artistId = bId
                        artist = t
                    }
                }
            }

            val thumbObj = renderer.optJSONObject("thumbnail")?.optJSONObject("musicThumbnailRenderer")
            val thumbs = thumbObj?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
            val thumbnailUrl = if (thumbs != null && thumbs.length() > 0) {
                thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")
            } else null

            if (!videoId.isNullOrBlank() && title.isNotBlank()) {
                songs.add(
                    YTSongItem(
                        videoId = videoId,
                        title = title.trim(),
                        artist = artist.trim(),
                        artistId = artistId,
                        thumbnailUrl = thumbnailUrl
                    )
                )
            }
        }

        fun scanJson(any: Any?) {
            when (any) {
                is JSONObject -> {
                    val keys = any.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = any.opt(key)
                        when (key) {
                            "musicResponsiveListItemRenderer" -> {
                                if (value is JSONObject) extractFromItem(value)
                            }
                            "musicCardShelfRenderer" -> {
                                if (value is JSONObject) extractFromCardShelf(value)
                            }
                            else -> {
                                scanJson(value)
                            }
                        }
                    }
                }
                is JSONArray -> {
                    for (i in 0 until any.length()) {
                        scanJson(any.opt(i))
                    }
                }
            }
        }

        scanJson(json)

        return YTSearchResult(
            songs = songs.distinctBy { it.videoId },
            artists = artists,
            albums = albums,
            playlists = playlists
        )
    }

    private fun parseExploreFeed(json: JSONObject): YTExploreFeed {
        val searchRes = parseSearchResults(json)
        return YTExploreFeed(
            trendingSongs = searchRes.songs.take(15),
            newReleases = searchRes.albums.take(10),
            charts = searchRes.songs.drop(15).take(15)
        )
    }

    private fun parseDurationToSeconds(text: String): Long {
        val parts = text.split(":").mapNotNull { it.trim().toLongOrNull() }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> 0L
        }
    }
}
