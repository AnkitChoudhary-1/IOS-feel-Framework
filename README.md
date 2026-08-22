# iOSFeel

**iOS-inspired interaction and motion framework for native Android, built with Kotlin and Jetpack Compose.**

iOSFeel is an experimental Android UI framework focused on recreating the **interaction quality** commonly associated with iOS — not simply copying how iOS components look.

The project focuses on things that are difficult to reproduce with a normal UI theme:

- spring-based motion
- interruptible animations
- gesture-driven navigation
- velocity-aware transitions
- elastic scrolling
- semantic haptics
- interactive bottom sheets
- iOS-style frosted blur
- subtle press interactions
- reusable native Compose components

> iOSFeel does **not** run iOS apps, emulate UIKit, use Apple private APIs, or attempt to reproduce Apple's proprietary implementation.

---

## Why iOSFeel?

Many Android libraries can make an app **look** similar to iOS.

But visual similarity alone does not make an interface feel the same.

For example:

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

iOSFeel focuses primarily on the second category.

The goal is to build Android interfaces where movement feels physically connected to the user's finger.

---

## Core Principles

### Native Android First

iOSFeel is built directly on:

- Kotlin
- Jetpack Compose
- Android input APIs
- Android haptics
- Android predictive back
- Android graphics APIs

It does not introduce another rendering engine.

### Behavior Over Pixel-Perfect Cloning

The framework prioritizes:

- gesture behavior
- motion
- velocity
- interruption
- scroll physics
- haptics
- transition continuity

over copying screenshots.

### One Interaction → One Progress Value

Instead of running unrelated animations:

```text
screen animation
title animation
shadow animation
background animation
```

iOSFeel tries to derive them from one physical interaction:

```text
gesture progress
       │
       ├── screen position
       ├── previous screen
       ├── navigation bar
       ├── material intensity
       └── haptic thresholds
```

This keeps transitions visually and physically connected.

### Interruptible by Default

Animations should not lock the interface.

Example:

```text
release sheet
     ↓
sheet starts springing
     ↓
touch it again
     ↓
spring stops immediately
     ↓
finger takes control
```

This principle is shared across:

- navigation
- sheets
- scrolling
- controls
- motion primitives

---

## Architecture

```text
iOSFeel
│
├── iosfeel-core
│   ├── tokens
│   ├── shared utilities
│   └── foundational APIs
│
├── iosfeel-motion
│   ├── spring motion
│   ├── velocity
│   ├── interruption
│   └── reusable motion state
│
├── iosfeel-haptics
│   ├── semantic feedback
│   ├── impacts
│   ├── thresholds
│   └── device capability handling
│
├── iosfeel-gesture
│   ├── gesture lifecycle
│   ├── velocity tracking
│   ├── direction locking
│   └── edge gestures
│
├── iosfeel-navigation
│   ├── navigation stack
│   ├── interactive swipe-back
│   ├── predictive back
│   └── push/pop transitions
│
├── iosfeel-scroll
│   ├── fling physics
│   ├── elastic boundaries
│   ├── overscroll
│   └── nested scrolling
│
├── iosfeel-sheet
│   ├── detents
│   ├── interactive dragging
│   ├── nested scrolling
│   ├── IME handling
│   └── dismissal
│
├── iosfeel-material
│   ├── backdrop capture
│   ├── frosted blur
│   ├── translucency
│   └── Android fallback rendering
│
└── iosfeel-components
    ├── buttons
    ├── toggles
    ├── segmented controls
    ├── sliders
    ├── navigation bars
    ├── tab bars
    ├── search fields
    ├── lists
    └── menus
```

---

## Motion Engine

Instead of describing animation only by duration:

```kotlin
animate(duration = 300)
```

iOSFeel models movement using physical state:

- position
- velocity
- target
- stiffness
- damping
- phase

Conceptually:

```kotlin
val motion = rememberIOSMotionState()

motion.springTo(
    targetPosition = 0f,
    initialVelocity = releaseVelocity,
    spec = IOSMotionPreset.Smooth
)
```

The current velocity is preserved when transitioning from finger-driven movement to spring-driven movement.

---

## Gesture Engine

Gestures expose reusable state:

- `translationX`, `translationY`
- `velocityX`, `velocityY`
- `progress`
- `phase`

Example lifecycle:

```text
Possible → Began → Changed → Ended
```
or:
```text
Possible → Cancelled
```

The gesture engine also supports velocity-aware decisions:

- **short + slow swipe** → cancel
- **long swipe** → complete
- **short + very fast flick** → complete

---

## Interactive Navigation

The navigation system supports a screen following the user's finger during a back gesture.

```text
Screen A
   ↓
Screen B
   ↓
left-edge swipe →

Screen B follows finger
Screen A reveals underneath
```

When released:

```text
progress + velocity
        ↓
 COMPLETE or CANCEL
```

The transition then continues using spring physics. Android Predictive Back can also drive the same transition renderer.

---

## Scroll Physics

Scrolling is treated as an important part of interaction design.

iOSFeel experiments with:

- velocity-based fling
- configurable deceleration
- elastic boundaries
- nonlinear resistance
- spring-back
- fling interruption
- nested scrolling

Architecture:

