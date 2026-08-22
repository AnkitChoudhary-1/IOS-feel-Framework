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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.navigation.IOSBackTransitionState
import dev.iosfeel.navigation.IOSNavigationEntry
import dev.iosfeel.navigation.IOSNavigationStack
import dev.iosfeel.navigation.IOSPushTransitionState
import dev.iosfeel.navigation.rememberIOSNavigationState
import kotlinx.coroutines.launch

@Composable
fun NavigationLaboratory(
    onBack: () -> Unit
) {
    val backTransition = remember { IOSBackTransitionState() }
    val pushTransition = remember { IOSPushTransitionState() }
    val frameMonitor = rememberFrameMonitor()
    val scope = rememberCoroutineScope()

    val navState = rememberIOSNavigationState(
        initialEntry = IOSNavigationEntry(key = "home", route = "home")
    )

    fun pushScreen(key: String, route: String) {
        scope.launch {
            pushTransition.prepare()
            navState.push(IOSNavigationEntry(key = key, route = route))
            pushTransition.animate()
            pushTransition.finish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOSNavigationStack(
            state = navState,
            backTransition = backTransition,
            pushTransition = pushTransition
        ) { entry ->
            when (entry.route) {
                "home" -> NavigationHomeScreen(
                    onExitLab = onBack,
                    onOpenProfile = { pushScreen("profile_1", "profile") },
                    stackSize = navState.size
                )
                "profile" -> ProfileScreen(
                    onPop = { navState.pop() },
                    onOpenPost = { pushScreen("post_1", "post") },
                    backTransition = backTransition,
                    frameMonitor = frameMonitor,
                    stackSize = navState.size
                )
                "post" -> PostDetailScreen(
                    onPop = { navState.pop() },
                    onOpenComments = { pushScreen("comments_1", "comments") },
                    backTransition = backTransition,
                    frameMonitor = frameMonitor,
                    stackSize = navState.size
                )
                "comments" -> CommentsScreen(
                    onPop = { navState.pop() },
                    backTransition = backTransition,
                    frameMonitor = frameMonitor,
                    stackSize = navState.size
                )
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unknown Route: ${entry.route}", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun NavigationHomeScreen(
    onExitLab: () -> Unit,
    onOpenProfile: () -> Unit,
    stackSize: Int
) {
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
        Column {
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
                        .clickable { onExitLab() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Text(
                    text = "iOSFeel — Navigation Lab",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Screen 1: Home Feed",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Phase 4B multi-screen stack architecture. Test a full 4-tier navigation chain with preserved Saveable state, animated push springs, and interruptible edge swipe pop with seamless re-grabbing.",
                fontSize = 14.sp,
                color = Color(0xFFAEAEB2),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Stack Depth Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "ACTIVE STACK STATUS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8E8E93),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current Depth: $stackSize (Root Screen)",
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF34C759)
                    )
                    Text(
                        text = "Chain: Home → Profile → Post → Comments",
                        fontSize = 12.sp,
                        color = Color(0xFFAEAEB2)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0A84FF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Push Profile Screen (Screen 2) →", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProfileScreen(
    onPop: () -> Unit,
    onOpenPost: () -> Unit,
    backTransition: IOSBackTransitionState,
    frameMonitor: FrameMonitorState,
    stackSize: Int
) {
    // Preserved UI State via rememberSaveable
    var likesCount by rememberSaveable { mutableIntStateOf(42) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Navigation Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹ Home",
                fontSize = 17.sp,
                color = Color(0xFF0A84FF),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPop() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Profile",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(18.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Screen 2: User Profile",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(14.dp))

        // State Preservation Demo Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1C1E))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "SAVEABLE STATE PRESERVATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Likes: $likesCount",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF2D55)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { likesCount++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Increment Likes (Preserved on Pop/Push)", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Telemetry Card
        NavigationTelemetryCard(
            backTransition = backTransition,
            frameMonitor = frameMonitor,
            stackSize = stackSize
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onOpenPost,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Push Post Detail (Screen 3) →", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PostDetailScreen(
    onPop: () -> Unit,
    onOpenComments: () -> Unit,
    backTransition: IOSBackTransitionState,
    frameMonitor: FrameMonitorState,
    stackSize: Int
) {
    var bookmarkState by rememberSaveable { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18181A))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Navigation Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹ Profile",
                fontSize = 17.sp,
                color = Color(0xFF0A84FF),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPop() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Post #932",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(18.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Screen 3: Post Details",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Bookmarks Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1C1E))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bookmarks: $bookmarkState",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Button(
                    onClick = { bookmarkState++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Bookmark", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        NavigationTelemetryCard(
            backTransition = backTransition,
            frameMonitor = frameMonitor,
            stackSize = stackSize
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onOpenComments,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Push Comments (Screen 4) →", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CommentsScreen(
    onPop: () -> Unit,
    backTransition: IOSBackTransitionState,
    frameMonitor: FrameMonitorState,
    stackSize: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Navigation Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹ Post",
                fontSize = 17.sp,
                color = Color(0xFF0A84FF),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPop() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Comments Thread",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(18.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Screen 4: Comments (Deepest)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Swipe from left edge to pop back through Post → Profile → Home.",
            fontSize = 13.sp,
            color = Color(0xFF34C759)
        )

        Spacer(modifier = Modifier.height(14.dp))

        NavigationTelemetryCard(
            backTransition = backTransition,
            frameMonitor = frameMonitor,
            stackSize = stackSize
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onPop,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Pop to Post", fontSize = 14.sp)
        }
    }
}

@Composable
private fun NavigationTelemetryCard(
    backTransition: IOSBackTransitionState,
    frameMonitor: FrameMonitorState,
    stackSize: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF242426))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TRANSITION TELEMETRY",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8E8E93),
                letterSpacing = 1.sp
            )
            Text(
                text = "Progress: %.3f".format(backTransition.progress.value),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF0A84FF)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { backTransition.progress.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF0A84FF),
            trackColor = Color(0xFF38383A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        TelemetryRow("Stack Size", "$stackSize screens")
        TelemetryRow("Phase", backTransition.phase.name)
        TelemetryRow("Interactive", backTransition.isInteractive.toString())
        TelemetryRow("Velocity", "${backTransition.velocity.toInt()} px/s")
        TelemetryRow("Frame Time", "%.2f ms".format(frameMonitor.frameTimeMs))
        TelemetryRow("FPS", "${frameMonitor.approximateFps.toInt()}")
    }
}

@Composable
private fun TelemetryRow(
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
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
