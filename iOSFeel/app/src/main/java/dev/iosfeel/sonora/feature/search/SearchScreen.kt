package dev.iosfeel.sonora.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.search.IOSSearchField
import dev.iosfeel.sonora.core.design.SonoraTheme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import dev.iosfeel.components.navigation.IOSLargeTitleTopBar
import dev.iosfeel.scroll.IOSScrollableLazyColumn

import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.rememberIOSBackdropState

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier
) {
    val colors = SonoraTheme.colors
    val typography = SonoraTheme.typography
    val listState = rememberLazyListState()
    val screenBackdrop = rememberIOSBackdropState()
    var searchQuery by remember { mutableStateOf("") }

    IOSBackdropLayout(
        state = screenBackdrop,
        modifier = modifier.fillMaxSize(),
        backdrop = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                IOSScrollableLazyColumn(
                    state = listState,
                    topFadeHeight = 24.dp,
                    bottomFadeHeight = 92.dp,
                    contentPadding = PaddingValues(top = 96.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.statusBarsPadding())
                        Spacer(modifier = Modifier.height(10.dp))
                        IOSSearchField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Artists, Songs, Lyrics, and More",
                            containerColor = colors.surfaceSecondary,
                            textColor = colors.textPrimary,
                            placeholderColor = colors.textTertiary,
                            cancelTextColor = colors.accent,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "Search your local music library" else "Searching for '$searchQuery'...",
                                style = typography.subhead.copy(
                                    color = colors.textSecondary
                                )
                            )
                        }
                    }
                }
            }
        },
        overlay = {
            IOSLargeTitleTopBar(
                title = "Search",
                subtitle = "Sonora",
                scrollState = listState,
                backdrop = screenBackdrop,
                titleColor = colors.textPrimary,
                subtitleColor = colors.accent,
                dividerColor = colors.separator
            )
        }
    )
}
