package dev.iosfeel.sonora.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.expandable.IOSExpandableSurfaceConfig
import dev.iosfeel.components.expandable.rememberIOSExpandableSurfaceState
import dev.iosfeel.components.floatingbar.IOSFloatingTabBar
import dev.iosfeel.components.floatingbar.IOSFloatingTabItem
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.LocalIOSMaterialOverride
import dev.iosfeel.material.rememberIOSBackdropState
import dev.iosfeel.motion.IOSSpringSpec
import dev.iosfeel.navigation.IOSNavigationEntry
import dev.iosfeel.navigation.IOSNavigationStack
import dev.iosfeel.navigation.IOSNavigationState
import dev.iosfeel.navigation.rememberIOSNavigationState
import dev.iosfeel.sonora.core.datastore.DeveloperSettings
import dev.iosfeel.sonora.core.datastore.LocalDeveloperSettings
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.di.SonoraContainer
import dev.iosfeel.sonora.core.media.controller.SonoraPlaybackController
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.RepeatMode
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.findAlbum
import dev.iosfeel.sonora.core.model.findArtist
import dev.iosfeel.sonora.core.model.sorted
import dev.iosfeel.sonora.feature.album.AlbumScreen
import dev.iosfeel.sonora.feature.artist.ArtistScreen
import dev.iosfeel.sonora.feature.developer.DeveloperSettingsScreen
import dev.iosfeel.sonora.feature.favorites.FavoritesScreen
import dev.iosfeel.sonora.feature.home.HomeScreen
import dev.iosfeel.sonora.feature.home.HomeViewModel
import dev.iosfeel.sonora.feature.library.LibraryRoute
import dev.iosfeel.sonora.feature.library.LibraryViewModel
import dev.iosfeel.sonora.feature.player.PlayerSurface
import dev.iosfeel.sonora.feature.player.actions.SongActionContext
import dev.iosfeel.sonora.feature.player.actions.SongActionsSheet
import dev.iosfeel.sonora.feature.player.actions.SongInfoSheet
import dev.iosfeel.sonora.feature.playlist.AddSongsToPlaylistSheet
import dev.iosfeel.sonora.feature.playlist.AddToPlaylistSheet
import dev.iosfeel.sonora.feature.playlist.CreatePlaylistSheet
import dev.iosfeel.sonora.feature.playlist.PlaylistDetailScreen
import dev.iosfeel.sonora.feature.playlist.PlaylistsScreen
import dev.iosfeel.sonora.feature.search.SearchScreen
import dev.iosfeel.sonora.feature.search.SearchViewModel
import dev.iosfeel.sonora.feature.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun SonoraNavigationShell(
    container: SonoraContainer,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(SonoraTab.Home) }

    val devSettings by container.preferences.developerSettings.collectAsState(initial = DeveloperSettings())

    val materialOverride = remember(devSettings) {
        IOSMaterialConfig(
            style = try {
                IOSMaterialStyle.valueOf(devSettings.materialStyle)
            } catch (_: Exception) {
                IOSMaterialStyle.Regular
            },
            customBlurRadius = devSettings.blurRadius.coerceAtLeast(0f).dp,
            customTintAlpha = devSettings.tintAlpha.coerceIn(0f, 1f),
            tint = if (devSettings.tintColorArgb != 0L) {
                Color(devSettings.tintColorArgb.toInt()).copy(alpha = devSettings.tintAlpha.coerceIn(0f, 1f))
            } else {
                null
            },
            borderColor = Color.White.copy(alpha = devSettings.borderAlpha.coerceIn(0f, 1f)),
            borderStroke = devSettings.borderStroke.coerceAtLeast(0f).dp,
            enabled = devSettings.backdropBlurEnabled
        )
    }

    val homeViewModel = remember {
        HomeViewModel(
            musicRepository = container.musicRepository,
            historyRepository = container.historyRepository,
            favoritesRepository = container.favoritesRepository,
            playlistRepository = container.playlistRepository
        )
    }
    val homeState by homeViewModel.state.collectAsState()

    val libraryViewModel = remember {
        LibraryViewModel(repository = container.musicRepository)
    }
    val libraryState by libraryViewModel.state.collectAsState()

    val searchViewModel = remember {
        SearchViewModel(
            musicRepository = container.musicRepository,
            playlistRepository = container.playlistRepository,
            preferences = container.preferences
        )
    }

    val favoriteIds by container.favoritesRepository.favoriteSongIds.collectAsState(initial = emptySet())
    val favoriteSongs by container.favoritesRepository.observeFavoriteSongs(container.musicRepository).collectAsState(initial = emptyList())
    val playlists by container.playlistRepository.playlists.collectAsState(initial = emptyList())

    val playbackController = container.playbackController
    val playbackState by playbackController.state.collectAsState()

    val playerExpansionConfig = remember(devSettings) {
        IOSExpandableSurfaceConfig(
            expansionThreshold = devSettings.completionThreshold,
            velocityThreshold = devSettings.velocityThreshold / 1000f,
            springSpec = IOSSpringSpec(
                stiffness = devSettings.playerStiffness,
                dampingRatio = devSettings.playerDamping
            )
        )
    }

    val playerExpansionState = rememberIOSExpandableSurfaceState(config = playerExpansionConfig)

    LaunchedEffect(Unit) {
        (playbackController as? SonoraPlaybackController)?.connect()
    }

    val backdropState = rememberIOSBackdropState()

    // Independent Navigation Stacks for each tab
    val homeNavState = rememberIOSNavigationState(
        initialEntry = IOSNavigationEntry(key = "home_root", route = "home")
    )
    val libraryNavState = rememberIOSNavigationState(
        initialEntry = IOSNavigationEntry(key = "library_root", route = "library")
    )
    val searchNavState = rememberIOSNavigationState(
        initialEntry = IOSNavigationEntry(key = "search_root", route = "search")
    )
    val settingsNavState = rememberIOSNavigationState(
        initialEntry = IOSNavigationEntry(key = "settings_root", route = "settings")
    )

    fun activeNav(): IOSNavigationState = when (currentTab) {
        SonoraTab.Home -> homeNavState
        SonoraTab.Library -> libraryNavState
        SonoraTab.Search -> searchNavState
        SonoraTab.Settings -> settingsNavState
    }

    // Modal sheet states
    var activeSongActions by remember { mutableStateOf<Song?>(null) }
    var activeSongActionContext by remember { mutableStateOf(SongActionContext()) }
    var activeSongInfo by remember { mutableStateOf<Song?>(null) }
    var activeAddToPlaylistSong by remember { mutableStateOf<Song?>(null) }
    var isCreatePlaylistSheetVisible by remember { mutableStateOf(false) }
    var activeRenamePlaylist by remember { mutableStateOf<Playlist?>(null) }
    var activeAddSongsPlaylist by remember { mutableStateOf<Playlist?>(null) }

    val tabItems = remember {
        listOf(
            IOSFloatingTabItem(
                value = SonoraTab.Home,
                label = "Home",
                icon = { selected ->
                    SonoraTabIcon(
                        tab = SonoraTab.Home,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            ),
            IOSFloatingTabItem(
                value = SonoraTab.Library,
                label = "Library",
                icon = { selected ->
                    SonoraTabIcon(
                        tab = SonoraTab.Library,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            ),
            IOSFloatingTabItem(
                value = SonoraTab.Search,
                label = "Search",
                icon = { selected ->
                    SonoraTabIcon(
                        tab = SonoraTab.Search,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            ),
            IOSFloatingTabItem(
                value = SonoraTab.Settings,
                label = "Settings",
                icon = { selected ->
                    SonoraTabIcon(
                        tab = SonoraTab.Settings,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            )
        )
    }

    CompositionLocalProvider(
        LocalIOSMaterialOverride provides materialOverride,
        LocalDeveloperSettings provides devSettings
    ) {
        IOSBackdropLayout(
            state = backdropState,
            modifier = modifier.fillMaxSize(),
            backdrop = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            (fadeIn(animationSpec = spring(stiffness = 500f, dampingRatio = 0.90f)) +
                             scaleIn(initialScale = 0.985f, animationSpec = spring(stiffness = 500f, dampingRatio = 0.90f)))
                            .togetherWith(
                                fadeOut(animationSpec = spring(stiffness = 500f, dampingRatio = 0.90f)) +
                                scaleOut(targetScale = 1.015f, animationSpec = spring(stiffness = 500f, dampingRatio = 0.90f))
                            )
                        },
                        label = "ios_tab_switch",
                        modifier = Modifier.fillMaxSize()
                    ) { targetTab ->
                        val targetNavState = when (targetTab) {
                            SonoraTab.Home -> homeNavState
                            SonoraTab.Library -> libraryNavState
                            SonoraTab.Search -> searchNavState
                            SonoraTab.Settings -> settingsNavState
                        }

                        IOSNavigationStack(
                            state = targetNavState,
                            modifier = Modifier.fillMaxSize()
                        ) { entry ->
                            RenderDestination(
                                entry = entry,
                                navState = targetNavState,
                                homeState = homeState,
                                libraryState = libraryState,
                                playbackState = playbackState,
                                favoriteSongs = favoriteSongs,
                                playlists = playlists,
                                searchViewModel = searchViewModel,
                                backdrop = backdropState,
                                onSongClick = { song, queue ->
                                    playbackController.playSong(song = song, queue = queue)
                                },
                                onPlayAll = { songs ->
                                    playbackController.setShuffle(false)
                                    playbackController.playQueue(songs = songs, startIndex = 0)
                                },
                                onShuffleAll = { songs ->
                                    playbackController.setShuffle(true)
                                    playbackController.playQueue(songs = songs, startIndex = 0)
                                },
                                onSongOptionsClick = { song, context ->
                                    activeSongActions = song
                                    activeSongActionContext = context
                                },
                                onOpenFavorites = {
                                    targetNavState.push(
                                        IOSNavigationEntry(
                                            key = "favorites_${System.currentTimeMillis()}",
                                            route = "favorites"
                                        )
                                    )
                                },
                                onOpenPlaylists = {
                                    targetNavState.push(
                                        IOSNavigationEntry(
                                            key = "playlists_${System.currentTimeMillis()}",
                                            route = "playlists"
                                        )
                                    )
                                },
                                onOpenPlaylist = { playlist ->
                                    targetNavState.push(
                                        IOSNavigationEntry(
                                            key = "playlist_${playlist.id}_${System.currentTimeMillis()}",
                                            route = "playlist/${playlist.id}"
                                        )
                                    )
                                },
                                onCreatePlaylist = {
                                    isCreatePlaylistSheetVisible = true
                                },
                                onRenamePlaylist = { playlist ->
                                    activeRenamePlaylist = playlist
                                },
                                onAddSongsToPlaylist = { playlist ->
                                    activeAddSongsPlaylist = playlist
                                },
                                onDeletePlaylist = { playlist ->
                                    scope.launch {
                                        container.playlistRepository.delete(playlist.id)
                                    }
                                },
                                libraryViewModel = libraryViewModel,
                                container = container,
                                onOpenDeveloperSettings = {
                                    targetNavState.push(
                                        IOSNavigationEntry(
                                            key = "dev_settings_${System.currentTimeMillis()}",
                                            route = "developer_settings"
                                        )
                                    )
                                },
                                onNavigateToLibrary = { currentTab = SonoraTab.Library }
                            )
                        }
                    }
                }
            },
            overlay = {
                Box(modifier = Modifier.fillMaxSize()) {
                    val tabBarProgress = playerExpansionState.progress.coerceIn(0f, 1f)
                    val tabBarAlpha = (1f - tabBarProgress * 2f).coerceIn(0f, 1f)
                    val tabBarTranslationY = with(density) { (tabBarProgress * 80.dp.toPx()) }

                    if (tabBarAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = tabBarAlpha
                                    translationY = tabBarTranslationY
                                },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            IOSFloatingTabBar(
                                items = tabItems,
                                selected = currentTab,
                                onSelected = { tab ->
                                    if (currentTab == tab) {
                                        val active = when (tab) {
                                            SonoraTab.Home -> homeNavState
                                            SonoraTab.Library -> libraryNavState
                                            SonoraTab.Search -> searchNavState
                                            SonoraTab.Settings -> settingsNavState
                                        }
                                        while (active.canGoBack) {
                                            active.pop()
                                        }
                                    } else {
                                        currentTab = tab
                                    }
                                },
                                backdrop = backdropState,
                                activeColor = colors.accent,
                                inactiveColor = colors.textTertiary
                            )
                        }
                    }

                    // Global Floating Player Surface
                    if (playbackState.hasActiveMedia) {
                        val currentSongId = playbackState.currentSong?.id
                        val isFav = currentSongId != null && favoriteIds.contains(currentSongId)

                        PlayerSurface(
                            playbackState = playbackState,
                            expansionState = playerExpansionState,
                            isFavorite = isFav,
                            onToggleFavorite = {
                                playbackState.currentSong?.let { song ->
                                    scope.launch {
                                        container.favoritesRepository.toggleFavorite(song.id)
                                    }
                                }
                            },
                            onPlayPause = { playbackController.togglePlayPause() },
                            onNext = { playbackController.seekToNext() },
                            onPrevious = { playbackController.seekToPrevious() },
                            onSeek = { positionMs -> playbackController.seekTo(positionMs) },
                            onToggleShuffle = {
                                playbackController.setShuffle(!playbackState.shuffleEnabled)
                            },
                            onCycleRepeat = {
                                val nextRepeat = when (playbackState.repeatMode) {
                                    RepeatMode.Off -> RepeatMode.All
                                    RepeatMode.All -> RepeatMode.One
                                    RepeatMode.One -> RepeatMode.Off
                                }
                                playbackController.setRepeatMode(nextRepeat)
                            },
                            onOptionsClick = {
                                playbackState.currentSong?.let { song ->
                                    activeSongActions = song
                                    activeSongActionContext = SongActionContext()
                                }
                            },
                            backdrop = backdropState,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Universal Song Actions Sheet
                    activeSongActions?.let { song ->
                        val isFav = favoriteIds.contains(song.id)
                        SonoraModalOverlay(onDismiss = { activeSongActions = null }) {
                            SongActionsSheet(
                                song = song,
                                isFavorite = isFav,
                                onToggleFavorite = {
                                    scope.launch {
                                        container.favoritesRepository.toggleFavorite(song.id)
                                    }
                                },
                                onPlayNext = {
                                    playbackController.playNext(song)
                                    activeSongActions = null
                                },
                                onAddToQueue = {
                                    playbackController.addToQueue(song)
                                    activeSongActions = null
                                },
                                onAddToPlaylist = {
                                    val target = song
                                    activeSongActions = null
                                    activeAddToPlaylistSong = target
                                },
                                onGoToAlbum = if (song.albumId != null) {
                                    {
                                        val albumId = song.albumId
                                        activeSongActions = null
                                        activeNav().push(
                                            IOSNavigationEntry(
                                                key = "album_${albumId}_${System.currentTimeMillis()}",
                                                route = "album/$albumId"
                                            )
                                        )
                                    }
                                } else null,
                                onGoToArtist = if (song.artistId != null) {
                                    {
                                        val artistId = song.artistId
                                        activeSongActions = null
                                        activeNav().push(
                                            IOSNavigationEntry(
                                                key = "artist_${artistId}_${System.currentTimeMillis()}",
                                                route = "artist/$artistId"
                                            )
                                        )
                                    }
                                } else null,
                                onSongInfo = {
                                    val target = song
                                    activeSongActions = null
                                    activeSongInfo = target
                                },
                                onRemoveFromPlaylist = if (activeSongActionContext.playlistId != null) {
                                    {
                                        val pid = activeSongActionContext.playlistId!!
                                        scope.launch {
                                            container.playlistRepository.removeSong(pid, song.id)
                                        }
                                        activeSongActions = null
                                    }
                                } else null,
                                context = activeSongActionContext
                            )
                        }
                    }

                    // Song Info Sheet
                    activeSongInfo?.let { song ->
                        SonoraModalOverlay(onDismiss = { activeSongInfo = null }) {
                            SongInfoSheet(
                                song = song,
                                onClose = { activeSongInfo = null }
                            )
                        }
                    }

                    // Add To Playlist Sheet
                    activeAddToPlaylistSong?.let { song ->
                        SonoraModalOverlay(onDismiss = { activeAddToPlaylistSong = null }) {
                            AddToPlaylistSheet(
                                song = song,
                                playlists = playlists,
                                onSelectPlaylist = { playlist ->
                                    scope.launch {
                                        container.playlistRepository.addSongs(playlist.id, listOf(song.id))
                                    }
                                    activeAddToPlaylistSong = null
                                },
                                onCreateNewPlaylist = {
                                    activeAddToPlaylistSong = null
                                    isCreatePlaylistSheetVisible = true
                                },
                                onClose = { activeAddToPlaylistSong = null }
                            )
                        }
                    }

                    // Create Playlist Sheet
                    if (isCreatePlaylistSheetVisible) {
                        SonoraModalOverlay(onDismiss = { isCreatePlaylistSheetVisible = false }) {
                            CreatePlaylistSheet(
                                title = "New Playlist",
                                confirmButtonText = "Create",
                                onDismiss = { isCreatePlaylistSheetVisible = false },
                                onConfirm = { name ->
                                    scope.launch {
                                        val newId = container.playlistRepository.create(name)
                                        activeAddToPlaylistSong?.let { song ->
                                            container.playlistRepository.addSongs(newId, listOf(song.id))
                                            activeAddToPlaylistSong = null
                                        }
                                    }
                                    isCreatePlaylistSheetVisible = false
                                }
                            )
                        }
                    }

                    // Rename Playlist Sheet
                    activeRenamePlaylist?.let { playlist ->
                        SonoraModalOverlay(onDismiss = { activeRenamePlaylist = null }) {
                            CreatePlaylistSheet(
                                initialName = playlist.name,
                                title = "Rename Playlist",
                                confirmButtonText = "Save",
                                onDismiss = { activeRenamePlaylist = null },
                                onConfirm = { newName ->
                                    scope.launch {
                                        container.playlistRepository.rename(playlist.id, newName)
                                    }
                                    activeRenamePlaylist = null
                                }
                            )
                        }
                    }

                    // Add Songs to Playlist Sheet
                    activeAddSongsPlaylist?.let { playlist ->
                        val existingIds = remember(playlist) { playlist.songs.map { it.id }.toSet() }
                        SonoraModalOverlay(onDismiss = { activeAddSongsPlaylist = null }) {
                            AddSongsToPlaylistSheet(
                                allSongs = libraryState.library.songs,
                                existingSongIds = existingIds,
                                onAddSongs = { songIds ->
                                    scope.launch {
                                        container.playlistRepository.addSongs(playlist.id, songIds)
                                    }
                                    activeAddSongsPlaylist = null
                                },
                                onCancel = { activeAddSongsPlaylist = null }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun SonoraModalOverlay(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = LocalSonoraColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(colors.surfaceElevated)
                .navigationBarsPadding()
        ) {
            content()
        }
    }
}

@Composable
private fun RenderDestination(
    entry: IOSNavigationEntry,
    navState: IOSNavigationState,
    homeState: dev.iosfeel.sonora.feature.home.HomeUiState,
    libraryState: dev.iosfeel.sonora.feature.library.LibraryUiState,
    playbackState: dev.iosfeel.sonora.core.model.PlaybackState,
    favoriteSongs: List<Song>,
    playlists: List<Playlist>,
    searchViewModel: SearchViewModel,
    backdrop: IOSBackdropState?,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    onSongOptionsClick: (Song, SongActionContext) -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit,
    onRenamePlaylist: (Playlist) -> Unit,
    onAddSongsToPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    libraryViewModel: LibraryViewModel,
    container: SonoraContainer,
    onOpenDeveloperSettings: () -> Unit,
    onNavigateToLibrary: () -> Unit
) {
    val route = entry.route

    when {
        route == "home" -> {
            HomeScreen(
                uiState = homeState,
                onAlbumClick = { album ->
                    navState.push(
                        IOSNavigationEntry(
                            key = "album_${album.id}_${System.currentTimeMillis()}",
                            route = "album/${album.id}"
                        )
                    )
                },
                onArtistClick = { artist ->
                    navState.push(
                        IOSNavigationEntry(
                            key = "artist_${artist.id}_${System.currentTimeMillis()}",
                            route = "artist/${artist.id}"
                        )
                    )
                },
                onSongClick = onSongClick,
                onPlaylistClick = onOpenPlaylist,
                onFavoritesClick = onOpenFavorites,
                onPlaylistsClick = onOpenPlaylists,
                onSongOptionsClick = { song -> onSongOptionsClick(song, SongActionContext()) },
                onNavigateToLibrary = onNavigateToLibrary
            )
        }

        route == "library" -> {
            LibraryRoute(
                viewModel = libraryViewModel,
                onSongClick = { song ->
                    val activeQueue = libraryState.library.songs.sorted(
                        sort = libraryState.songSort,
                        direction = libraryState.sortDirection
                    )
                    onSongClick(song, activeQueue)
                },
                onAlbumClick = { album ->
                    navState.push(
                        IOSNavigationEntry(
                            key = "album_${album.id}_${System.currentTimeMillis()}",
                            route = "album/${album.id}"
                        )
                    )
                },
                onArtistClick = { artist ->
                    navState.push(
                        IOSNavigationEntry(
                            key = "artist_${artist.id}_${System.currentTimeMillis()}",
                            route = "artist/${artist.id}"
                        )
                    )
                },
                onFavoritesClick = onOpenFavorites,
                onPlaylistsClick = onOpenPlaylists,
                onSongOptionsClick = { song -> onSongOptionsClick(song, SongActionContext()) },
                onPlayAlbum = onPlayAll,
                onShuffleAlbum = onShuffleAll
            )
        }

        route == "favorites" -> {
            FavoritesScreen(
                favoriteSongs = favoriteSongs,
                onBack = { navState.pop() },
                onSongClick = onSongClick,
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll,
                onSongOptionsClick = { song -> onSongOptionsClick(song, SongActionContext()) }
            )
        }

        route == "playlists" -> {
            PlaylistsScreen(
                playlists = playlists,
                onBack = { navState.pop() },
                onPlaylistClick = onOpenPlaylist,
                onCreatePlaylist = onCreatePlaylist
            )
        }

        route.startsWith("playlist/") -> {
            val playlistId = route.substringAfter("playlist/").toLongOrNull() ?: -1L
            val playlist = playlists.find { it.id == playlistId }

            PlaylistDetailScreen(
                playlist = playlist,
                onBack = { navState.pop() },
                onSongClick = onSongClick,
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll,
                onAddSongs = { playlist?.let { onAddSongsToPlaylist(it) } },
                onRenamePlaylist = { playlist?.let { onRenamePlaylist(it) } },
                onDeletePlaylist = {
                    playlist?.let {
                        onDeletePlaylist(it)
                        navState.pop()
                    }
                },
                onSongOptionsClick = { song ->
                    onSongOptionsClick(
                        song,
                        SongActionContext(playlistId = playlistId, allowRemoveFromPlaylist = true)
                    )
                }
            )
        }

        route.startsWith("album/") -> {
            val albumId = route.substringAfter("album/").toLongOrNull() ?: -1L
            val album = libraryState.library.findAlbum(albumId)

            AlbumScreen(
                album = album,
                currentPlayingSongId = playbackState.currentSong?.id,
                onBack = { navState.pop() },
                onArtistClick = { artistId ->
                    navState.push(
                        IOSNavigationEntry(
                            key = "artist_${artistId}_${System.currentTimeMillis()}",
                            route = "artist/$artistId"
                        )
                    )
                },
                onSongClick = onSongClick,
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll
            )
        }

        route.startsWith("artist/") -> {
            val artistId = route.substringAfter("artist/").toLongOrNull() ?: -1L
            val artist = libraryState.library.findArtist(artistId)

            ArtistScreen(
                artist = artist,
                currentPlayingSongId = playbackState.currentSong?.id,
                onBack = { navState.pop() },
                onAlbumClick = { album ->
                    navState.push(
                        IOSNavigationEntry(
                            key = "album_${album.id}_${System.currentTimeMillis()}",
                            route = "album/${album.id}"
                        )
                    )
                },
                onSongClick = onSongClick,
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll
            )
        }

        route == "search" -> {
            SearchScreen(
                viewModel = searchViewModel,
                onSongClick = onSongClick,
                onAlbumClick = { album ->
                    navState.push(
                        IOSNavigationEntry(
                            key = "album_${album.id}_${System.currentTimeMillis()}",
                            route = "album/${album.id}"
                        )
                    )
                },
                onArtistClick = { artist ->
                    navState.push(
                        IOSNavigationEntry(
                            key = "artist_${artist.id}_${System.currentTimeMillis()}",
                            route = "artist/${artist.id}"
                        )
                    )
                },
                onPlaylistClick = onOpenPlaylist,
                onSongOptionsClick = { song -> onSongOptionsClick(song, SongActionContext()) }
            )
        }

        route == "settings" -> {
            SettingsScreen(
                preferences = container.preferences,
                onOpenDeveloperSettings = onOpenDeveloperSettings
            )
        }

        route == "developer_settings" -> {
            DeveloperSettingsScreen(
                preferences = container.preferences,
                onBack = { navState.pop() }
            )
        }
    }
}
