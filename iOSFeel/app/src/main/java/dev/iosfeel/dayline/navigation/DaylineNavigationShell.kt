package dev.iosfeel.dayline.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.iosfeel.components.tab.IOSTabBar
import dev.iosfeel.components.tab.IOSTabItem
import dev.iosfeel.dayline.core.datastore.DaylinePreferences
import dev.iosfeel.dayline.core.design.DaylineTheme
import dev.iosfeel.dayline.core.di.DaylineContainer
import dev.iosfeel.dayline.feature.capture.CaptureViewModel
import dev.iosfeel.dayline.feature.capture.QuickCaptureSheet
import dev.iosfeel.dayline.feature.insights.InsightsScreen
import dev.iosfeel.dayline.feature.plan.PlanScreen
import dev.iosfeel.dayline.feature.settings.SettingsScreen
import dev.iosfeel.dayline.feature.today.TodayScreen
import dev.iosfeel.dayline.feature.today.TodayViewModel
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
    val context = LocalContext.current
    val container = remember(context) { DaylineContainer.getInstance(context) }

    val todayViewModel: TodayViewModel = viewModel(
        factory = TodayViewModel.Factory(
            taskRepository = container.taskRepository,
            timelineRepository = container.timelineRepository
        )
    )

    val captureViewModel: CaptureViewModel = viewModel(
        factory = CaptureViewModel.Factory(
            taskRepository = container.taskRepository
        )
    )

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
                            DaylineTab.Today -> TodayScreen(
                                viewModel = todayViewModel,
                                onOpenCapture = { openCapture() }
                            )
                            DaylineTab.Plan -> PlanScreen(
                                onOpenCapture = { openCapture() }
                            )
                            DaylineTab.Capture -> TodayScreen(
                                viewModel = todayViewModel,
                                onOpenCapture = { openCapture() }
                            )
                            DaylineTab.Insights -> InsightsScreen()
                            DaylineTab.You -> SettingsScreen(
                                preferences = preferences
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
        QuickCaptureSheet(
            viewModel = captureViewModel,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                }
            }
        )
    }
}
