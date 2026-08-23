package dev.iosfeel.sonora.core.design.artwork

import androidx.compose.foundation.layout.Box
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
import dev.iosfeel.sonora.core.model.Album

@Composable
fun AlbumArtwork(
    album: Album,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(album.contentUri())
            .crossfade(true)
            .build(),
        contentDescription = "${album.title} artwork",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        loading = {
            MissingArtwork(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = cornerRadius
            )
        },
        error = {
            MissingArtwork(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = cornerRadius
            )
        }
    )
}
