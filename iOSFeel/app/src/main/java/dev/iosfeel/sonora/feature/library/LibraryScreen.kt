package dev.iosfeel.sonora.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import dev.iosfeel.components.segmented.IOSSegmentedControl
import dev.iosfeel.components.segmented.IOSSegmentedItem
import dev.iosfeel.sonora.core.design.SonoraTheme

enum class LibraryTab {
    Songs,
    Albums,
    Artists
}

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier
) {
    val colors = SonoraTheme.colors
    val typography = SonoraTheme.typography
    var selectedTab by remember { mutableStateOf(LibraryTab.Songs) }

    val tabs = remember {
        listOf(
            IOSSegmentedItem(LibraryTab.Songs, "Songs"),
            IOSSegmentedItem(LibraryTab.Albums, "Albums"),
            IOSSegmentedItem(LibraryTab.Artists, "Artists")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(54.dp))

        Text(
            text = "Library",
            style = typography.largeTitle.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        IOSSegmentedControl(
            items = tabs,
            selectedValue = selectedTab,
            onSelected = { selectedTab = it },
            containerColor = colors.surfaceSecondary,
            selectedPillColor = colors.surface,
            selectedTextColor = colors.textPrimary,
            unselectedTextColor = colors.textSecondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No ${selectedTab.name} Found",
                    style = typography.title3.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Local audio indexing starts in Phase 1.",
                    style = typography.subhead.copy(
                        color = colors.textSecondary
                    )
                )
            }
        }
    }
}
