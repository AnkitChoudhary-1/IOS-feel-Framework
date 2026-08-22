package dev.iosfeel.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.haptics.IOSHapticThreshold
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.motion.IOSMotionBounds
import dev.iosfeel.motion.IOSMotionDragConfig
import dev.iosfeel.motion.IOSMotionSettings
import dev.iosfeel.motion.IOSMotionState
import dev.iosfeel.motion.iosMotionDrag
import dev.iosfeel.motion.rememberIOSMotionSettings
import dev.iosfeel.motion.rememberIOSMotionState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MotionLaboratory(
    onBack: (() -> Unit)? = null
) {
    val motionState = rememberIOSMotionState()
    val settings = rememberIOSMotionSettings()
    val frameMonitor = rememberFrameMonitor()
    val haptics = rememberIOSHaptics()
    val scope = rememberCoroutineScope()

    // Reusable Haptic Threshold (Phase 2B)
    val hapticThreshold = remember {
        IOSHapticThreshold(threshold = 200f)
    }

    LaunchedEffect(motionState.position.value) {
        hapticThreshold.update(
            value = motionState.position.value,
            haptics = haptics
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = Color(0xFF0A84FF),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C1C1E))
                        .clickable { onBack() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Text(
                text = "iOSFeel — Motion Laboratory",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = motionState.position.value.roundToInt(),
                            y = 0
                        )
                    }
                    .size(
                        width = 130.dp,
                        height = 90.dp
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(26.dp)
                    )
                    .iosMotionDrag(
                        state = motionState,
                        config = IOSMotionDragConfig(
                            targetPosition = 0f,
                            springSpec = settings.currentSpec(),
                            bounds = IOSMotionBounds(
                                min = -500f,
                                max = 500f
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DRAG ME",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Debug Panel
        MotionDebugPanel(
            state = motionState,
            frameMonitor = frameMonitor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Spring Tuning Controls
        SpringControls(
            settings = settings,
            onReset = {
                scope.launch {
                    motionState.snapTo(0f)
                    hapticThreshold.reset()
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MotionDebugPanel(
    state: IOSMotionState,
    frameMonitor: FrameMonitorState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1E))
            .padding(16.dp)
    ) {
        Text(
            text = "STATE & PERFORMANCE",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8E8E93),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        DebugRow("Position", "${state.position.value.roundToInt()} px")
        DebugRow("Velocity", "${state.velocity.roundToInt()} px/s")
        DebugRow("Target", "${state.target.roundToInt()} px")
        DebugRow("State", "${state.phase}")
        DebugRow("Haptic Threshold", "200 px (Activated/Deactivated)")
        DebugRow("Frame", "%.2f ms".format(frameMonitor.frameTimeMs))
        DebugRow("Approx FPS", "${frameMonitor.approximateFps.roundToInt()}")
    }
}

@Composable
private fun DebugRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFAEAEB2)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun SpringControls(
    settings: IOSMotionSettings,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1E))
            .padding(16.dp)
    ) {
        Text(
            text = "SPRING TUNING",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8E8E93),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Stiffness: ${settings.stiffness.roundToInt()}",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFAEAEB2)
        )

        Slider(
            value = settings.stiffness,
            onValueChange = { settings.stiffness = it },
            valueRange = 100f..800f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF0A84FF),
                inactiveTrackColor = Color(0xFF38383A)
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Damping: ${"%.2f".format(settings.dampingRatio)}",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFAEAEB2)
        )

        Slider(
            value = settings.dampingRatio,
            onValueChange = { settings.dampingRatio = it },
            valueRange = 0.3f..1.5f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF0A84FF),
                inactiveTrackColor = Color(0xFF38383A)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { settings.useSnappy() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Snappy", fontSize = 12.sp)
            }

            Button(
                onClick = { settings.useSmooth() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Smooth", fontSize = 12.sp)
            }

            Button(
                onClick = { settings.useGentle() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Gentle", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3A3A3C),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Reset Position", fontSize = 13.sp)
        }
    }
}
