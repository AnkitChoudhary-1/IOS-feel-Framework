package dev.iosfeel.sonora.feature.developer

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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.sonora.core.design.SonoraTheme

@Composable
fun DeveloperSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SonoraTheme.colors
    val typography = SonoraTheme.typography

    var playerStiffness by remember { mutableFloatStateOf(400f) }
    var playerDamping by remember { mutableFloatStateOf(0.85f) }
    var completionThreshold by remember { mutableFloatStateOf(0.35f) }
    var velocityThreshold by remember { mutableFloatStateOf(900f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(54.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹ Settings",
                    style = typography.headline.copy(
                        color = colors.accent,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Developer Lab",
                style = typography.largeTitle.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Live tuning parameters for iOSFeel motion, gestures, and sheets.",
                style = typography.subhead.copy(color = colors.textSecondary)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "PLAYER TRANSITION (PHASE 3)",
                style = typography.caption1.copy(
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .padding(16.dp)
            ) {
                Column {
                    TuningSlider(
                        title = "Spring Stiffness",
                        value = playerStiffness,
                        valueRange = 100f..1000f,
                        valueLabel = "${playerStiffness.toInt()}",
                        onValueChange = { playerStiffness = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TuningSlider(
                        title = "Spring Damping Ratio",
                        value = playerDamping,
                        valueRange = 0.3f..1.2f,
                        valueLabel = "%.2f".format(playerDamping),
                        onValueChange = { playerDamping = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TuningSlider(
                        title = "Completion Threshold",
                        value = completionThreshold,
                        valueRange = 0.1f..0.8f,
                        valueLabel = "${(completionThreshold * 100).toInt()}%",
                        onValueChange = { completionThreshold = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TuningSlider(
                        title = "Velocity Threshold (px/s)",
                        value = velocityThreshold,
                        valueRange = 300f..2500f,
                        valueLabel = "${velocityThreshold.toInt()} px/s",
                        onValueChange = { velocityThreshold = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun TuningSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    val colors = SonoraTheme.colors
    val typography = SonoraTheme.typography

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = typography.body.copy(color = colors.textPrimary)
            )
            Text(
                text = valueLabel,
                style = typography.subhead.copy(
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = colors.accent,
                activeTrackColor = colors.accent,
                inactiveTrackColor = colors.surfaceSecondary
            )
        )
    }
}
