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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.material.rememberIOSBackdropState
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.datastore.DeveloperSettings
import dev.iosfeel.sonora.core.datastore.SonoraPreferences
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import kotlinx.coroutines.launch

@Composable
fun DeveloperSettingsScreen(
    preferences: SonoraPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val scope = rememberCoroutineScope()

    val savedSettings by preferences.developerSettings.collectAsState(initial = DeveloperSettings())

    // Live Blur Tuners
    var blurRadius by remember { mutableFloatStateOf(savedSettings.blurRadius) }
    var tintAlpha by remember { mutableFloatStateOf(savedSettings.tintAlpha) }
    var cornerRadius by remember { mutableFloatStateOf(savedSettings.cornerRadius) }
    var borderStroke by remember { mutableFloatStateOf(savedSettings.borderStroke) }
    var borderAlpha by remember { mutableFloatStateOf(savedSettings.borderAlpha) }
    var selectedStyle by remember {
        mutableStateOf(
            try {
                IOSMaterialStyle.valueOf(savedSettings.materialStyle)
            } catch (_: Exception) {
                IOSMaterialStyle.Regular
            }
        )
    }
    var selectedTintColorArgb by remember { mutableStateOf(savedSettings.tintColorArgb) }
    var backdropBlurEnabled by remember { mutableStateOf(savedSettings.backdropBlurEnabled) }

    // Motion Tuners
    var playerStiffness by remember { mutableFloatStateOf(savedSettings.playerStiffness) }
    var playerDamping by remember { mutableFloatStateOf(savedSettings.playerDamping) }
    var completionThreshold by remember { mutableFloatStateOf(savedSettings.completionThreshold) }
    var velocityThreshold by remember { mutableFloatStateOf(savedSettings.velocityThreshold) }

    // Sync with saved settings when external changes/resets occur
    LaunchedEffect(savedSettings) {
        blurRadius = savedSettings.blurRadius
        tintAlpha = savedSettings.tintAlpha
        cornerRadius = savedSettings.cornerRadius
        borderStroke = savedSettings.borderStroke
        borderAlpha = savedSettings.borderAlpha
        selectedStyle = try {
            IOSMaterialStyle.valueOf(savedSettings.materialStyle)
        } catch (_: Exception) {
            IOSMaterialStyle.Regular
        }
        selectedTintColorArgb = savedSettings.tintColorArgb
        backdropBlurEnabled = savedSettings.backdropBlurEnabled
        playerStiffness = savedSettings.playerStiffness
        playerDamping = savedSettings.playerDamping
        completionThreshold = savedSettings.completionThreshold
        velocityThreshold = savedSettings.velocityThreshold
    }

    fun persist() {
        scope.launch {
            preferences.updateDeveloperSettings(
                DeveloperSettings(
                    blurRadius = blurRadius.coerceAtLeast(0f),
                    tintAlpha = tintAlpha.coerceIn(0f, 1f),
                    cornerRadius = cornerRadius.coerceAtLeast(0f),
                    borderStroke = borderStroke.coerceAtLeast(0f),
                    borderAlpha = borderAlpha.coerceIn(0f, 1f),
                    materialStyle = selectedStyle.name,
                    tintColorArgb = selectedTintColorArgb,
                    backdropBlurEnabled = backdropBlurEnabled,
                    playerStiffness = playerStiffness.coerceAtLeast(10f),
                    playerDamping = playerDamping.coerceAtLeast(0.1f),
                    completionThreshold = completionThreshold.coerceIn(0.05f, 0.95f),
                    velocityThreshold = velocityThreshold.coerceAtLeast(10f)
                )
            )
        }
    }

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
                topFadeHeight = 24.dp,
                bottomFadeHeight = 40.dp
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
                        text = "Interactive playground for iOS glass material shaders, springs, and gesture mechanics.",
                        style = typography.subheadline.copy(color = colors.textSecondary)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 1. LIVE BLUR & GLASS INTERACTIVE PREVIEW
                item {
                    Text(
                        text = "LIVE MATERIAL PREVIEW",
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
                            .height(220.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF0F141C)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Vibrant colorful geometric background to prove backdrop blur
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF2D55),
                                            Color(0xFFAF52DE),
                                            Color(0xFF007AFF),
                                            Color(0xFF5856D6),
                                            Color.Transparent
                                        ),
                                        radius = 450f
                                    )
                                )
                        )

                        // Floating dynamic glass sample card
                        val effectiveConfig = IOSMaterialConfig(
                            style = selectedStyle,
                            cornerRadius = cornerRadius.dp,
                            tint = if (selectedTintColorArgb != 0L) {
                                Color(selectedTintColorArgb.toInt()).copy(alpha = tintAlpha)
                            } else {
                                null
                            },
                            borderColor = Color.White.copy(alpha = borderAlpha),
                            borderStroke = borderStroke.dp,
                            customBlurRadius = blurRadius.dp,
                            customTintAlpha = tintAlpha,
                            enabled = backdropBlurEnabled
                        )

                        IOSMaterialSurface(
                            config = effectiveConfig,
                            backdrop = if (backdropBlurEnabled) backdropState else null,
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .height(115.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = RoundedCornerShape(cornerRadius.dp),
                                    spotColor = Color.Black.copy(alpha = 0.35f)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(
                                                            Color(0xFFFF375F),
                                                            Color(0xFFFF9F0A)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = SonoraIcons.MusicNote,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Blinding Lights",
                                                fontWeight = FontWeight.Bold,
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
                        text = "ADVANCED BLUR CONTROLLER",
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
                            .clip(RoundedCornerShape(16.dp))
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
                                Column {
                                    Text(
                                        text = "Backdrop Frosted Blur",
                                        style = typography.body.copy(color = colors.textPrimary)
                                    )
                                    Text(
                                        text = "Hardware RenderEffect real-time sampler",
                                        style = typography.footnote.copy(color = colors.textSecondary)
                                    )
                                }

                                Switch(
                                    checked = backdropBlurEnabled,
                                    onCheckedChange = {
                                        backdropBlurEnabled = it
                                        persist()
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Material Style Selector
                            Text(
                                text = "Material Style Preset",
                                style = typography.body.copy(color = colors.textPrimary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val styles = listOf(
                                    IOSMaterialStyle.UltraThin,
                                    IOSMaterialStyle.Thin,
                                    IOSMaterialStyle.Regular,
                                    IOSMaterialStyle.Thick
                                )
                                styles.forEach { style ->
                                    val isSelected = selectedStyle == style
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) colors.accent else colors.surfaceSecondary)
                                            .clickable {
                                                selectedStyle = style
                                                persist()
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = style.name.take(4),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else colors.textPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Blur Radius Slider
                            TuningSlider(
                                title = "Blur Radius",
                                value = blurRadius,
                                valueRange = 0f..80f,
                                valueLabel = "${blurRadius.toInt()} dp",
                                onValueChange = {
                                    blurRadius = it
                                    persist()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tint Opacity Slider
                            TuningSlider(
                                title = "Tint Opacity",
                                value = tintAlpha,
                                valueRange = 0f..1f,
                                valueLabel = "${(tintAlpha * 100).toInt()}%",
                                onValueChange = {
                                    tintAlpha = it
                                    persist()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Corner Radius Slider
                            TuningSlider(
                                title = "Corner Radius",
                                value = cornerRadius,
                                valueRange = 0f..40f,
                                valueLabel = "${cornerRadius.toInt()} dp",
                                onValueChange = {
                                    cornerRadius = it
                                    persist()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Border Stroke Slider
                            TuningSlider(
                                title = "Border Width",
                                value = borderStroke,
                                valueRange = 0f..3f,
                                valueLabel = "%.1f dp".format(borderStroke),
                                onValueChange = {
                                    borderStroke = it
                                    persist()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Border Alpha Slider
                            TuningSlider(
                                title = "Border Opacity",
                                value = borderAlpha,
                                valueRange = 0f..1f,
                                valueLabel = "${(borderAlpha * 100).toInt()}%",
                                onValueChange = {
                                    borderAlpha = it
                                    persist()
                                }
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
                                    0L to ("Adaptive" to colors.surfaceSecondary),
                                    0xFF141416L to ("Dark" to Color(0xFF141416)),
                                    0xFFF6F6F8L to ("Light" to Color(0xFFF6F6F8)),
                                    0xFF007AFFL to ("Blue" to Color(0xFF007AFF)),
                                    0xFFAF52DEL to ("Purple" to Color(0xFFAF52DE)),
                                    0xFFFF2D55L to ("Ruby" to Color(0xFFFF2D55))
                                )
                                palette.forEach { (colorArgb, meta) ->
                                    val (name, displayColor) = meta
                                    val isSelected = selectedTintColorArgb == colorArgb
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(displayColor)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) colors.accent else Color.Gray.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedTintColorArgb = colorArgb
                                                persist()
                                            }
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
                                        scope.launch {
                                            preferences.resetDeveloperSettings()
                                        }
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .padding(16.dp)
                    ) {
                        Column {
                            TuningSlider(
                                title = "Spring Stiffness",
                                value = playerStiffness,
                                valueRange = 100f..1000f,
                                valueLabel = "${playerStiffness.toInt()} f",
                                onValueChange = {
                                    playerStiffness = it
                                    persist()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TuningSlider(
                                title = "Damping Ratio",
                                value = playerDamping,
                                valueRange = 0.3f..1.2f,
                                valueLabel = "%.2f".format(playerDamping),
                                onValueChange = {
                                    playerDamping = it
                                    persist()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TuningSlider(
                                title = "Completion Threshold",
                                value = completionThreshold,
                                valueRange = 0.1f..0.8f,
                                valueLabel = "${(completionThreshold * 100).toInt()}%",
                                onValueChange = {
                                    completionThreshold = it
                                    persist()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TuningSlider(
                                title = "Velocity Threshold (px/s)",
                                value = velocityThreshold,
                                valueRange = 300f..2500f,
                                valueLabel = "${velocityThreshold.toInt()} px/s",
                                onValueChange = {
                                    velocityThreshold = it
                                    persist()
                                }
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
                style = typography.subheadline.copy(
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
