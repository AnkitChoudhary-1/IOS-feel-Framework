package dev.iosfeel.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dev.iosfeel.components.badge.IOSBadge
import dev.iosfeel.components.button.IOSButton
import dev.iosfeel.components.button.IOSButtonStyle
import dev.iosfeel.components.iconbutton.IOSIconButton
import dev.iosfeel.components.list.IOSListRow
import dev.iosfeel.components.list.IOSListSection
import dev.iosfeel.components.menu.IOSMenu
import dev.iosfeel.components.navigation.IOSNavigationBar
import dev.iosfeel.components.search.IOSSearchField
import dev.iosfeel.components.segmented.IOSSegmentedControl
import dev.iosfeel.components.segmented.IOSSegmentedItem
import dev.iosfeel.components.slider.IOSSlider
import dev.iosfeel.components.tab.IOSTabBar
import dev.iosfeel.components.tab.IOSTabItem
import dev.iosfeel.components.theme.IOSFeelTheme
import dev.iosfeel.components.toggle.IOSToggle
import dev.iosfeel.core.tokens.IOSActionRole
import dev.iosfeel.core.tokens.IOSShapes
import dev.iosfeel.core.tokens.IOSSpacing
import kotlin.math.roundToInt

enum class ComponentLabMode {
    Catalog,
    ProfilePrototype
}

