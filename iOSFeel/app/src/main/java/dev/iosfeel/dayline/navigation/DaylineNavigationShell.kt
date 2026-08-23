package dev.iosfeel.dayline.navigation

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.button.IOSButton
import dev.iosfeel.components.button.IOSButtonStyle
import dev.iosfeel.components.tab.IOSTabBar
import dev.iosfeel.components.tab.IOSTabItem
import dev.iosfeel.dayline.core.datastore.DaylinePreferences
import dev.iosfeel.dayline.core.design.DaylineTheme
import dev.iosfeel.dayline.feature.insights.InsightsScreen
import dev.iosfeel.dayline.feature.plan.PlanScreen
import dev.iosfeel.dayline.feature.settings.SettingsScreen
import dev.iosfeel.dayline.feature.today.TodayScreen
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.rememberIOSBackdropState
import dev.iosfeel.sheet.IOSSheet
import dev.iosfeel.sheet.IOSSheetDetent
import dev.iosfeel.sheet.rememberIOSSheetState
import kotlinx.coroutines.launch

@Composable
fun DaylineNavigationShell(
    preferences: DaylinePreferences,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    var currentTab by remember { mutableStateOf(DaylineTab.Today) }

    val backdropState = rememberIOSBackdropState()
    val sheetState = rememberIOSSheetState(
        initialDetent = IOSSheetDetent.Medium,
        initialVisible = false
    )
    val scope = rememberCoroutineScope()

    val openCapture = {
        scope.launch {
            sheetState.show(IOSSheetDetent.Medium)
        }
    }

    val tabItems = remember {
        listOf(
            IOSTabItem(
                value = DaylineTab.Today,
                label = "Today",
                icon = { selected ->
                    DaylineTabIcon(
                        tab = DaylineTab.Today,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            ),
            IOSTabItem(
                value = DaylineTab.Plan,
                label = "Plan",
                icon = { selected ->
                    DaylineTabIcon(
                        tab = DaylineTab.Plan,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            ),
            IOSTabItem(
                value = DaylineTab.Capture,
                label = null,
                icon = { selected ->
                    DaylineTabIcon(
                        tab = DaylineTab.Capture,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            ),
            IOSTabItem(
                value = DaylineTab.Insights,
                label = "Insights",
                icon = { selected ->
                    DaylineTabIcon(
                        tab = DaylineTab.Insights,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            ),
            IOSTabItem(
                value = DaylineTab.You,
                label = "You",
                icon = { selected ->
                    DaylineTabIcon(
                        tab = DaylineTab.You,
                        selected = selected,
                        activeColor = colors.accent,
                        inactiveColor = colors.textTertiary
                    )
                }
            )
        )
    }

    IOSSheet(
        state = sheetState,
        detents = listOf(IOSSheetDetent.Medium, IOSSheetDetent.Large),
        onDismissRequest = {},
        backgroundContent = {
            IOSBackdropLayout(
                state = backdropState,
                modifier = modifier.fillMaxSize(),
                backdrop = {
                    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
                        when (currentTab) {
                            DaylineTab.Today -> TodayScreen(onOpenCapture = { openCapture() })
                            DaylineTab.Plan -> PlanScreen(onOpenCapture = { openCapture() })
                            DaylineTab.Capture -> TodayScreen(onOpenCapture = { openCapture() })
                            DaylineTab.Insights -> InsightsScreen()
                            DaylineTab.You -> SettingsScreen(preferences = preferences)
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
                            onSelected = { tab ->
                                if (tab == DaylineTab.Capture) {
                                    openCapture()
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
            )
        }
    ) {
        CaptureSheetContent(
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                }
            }
        )
    }
}

@Composable
private fun CaptureSheetContent(
    onDismiss: () -> Unit
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Quick Capture",
            style = typography.title1.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "What would you like to add?",
            style = typography.body.copy(
                color = colors.textSecondary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        IOSButton(
            text = "Task",
            onClick = onDismiss,
            style = IOSButtonStyle.Filled,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        IOSButton(
            text = "Habit",
            onClick = onDismiss,
            style = IOSButtonStyle.Tinted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        IOSButton(
            text = "Event",
            onClick = onDismiss,
            style = IOSButtonStyle.Plain,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
