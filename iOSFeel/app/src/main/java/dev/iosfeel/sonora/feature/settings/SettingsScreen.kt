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
import kotlinx.coroutines.launch

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(54.dp))

            Text(
                text = "Settings",
                style = typography.largeTitle.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Appearance Section
        item {
            Text(
                text = "APPEARANCE",
                style = typography.caption1.copy(
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            IOSSegmentedControl(
                items = themeOptions,
                selectedValue = currentTheme,
                onSelected = { mode ->
                    scope.launch { preferences.setThemeMode(mode) }
                },
                containerColor = colors.surfaceSecondary,
                selectedPillColor = colors.surface,
                selectedTextColor = colors.textPrimary,
                unselectedTextColor = colors.textSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Feedback Section
        item {
            Text(
                text = "INTERACTIONS & HAPTICS",
                style = typography.caption1.copy(
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tactile Haptics",
                        style = typography.body.copy(color = colors.textPrimary)
                    )

                    IOSToggle(
                        checked = isHapticsEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { preferences.setHapticsEnabled(enabled) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Developer Settings (if unlocked)
        if (isDevModeEnabled) {
            item {
                Text(
                    text = "IOSFEEL LABORATORY",
                    style = typography.caption1.copy(
                        color = colors.textTertiary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenDeveloperSettings
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Developer Settings",
                                style = typography.body.copy(
                                    color = colors.accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Live tuning for physics, springs, and gestures",
                                style = typography.footnote.copy(color = colors.textSecondary)
                            )
                        }

                        Text(
                            text = "›",
                            style = typography.title2.copy(color = colors.textTertiary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // About & Version (Tap 7 times to unlock Developer Mode)
        item {
            Text(
                text = "ABOUT",
                style = typography.caption1.copy(
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        versionTapCount++
                        if (versionTapCount in 1..6) {
                            haptics.impact(IOSImpact.Light)
                        } else if (versionTapCount >= 7 && !isDevModeEnabled) {
                            haptics.notification(IOSNotification.Success)
                            scope.launch {
                                preferences.setDeveloperModeEnabled(true)
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sonora Version",
                        style = typography.body.copy(color = colors.textPrimary)
                    )

                    Text(
                        text = "1.0.0 (Phase 0)",
                        style = typography.body.copy(color = colors.textSecondary)
                    )
                }
            }

            if (!isDevModeEnabled && versionTapCount in 3..6) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${7 - versionTapCount} taps away from unlocking Developer Mode",
                    style = typography.footnote.copy(color = colors.accent)
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
