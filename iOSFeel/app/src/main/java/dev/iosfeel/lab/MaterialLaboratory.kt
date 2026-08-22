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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.material.rememberIOSBackdropState
import dev.iosfeel.material.resolveIOSMaterial
import dev.iosfeel.scroll.IOSFlingObserver
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.scroll.rememberIOSScrollInteractionState
import kotlin.math.roundToInt

enum class LabBackdropMode {
    LiveFeed,
    VibrantMesh,
    HighContrastCity,
    DarkStudio
}

@Composable
fun MaterialLaboratory(
    onBack: () -> Unit
) {
    val frameMonitor = rememberFrameMonitor()
    val backdropState = rememberIOSBackdropState()
    val listState = rememberLazyListState()
    val scrollInteractionState = rememberIOSScrollInteractionState()

    var scrollVelocity by remember { mutableFloatStateOf(0f) }
    var backdropMode by remember { mutableStateOf(LabBackdropMode.LiveFeed) }
    var selectedStyle by remember { mutableStateOf(IOSMaterialStyle.Regular) }
    var isPlaying by remember { mutableStateOf(true) }

    val resolved = remember(selectedStyle) { resolveIOSMaterial(selectedStyle) }

    val feedItems = remember {
        List(40) { index ->
            "Feed Post #${index + 1}: High-performance 120Hz scrolling scene captured into GraphicsLayer backdrop."
        }
    }

    val config = remember(selectedStyle) {
        IOSMaterialConfig(
            style = selectedStyle,
            cornerRadius = 20.dp
        )
    }

    val flingObserver = remember {
        object : IOSFlingObserver {
            override fun onFlingStarted(velocity: Float) {
                scrollVelocity = velocity
            }
            override fun onFlingVelocityChanged(velocity: Float) {
                scrollVelocity = velocity
            }
            override fun onFlingEnded() {
                scrollVelocity = 0f
            }
        }
    }

    IOSBackdropLayout(
        state = backdropState,
        backdrop = {
            // Background Scene
            when (backdropMode) {
                LabBackdropMode.LiveFeed -> {
                    IOSScrollableLazyColumn(
                        state = listState,
                        interactionState = scrollInteractionState,
                        flingObserver = flingObserver,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F0F12))
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(110.dp))
                        }
                        items(feedItems) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E))
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✨", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                }
                LabBackdropMode.VibrantMesh -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFFF2D55),
                                        Color(0xFF5856D6),
                                        Color(0xFF007AFF),
                                        Color(0xFF34C759),
                                        Color(0xFFFF9500),
                                        Color(0xFFFF2D55)
                                    )
                                )
                            )
                    )
                }
                LabBackdropMode.HighContrastCity -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0A0A0C))
                    ) {
                        repeat(10) { row ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                repeat(4) { col ->
                                    val color = if ((row + col) % 2 == 0) Color(0xFFFF375F) else Color(0xFF30D158)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(90.dp)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color.copy(alpha = 0.85f))
                                    )
                                }
                            }
                        }
                    }
                }
                LabBackdropMode.DarkStudio -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF2C2C2E), Color(0xFF000000)),
                                    radius = 1200f
                                )
                            )
                    )
                }
            }
        },
        overlay = {
            // Floating Frosted Material UI Overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Floating Header Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    IOSMaterialSurface(
                        backdrop = backdropState,
                        config = config.copy(cornerRadius = 18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "‹",
                                    fontSize = 26.sp,
                                    color = Color(0xFF0A84FF),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onBack() }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Frosted Material",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = selectedStyle.name.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A84FF),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Middle Floating Material Card & Style Switcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp)
                ) {
                    IOSMaterialSurface(
                        backdrop = backdropState,
                        config = config,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFFF2D55), Color(0xFFFF9500))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎵", fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Starboy (Lossless Audio)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "The Weeknd • Daft Punk",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable { isPlaying = !isPlaying },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (isPlaying) "⏸" else "▶", fontSize = 16.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Material Style Switcher
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IOSMaterialStyle.values().forEach { style ->
                                    val isSelected = selectedStyle == style
                                    Button(
                                        onClick = { selectedStyle = style },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color(0xFF0A84FF) else Color.White.copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = style.name,
                                            fontSize = 9.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Material Telemetry Card
                    IOSMaterialSurface(
                        backdrop = backdropState,
                        config = config.copy(cornerRadius = 14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Blur: ${resolved.blurRadius} | Tint: ${(resolved.tintAlpha * 100).roundToInt()}%",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF0A84FF)
                                )
                                Text(
                                    text = "FPS: ${frameMonitor.approximateFps.roundToInt()}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Frame: %.2f ms".format(frameMonitor.frameTimeMs),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Velocity: ${scrollVelocity.roundToInt()} px/s",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Bottom Floating Material Tab Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    IOSMaterialSurface(
                        backdrop = backdropState,
                        config = config.copy(cornerRadius = 24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏠 Home", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("🔍 Explore", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("💬 Chats", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("👤 Profile", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    )
}
