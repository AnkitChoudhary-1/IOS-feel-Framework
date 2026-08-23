package dev.iosfeel.sonora.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.iosfeel.components.tab.IOSTabBar
import dev.iosfeel.components.tab.IOSTabItem
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.rememberIOSBackdropState
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.di.SonoraContainer
import dev.iosfeel.sonora.feature.developer.DeveloperSettingsScreen
import dev.iosfeel.sonora.feature.home.HomeScreen
import dev.iosfeel.sonora.feature.library.LibraryRoute
import dev.iosfeel.sonora.feature.library.LibraryViewModel
import dev.iosfeel.sonora.feature.search.SearchScreen
import dev.iosfeel.sonora.feature.settings.SettingsScreen

@Composable
fun SonoraNavigationShell(
    container: SonoraContainer,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    var currentTab by remember { mutableStateOf(SonoraTab.Home) }
    var inDeveloperLab by remember { mutableStateOf(false) }

    val libraryViewModel = remember {
        LibraryViewModel(repository = container.musicRepository)
    }
    val libraryState by libraryViewModel.state.collectAsState()

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
                            }
                        )
                        SonoraTab.Library -> LibraryRoute(
                            viewModel = libraryViewModel,
                            onSongClick = {
                                // Phase 2 Playback
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
                Box(
                    modifier = Modifier.fillMaxSize(),
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
        )
    }
}