```text
finger
  ↓
Compose scroll
  ↓
content consumption
  ↓
nested scroll
  ↓
remaining movement
  ↓
elastic overscroll
```

iOSFeel does not replace `LazyColumn`. Instead it integrates with Compose's existing scrolling infrastructure.

---

## Haptic Engine

Components do not request raw vibration durations. Instead they request meaning:

```kotlin
haptics.selection()

haptics.impact(IOSImpact.Light)

haptics.notification(IOSNotification.Success)
```

The engine selects the best available implementation for the device. Haptics are treated as interaction punctuation, not constant vibration:

```text
drag → drag → drag → cross snap threshold → *tick*
```

---

## Sheets

`IOSSheet` combines motion, gestures, haptics, and nested scrolling.

Example detents:

```kotlin
listOf(
    IOSSheetDetent.Compact,
    IOSSheetDetent.Medium,
    IOSSheetDetent.Large
)
```

Behavior:
```text
Compact ↔ Medium ↔ Large
```

A fast fling can move toward the next logical detent even when the closest position is somewhere else.

Sheets are designed to support:
- interruption & re-grabbing
- detent haptics
- nested `LazyColumn`
- IME / keyboard handling
- drag-to-dismiss & scrim dismissal
- state restoration
- accessibility actions

---

## iOS-Style Blur Materials

iOSFeel intentionally uses a clean frosted blur material system:

```text
background content
       ↓
localized backdrop blur
       ↓
translucent tint
       ↓
subtle separator
       ↓
sharp foreground content
```

No:
- ❌ refraction
- ❌ lens distortion
- ❌ moving shine
- ❌ glass wobble
- ❌ Liquid Glass shaders

On Android 12+ (API 31+), the framework uses native graphics blur (`RenderEffect`). Older versions fall back gracefully to translucent materials.

---

## Components

The component module builds reusable controls on top of the lower-level engines:

- `IOSButton` & `IOSIconButton`
- `IOSToggle`
- `IOSSegmentedControl`
- `IOSSlider`
- `IOSSearchField`
- `IOSListRow` & `IOSListSection`
- `IOSNavigationBar`
- `IOSTabBar`
- `IOSMenu`
- `IOSBadge`

Components reuse shared motion/haptic/material behavior instead of implementing their own interaction systems.

---

## Example

```kotlin
@Composable
fun ExampleScreen() {
    IOSFeelTheme {
        var selected by remember {
            mutableStateOf("Posts")
        }

        Column {
            IOSNavigationBar(
                title = "Profile"
            )

            IOSSegmentedControl(
                items = listOf(
                    IOSSegmentedItem("Posts", "Posts"),
                    IOSSegmentedItem("Reels", "Reels"),
                    IOSSegmentedItem("Tagged", "Tagged")
                ),
                selectedValue = selected,
                onSelected = { selected = it }
            )

            IOSButton(
                onClick = { /* Follow */ }
            ) {
                Text("Follow")
            }
        }
    }
}
```

---

## Design Philosophy

iOSFeel aims to remain:
- **native**
- **small & modular**
- **interruptible**
- **testable**
- **accessible**
- **performance-aware**

It is not a second operating system or another Flutter-like rendering engine. Where Android provides excellent infrastructure, iOSFeel leverages it directly.

---

## Performance

Interaction quality means nothing if it introduces jank.

The project is designed around:
- GPU translations
- normalized interaction state
- small localized blur regions
- minimal per-frame allocations
- Compose native scrolling
- interruptible animations

Performance testing targets 60 Hz, 90 Hz, and 120 Hz displays across scrolling feeds, sheets, transitions, and blurred surfaces.

---

## Accessibility

Custom visuals should never remove native accessibility.

Components expose proper semantics for:
- buttons, switches, tabs, sliders, menus
- editable text & selected state
- dismiss actions
- large font sizes & screen readers
- keyboard navigation & reduced motion

---

## Project Roadmap

- **Phase 0** Foundation ✅
- **Phase 1** Motion Engine ✅
- **Phase 2** Haptic Engine ✅
- **Phase 3** Gesture Engine ✅
- **Phase 4** Interactive Navigation ✅
- **Phase 5** Scroll Physics ✅
- **Phase 6** Sheets ✅
- **Phase 7** Frosted Blur Materials ✅ *(Revised)*
- **Phase 8** Components ✅ *(Revised)*
- **Phase 9** Real Social-App Benchmark ⏳ *(Next)*

Phase 9 focuses on validating the framework against a realistic social-media style interface rather than adding more low-level features.

---

## What iOSFeel Is Not

iOSFeel is not:
- an iOS emulator
- an XNU implementation
- UIKit for Android
- an IPA runtime
- an Apple API compatibility layer
- a pixel-perfect iOS clone
- a replacement for Jetpack Compose

It is an experimental interaction framework for native Android.

---

## Status

🚧 **Experimental / Early Development**

APIs may change while interaction models and physics are being tuned.

---

## Inspiration & Attribution

iOSFeel is inspired by the interaction quality and motion principles found across modern mobile interfaces. The project is independently implemented using publicly available Android APIs and does not contain Apple proprietary code, private APIs, assets, or implementation details.

---

## License

Apache License 2.0

<p align="center">
  <b>iOSFeel</b><br>
  Native Android. Interaction first.
</p>