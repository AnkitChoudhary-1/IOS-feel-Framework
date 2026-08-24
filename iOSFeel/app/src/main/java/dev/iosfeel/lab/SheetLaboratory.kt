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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sheet.IOSSheet
import dev.iosfeel.sheet.IOSSheetConfig
import dev.iosfeel.sheet.detent.IOSSheetDetent
import dev.iosfeel.sheet.IOSSheetImeBehavior
import dev.iosfeel.sheet.rememberIOSSheetState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun SheetLaboratory(
    onBack: () -> Unit
) {
    val sheetState = rememberIOSSheetState(initialDetent = IOSSheetDetent.Medium)
    val frameMonitor = rememberFrameMonitor()
    val scope = rememberCoroutineScope()

    val detents = remember {
        listOf<IOSSheetDetent>(
            IOSSheetDetent.Large,
            IOSSheetDetent.Medium,
            IOSSheetDetent.Compact
        )
    }

    var dismissOnScrimTap by remember { mutableStateOf(true) }
    var dismissible by remember { mutableStateOf(true) }
    var imeExpandToLarge by remember { mutableStateOf(true) }
    var postLikes by remember { mutableIntStateOf(1420) }

    val comments = remember {
        mutableStateListOf(
            "Alex: The physics feel identical to native iOS bottom sheets!",
            "Jordan: Nested scroll handoff from comments list to sheet collapse is seamless.",
            "Elena: Notice how the background scales down and dims automatically.",
            "Sam: Fast fling downward past Compact dismisses cleanly.",
            "Taylor: Interruptible spring animation lets you grab it mid-flight.",
            "Chris: IME keyboard insets animate smoothly without clipping input.",
            "Morgan: Haptic click triggers on every detent crossing.",
            "Casey: Rotation preserves logical Detent state across dimension changes."
        )
    }

    var commentText by remember { mutableStateOf("") }

    val config = remember(dismissOnScrimTap, dismissible, imeExpandToLarge) {
        IOSSheetConfig(
            dismissible = dismissible,
            dismissOnScrimTap = dismissOnScrimTap,
            imeBehavior = if (imeExpandToLarge) IOSSheetImeBehavior.ExpandToLarge else IOSSheetImeBehavior.KeepDetent,
            useImePadding = true
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOSSheet(
            state = sheetState,
            detents = detents,
            config = config,
            onDismissRequest = {
                // Sheet dismissed callback
            },
            backgroundContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF141416))
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Bar
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
                            text = "iOSFeel — Sheet Laboratory",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Social Feed Post Mockup
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(16.dp)
                    ) {
                        Column {
                            // User Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0A84FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🍎", fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "iosfeel.runtime",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Cupertino, CA • 2h ago",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8E8E93)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Production-ready multi-detent bottom sheet component featuring velocity-aware snapping, nested fling handoff, IME keyboard cooperation, and backdrop scale transformation.",
                                fontSize = 13.sp,
                                color = Color(0xFFAEAEB2),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Post Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        text = "❤️ $postLikes",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFFF2D55),
                                        modifier = Modifier.clickable { postLikes++ }
                                    )
                                    Text(
                                        text = "💬 ${comments.size + 924}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        modifier = Modifier.clickable {
                                            scope.launch { sheetState.show(IOSSheetDetent.Medium) }
                                        }
                                    )
                                }

                                Button(
                                    onClick = {
                                        scope.launch { sheetState.show(IOSSheetDetent.Medium) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Open Sheet", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Configuration Options Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "INTERACTION SETTINGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8E8E93),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingToggleRow("Dismiss on Scrim Tap", dismissOnScrimTap) { dismissOnScrimTap = it }
                        SettingToggleRow("Drag-to-Dismiss Past Compact", dismissible) { dismissible = it }
                        SettingToggleRow("Expand to Large on IME Open", imeExpandToLarge) { imeExpandToLarge = it }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Programmatic Control Buttons
                    Text(
                        text = "PROGRAMMATIC CONTROLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8E8E93),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { scope.launch { sheetState.expand() } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Expand", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { scope.launch { sheetState.animateTo(IOSSheetDetent.Medium) } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Medium", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { scope.launch { sheetState.collapse() } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Collapse", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    if (sheetState.visible) sheetState.dismiss()
                                    else sheetState.show(IOSSheetDetent.Medium)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sheetState.visible) Color(0xFFFF453A) else Color(0xFF34C759)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (sheetState.visible) "Dismiss" else "Show", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Telemetry Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "LIVE SHEET TELEMETRY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8E8E93),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = sheetState.phase.name.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sheetState.visible) Color(0xFF0A84FF) else Color(0xFF8E8E93)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        SheetTelemetryRow("Detent", sheetState.currentDetent.id)
                        SheetTelemetryRow("Visible", sheetState.visible.toString())
                        SheetTelemetryRow("Offset", "${sheetState.offset.roundToInt()} px")
                        SheetTelemetryRow("Velocity", "${sheetState.velocity.roundToInt()} px/s")
                        SheetTelemetryRow("Frame Time", "%.2f ms".format(frameMonitor.frameTimeMs))
                        SheetTelemetryRow("FPS", "${frameMonitor.approximateFps.roundToInt()}")
                    }
                }
            }
        ) {
            // Inside IOSSheetScope
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Comments Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments (${comments.size + 924})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "✕",
                        fontSize = 16.sp,
                        color = Color(0xFF8E8E93),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2E))
                            .clickable {
                                scope.launch { state.dismiss(2400f) }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Interactive Comments List
                IOSScrollableLazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(comments) { comment ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2C2C2E))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0A84FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💬", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = comment,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                // Comment Input Bar with IME synchronization
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2C2C2E))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF0A84FF)),
                        decorationBox = { innerTextField ->
                            if (commentText.isEmpty()) {
                                Text(
                                    text = "Add a comment...",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (commentText.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Post",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A84FF),
                            modifier = Modifier.clickable {
                                comments.add(0, "You: $commentText")
                                commentText = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759),
                uncheckedThumbColor = Color(0xFF8E8E93),
                uncheckedTrackColor = Color(0xFF3A3A3C)
            )
        )
    }
}

@Composable
private fun SheetTelemetryRow(
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
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFAEAEB2)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
