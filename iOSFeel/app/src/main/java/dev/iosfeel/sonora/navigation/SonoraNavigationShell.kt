package dev.iosfeel.sonora.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
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
import dev.iosfeel.sonora.core.model.RepeatMode
import dev.iosfeel.sonora.core.model.findAlbum
import dev.iosfeel.sonora.core.model.findArtist
import dev.iosfeel.sonora.core.model.sorted
import dev.iosfeel.sonora.feature.album.AlbumScreen
import dev.iosfeel.sonora.feature.artist.ArtistScreen
import dev.iosfeel.sonora.feature.developer.DeveloperSettingsScreen
import dev.iosfeel.sonora.feature.home.HomeScreen
import dev.iosfeel.sonora.feature.home.HomeViewModel
import dev.iosfeel.sonora.feature.library.LibraryRoute
import dev.iosfeel.sonora.feature.library.LibraryViewModel
import dev.iosfeel.sonora.feature.player.PlayerSurface
import dev.iosfeel.sonora.feature.search.SearchScreen
import dev.iosfeel.sonora.feature.settings.SettingsScreen

@Composable
fun SonoraNavigationShell(
    container: SonoraContainer,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val density = LocalDensity.current
    var currentTab by remember { mutableStateOf(SonoraTab.Home) }
    var inDeveloperLab by remember { mutableStateOf(false) }

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
            tint = androidx.compose.ui.graphics.Color(devSettings.tintColorArgb.toInt()).copy(alpha = devSettings.tintAlpha.coerceIn(0f, 1f)),
            borderColor = androidx.compose.ui.graphics.Color.White.copy(alpha = devSettings.borderAlpha.coerceIn(0f, 1f)),
            borderStroke = devSettings.borderStroke.coerceAtLeast(0f).dp,
            enabled = devSettings.backdropBlurEnabled
        )
    }

    val homeViewModel = remember {
        HomeViewModel(
            musicRepository = container.musicRepository,
            historyRepository = container.historyRepository
        )
    }
    val homeState by homeViewModel.state.collectAsState()

    val libraryViewModel = remember {
        LibraryViewModel(repository = container.musicRepository)
    }
    val libraryState by libraryViewModel.state.collectAsState()

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
        if (inDeveloperLab) {
            DeveloperSettingsScreen(
                preferences = container.preferences,
                onBack = { inDeveloperLab = false }
            )
        } else {
        IOSBackdropLayout(
            state = backdropState,
            modifier = modifier.fillMaxSize(),
            backdrop = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                ) {
                    when (currentTab) {
                        SonoraTab.Home -> {
                            IOSNavigationStack(
                                state = homeNavState,
                                modifier = Modifier.fillMaxSize()
                            ) { entry ->
                                RenderDestination(
                                    entry = entry,
                                    navState = homeNavState,
                                    homeState = homeState,
                                    libraryState = libraryState,
                                    playbackState = playbackState,
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
                                    libraryViewModel = libraryViewModel,
                                    container = container,
                                    onOpenDeveloperSettings = { inDeveloperLab = true },
                                    onNavigateToLibrary = { currentTab = SonoraTab.Library }
                                )
                            }
                        }

                        SonoraTab.Library -> {
                            IOSNavigationStack(
                                state = libraryNavState,
                                modifier = Modifier.fillMaxSize()
                            ) { entry ->
                                RenderDestination(
                                    entry = entry,
                                    navState = libraryNavState,
                                    homeState = homeState,
                                    libraryState = libraryState,
                                    playbackState = playbackState,
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
                                    libraryViewModel = libraryViewModel,
                                    container = container,
                                    onOpenDeveloperSettings = { inDeveloperLab = true },
                                    onNavigateToLibrary = { currentTab = SonoraTab.Library }
                                )
                            }
                        }

                        SonoraTab.Search -> {
                            IOSNavigationStack(
                                state = searchNavState,
                                modifier = Modifier.fillMaxSize()
                            ) { entry ->
                                RenderDestination(
                                    entry = entry,
                                    navState = searchNavState,
                                    homeState = homeState,
                                    libraryState = libraryState,
                                    playbackState = playbackState,
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
                                    libraryViewModel = libraryViewModel,
                                    container = container,
                                    onOpenDeveloperSettings = { inDeveloperLab = true },
                                    onNavigateToLibrary = { currentTab = SonoraTab.Library }
                                )
                            }
                        }

                        SonoraTab.Settings -> {
                            IOSNavigationStack(
                                state = settingsNavState,
                                modifier = Modifier.fillMaxSize()
                            ) { entry ->
                                RenderDestination(
                                    entry = entry,
                                    navState = settingsNavState,
                                    homeState = homeState,
                                    libraryState = libraryState,
                                    playbackState = playbackState,
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
                                    libraryViewModel = libraryViewModel,
                                    container = container,
                                    onOpenDeveloperSettings = { inDeveloperLab = true },
                                    onNavigateToLibrary = { currentTab = SonoraTab.Library }
                                )
                            }
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
                                        // Reselect tab: pop to root if navigated deep
                                        val activeNav = when (tab) {
                                            SonoraTab.Home -> homeNavState
                                            SonoraTab.Library -> libraryNavState
                                            SonoraTab.Search -> searchNavState
                                            SonoraTab.Settings -> settingsNavState
                                        }
                                        while (activeNav.canGoBack) {
                                            activeNav.pop()
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
                        PlayerSurface(
                            playbackState = playbackState,
                            expansionState = playerExpansionState,
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
                            backdrop = backdropState,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        )
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
    backdrop: IOSBackdropState?,
    onSongClick: (dev.iosfeel.sonora.core.model.Song, List<dev.iosfeel.sonora.core.model.Song>) -> Unit,
    onPlayAll: (List<dev.iosfeel.sonora.core.model.Song>) -> Unit,
    onShuffleAll: (List<dev.iosfeel.sonora.core.model.Song>) -> Unit,
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
                onPlayAlbum = onPlayAll,
                onShuffleAlbum = onShuffleAll
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
                onShuffleAll = onShuffleAll,
                backdrop = backdrop
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
                onShuffleAll = onShuffleAll,
                backdrop = backdrop
            )
        }

        route == "search" -> {
            SearchScreen()
        }

        route == "settings" -> {
            SettingsScreen(
                preferences = container.preferences,
                onOpenDeveloperSettings = onOpenDeveloperSettings
            )
        }
    }
}
