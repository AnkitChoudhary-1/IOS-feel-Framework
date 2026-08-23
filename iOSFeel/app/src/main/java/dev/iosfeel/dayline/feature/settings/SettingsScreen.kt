package dev.iosfeel.dayline.feature.settings

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import dev.iosfeel.dayline.core.datastore.AppTheme
import dev.iosfeel.dayline.core.datastore.DaylinePreferences
import dev.iosfeel.dayline.core.design.DaylineTheme
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.IOSNotification
import dev.iosfeel.haptics.rememberIOSHaptics
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferences: DaylinePreferences,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography
    val scope = rememberCoroutineScope()
    val haptics = rememberIOSHaptics()

    val currentTheme by preferences.theme.collectAsState(initial = AppTheme.System)
    val isDevModeEnabled by preferences.isDeveloperModeEnabled.collectAsState(initial = false)

    var versionTapCount by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You",
            style = typography.largeTitle.copy(
                color = colors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Appearance Section
        Text(
            text = "APPEARANCE",
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Theme",
                    style = typography.body.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                IOSSegmentedControl(
                    items = listOf(
                        IOSSegmentedItem(label = "System", value = AppTheme.System),
                        IOSSegmentedItem(label = "Light", value = AppTheme.Light),
                        IOSSegmentedItem(label = "Dark", value = AppTheme.Dark)
                    ),
                    selectedValue = currentTheme,
                    onSelected = { newTheme ->
                        scope.launch {
                            preferences.setTheme(newTheme)
                        }
                    },
                    containerColor = colors.surfaceSecondary,
                    selectedPillColor = colors.surface,
                    selectedTextColor = colors.textPrimary,
                    unselectedTextColor = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Data & Privacy Section
        Text(
            text = "PRIVACY & STORAGE",
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Local-First Architecture",
                    style = typography.headline.copy(
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "All your Dayline tasks, notes, habits, and timeline events remain securely on your device.",
                    style = typography.caption.copy(
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // 3. Developer Mode Status Section
        if (isDevModeEnabled) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DEVELOPER SETTINGS",
                style = typography.caption.copy(
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "iOSFeel Framework Tuner",
                            style = typography.body.copy(
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Developer Mode Active",
                            style = typography.caption.copy(
                                color = colors.accent
                            )
                        )
                    }

                    Text(
                        text = "Disable",
                        style = typography.caption.copy(
                            color = colors.destructive,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch {
                                preferences.setDeveloperModeEnabled(false)
                                haptics.impact(IOSImpact.Medium)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. About Section
        Text(
            text = "ABOUT",
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val nextCount = versionTapCount + 1
                    versionTapCount = nextCount
                    haptics.impact(IOSImpact.Light)

                    if (nextCount >= 7) {
                        versionTapCount = 0
                        scope.launch {
                            preferences.setDeveloperModeEnabled(true)
                            haptics.notification(IOSNotification.Success)
                        }
                    }
                }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dayline",
                        style = typography.body.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Powered by iOSFeel",
                        style = typography.caption.copy(
                            color = colors.textTertiary
                        )
                    )
                }

                Text(
                    text = "v1.0.0 (Phase 0)",
                    style = typography.caption.copy(
                        color = colors.textSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
