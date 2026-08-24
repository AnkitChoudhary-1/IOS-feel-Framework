package dev.iosfeel.sonora.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.segmented.IOSSegmentedControl
import dev.iosfeel.components.segmented.IOSSegmentedItem
import dev.iosfeel.components.toggle.IOSToggle
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.IOSNotification
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.sonora.core.datastore.SonoraPreferences
import dev.iosfeel.sonora.core.datastore.ThemeMode
import dev.iosfeel.sonora.core.design.SonoraTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import dev.iosfeel.components.navigation.IOSLargeTitleTopBar
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import kotlinx.coroutines.launch
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.rememberIOSBackdropState

@Composable
fun SettingsScreen(
    preferences: SonoraPreferences,
    onOpenDeveloperSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SonoraTheme.colors
    val typography = SonoraTheme.typography
    val scope = rememberCoroutineScope()
    val haptics = rememberIOSHaptics()
    val listState = rememberLazyListState()
    val screenBackdrop = rememberIOSBackdropState()

    val currentTheme by preferences.themeMode.collectAsState(initial = ThemeMode.System)
    val isDevModeEnabled by preferences.isDeveloperModeEnabled.collectAsState(initial = false)
    val isHapticsEnabled by preferences.isHapticsEnabled.collectAsState(initial = true)

    var versionTapCount by remember { mutableIntStateOf(0) }

    val themeOptions = remember {
        listOf(
            IOSSegmentedItem(ThemeMode.System, "System"),
            IOSSegmentedItem(ThemeMode.Light, "Light"),
            IOSSegmentedItem(ThemeMode.Dark, "Dark")
        )
    }

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
                    contentPadding = PaddingValues(top = 96.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.statusBarsPadding())
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Appearance Section
                    item {
                        SectionHeader(title = "APPEARANCE")
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated)
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Theme",
                                    style = typography.subheadline.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                IOSSegmentedControl(
                                    items = themeOptions,
                                    selectedValue = currentTheme,
                                    onSelected = { mode ->
                                        scope.launch {
                                            preferences.setThemeMode(mode)
                                        }
                                    },
                                    containerColor = colors.surfaceSecondary,
                                    selectedPillColor = colors.surface,
                                    selectedTextColor = colors.textPrimary,
                                    unselectedTextColor = colors.textSecondary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // Feedback Section
                    item {
                        SectionHeader(title = "FEEDBACK & INTERACTION")
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Haptic Feedback",
                                        style = typography.headline,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Vibrate on playback controls and navigation",
                                        style = typography.caption1,
                                        color = colors.textSecondary
                                    )
                                }

                                IOSToggle(
                                    checked = isHapticsEnabled,
                                    onCheckedChange = { enabled ->
                                        scope.launch {
                                            preferences.setHapticsEnabled(enabled)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // Developer Lab Section (if unlocked)
                    if (isDevModeEnabled) {
                        item {
                            SectionHeader(title = "DEVELOPER")
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceElevated)
                                    .clickable {
                                        haptics.impact(IOSImpact.Medium)
                                        onOpenDeveloperSettings()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Developer Settings & Lab",
                                            style = typography.headline.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.accent
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Tune blur, haptics, physics, and gestures live",
                                            style = typography.caption1,
                                            color = colors.textSecondary
                                        )
                                    }

                                    Text(
                                        text = "›",
                                        style = typography.title2,
                                        color = colors.textTertiary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }

                    // About Section
                    item {
                        SectionHeader(title = "ABOUT")
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated)
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Version",
                                        style = typography.headline,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "1.0.0 (iOSFeel)",
                                        style = typography.subheadline,
                                        color = colors.textSecondary,
                                        modifier = Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (!isDevModeEnabled) {
                                                versionTapCount++
                                                if (versionTapCount >= 7) {
                                                    haptics.notification(IOSNotification.Success)
                                                    scope.launch {
                                                        preferences.setDeveloperModeEnabled(true)
                                                    }
                                                } else {
                                                    haptics.impact(IOSImpact.Light)
                                                }
                                            }
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Framework",
                                        style = typography.headline,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "dev.iosfeel:1.0.0",
                                        style = typography.subheadline,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }

                        if (!isDevModeEnabled && versionTapCount in 3..6) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${7 - versionTapCount} taps away from unlocking Developer Mode",
                                style = typography.footnote.copy(color = colors.accent)
                            )
                        }

                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        },
        overlay = {
            IOSLargeTitleTopBar(
                title = "Settings",
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

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = SonoraTheme.typography.caption1.copy(
            color = SonoraTheme.colors.textTertiary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
