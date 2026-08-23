# iOSFeel

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Min%20SDK-26%20(Oreo)-orange" alt="Min SDK" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License" />
</p>

**iOS-inspired interaction and motion framework for native Android, built with Kotlin and Jetpack Compose.**

iOSFeel is an experimental Android UI framework focused on recreating the **interaction quality** commonly associated with iOS — not simply copying how iOS components look.

---

## 📱 Why iOSFeel?

Many Android libraries can make an app **look** similar to iOS. But visual similarity alone does not make an interface feel the same.

```text
Button shape        → visual
Corner radius       → visual
Font size           → visual

Spring response     → interaction
Swipe interruption  → interaction
Scroll deceleration → interaction
Haptic timing       → interaction
Gesture velocity    → interaction
Sheet resistance    → interaction
```

iOSFeel focuses primarily on the second category: building native Android interfaces where movement feels physically connected to the user's finger.

---

## 📦 Installation

### Step 1: Add JitPack Repository

In your root **`settings.gradle.kts`** (or root `build.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

### Step 2: Add Dependencies

In your module's **`build.gradle.kts`** (e.g. `app/build.gradle.kts`):

#### Option A: All-in-One Component Kit (Recommended)
Includes all 8 underlying physics, motion, gesture, haptic, navigation, scroll, sheet, and blur engines + full component system:

```kotlin
dependencies {
    val iosFeelVersion = "main-SNAPSHOT" // or release tag e.g. "v1.0.0"

    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-components:$iosFeelVersion")
}
```

#### Option B: Standalone Modular Engines
Import only the specific engines your application needs:

```kotlin
dependencies {
    val iosFeelVersion = "main-SNAPSHOT"

    // Core tokens & shared primitives
    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-core:$iosFeelVersion")

    // Spring physics, velocity tracking & interruptible motion
    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-motion:$iosFeelVersion")

    // Semantic haptic feedback & impact patterns
    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-haptics:$iosFeelVersion")

    // Gesture lifecycle, directional locking & edge drag
    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-gesture:$iosFeelVersion")

    // Interactive swipe-back & predictive navigation stack
    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-navigation:$iosFeelVersion")

    // Elastic overscroll, non-linear resistance & decay flings
    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-scroll:$iosFeelVersion")

    // Multi-detent interactive bottom sheets with nested scroll
    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-sheet:$iosFeelVersion")

    // iOS 18-style frosted backdrop blur materials
    implementation("com.github.AnkitChoudhary-1.IOS-feel-Framework:iosfeel-material:$iosFeelVersion")
}
```

---

## ⚡ Quick Start Guide

### 1. Apply the Theme (`IOSFeelTheme`)

Wrap your root Composable or Activity with `IOSFeelTheme`:

```kotlin
import dev.iosfeel.components.theme.IOSFeelTheme

@Composable
fun App() {
    IOSFeelTheme(darkTheme = isSystemInDarkTheme()) {
        MainScreen()
    }
}
```

---

### 2. Interactive Navigation Stack with Swipe-Back

```kotlin
import androidx.compose.runtime.Composable
import dev.iosfeel.navigation.IOSNavigationStack
import dev.iosfeel.navigation.rememberIOSNavigationState

@Composable
fun AppNavigation() {
    val navState = rememberIOSNavigationState(initialRoute = "home")

    IOSNavigationStack(
        state = navState,
        swipeBackEnabled = true
    ) { route ->
        when (route) {
            "home" -> HomeScreen(
                onOpenProfile = { navState.push("profile") }
            )
            "profile" -> ProfileScreen(
                onBack = { navState.pop() }
            )
        }
    }
}
```

---

### 3. Elastic 120Hz Overscroll (`IOSScrollableLazyColumn`)

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.scroll.rememberIOSScrollInteractionState

@Composable
fun FeedList() {
    val interactionState = rememberIOSScrollInteractionState()

    IOSScrollableLazyColumn(
        interactionState = interactionState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(40) { index ->
            Text(
                text = "Post #$index",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
```

---

### 4. Interactive Bottom Sheet (`IOSSheet`)

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.sheet.IOSSheet
import dev.iosfeel.sheet.IOSSheetDetent
import dev.iosfeel.sheet.rememberIOSSheetState

@Composable
fun CommentsSheet(onDismiss: () -> Unit) {
    val sheetState = rememberIOSSheetState(
        initialDetent = IOSSheetDetent.Medium,
        supportedDetents = setOf(
            IOSSheetDetent.Compact,
            IOSSheetDetent.Medium,
            IOSSheetDetent.Large
        )
    )

    IOSSheet(
        state = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Comments")
            // Sheet content...
        }
    }
}
```

---

### 5. Frosted Blur Backdrop (`IOSMaterialSurface`)

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.material.rememberIOSBackdropState

@Composable
fun FrostedScene() {
    val backdropState = rememberIOSBackdropState()

    IOSBackdropLayout(
        state = backdropState,
        backdrop = {
            // Your scrolling feed or vibrant background
        },
        overlay = {
            // Floating frosted material surface
            IOSMaterialSurface(
                backdrop = backdropState,
                config = IOSMaterialConfig(
                    style = IOSMaterialStyle.Regular, // UltraThin, Thin, Regular, Thick
                    cornerRadius = 24.dp
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Home")
                    Text("Explore")
                    Text("Profile")
                }
            }
        }
    )
}
```

