package dev.iosfeel.sonora.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import dev.iosfeel.components.expandable.rememberIOSExpandableSurfaceState
import dev.iosfeel.components.tab.IOSTabBar
import dev.iosfeel.components.tab.IOSTabItem
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.rememberIOSBackdropState
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.di.SonoraContainer
import dev.iosfeel.sonora.core.media.controller.SonoraPlaybackController
import dev.iosfeel.sonora.core.model.RepeatMode
import dev.iosfeel.sonora.core.model.sorted
import dev.iosfeel.sonora.feature.developer.DeveloperSettingsScreen
import dev.iosfeel.sonora.feature.home.HomeScreen
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

    val libraryViewModel = remember {
        LibraryViewModel(repository = container.musicRepository)
    }
    val libraryState by libraryViewModel.state.collectAsState()

    val playbackController = container.playbackController
    val playbackState by playbackController.state.collectAsState()

    val playerExpansionState = rememberIOSExpandableSurfaceState()

    LaunchedEffect(Unit) {
        (playbackController as? SonoraPlaybackController)?.connect()
    }

    val backdropState = rememberIOSBackdropState()

    val tabItems = remember {
        listOf(
            IOSTabItem(
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
            IOSTabItem(
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
            IOSTabItem(
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
            IOSTabItem(
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

    if (inDeveloperLab) {
        DeveloperSettingsScreen(onBack = { inDeveloperLab = false })
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
                        SonoraTab.Home -> HomeScreen(
                            library = libraryState.library,
                            onAlbumClick = { album ->
                                libraryViewModel.openAlbum(album)
                                currentTab = SonoraTab.Library
                            },
                            onSongClick = { song ->
                                playbackController.playSong(
                                    song = song,
                                    queue = libraryState.library.songs
                                )
                            }
                        )
                        SonoraTab.Library -> LibraryRoute(
                            viewModel = libraryViewModel,
                            onSongClick = { song ->
                                val activeQueue = libraryState.library.songs.sorted(
                                    sort = libraryState.songSort,
                                    direction = libraryState.sortDirection
                                )
                                playbackController.playSong(
                                    song = song,
                                    queue = activeQueue
                                )
                            },
                            onPlayAlbum = { songs ->
                                playbackController.setShuffle(false)
                                playbackController.playQueue(songs = songs, startIndex = 0)
                            },
                            onShuffleAlbum = { songs ->
                                playbackController.setShuffle(true)
                                playbackController.playQueue(songs = songs, startIndex = 0)
                            }
                        )
                        SonoraTab.Search -> SearchScreen()
                        SonoraTab.Settings -> SettingsScreen(
                            preferences = container.preferences,
                            onOpenDeveloperSettings = { inDeveloperLab = true }
                        )
                    }
                }
            },
            overlay = {
                Box(modifier = Modifier.fillMaxSize()) {
                    // TabBar (anchored at bottom, fades out when player expands)
                    val tabBarProgress = playerExpansionState.progress
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
                            IOSTabBar(
                                items = tabItems,
                                selected = currentTab,
                                onSelected = { currentTab = it },
                                backdrop = backdropState,
                                activeColor = colors.accent,
                                inactiveColor = colors.textTertiary
                            )
                        }
                    }

                    // Global Player Surface
                    if (playbackState.hasActiveMedia) {
                        val bottomOffset = if (playerExpansionState.progress < 0.1f) 64.dp else 0.dp
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = bottomOffset)
                        )
                    }
                }
            }
        )
    }
}