@Composable
fun ComponentLaboratory(
    onBack: () -> Unit
) {
    val frameMonitor = rememberFrameMonitor()
    var currentMode by remember { mutableStateOf(ComponentLabMode.Catalog) }
    var isDarkTheme by remember { mutableStateOf(true) }

    // Catalog States
    var toggleState by remember { mutableStateOf(true) }
    var continuousSliderValue by remember { mutableFloatStateOf(0.65f) }
    var steppedSliderValue by remember { mutableFloatStateOf(3f) }
    var selectedSegment by remember { mutableStateOf("posts") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("home") }
    var menuExpanded by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("None") }

    val modeItems = remember {
        listOf(
            IOSSegmentedItem(ComponentLabMode.Catalog, "Catalog"),
            IOSSegmentedItem(ComponentLabMode.ProfilePrototype, "Profile Demo")
        )
    }

    val segmentItems = remember {
        listOf(
            IOSSegmentedItem("posts", "Posts"),
            IOSSegmentedItem("reels", "Reels"),
            IOSSegmentedItem("tagged", "Tagged")
        )
    }

    val tabItems = remember {
        listOf(
            IOSTabItem("home", "Home") { isSelected ->
                Text("🏠", fontSize = 18.sp, color = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93))
            },
            IOSTabItem("search", "Search") { isSelected ->
                Text("🔍", fontSize = 18.sp, color = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93))
            },
            IOSTabItem("create", null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF007AFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            IOSTabItem("reels", "Reels", badgeCount = 5) { isSelected ->
                Text("🎬", fontSize = 18.sp, color = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93))
            },
            IOSTabItem("profile", "Profile", showBadgeDot = true) { isSelected ->
                Text("👤", fontSize = 18.sp, color = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93))
            }
        )
    }

    IOSFeelTheme(darkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(IOSFeelTheme.colors.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Navigation Bar
                IOSNavigationBar(
                    title = if (currentMode == ComponentLabMode.Catalog) "Components v2" else "ankit",
                    backButtonVisible = true,
                    backButtonLabel = "Lab",
                    onBack = onBack,
                    material = true,
                    trailing = {
                        IOSIconButton(
                            onClick = { menuExpanded = true },
                            contentDescription = "Options"
                        ) {
                            Text("⋯", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                        }
                    }
                )

                // Mode Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        IOSSegmentedControl(
                            items = modeItems,
                            selectedValue = currentMode,
                            onSelected = { currentMode = it }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IOSIconButton(
                        onClick = { isDarkTheme = !isDarkTheme },
                        material = true,
                        contentDescription = "Toggle Dark Mode"
                    ) {
                        Text(if (isDarkTheme) "🌙" else "☀️", fontSize = 16.sp)
                    }
                }

                when (currentMode) {
                    ComponentLabMode.Catalog -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Section 1: Buttons & Icon Buttons
                            item {
                                CatalogSectionHeader(title = "BUTTONS & ICON BUTTONS")

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IOSButton(
                                            onClick = { lastAction = "Primary Clicked" },
                                            style = IOSButtonStyle.Filled,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Primary Filled", fontWeight = FontWeight.SemiBold)
                                        }

                                        IOSButton(
                                            onClick = { lastAction = "Tinted Clicked" },
                                            style = IOSButtonStyle.Tinted,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Tinted Button", fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IOSButton(
                                            onClick = { lastAction = "Material Clicked" },
                                            style = IOSButtonStyle.Material,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("✨ Material Button", fontWeight = FontWeight.SemiBold)
                                        }

                                        IOSIconButton(
                                            onClick = { lastAction = "Heart Icon Clicked" },
                                            material = true,
                                            contentDescription = "Like"
                                        ) {
                                            Text("❤️", fontSize = 16.sp)
                                        }

                                        IOSIconButton(
                                            onClick = { lastAction = "Bookmark Icon Clicked" },
                                            contentDescription = "Bookmark"
                                        ) {
                                            Text("🔖", fontSize = 16.sp)
                                        }
                                    }
                                }
                            }

                            // Section 2: Sliders & Badges
                            item {
                                CatalogSectionHeader(title = "SLIDERS & BADGES")

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(IOSShapes.Card)
                                        .background(IOSFeelTheme.colors.surface)
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Continuous Volume",
                                                fontSize = 14.sp,
                                                color = IOSFeelTheme.colors.labelPrimary
                                            )
                                            Text(
                                                text = "${(continuousSliderValue * 100).roundToInt()}%",
                                                fontSize = 13.sp,
                                                color = IOSFeelTheme.colors.labelSecondary
                                            )
                                        }

                                        IOSSlider(
                                            value = continuousSliderValue,
                                            onValueChange = { continuousSliderValue = it }
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Stepped Snapping (5 steps)",
                                                fontSize = 14.sp,
                                                color = IOSFeelTheme.colors.labelPrimary
                                            )
                                            Text(
                                                text = "Step ${steppedSliderValue.roundToInt()}",
                                                fontSize = 13.sp,
                                                color = IOSFeelTheme.colors.labelSecondary
                                            )
                                        }

                                        IOSSlider(
                                            value = steppedSliderValue,
                                            onValueChange = { steppedSliderValue = it },
                                            valueRange = 0f..5f,
                                            steps = 4
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Badges:", fontSize = 13.sp, color = IOSFeelTheme.colors.labelSecondary)
                                            IOSBadge(count = null) // Dot
                                            IOSBadge(count = 3)
                                            IOSBadge(count = 42)
                                            IOSBadge(count = 120) // 99+
                                        }
                                    }
                                }
                            }

                            // Section 3: Toggle & Search
                            item {
                                CatalogSectionHeader(title = "TOGGLE & SEARCH FIELD")

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(IOSShapes.Card)
                                        .background(IOSFeelTheme.colors.surface)
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Haptic Feedback", fontSize = 15.sp, color = IOSFeelTheme.colors.labelPrimary)
                                            Text("Spring-animated thumb travel", fontSize = 12.sp, color = IOSFeelTheme.colors.labelSecondary)
                                        }

                                        IOSToggle(
                                            checked = toggleState,
                                            onCheckedChange = {
                                                toggleState = it
                                                lastAction = "Toggle changed: $it"
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                IOSSearchField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = "Search catalog or settings...",
                                    onSearch = { lastAction = "Search: $it" }
                                )
                            }

                            // Section 4: Grouped List Sections
                            item {
                                CatalogSectionHeader(title = "GROUPED SETTINGS SECTIONS")

                                IOSListSection(
                                    title = "Account & Privacy",
                                    footer = "Manage your Apple-grade Android experience and security credentials."
                                ) {
                                    IOSListRow(
                                        title = "Personal Information",
                                        subtitle = "Email, Phone, Birthday",
                                        leading = { Text("👤", fontSize = 20.sp) },
                                        onClick = { lastAction = "Clicked Personal Info" }
                                    )
                                    IOSListRow(
                                        title = "Face ID & Passcode",
                                        subtitle = "Biometrics & App Lock",
                                        leading = { Text("🛡️", fontSize = 20.sp) },
                                        onClick = { lastAction = "Clicked Face ID" }
                                    )
                                    IOSListRow(
                                        title = "Sound & Haptics",
                                        subtitle = "Rich Haptic Patterns",
                                        leading = { Text("🔔", fontSize = 20.sp) },
                                        showDivider = false,
                                        onClick = { lastAction = "Clicked Haptics" }
                                    )
                                }
                            }

                            // Live Telemetry Panel
                            item {
                                CatalogSectionHeader(title = "TELEMETRY & ACTIONS")

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(IOSShapes.Card)
                                        .background(IOSFeelTheme.colors.surface)
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Last Action: $lastAction",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = IOSFeelTheme.colors.success
                                        )
                                        Text(
                                            text = "FPS: ${frameMonitor.approximateFps.roundToInt()} | Frame: %.2f ms".format(frameMonitor.frameTimeMs),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = IOSFeelTheme.colors.labelPrimary
                                        )
                                        Text(
                                            text = "Theme: ${if (isDarkTheme) "Dark" else "Light"} | Tab: $selectedTab",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = IOSFeelTheme.colors.labelSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(90.dp))
                            }
                        }
                    }

                    ComponentLabMode.ProfilePrototype -> {
                        // Integrated Social Profile Prototype
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Profile Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFFFF2D55),
                                                    Color(0xFFFF9500),
                                                    Color(0xFF5856D6),
                                                    Color(0xFFFF2D55)
                                                )
                                            )
                                        )
                                        .padding(3.dp)
                                        .clip(CircleShape)
                                        .background(IOSFeelTheme.colors.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👨‍💻", fontSize = 36.sp)
                                }

                                // Stats
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ProfileStatItem(count = "128", label = "Posts")
                                    ProfileStatItem(count = "3.4K", label = "Followers")
                                    ProfileStatItem(count = "412", label = "Following")
                                }
                            }

                            // Bio
                            Text(
                                text = "Ankit Sharma",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = IOSFeelTheme.colors.labelPrimary
                            )
                            Text(
                                text = "Building iOSFeel for Android • 120Hz physics & Glass UI 🚀",
                                fontSize = 13.sp,
                                color = IOSFeelTheme.colors.labelPrimary.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "🔗 github.com/iosfeel/framework",
                                fontSize = 13.sp,
                                color = IOSFeelTheme.colors.accent,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IOSButton(
                                    onClick = { lastAction = "Followed" },
                                    style = IOSButtonStyle.Filled,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Follow", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }

                                IOSButton(
                                    onClick = { lastAction = "Message Opened" },
                                    style = IOSButtonStyle.Tinted,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Message", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }

                                IOSIconButton(
                                    onClick = { menuExpanded = true },
                                    material = true,
                                    contentDescription = "More"
                                ) {
                                    Text("⌄", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tab Switcher
                            IOSSegmentedControl(
                                items = segmentItems,
                                selectedValue = selectedSegment,
                                onSelected = { selectedSegment = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid Feed
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(18) { index ->
                                    val gradient = when (index % 4) {
                                        0 -> listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
                                        1 -> listOf(Color(0xFFFF2D55), Color(0xFFFF9500))
                                        2 -> listOf(Color(0xFF30D158), Color(0xFF007AFF))
                                        else -> listOf(Color(0xFFBF5AF2), Color(0xFFFF375F))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Brush.linearGradient(gradient)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Options Context Menu
            IOSMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                item(
                    label = "Save to Collection",
                    icon = "🔖",
                    onClick = { lastAction = "Saved to Collection" }
                )
                item(
                    label = "Share Profile",
                    icon = "↗",
                    onClick = { lastAction = "Shared Profile" }
                )
                item(
                    label = "Copy Profile Link",
                    icon = "📋",
                    onClick = { lastAction = "Copied Profile Link" }
                )
                separator()
                item(
                    label = "Block User",
                    icon = "🚫",
                    role = IOSActionRole.Destructive,
                    onClick = { lastAction = "User Blocked" }
                )
            }

            // Floating Glass Bottom Tab Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                IOSTabBar(
                    items = tabItems,
                    selected = selectedTab,
                    onSelected = {
                        selectedTab = it
                        lastAction = "Tab selected: $it"
                    },
                    onReselect = {
                        lastAction = "Tab reselected (Scroll to top): $it"
                    }
                )
            }
        }
    }
}

@Composable
private fun CatalogSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = IOSFeelTheme.colors.labelSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = IOSFeelTheme.colors.labelPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = IOSFeelTheme.colors.labelSecondary
        )
    }
}