---

### 6. Semantic Haptics

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.IOSNotification
import dev.iosfeel.haptics.rememberIOSHaptics

@Composable
fun HapticButton() {
    val haptics = rememberIOSHaptics()

    Button(onClick = {
        haptics.impact(IOSImpact.Light) // Light, Medium, Heavy, Soft, Rigid
    }) {
        Text("Tap with iOS Haptic")
    }
}
```

---

### 7. UI Components Kit

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.button.IOSButton
import dev.iosfeel.components.button.IOSButtonStyle
import dev.iosfeel.components.iconbutton.IOSIconButton
import dev.iosfeel.components.navigation.IOSNavigationBar
import dev.iosfeel.components.segmented.IOSSegmentedControl
import dev.iosfeel.components.segmented.IOSSegmentedItem
import dev.iosfeel.components.slider.IOSSlider
import dev.iosfeel.components.tab.IOSTabBar
import dev.iosfeel.components.tab.IOSTabItem
import dev.iosfeel.components.toggle.IOSToggle

@Composable
fun ComponentsExample() {
    var toggleChecked by remember { mutableStateOf(true) }
    var selectedSection by remember { mutableStateOf("Posts") }
    var sliderValue by remember { mutableStateOf(0.5f) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Navigation Bar
        IOSNavigationBar(
            title = "Settings",
            backButtonVisible = true,
            onBack = { /* Pop */ }
        )

        // Buttons
        IOSButton(
            onClick = { /* Follow */ },
            style = IOSButtonStyle.Filled // Filled, Tinted, Material, Plain
        ) {
            Text("Follow")
        }

        // Toggle Switch
        IOSToggle(
            checked = toggleChecked,
            onCheckedChange = { toggleChecked = it }
        )

        // Segmented Control
        IOSSegmentedControl(
            items = listOf(
                IOSSegmentedItem("Posts", "Posts"),
                IOSSegmentedItem("Reels", "Reels"),
                IOSSegmentedItem("Tagged", "Tagged")
            ),
            selectedValue = selectedSection,
            onSelected = { selectedSection = it }
        )

        // Stepped / Continuous Slider
        IOSSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            steps = 4
        )
    }
}
```

---

## 🏗️ Architecture

```text
                                         app
                                          │
                                  iosfeel-components
                                          │
      ┌───────────────┬───────────────────┼───────────────┬───────────────┬───────────────┐
      ↓               ↓                   ↓               ↓               ↓               ↓
iosfeel-motion  iosfeel-haptics   iosfeel-gesture  iosfeel-scroll  iosfeel-sheet  iosfeel-material
      │               │                   │               │               │               │
      └───────────────┴───────────────────┼───────────────┴───────────────┴───────────────┘
                                          ↓
                                  iosfeel-navigation
                                          ↓
                                    iosfeel-core
```

