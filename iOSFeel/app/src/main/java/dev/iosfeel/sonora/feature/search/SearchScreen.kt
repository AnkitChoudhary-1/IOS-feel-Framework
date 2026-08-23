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

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier
) {
    val colors = SonoraTheme.colors
    val typography = SonoraTheme.typography
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(54.dp))

        Text(
            text = "Search",
            style = typography.largeTitle.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
