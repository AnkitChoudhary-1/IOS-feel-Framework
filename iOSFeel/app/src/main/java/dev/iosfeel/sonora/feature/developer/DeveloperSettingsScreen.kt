package dev.iosfeel.sonora.feature.developer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.material.rememberIOSBackdropState
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons

@Composable
fun DeveloperSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    // Live Blur Tuners
    var blurRadius by remember { mutableFloatStateOf(24f) }
    var tintAlpha by remember { mutableFloatStateOf(0.40f) }
    var cornerRadius by remember { mutableFloatStateOf(24f) }
    var borderStroke by remember { mutableFloatStateOf(0.5f) }
    var borderAlpha by remember { mutableFloatStateOf(0.20f) }
    var selectedStyle by remember { mutableStateOf(IOSMaterialStyle.Regular) }
    var selectedTintColor by remember { mutableStateOf(Color.White) }
    var backdropBlurEnabled by remember { mutableStateOf(true) }

    // Motion Tuners
    var playerStiffness by remember { mutableFloatStateOf(400f) }
    var playerDamping by remember { mutableFloatStateOf(0.85f) }
    var completionThreshold by remember { mutableFloatStateOf(0.38f) }
    var velocityThreshold by remember { mutableFloatStateOf(900f) }

    val backdropState = rememberIOSBackdropState()

    IOSBackdropLayout(
        state = backdropState,
        modifier = modifier.fillMaxSize(),
        backdrop = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            )
        },
        overlay = {
            IOSScrollableLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp),
                topFadeHeight = 16.dp,
                bottomFadeHeight = 32.dp
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Developer Lab",
                        style = typography.largeTitle.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Real-time blur calibration playground and motion physics tuning.",
                        style = typography.subhead.copy(color = colors.textSecondary)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 1. LIVE BLUR PREVIEW PLAYGROUND
                item {
                    Text(
                        text = "LIVE BLUR PLAYGROUND",
                        style = typography.caption1.copy(
                            color = colors.textTertiary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Colorful Graphic Canvas for live blur verification
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1B1B3A),
                                        Color(0xFF0F0C29),
                                        Color(0xFF24243E)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Vibrant geometric decorative objects in background
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 16.dp, start = 20.dp)
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFF007F), Color(0xFFFF5252))
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 16.dp, end = 24.dp)
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFF007AFF))
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFFD600), Color(0xFFFF9100))
                                    )
                                )
                        )

                        Text(
                            text = "iOSFeel Sonora Engine",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                        )

                        // Floating Frosted Glass Card under Test
                        val previewConfig = IOSMaterialConfig(
                            style = selectedStyle,
                            customBlurRadius = blurRadius.dp,
                            customTintAlpha = tintAlpha,
                            tint = selectedTintColor.copy(alpha = tintAlpha),
                            cornerRadius = cornerRadius.dp,
                            borderStroke = borderStroke.dp,
                            borderColor = Color.White.copy(alpha = borderAlpha),
                            enabled = backdropBlurEnabled
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = RoundedCornerShape(cornerRadius.dp),
                                    spotColor = Color.Black.copy(alpha = 0.4f)
                                )
                        ) {
                            IOSMaterialSurface(
                                backdrop = if (backdropBlurEnabled) backdropState else null,
                                config = previewConfig,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFFF2D55)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = SonoraIcons.Play,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Blinding Lights",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "The Weeknd • After Hours",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = SonoraIcons.MoreHorizontal,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 2. ADVANCED BLUR CONTROLLER CONTROLS
                item {
                    Text(
                        text = "ADVANCED BLUR CONTROLS",
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
                            // Backdrop Blur Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Live Backdrop Blur",
                                    style = typography.body.copy(color = colors.textPrimary)
                                )
                                Switch(
                                    checked = backdropBlurEnabled,
                                    onCheckedChange = { backdropBlurEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.accent
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TuningSlider(
                                title = "Blur Radius",
                                value = blurRadius,
                                valueRange = 0f..60f,
                                valueLabel = "${blurRadius.toInt()} dp",
                                onValueChange = { blurRadius = it }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TuningSlider(
                                title = "Tint Opacity",
                                value = tintAlpha,
                                valueRange = 0f..1f,
                                valueLabel = "${(tintAlpha * 100).toInt()}%",
                                onValueChange = { tintAlpha = it }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TuningSlider(
                                title = "Corner Radius",
                                value = cornerRadius,
                                valueRange = 0f..40f,
                                valueLabel = "${cornerRadius.toInt()} dp",
                                onValueChange = { cornerRadius = it }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TuningSlider(
                                title = "Border Separator Stroke",
                                value = borderStroke,
                                valueRange = 0f..3f,
                                valueLabel = "%.1f dp".format(borderStroke),
                                onValueChange = { borderStroke = it }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TuningSlider(
                                title = "Border Separator Alpha",
                                value = borderAlpha,
                                valueRange = 0f..1f,
                                valueLabel = "${(borderAlpha * 100).toInt()}%",
                                onValueChange = { borderAlpha = it }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tint Preset Colors
                            Text(
                                text = "Tint Palette",
                                style = typography.body.copy(color = colors.textPrimary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val palette = listOf(
                                    Color.White to "White",
                                    Color.Black to "Dark",
                                    Color(0xFF007AFF) to "Blue",
                                    Color(0xFFAF52DE) to "Purple",
                                    Color(0xFFFF2D55) to "Ruby"
                                )
                                palette.forEach { (paletteColor, _) ->
                                    val isSelected = selectedTintColor == paletteColor
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(paletteColor)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) colors.accent else Color.Gray.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            )
                                            .clickable { selectedTintColor = paletteColor }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Reset Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceSecondary)
                                    .clickable {
                                        blurRadius = 24f
                                        tintAlpha = 0.40f
                                        cornerRadius = 24f
                                        borderStroke = 0.5f
                                        borderAlpha = 0.20f
                                        selectedTintColor = Color.White
                                        selectedStyle = IOSMaterialStyle.Regular
                                        backdropBlurEnabled = true
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Reset to iOS Defaults",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.accent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 3. MOTION & GESTURE CALIBRATION
                item {
                    Text(
                        text = "PLAYER MOTION & GESTURES",
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
    )
}

@Composable
private fun TuningSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = typography.body.copy(color = colors.textPrimary, fontSize = 14.sp)
            )
            Text(
                text = valueLabel,
                style = typography.subhead.copy(
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
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