| Module | Description | Key APIs |
| :--- | :--- | :--- |
| **`iosfeel-core`** | Central design tokens, interaction phases, shared math | `IOSSpacing`, `IOSShapes`, `IOSMotionTokens`, `IOSComponentTokens` |
| **`iosfeel-motion`** | Physical spring engine with velocity continuity and interruption | `rememberIOSMotionState()`, `IOSSpringSpec`, `IOSMotionPreset` |
| **`iosfeel-haptics`** | Semantic haptics with rich fallback mechanisms | `rememberIOSHaptics()`, `IOSImpact`, `IOSNotification` |
| **`iosfeel-gesture`** | Full gesture lifecycle, directional locking, edge tracking | `IOSGestureConfig`, `IOSGesturePhase`, `iosEdgeDragModifier` |
| **`iosfeel-navigation`** | Interactive swipe-to-pop navigation and Predictive Back | `IOSNavigationStack`, `rememberIOSNavigationState()` |
| **`iosfeel-scroll`** | Elastic boundaries, non-linear overscroll resistance, decay flings | `IOSScrollableLazyColumn`, `rememberIOSScrollInteractionState()` |
| **`iosfeel-sheet`** | Multi-detent bottom sheet with nested scroll & IME handling | `IOSSheet`, `rememberIOSSheetState()`, `IOSSheetDetent` |
| **`iosfeel-material`** | Frosted blur backdrop system with Android 12+ hardware acceleration | `IOSMaterialSurface`, `IOSBackdropLayout`, `IOSMaterialStyle` |
| **`iosfeel-components`** | Production iOS 18-styled Compose component kit | `IOSButton`, `IOSToggle`, `IOSSegmentedControl`, `IOSTabBar`, `IOSSlider`, `IOSMenu` |

---

## 🎯 Core Principles

1. **Native Android First**: Built directly on Kotlin, Jetpack Compose, Android input APIs, Android Haptics, and `RenderEffect`. No web views or custom rendering engines.
2. **Behavior Over Pixel-Cloning**: Prioritizes gesture dynamics, physical springs, interruption, deceleration, and continuity over static visual replication.
3. **One Interaction → One Progress Value**: Coordinates screen position, navigation bar transition, shadow alpha, and haptic thresholds from a single continuous interaction state.
4. **Interruptible by Default**: Animations never lock the interface; tapping or dragging an animating element immediately hands control back to the user's finger.

---

## ⚡ Performance & 120Hz Target

- **Zero Allocation in Hot Paths**: Pre-calculated spring matrices and cached render layers.
- **Localized Backdrop Rendering**: Blurs only targeted overlays rather than full-screen passes.
- **Compose GraphicsLayer Integration**: Hardware-accelerated GPU offloading for translation and scale transforms.
- Tested and tuned for **60Hz, 90Hz, and 120Hz** displays.

---

## 📋 System Requirements

- **Min SDK**: API 26 (Android 8.0 Oreo)
- **Compile / Target SDK**: API 34+
- **Jetpack Compose**: Compose 1.6+ (BOM 2024.04.01+)
- **Kotlin**: 2.0+

---

## 🛣️ Project Roadmap

- **Phase 0** Foundation ✅
- **Phase 1** Motion Engine ✅
- **Phase 2** Haptic Engine ✅
- **Phase 3** Gesture Engine ✅
- **Phase 4** Interactive Navigation ✅
- **Phase 5** Scroll Physics ✅
- **Phase 6** Sheets ✅
- **Phase 7** Frosted Blur Materials ✅ *(Revised)*
- **Phase 8** Components Kit ✅ *(Revised)*
- **Phase 9** Real Social-App Benchmark ⏳ *(Next)*

---

## 📄 License

```
Copyright 2026 iOSFeel Framework Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

<p align="center">
  <b>iOSFeel</b><br>
  Native Android. Interaction first.
</p>