package dev.iosfeel.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.haptics.IOSHapticEngine
import dev.iosfeel.haptics.IOSHapticEvent
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.IOSNotification
import dev.iosfeel.haptics.rememberIOSHaptics

@Composable
fun HapticLaboratory(
    onBack: () -> Unit
) {
    val haptics = rememberIOSHaptics()
    var spamCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
                text = "iOSFeel — Haptic Laboratory",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Capabilities Card
        if (haptics is IOSHapticEngine) {
            val caps = haptics.capabilities
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1C1E))
                .padding(16.dp)
            ) {
                Text(
                    text = "HARDWARE CAPABILITIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                CapabilityRow("Vibrator Hardware", caps.hasVibrator.toString())
                CapabilityRow("Predefined Tick", caps.supportsTick.toString())
                CapabilityRow("Predefined Click", caps.supportsClick.toString())
                CapabilityRow("Predefined Heavy Click", caps.supportsHeavyClick.toString())
                CapabilityRow("Primitive Click", caps.supportsPrimitiveClick.toString())
                CapabilityRow("Primitive Tick", caps.supportsPrimitiveTick.toString())
                CapabilityRow("Primitive Low Tick", caps.supportsPrimitiveLowTick.toString())
                CapabilityRow(
                    "Rich Composition",
                    (caps.supportsPrimitiveTick && caps.supportsPrimitiveClick).toString()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // System Semantics Section
        HapticSection(title = "SYSTEM SEMANTICS") {
            HapticButton(
                title = "Selection (CLOCK_TICK)",
                description = "Light discreet click for item changes and stepping",
                onClick = { haptics.perform(IOSHapticEvent.Selection) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HapticButton(
                title = "Gesture Start",
                description = "Discreet indication that a gesture has begun",
                onClick = { haptics.perform(IOSHapticEvent.GestureStart) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HapticButton(
                title = "Gesture End",
                description = "Tactile signal when a gesture finishes",
                onClick = { haptics.perform(IOSHapticEvent.GestureEnd) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HapticButton(
                title = "Threshold Activate",
                description = "Snap feedback when drag crosses an actionable boundary",
                onClick = { haptics.perform(IOSHapticEvent.ThresholdActivated) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HapticButton(
                title = "Threshold Deactivate",
                description = "Tactile cue when moving backward out of active threshold",
                onClick = { haptics.perform(IOSHapticEvent.ThresholdDeactivated) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Impact Feedback Section
        HapticSection(title = "IMPACT FEEDBACK") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { haptics.impact(IOSImpact.Light) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2C2E),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Light", fontSize = 12.sp)
                }

                Button(
                    onClick = { haptics.impact(IOSImpact.Medium) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2C2E),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Medium", fontSize = 12.sp)
                }

                Button(
                    onClick = { haptics.impact(IOSImpact.Heavy) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2C2E),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Heavy", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification Feedback Section
        HapticSection(title = "NOTIFICATION FEEDBACK") {
            HapticButton(
                title = "Success (Composed Accent / Confirm)",
                description = "Dual-tick composition if supported, else system confirm",
                onClick = { haptics.notification(IOSNotification.Success) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HapticButton(
                title = "Warning",
                description = "Medium impact cue for cautionary states",
                onClick = { haptics.notification(IOSNotification.Warning) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HapticButton(
                title = "Error (Reject / Heavy)",
                description = "Strong feedback for failed validations or constraints",
                onClick = { haptics.notification(IOSNotification.Error) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rate Limiter Test Section
        HapticSection(title = "RATE LIMITING TEST") {
            HapticButton(
                title = "Spam Selection (Taps: $spamCount)",
                description = "Rapid triggers are throttled by 35ms minimum interval limiter",
                onClick = {
                    spamCount++
                    haptics.selection()
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HapticSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1E))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8E8E93),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        content()
    }
}

@Composable
private fun HapticButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2C2C2E),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFFAEAEB2)
            )
        }
    }
}

@Composable
private fun CapabilityRow(
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
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFAEAEB2)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (value == "true") Color(0xFF34C759) else Color(0xFFFF453A)
        )
    }
}
