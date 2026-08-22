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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun LaboratoryApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            LaboratoryHome(navController = navController)
        }
        composable("motion") {
            MotionLaboratory(onBack = { navController.popBackStack() })
        }
        composable("haptics") {
            HapticLaboratory(onBack = { navController.popBackStack() })
        }
        composable("gestures") {
            GestureLaboratory(onBack = { navController.popBackStack() })
        }
        composable("navigation") {
            NavigationLaboratory(onBack = { navController.popBackStack() })
        }
        composable("scroll") {
            ScrollLaboratory(onBack = { navController.popBackStack() })
        }
        composable("sheet") {
            SheetLaboratory(onBack = { navController.popBackStack() })
        }
        composable("materials") {
            MaterialLaboratory(onBack = { navController.popBackStack() })
        }
        composable("components") {
            ComponentLaboratory(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun LaboratoryHome(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // App Header
        Text(
            text = "iOSFeel",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "iOS-Grade Interaction Runtime for Android",
            fontSize = 14.sp,
            color = Color(0xFF8E8E93)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Active Lab Modules
        Text(
            text = "LABORATORIES",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8E8E93),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabItem(
            title = "Motion Laboratory",
            description = "Phase 1: Springs, presets, velocity tracking, and frame metrics",
            status = "AVAILABLE",
            statusColor = Color(0xFF34C759),
            onClick = { navController.navigate("motion") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabItem(
            title = "Haptic Laboratory",
            description = "Phase 2: Semantic View feedback, rich compositions, rate limiting",
            status = "AVAILABLE",
            statusColor = Color(0xFF34C759),
            onClick = { navController.navigate("haptics") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabItem(
            title = "Gesture Laboratory",
            description = "Phase 3: Lifecycle, direction locking, edge swipe, and completion decisions",
            status = "AVAILABLE",
            statusColor = Color(0xFF34C759),
            onClick = { navController.navigate("gestures") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabItem(
            title = "Navigation Laboratory",
            description = "Phase 4: Multi-screen stack, interactive edge-pop, Predictive Back, parallax",
            status = "AVAILABLE",
            statusColor = Color(0xFF34C759),
            onClick = { navController.navigate("navigation") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabItem(
            title = "Scroll Laboratory",
            description = "Phase 5: Spline decay fling, elastic rubber-banding, nested-scroll handoff",
            status = "AVAILABLE",
            statusColor = Color(0xFF34C759),
            onClick = { navController.navigate("scroll") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabItem(
            title = "Sheet Laboratory",
            description = "Phase 6: Multi-detent bottom sheet, velocity-driven snapping, nested list handoff",
            status = "AVAILABLE",
            statusColor = Color(0xFF34C759),
            onClick = { navController.navigate("sheet") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabItem(
            title = "Materials & Glass",
            description = "Phase 7: Multi-tier glass rendering, directional highlights, AGSL shaders",
            status = "AVAILABLE",
            statusColor = Color(0xFF34C759),
            onClick = { navController.navigate("materials") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabItem(
            title = "Component System",
            description = "Phase 8: IOSButtons, IOSToggle, Segmented Control, List Rows, TabBar, Search",
            status = "AVAILABLE",
            statusColor = Color(0xFF34C759),
            onClick = { navController.navigate("components") }
        )
    }
}

@Composable
private fun LabItem(
    title: String,
    description: String,
    status: String,
    statusColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1E))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFFAEAEB2),
                lineHeight = 18.sp
            )
        }
    }
}
