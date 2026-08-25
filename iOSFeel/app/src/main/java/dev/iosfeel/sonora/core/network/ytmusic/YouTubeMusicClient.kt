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
        private const val ANDROID_MUSIC_VERSION = "6.40.52"

        private const val USER_AGENT_WEB =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    private fun createWebContext(): JSONObject {
        val clientObj = JSONObject().apply {
            put("clientName", "WEB_REMIX")
            put("clientVersion", WEB_REMIX_VERSION)
            put("hl", "en")
            put("gl", "US")
        }
        return JSONObject().apply {
            put("client", clientObj)
        }
    }

    private fun createAndroidContext(): JSONObject {
        val clientObj = JSONObject().apply {
            put("clientName", "ANDROID_MUSIC")
            put("clientVersion", ANDROID_MUSIC_VERSION)
            put("androidSdkVersion", 34)
            put("hl", "en")
            put("gl", "US")
        }
        return JSONObject().apply {
            put("client", clientObj)
        }
    }

    suspend fun search(query: String): YTSearchResult = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext YTSearchResult()

        val payload = JSONObject().apply {
            put("context", createWebContext())
            put("query", query)
        }

        val request = Request.Builder()
            .url("$BASE_URL/search")
            .header("User-Agent", USER_AGENT_WEB)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com")
            .header("X-YouTube-Client-Name", "67")
            .header("X-YouTube-Client-Version", WEB_REMIX_VERSION)
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
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
        if (query.isBlank()) return@withContext emptyList()

        val payload = JSONObject().apply {
            put("context", createWebContext())
            put("input", query)
        }

        val request = Request.Builder()
            .url("$BASE_URL/music/get_search_suggestions")
            .header("User-Agent", USER_AGENT_WEB)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
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
                            val text = (0 until runs.length()).joinToString("") { runs.optJSONObject(it)?.optString("text") ?: "" }
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
        val payload = JSONObject().apply {
            put("context", createWebContext())
            put("browseId", "FEmusic_explore")
        }

        val request = Request.Builder()
            .url("$BASE_URL/browse")
            .header("User-Agent", USER_AGENT_WEB)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext fallbackExploreFeed()
                val bodyStr = response.body?.string() ?: return@withContext fallbackExploreFeed()
                val json = JSONObject(bodyStr)
                return@withContext parseExploreFeed(json)
            }
        } catch (e: Exception) {
            fallbackExploreFeed()
        }
    }

    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        if (videoId.isBlank()) return@withContext null

        val payload = JSONObject().apply {
            put("context", createAndroidContext())
            put("videoId", videoId)
        }

        val request = Request.Builder()
            .url("$BASE_URL/player")
            .header("User-Agent", "com.google.android.apps.youtube.music/$ANDROID_MUSIC_VERSION")
            .header("Origin", "https://music.youtube.com")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
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
                    val mimeType = format.optString("mimeType")
                    if (mimeType.startsWith("audio/")) {
                        val bitrate = format.optInt("bitrate", 0)
                        val url = format.optString("url")
                        if (url.isNotBlank() && bitrate > highestBitrate) {
                            highestBitrate = bitrate
                            bestUrl = url
                        }
                    }
                }

                return@withContext bestUrl
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseSearchResults(json: JSONObject): YTSearchResult {
        val songs = mutableListOf<YTSongItem>()
        val artists = mutableListOf<YTArtistItem>()
        val albums = mutableListOf<YTAlbumItem>()
        val playlists = mutableListOf<YTPlaylistItem>()

        fun extractFromItem(renderer: JSONObject) {
            val navEndpoint = renderer.optJSONObject("navigationEndpoint")
            val watchEndpoint = navEndpoint?.optJSONObject("watchEndpoint")
            val videoId = watchEndpoint?.optString("videoId") ?: renderer.optJSONObject("playlistItemData")?.optString("videoId")

            // Title
            val flexColumns = renderer.optJSONArray("flexColumns") ?: return
            val col0 = flexColumns.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            val titleRuns = col0?.optJSONObject("text")?.optJSONArray("runs")
            val title = titleRuns?.optJSONObject(0)?.optString("text") ?: ""

            // Subtitle / Artist / Album / Duration
            val col1 = flexColumns.optJSONObject(1)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            val subtitleRuns = col1?.optJSONObject("text")?.optJSONArray("runs")

            var artist = "Unknown Artist"
            var artistId: String? = null
            var album: String? = null
            var durationSec = 0L

            if (subtitleRuns != null && subtitleRuns.length() > 0) {
                val texts = mutableListOf<String>()
                for (k in 0 until subtitleRuns.length()) {
                    val r = subtitleRuns.optJSONObject(k)
                    val t = r?.optString("text") ?: ""
                    val endpoint = r?.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")
                    val bId = endpoint?.optString("browseId")
                    if (bId != null && bId.startsWith("UC")) {
                        artistId = bId
                    }
                    if (t.isNotBlank() && t != " • ") {
                        texts.add(t)
                    }
                }
                if (texts.isNotEmpty()) artist = texts[0]
                if (texts.size > 1 && !texts[1].contains(":")) album = texts[1]
                val lastText = texts.lastOrNull() ?: ""
                if (lastText.contains(":")) {
                    durationSec = parseDurationToSeconds(lastText)
                }
            }

            // Thumbnail
            val thumbObj = renderer.optJSONObject("thumbnail")?.optJSONObject("musicThumbnailRenderer")
            val thumbs = thumbObj?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
            val thumbnailUrl = thumbs?.optJSONObject(thumbs.length() - 1)?.optString("url")

            if (!videoId.isNullOrBlank() && title.isNotBlank()) {
                songs.add(
                    YTSongItem(
                        videoId = videoId,
                        title = title,
                        artist = artist,
                        artistId = artistId,
                        album = album,
                        durationSeconds = durationSec,
                        thumbnailUrl = thumbnailUrl
                    )
                )
            }
        }

        fun scanJsonArray(arr: JSONArray) {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val itemRenderer = obj.optJSONObject("musicResponsiveListItemRenderer")
                if (itemRenderer != null) {
                    extractFromItem(itemRenderer)
                }
                obj.optJSONObject("contents")?.let { scanJsonArray(JSONArray().put(it)) }
                obj.optJSONArray("contents")?.let { scanJsonArray(it) }
                obj.optJSONArray("items")?.let { scanJsonArray(it) }
            }
        }

        val tab = json.optJSONObject("contents")
            ?.optJSONObject("tabbedSearchResultsRenderer")
            ?.optJSONArray("tabs")
            ?.optJSONObject(0)
            ?.optJSONObject("tabRenderer")
            ?.optJSONObject("content")
            ?.optJSONObject("sectionListRenderer")
            ?.optJSONArray("contents")

        if (tab != null) {
            scanJsonArray(tab)
        }

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

    private fun fallbackExploreFeed(): YTExploreFeed {
        return YTExploreFeed()
    }
}
