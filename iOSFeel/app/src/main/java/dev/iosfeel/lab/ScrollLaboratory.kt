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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.scroll.IOSFlingObserver
import dev.iosfeel.scroll.IOSScrollConfig
import dev.iosfeel.scroll.IOSScrollPhase
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.scroll.calculateIOSResistanceMultiplier
import dev.iosfeel.scroll.rememberIOSFlingBehavior
import dev.iosfeel.scroll.rememberIOSScrollInteractionState
import kotlin.math.roundToInt

enum class ScrollMode {
    StandardLazyColumn,
    IOSFlingOnly,
    FullIOSElasticScroll
}

data class StressFeedItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val timestamp: String
)

@Composable
fun ScrollLaboratory(
    onBack: () -> Unit
) {
    var activeMode by remember { mutableStateOf(ScrollMode.FullIOSElasticScroll) }
    var flingMultiplier by remember { mutableFloatStateOf(1.0f) }
    var liveVelocity by remember { mutableFloatStateOf(0f) }

    val config = remember(flingMultiplier) {
        IOSScrollConfig(
            flingVelocityMultiplier = flingMultiplier,
            resistanceFactor = 0.55f,
            resistanceExponent = 0.85f,
            maxOverscrollPx = 220f
        )
    }

    val interactionState = rememberIOSScrollInteractionState(config)
    val frameMonitor = rememberFrameMonitor()

    val flingObserver = remember {
        object : IOSFlingObserver {
            override fun onFlingStarted(velocity: Float) {
                liveVelocity = velocity
            }

            override fun onFlingVelocityChanged(velocity: Float) {
                liveVelocity = velocity
            }

            override fun onFlingEnded() {
                liveVelocity = 0f
            }
        }
    }

    val stressFeedItems = remember {
        List(1000) { index ->
            StressFeedItem(
                id = index + 1,
                title = "Item #${index + 1} — iOSFeel High Performance Feed",
                subtitle = "Sub-pixel physics with 120Hz smooth spline decay & non-linear elastic rubber band.",
                timestamp = "${(index % 59) + 1}m ago"
            )
        }
    }

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Bar
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
                text = "iOSFeel — Scroll Laboratory (5B)",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mode Selector Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip(
                title = "Standard",
                selected = activeMode == ScrollMode.StandardLazyColumn,
                onClick = { activeMode = ScrollMode.StandardLazyColumn },
                modifier = Modifier.weight(1f)
            )
            ModeChip(
                title = "IOS Fling",
                selected = activeMode == ScrollMode.IOSFlingOnly,
                onClick = { activeMode = ScrollMode.IOSFlingOnly },
                modifier = Modifier.weight(1f)
            )
            ModeChip(
                title = "Full Elastic",
                selected = activeMode == ScrollMode.FullIOSElasticScroll,
                onClick = { activeMode = ScrollMode.FullIOSElasticScroll },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Telemetry Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1C1C1E))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PHYSICS TELEMETRY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 1.sp
                )

                val phaseText = if (activeMode == ScrollMode.FullIOSElasticScroll) {
                    interactionState.phase.name.uppercase()
                } else if (listState.isScrollInProgress) {
                    "SCROLLING"
                } else {
                    "IDLE"
                }

                Text(
                    text = phaseText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (interactionState.overscroll != 0f) Color(0xFFFF9F0A) else Color(0xFF34C759)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val currentResistance = calculateIOSResistanceMultiplier(
                overscroll = interactionState.overscroll,
                config = config
            )

            ScrollTelemetryRow("Live Velocity", "${liveVelocity.roundToInt()} px/s")
            ScrollTelemetryRow("Overscroll Stretch", "%.1f px".format(interactionState.overscroll))
            ScrollTelemetryRow("Resistance Coeff", "%.3f".format(currentResistance))
            ScrollTelemetryRow("Feed Items", "${stressFeedItems.size} rows")
            ScrollTelemetryRow("Frame Time", "%.2f ms".format(frameMonitor.frameTimeMs))
            ScrollTelemetryRow("FPS", "${frameMonitor.approximateFps.roundToInt()}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fling Velocity Multiplier Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "FLING MULTIPLIER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "%.2fx".format(flingMultiplier),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A84FF)
                )
            }
            Slider(
                value = flingMultiplier,
                onValueChange = { flingMultiplier = it },
                valueRange = 0.7f..1.6f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF0A84FF),
                    activeTrackColor = Color(0xFF0A84FF),
                    inactiveTrackColor = Color(0xFF38383A)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stress Feed List View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF121214))
        ) {
            when (activeMode) {
                ScrollMode.StandardLazyColumn -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        items(stressFeedItems, key = { it.id }) { item ->
                            FeedItemCard(item)
                        }
                    }
                }
                ScrollMode.IOSFlingOnly -> {
                    val customFling = rememberIOSFlingBehavior(
                        config = config,
                        observer = flingObserver
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        flingBehavior = customFling
                    ) {
                        items(stressFeedItems, key = { it.id }) { item ->
                            FeedItemCard(item)
                        }
                    }
                }
                ScrollMode.FullIOSElasticScroll -> {
                    IOSScrollableLazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        config = config,
                        flingObserver = flingObserver,
                        interactionState = interactionState
                    ) {
                        items(stressFeedItems, key = { it.id }) { item ->
                            FeedItemCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF0A84FF) else Color(0xFF1C1C1E))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFFAEAEB2)
        )
    }
}

@Composable
private fun FeedItemCard(item: StressFeedItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${item.id}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF0A84FF)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = item.timestamp,
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFFAEAEB2),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ScrollTelemetryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
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
