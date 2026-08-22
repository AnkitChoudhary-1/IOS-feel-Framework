package dev.iosfeel.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.gesture.IOSGestureAxisDirection
import dev.iosfeel.gesture.IOSGestureDecision
import dev.iosfeel.gesture.IOSGesturePhase
import dev.iosfeel.gesture.decideDirectionalGestureCompletion
import dev.iosfeel.gesture.iosEdgeSwipe
import dev.iosfeel.gesture.rememberIOSGestureState
import dev.iosfeel.haptics.IOSHapticEvent
import dev.iosfeel.haptics.rememberIOSHaptics
import kotlin.math.roundToInt

@Composable
fun GestureLaboratory(
    onBack: () -> Unit
) {
    val gesture = rememberIOSGestureState()
    val haptics = rememberIOSHaptics()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val edgeWidthPx = with(density) { 64.dp.toPx() }

    val decision = decideDirectionalGestureCompletion(
        progress = gesture.progress,
        velocity = gesture.velocityX,
        direction = IOSGestureAxisDirection.Positive
    )

    var previousDecision by remember {
        mutableStateOf(IOSGestureDecision.Cancel)
    }

    // Connect Gesture Decision Transitions to Haptic Thresholds
    LaunchedEffect(decision, gesture.phase) {
        if (gesture.phase == IOSGesturePhase.Changed || gesture.phase == IOSGesturePhase.Began) {
            if (decision != previousDecision) {
                when (decision) {
                    IOSGestureDecision.Complete ->
                        haptics.perform(IOSHapticEvent.ThresholdActivated)
                    IOSGestureDecision.Cancel ->
                        haptics.perform(IOSHapticEvent.ThresholdDeactivated)
                }
                previousDecision = decision
            }
        } else if (gesture.phase == IOSGesturePhase.Idle || gesture.phase == IOSGesturePhase.Ended || gesture.phase == IOSGesturePhase.Cancelled) {
            previousDecision = IOSGestureDecision.Cancel
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .iosEdgeSwipe(
                state = gesture,
                edgeWidthPx = edgeWidthPx,
                progressDistancePx = screenWidthPx * 0.75f
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Text(
                    text = "iOSFeel — Gesture Laboratory",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edge swipe guide indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (gesture.phase == IOSGesturePhase.Changed || gesture.phase == IOSGesturePhase.Began)
                                    Color(0xFF34C759)
                                else Color(0xFF0A84FF),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Swipe right from the left screen edge (within 64dp) to initiate gesture",
                        fontSize = 13.sp,
                        color = Color(0xFFAEAEB2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Gestural Canvas Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1C1C1E))
                    .border(
                        width = 1.dp,
                        color = if (decision == IOSGestureDecision.Complete) Color(0xFF34C759) else Color(0xFF38383A),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                // Interactive Card offset with translation
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = gesture.translationX.coerceAtLeast(0f).roundToInt(),
                                y = 0
                            )
                        }
                        .padding(start = 24.dp)
                        .size(width = 160.dp, height = 120.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (decision == IOSGestureDecision.Complete) Color(0xFF34C759) else Color.White
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (decision == IOSGestureDecision.Complete) "RELEASE TO COMPLETE" else "SWIPE RIGHT",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${(gesture.progress * 100).roundToInt()}%",
                            color = Color.Black.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NORMALIZED PROGRESS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8E8E93),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "%.2f".format(gesture.progress),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { gesture.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (decision == IOSGestureDecision.Complete) Color(0xFF34C759) else Color(0xFF0A84FF),
                    trackColor = Color(0xFF2C2C2E)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Decision & Gesture State Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(16.dp)
            ) {
                Text(
                    text = "DECISION & TELEMETRY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                GestureDebugRow("Phase", gesture.phase.name)
                GestureDebugRow("Translation X", "${gesture.translationX.roundToInt()} px")
                GestureDebugRow("Translation Y", "${gesture.translationY.roundToInt()} px")
                GestureDebugRow("Velocity X", "${gesture.velocityX.roundToInt()} px/s")
                GestureDebugRow("Velocity Y", "${gesture.velocityY.roundToInt()} px/s")
                GestureDebugRow(
                    label = "Decision",
                    value = decision.name.uppercase(),
                    valueColor = if (decision == IOSGestureDecision.Complete) Color(0xFF34C759) else Color(0xFFFF9F0A)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Button
            Button(
                onClick = { gesture.reset() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reset Gesture State", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GestureDebugRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFAEAEB2)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
