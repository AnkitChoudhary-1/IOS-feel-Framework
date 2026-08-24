package dev.iosfeel.navigation.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.iosfeel.navigation.transition.IOSNavigationTransitionState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlin.math.roundToInt

/**
 * Pure composable rendering the iOS-standard navigation transition with parallax offset,
 * leading edge shadow, and dimming scrim.
 */
@Composable
@ExperimentalIOSFeelV2Api
fun IOSNavigationRenderer(
    transitionState: IOSNavigationTransitionState,
    currentScreen: @Composable () -> Unit,
    previousScreen: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val progress = transitionState.progress

        // Render previous underlying screen (if one exists and transition is active)
        if (previousScreen != null && progress > 0f) {
            val prevParallaxX = lerp(-widthPx * 0.22f, 0f, progress)
            val scrimAlpha = (1f - progress) * 0.18f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(prevParallaxX.roundToInt(), 0) }
            ) {
                previousScreen()
                // Scrim overlay dimming previous screen
                if (scrimAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = scrimAlpha))
                    )
                }
            }
        }

        // Render current top screen
        val currentX = widthPx * progress
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(currentX.roundToInt(), 0) }
                .shadow(
                    elevation = if (progress > 0f) 16.dp else 0.dp,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
        ) {
            currentScreen()
        }
    }
}
