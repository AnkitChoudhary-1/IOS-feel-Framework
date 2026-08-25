package dev.iosfeel.components.floatingbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialStyle

/**
 * Modern iOS Floating Top Bar with Animated Scrolling Title Pill.
 *
 * Renders an authentic iOS floating top bar with:
 * - A circular frosted Back/Navigation button on the left.
 * - An animated frosted glass title pill that smoothly springs in when the user scrolls down
 *   (scroll offset > [scrollThresholdPx]) and collapses when returning to top.
 * - Trailing action buttons on the right.
 *
 * Overload for [LazyListState].
 */
@Composable
fun IOSScrollPillTopBar(
    title: String,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    navigation: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    backdrop: IOSBackdropState? = null,
    materialStyle: IOSMaterialStyle = IOSMaterialStyle.Regular,
    textColor: Color = Color.Unspecified,
    scrollThresholdPx: Int = 40
) {
    val isScrolled by remember(scrollState, scrollThresholdPx) {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > scrollThresholdPx
        }
    }

    IOSScrollPillTopBar(
        title = title,
        visible = isScrolled,
        modifier = modifier,
        icon = icon,
        onBack = onBack,
        navigation = navigation,
        actions = actions,
        backdrop = backdrop,
        materialStyle = materialStyle,
        textColor = textColor
    )
}

/**
 * Overload for [LazyGridState].
 */
@Composable
fun IOSScrollPillTopBar(
    title: String,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    navigation: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    backdrop: IOSBackdropState? = null,
    materialStyle: IOSMaterialStyle = IOSMaterialStyle.Regular,
    textColor: Color = Color.Unspecified,
    scrollThresholdPx: Int = 40
) {
    val isScrolled by remember(gridState, scrollThresholdPx) {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > scrollThresholdPx
        }
    }

    IOSScrollPillTopBar(
        title = title,
        visible = isScrolled,
        modifier = modifier,
        icon = icon,
        onBack = onBack,
        navigation = navigation,
        actions = actions,
        backdrop = backdrop,
        materialStyle = materialStyle,
        textColor = textColor
    )
}

/**
 * Base overload taking a direct [visible] flag.
 */
@Composable
fun IOSScrollPillTopBar(
    title: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    navigation: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    backdrop: IOSBackdropState? = null,
    materialStyle: IOSMaterialStyle = IOSMaterialStyle.Regular,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (navigation != null) {
                navigation()
            } else if (onBack != null) {
                IOSFloatingIconButton(
                    onClick = onBack,
                    size = 40.dp
                ) {
                    Text(
                        text = "‹",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light,
                        color = if (textColor != Color.Unspecified) textColor else Color.White
                    )
                }
            }

            IOSAnimatedTitlePill(
                title = title,
                visible = visible,
                icon = icon,
                backdrop = backdrop,
                style = materialStyle,
                textColor = textColor
            )
        }

        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions
            )
        }
    }
}
