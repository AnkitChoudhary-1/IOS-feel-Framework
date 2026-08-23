package dev.iosfeel.sonora.feature.library

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.iosfeel.components.button.IOSButton
import dev.iosfeel.components.button.IOSButtonStyle
import dev.iosfeel.components.segmented.IOSSegmentedControl
import dev.iosfeel.components.segmented.IOSSegmentedItem
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.media.MediaPermission
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.LibrarySection
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.SongSort
import dev.iosfeel.sonora.feature.library.albums.AlbumDetailScreen
import dev.iosfeel.sonora.feature.library.albums.AlbumGrid
import dev.iosfeel.sonora.feature.library.artists.ArtistDetailScreen
import dev.iosfeel.sonora.feature.library.artists.ArtistList
import dev.iosfeel.sonora.feature.library.songs.SongList

@Composable
fun LibraryRoute(
    viewModel: LibraryViewModel,
    onSongClick: (Song) -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onPlayAlbum: (List<Song>) -> Unit = {},
    onShuffleAlbum: (List<Song>) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.permissionChanged(granted)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hasPermission = MediaPermission.hasAudioPermission(context)
                viewModel.permissionChanged(hasPermission)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LibraryScreen(
        state = state,
        onRequestPermission = {
            permissionLauncher.launch(MediaPermission.requiredAudioPermission)
        },
        onOpenSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onSectionSelected = viewModel::selectSection,
        onSortSelected = viewModel::selectSort,
        onAlbumSelected = { album ->
            onAlbumClick(album)
        },
        onCloseAlbum = viewModel::closeAlbum,
        onArtistSelected = { artist ->
            onArtistClick(artist)
        },
        onCloseArtist = viewModel::closeArtist,
        onRefresh = viewModel::refresh,
        onSongClick = onSongClick,
        onPlayAlbum = onPlayAlbum,
        onShuffleAlbum = onShuffleAlbum
    )
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onSectionSelected: (LibrarySection) -> Unit,
    onSortSelected: (SongSort) -> Unit,
    onAlbumSelected: (Album) -> Unit,
    onCloseAlbum: () -> Unit,
    onArtistSelected: (Artist) -> Unit,
    onCloseArtist: () -> Unit,
    onRefresh: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAlbum: (List<Song>) -> Unit = {},
    onShuffleAlbum: (List<Song>) -> Unit = {}
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    val segmentItems = remember {
        listOf(
            IOSSegmentedItem(value = LibrarySection.Songs, label = "Songs"),
            IOSSegmentedItem(value = LibrarySection.Albums, label = "Albums"),
            IOSSegmentedItem(value = LibrarySection.Artists, label = "Artists")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Library",
                style = typography.largeTitle,
                color = colors.textPrimary
            )

            if (state.permissionGranted && state.library.songs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                IOSSegmentedControl(
                    items = segmentItems,
                    selectedValue = state.section,
                    onSelected = onSectionSelected,
                    containerColor = colors.surfaceSecondary,
                    selectedPillColor = colors.surface,
                    selectedTextColor = colors.textPrimary,
                    unselectedTextColor = colors.textSecondary
                )
            }
        }

        when {
            !state.permissionGranted -> {
                PermissionRequestView(
                    onRequestPermission = onRequestPermission,
                    onOpenSettings = onOpenSettings
                )
            }

            state.loading && state.library.songs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = colors.accent,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning your music…",
                            style = typography.subheadline,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            state.library.songs.isEmpty() -> {
                EmptyLibraryView(onRefresh = onRefresh)
            }

            else -> {
                when (state.section) {
                    LibrarySection.Songs -> {
                        SongList(
                            songs = state.library.songs,
                            sort = state.songSort,
                            sortDirection = state.sortDirection,
                            onSortSelected = onSortSelected,
                            onSongClick = onSongClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    LibrarySection.Albums -> {
                        AlbumGrid(
                            albums = state.library.albums,
                            onAlbumClick = onAlbumSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    LibrarySection.Artists -> {
                        ArtistList(
                            artists = state.library.artists,
                            onArtistClick = onArtistSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestView(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SonoraIcons.MusicNote,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sonora Needs Access\nto Your Music",
                style = typography.title2,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Allow access to your device's audio files so Sonora can play your local tracks.",
                style = typography.subheadline,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            IOSButton(
                text = "Allow Access",
                onClick = onRequestPermission,
                style = IOSButtonStyle.Filled,
                modifier = Modifier.fillMaxWidth(0.75f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            IOSButton(
                text = "Open App Settings",
                onClick = onOpenSettings,
                style = IOSButtonStyle.Plain,
                modifier = Modifier.fillMaxWidth(0.75f)
            )
        }
    }
}

@Composable
private fun EmptyLibraryView(onRefresh: () -> Unit) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = SonoraIcons.Folder,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Music Found",
                style = typography.title2,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add audio files to your device storage to listen to them in Sonora.",
                style = typography.subheadline,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            IOSButton(
                text = "Scan Again",
                onClick = onRefresh,
                style = IOSButtonStyle.Tinted
            )
        }
    }
}
