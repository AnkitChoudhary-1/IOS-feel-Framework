package dev.iosfeel.sonora.core.design.artwork

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.iosfeel.sonora.core.model.Song

@Composable
fun SongArtwork(
    song: Song,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val context = LocalContext.current
    val artworkModel: Any? = song.artworkUrl ?: song.albumId?.let {
        ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it)
    } ?: song.contentUri

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(artworkModel)
            .size(256, 256)
            .crossfade(false)
            .build(),
        contentDescription = "${song.title} artwork",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        loading = {
            MissingArtwork(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = cornerRadius,
                iconSize = 20.dp
            )
        },
        error = {
            MissingArtwork(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = cornerRadius,
                iconSize = 20.dp
            )
        }
    )
}
