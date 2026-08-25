package dev.iosfeel.components.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.components.interaction.iosPressEffect
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.material.LocalIOSDarkTheme

/**
 * Modern iOS Collapsing Large Title Top Bar.
 *
 * Stays pinned/sticky at the top of the screen:
 * - At scroll position = 0: Expands into full iOS Large Title (30sp Bold) with optional subtitle.
 * - While scrolling up: Smoothly shrinks into a compact inline title (17sp SemiBold) with frosted glass backdrop.
 * - While scrolling down: Smoothly springs back into full Large Title format.
 */
@Composable
fun IOSLargeTitleTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    scrollState: LazyListState? = null,
    scrollOffset: Float? = null,
    collapseRangePx: Float = 140f,
    backButtonVisible: Boolean = false,
    backButtonLabel: String = "Back",
    onBack: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    backdrop: IOSBackdropState? = null,
    titleColor: Color = Color.Unspecified,
    subtitleColor: Color = Color.Unspecified,
    dividerColor: Color = Color.Unspecified,
    usePillTitle: Boolean = false,
    titleIcon: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIOSDarkTheme.current
    val haptics = rememberIOSHaptics()

    val rawProgress = remember(scrollState, scrollOffset, collapseRangePx) {
        derivedStateOf {
            if (scrollOffset != null) {
                (scrollOffset / collapseRangePx).coerceIn(0f, 1f)
            } else if (scrollState != null) {
                val firstIndex = scrollState.firstVisibleItemIndex
                val firstOffset = scrollState.firstVisibleItemScrollOffset
                if (firstIndex > 0) 1f else (firstOffset.toFloat() / collapseRangePx).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.value,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.88f),
        label = "ios_large_title_collapse"
    )

    val resolvedTitleColor = if (titleColor != Color.Unspecified) {
        titleColor
    } else if (isDark) {
        Color.White
    } else {
        Color(0xFF000000)
    }

    val resolvedSubtitleColor = if (subtitleColor != Color.Unspecified) {
        subtitleColor
    } else {
        Color(0xFF007AFF)
    }

    val resolvedDividerColor = if (dividerColor != Color.Unspecified) {
        dividerColor
    } else if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    val compactBarHeight = 44.dp
    val expandedExtraHeight = 46.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Frosted Glass Layer (fades in as user scrolls up)
        if (animatedProgress > 0.005f) {
            if (backdrop != null) {
                IOSMaterialSurface(
                    backdrop = backdrop,
                    config = IOSMaterialConfig(
                        style = IOSMaterialStyle.Regular,
                        cornerRadius = 0.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .matchParentSize()
                        .alpha(animatedProgress)
                ) {
                    Box(modifier = Modifier.fillMaxWidth())
                }
            } else {
                val frostedColor = if (isDark) {
                    Color(0xFF1C1C1E).copy(alpha = 0.88f * animatedProgress)
                } else {
                    Color(0xFFF9F9F9).copy(alpha = 0.88f * animatedProgress)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .matchParentSize()
                        .background(frostedColor)
                )
            }
        }

        // Bottom Divider line (fades in when scrolled)
        if (animatedProgress > 0.05f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(0.5.dp)
                    .alpha(animatedProgress)
                    .background(resolvedDividerColor)
            )
        }

        // Navigation Bar Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Compact Header Row (44dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(compactBarHeight)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Leading Slot (Back button or custom)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (leading != null) {
                        leading()
                    } else if (backButtonVisible && onBack != null) {
                        val backInteraction = remember { MutableInteractionSource() }
                        Row(
                            modifier = Modifier
                                .semantics { role = Role.Button }
                                .iosPressEffect(
                                    interactionSource = backInteraction,
                                    config = IOSPressConfig(pressedScale = 0.94f, pressedAlpha = 0.6f)
                                )
                                .clickable(
                                    interactionSource = backInteraction,
                                    indication = null
                                ) {
                                    haptics.impact(IOSImpact.Light)
                                    onBack()
                                }
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "‹",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Light,
                                color = resolvedSubtitleColor,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                            Text(
                                text = backButtonLabel,
                                fontSize = 17.sp,
                                color = resolvedSubtitleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Compact Centered Title (fades in as user scrolls up)
                if (animatedProgress > 0.02f) {
                    val pillAlpha = (animatedProgress * 1.3f - 0.3f).coerceIn(0f, 1f)
                    if (usePillTitle) {
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .graphicsLayer {
                                    alpha = pillAlpha
                                    scaleX = 0.88f + (0.12f * pillAlpha)
                                    scaleY = 0.88f + (0.12f * pillAlpha)
                                    translationY = (1f - pillAlpha) * 6f
                                }
                                .shadow(
                                    elevation = 4.dp,
                                    shape = CircleShape,
                                    spotColor = Color.Black.copy(alpha = 0.18f)
                                )
                                .clip(CircleShape)
                                .border(
                                    width = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                )
                        ) {
                            IOSMaterialSurface(
                                backdrop = backdrop,
                                config = IOSMaterialConfig(
                                    style = IOSMaterialStyle.Regular,
                                    cornerRadius = 18.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    titleIcon?.invoke()
                                    Text(
                                        text = title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = resolvedTitleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = resolvedTitleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 72.dp)
                                .graphicsLayer {
                                    alpha = (animatedProgress * 1.4f - 0.4f).coerceIn(0f, 1f)
                                    translationY = (1f - animatedProgress) * 12f
                                }
                        )
                    }
                }

                // Trailing Actions Slot
                if (trailing != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterEnd),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        trailing()
                    }
                }
            }

            // Expanded Large Title Section (smoothly shrinks and fades out as user scrolls up)
            if (animatedProgress < 0.98f) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(expandedExtraHeight * (1f - animatedProgress))
                        .clipToBounds()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = (1f - animatedProgress * 1.3f).coerceIn(0f, 1f)
                            translationY = -animatedProgress * 10f
                        }
                ) {
                    if (subtitle != null && animatedProgress < 0.5f) {
                        Text(
                            text = subtitle.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = resolvedSubtitleColor,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Text(
                        text = title,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = resolvedTitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
