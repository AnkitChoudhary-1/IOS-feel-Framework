# Project: iOSFeel — High-Fidelity iOS Interaction Framework for Native Android

You are a senior Android framework engineer specializing in Kotlin, Jetpack Compose, Android graphics, touch/input systems, animation physics, haptics, rendering performance, and UI framework architecture.

Your task is to architect and progressively implement an experimental open-source **native Android UI/interaction framework** called **iOSFeel**.

The objective is **not** to emulate iOS, run iOS applications, copy Apple proprietary code/assets, or recreate UIKit/SwiftUI internally.

The objective is:

> Build a native Kotlin/Jetpack Compose framework that uses Android's capabilities as deeply as practical to reproduce the *interaction qualities* associated with polished modern iOS applications: physical motion, responsive gestures, elastic scrolling, interactive navigation, synchronized haptics, translucent materials, fluid sheets, interruption-friendly animations, and coherent component behavior.

The framework must be usable by ordinary native Android applications.

Example desired developer experience:

```kotlin
IOSNavigationStack {
    IOSScaffold(
        topBar = {
            IOSNavigationBar(
                title = "Instagram"
            )
        },
        bottomBar = {
            IOSTabBar(...)
        }
    ) {
        IOSBouncyScroll {
            Feed()
        }
    }
}
```

---

# 1. NON-GOALS

Do NOT attempt to:

* recreate XNU;
* modify the Android kernel;
* execute `.ipa` files;
* emulate an iPhone;
* recreate private Apple APIs;
* copy Apple's proprietary source code;
* distribute Apple assets or fonts without permission;
* pretend that Android is actually running UIKit;
* create pixel-perfect clones by hardcoding screenshots;
* simply rename Material components to `IOSButton`, etc.

This project is about **behavioral and interaction fidelity**, not superficial theming.

---

# 2. CORE PHILOSOPHY

Most "iOS-style Android" libraries focus on:

* corner radius;
* colors;
* icons;
* typography;
* spacing.

That is insufficient.

A polished interface feels polished because:

```text
Touch
 ↓
Gesture recognition
 ↓
Velocity
 ↓
Interactive movement
 ↓
Physical animation
 ↓
Visual transformation
 ↓
Haptic response
 ↓
Final state
```

These systems must work together.

Therefore, iOSFeel should primarily be an **interaction and motion framework**, with components built on top of it.

Architecture:

```text
Application
      ↓
iOSFeel Components
      ↓
────────────────────────
Navigation Engine
Gesture Engine
Motion Engine
Scroll Physics
Haptic Engine
Material Engine
────────────────────────
      ↓
Jetpack Compose
      ↓
Android Graphics/Input APIs
      ↓
Android
      ↓
Hardware
```

---

# 3. TECHNOLOGY STACK

Use:

* Kotlin
* Jetpack Compose
* Kotlin Coroutines
* AndroidX
* Compose Foundation
* Compose UI
* Compose Animation where appropriate
* Canvas where necessary
* Android graphics APIs
* RenderEffect where appropriate
* RuntimeShader / AGSL where supported and justified
* Android `VibratorManager` / vibration APIs
* Android touch/input APIs
* `VelocityTracker`
* nested scrolling APIs
* Android window/insets APIs
* edge-to-edge rendering
* Android predictive-back APIs where appropriate
* Macrobenchmark
* Baseline Profiles
* JUnit
* Compose UI testing

Avoid unnecessary third-party dependencies.

Prefer Android/AndroidX primitives whenever possible.

The minimum Android version should be chosen deliberately after researching which APIs are actually required.

Newer capabilities should use graceful fallbacks where practical.

---

# 4. PROJECT ARCHITECTURE

Do NOT create one enormous `app` module.

Use approximately:

```text
iosfeel/
│
├── iosfeel-core/
│
├── iosfeel-motion/
│
├── iosfeel-gesture/
│
├── iosfeel-haptics/
│
├── iosfeel-scroll/
│
├── iosfeel-material/
│
├── iosfeel-navigation/
│
├── iosfeel-components/
│
├── iosfeel-testing/
│
├── benchmark/
│
├── sample/
│
└── docs/
```

Dependencies should flow downward.

For example:

```text
components
   ↓
navigation
   ↓
gesture + motion + haptics
   ↓
core
```

Do not allow circular module dependencies.

---

# 5. CORE MODULE

`iosfeel-core` should contain shared primitives rather than visual components.

Possible concepts:

```kotlin
interface IOSInteractionState

data class IOSGestureState(...)
data class IOSVelocity(...)
data class IOSMotionState(...)

enum class IOSInteractionPhase {
    Idle,
    Began,
    Changed,
    Completed,
    Cancelled
}
```

Provide common infrastructure for:

* density conversion;
* velocity normalization;
* interaction lifecycle;
* frame timing;
* feature detection;
* API-level capability detection;
* reduced-motion handling;
* accessibility handling;
* logging/debug instrumentation.

Do not over-engineer abstractions before they are required.

---

# 6. MOTION ENGINE

This is one of the most important modules.

Create:

```text
iosfeel-motion/
```

The framework should NOT treat every animation as:

```text
start → fixed duration → end
```

Interactive UI should retain physical information.

Represent concepts such as:

```text
position
velocity
target
stiffness
damping
mass
progress
direction
```

Potential API:

```kotlin
IOSSpring(
    target = 1f,
    initialVelocity = velocity,
    specification = IOSSpringSpec(...)
)
```

Possible specification:

```kotlin
data class IOSSpringSpec(
    val stiffness: Float,
    val dampingRatio: Float,
    val mass: Float = 1f
)
```

Do not blindly invent constants and label them "Apple values."

Create documented presets based on our own tuning:

```kotlin
IOSMotionPreset.Snappy
IOSMotionPreset.Smooth
IOSMotionPreset.Gentle
IOSMotionPreset.Interactive
```

Document clearly that these are iOSFeel presets.

## Critical requirement: interruption

Animations must be interruptible.

Example:

```text
User starts swipe
      ↓
releases
      ↓
spring begins
      ↓
user touches again BEFORE completion
      ↓
animation stops
      ↓
current position + velocity become
new gesture starting conditions
```

The interface should never feel like it ignores the user's finger because an animation is currently running.

---

# 7. GESTURE ENGINE

Create:

```text
iosfeel-gesture/
```

Implement reusable primitives for:

* drag;
* swipe;
* edge swipe;
* long press;
* velocity tracking;
* gesture cancellation;
* gesture competition;
* direction locking;
* nested gesture handling.

Potential API:

```kotlin
rememberIOSDragState()

Modifier.iosDraggable(...)

Modifier.iosEdgeSwipe(...)

Modifier.iosInteractiveGesture(...)
```

Every gesture should expose continuous progress.

Example:

```text
0.00 → beginning
0.25 → 25%
0.50 → 50%
1.00 → complete
```

Components should consume this progress directly.

Do not start an unrelated animation every time the finger moves.

---

# 8. INTERACTIVE NAVIGATION

Create:

```text
iosfeel-navigation/
```

This is one of the first flagship features.

Build:

```kotlin
IOSNavigationStack
IOSNavigationBar
IOSBackGesture
IOSTransition
```

Example:

```kotlin
IOSNavigationStack {
    IOSScreen("feed") {
        FeedScreen()
    }

    IOSScreen("profile") {
        ProfileScreen()
    }
}
```

Implement an interactive edge-back gesture.

Expected flow:

```text
finger touches edge
        ↓
drag begins
        ↓
current screen follows finger
        ↓
previous screen revealed
        ↓
navigation elements interpolate
        ↓
velocity continuously tracked
        ↓
finger released
        ↓
completion decision
        ↓
spring completes OR cancels
```

The completion decision should consider both:

```text
progress
+
velocity
```

A fast short flick may complete.

A slow short drag may cancel.

Integrate correctly with Android's modern back system where appropriate rather than fighting the operating system.

---

# 9. SCROLL PHYSICS

Create:

```text
iosfeel-scroll/
```

Do NOT merely change an overscroll color.

Build an experimental scrolling behavior focusing on:

* momentum;
* velocity-dependent deceleration;
* elastic boundaries;
* overscroll resistance;
* spring-back;
* interruption;
* nested scrolling;
* fling behavior.

Potential API:

```kotlin
IOSBouncyScroll {
    ...
}
```

or a reusable state/behavior:

```kotlin
rememberIOSScrollState()
```

Model boundary resistance.

Conceptually:

```text
normal region:

finger movement ≈ content movement

past boundary:

additional finger movement
        ↓
increasing resistance
        ↓
smaller content displacement
```

Never claim constants are identical to Apple's unless supported by legitimate public documentation.

Tune empirically.

---

# 10. HAPTIC ENGINE

Create:

```text
iosfeel-haptics/
```

API should be semantic.

Example:

```kotlin
IOSHaptics.selection()

IOSHaptics.impact(
    IOSImpact.Light
)

IOSHaptics.notification(
    IOSNotification.Success
)
```

Components should not contain random vibration calls.

Haptics should correspond to meaningful interaction events.

Example:

```text
sheet dragged
     ↓
cross snap threshold
     ↓
light feedback
     ↓
sheet settles
```

Support:

* capable modern Android devices;
* graceful degradation;
* devices with weak vibration motors;
* disabled system haptics where appropriate.

Avoid excessive vibration.

The objective is precision, not strength.

---

# 11. MATERIAL / GLASS ENGINE

Create:

```text
iosfeel-material/
```

This module should explore sophisticated translucent surfaces using Android's supported rendering APIs.

Possible API:

```kotlin
IOSMaterial(
    style = IOSMaterialStyle.Thin
) {
    ...
}
```

and potentially:

```kotlin
IOSGlassSurface(...)
```

Explore:

* backdrop-like blur where technically possible;
* translucency;
* tint;
* contrast management;
* dynamic highlights;
* borders;
* background awareness;
* animation of material properties.

For supported Android versions investigate:

* `RenderEffect`
* Compose graphics layers
* AGSL / `RuntimeShader`

But do NOT implement expensive GPU effects merely because they look impressive.

Measure performance.

Provide fallbacks:

```text
High capability
→ full material effect

Medium capability
→ simplified blur/translucency

Low capability
→ translucent/tinted surface
```

---

# 12. SHEET ENGINE

Implement:

```kotlin
IOSSheet(...)
```

Support:

```kotlin
IOSDetent.Compact
IOSDetent.Medium
IOSDetent.Large
```

and eventually custom detents.

The sheet should coordinate:

```text
drag
velocity
position
background transform
corner radius
material
snap threshold
haptic
spring
```

The sheet must be interactive.

Dragging must directly control its visual position.

Support interruption during settling.

Handle nested scrolling carefully.

---

# 13. COMPONENT LIBRARY

Only build components after foundational systems exist.

Eventually provide:

```text
IOSButton
IOSIconButton
IOSSwitch
IOSSlider
IOSSegmentedControl

IOSNavigationBar
IOSLargeTitleBar
IOSTabBar

IOSSheet
IOSContextMenu
IOSAlert

IOSList
IOSListRow

IOSTextField
IOSSearchField

IOSProgressIndicator
```

Every component should reuse the shared engines.

For example:

```text
IOSSheet
 ├── Gesture Engine
 ├── Motion Engine
 ├── Haptic Engine
 └── Material Engine
```

Do not implement separate animation logic independently inside every component.

---

# 14. BUTTON BEHAVIOR

Even simple controls should demonstrate the philosophy.

For example:

```kotlin
IOSButton(
    onClick = { ... }
) {
    Text("Follow")
}
```

Possible states:

```text
Idle
 ↓
Pressed
 ↓
Released
 ↓
Activated
```

The visual response should begin when the finger goes down rather than waiting for click completion.

Handle:

* press;
* release;
* cancellation;
* dragging outside;
* disabled state;
* accessibility.

---

# 15. TAB BAR

Implement:

```kotlin
IOSTabBar(...)
```

Support:

* animated selection;
* semantic haptics;
* edge-to-edge placement;
* safe system inset handling;
* optional translucent/material background;
* smooth state restoration.

Do not simply restyle Material `NavigationBar`.

---

# 16. SYSTEM INTEGRATION

The framework should cooperate with Android rather than trying to hide Android.

Support:

* edge-to-edge;
* status/navigation bar insets;
* IME;
* keyboard animations;
* back handling;
* predictive back;
* orientation;
* configuration changes;
* accessibility;
* high refresh-rate displays;
* lifecycle;
* saved state.

Pay special attention to keyboard transitions.

A beautiful custom sheet that breaks when the keyboard appears is unacceptable.

---

# 17. ACCESSIBILITY

Visual fidelity must NOT come at the expense of accessibility.

Respect:

* touch target requirements;
* screen readers;
* Compose semantics;
* font scaling;
* reduced motion where detectable/applicable;
* contrast;
* system haptic preferences;
* keyboard navigation where relevant.

Do not hardcode tiny touch targets simply because a visual reference appears small.

---

# 18. PERFORMANCE TARGETS

Assume modern devices can operate at:

```text
60 Hz
90 Hz
120 Hz
```

At 120 Hz, the frame budget is roughly:

```text
8.33 ms
```

Avoid:

* unnecessary recomposition;
* excessive object allocation during gestures;
* expensive blur every frame;
* repeated shader creation;
* unnecessary layout passes;
* unnecessary bitmap allocations.

Use:

* stable state;
* `remember`;
* graphics-layer transforms where appropriate;
* caching;
* profiling.

Never claim something is "zero overhead."

Measure it.

---

# 19. BENCHMARKING

Create:

```text
benchmark/
```

Measure important interactions such as:

```text
navigation transition
scrolling
sheet dragging
material rendering
tab switching
large lists
```

Track:

```text
frame time
jank
CPU usage
GPU workload where available
memory allocation
startup impact
```

Performance is part of the experience.

---

# 20. DEBUG MODE

Create optional development overlays.

For example:

```kotlin
IOSFeelDebugOverlay()
```

Display:

```text
FPS
frame time
gesture velocity
gesture progress
spring velocity
spring target
active interaction
current detent
```

This will make physics tuning dramatically easier.

---

# 21. DEVICE CALIBRATION

Do NOT hardcode behavior around one specific phone.

Create capability detection.

Conceptually:

```kotlin
data class IOSFeelCapabilities(
    val supportsAdvancedBlur: Boolean,
    val supportsRuntimeShader: Boolean,
    val refreshRate: Float,
    val hapticCapabilities: ...
)
```

Allow device-specific optimization without making the public API device-specific.

---

# 22. REFERENCE AND TUNING METHODOLOGY

We are trying to reproduce interaction *qualities*, not proprietary implementation.

When studying an interaction, break it into measurable properties.

Example:

```text
Navigation swipe

1. Where can gesture begin?
2. How much does screen move relative to finger?
3. What happens to previous screen?
4. How does opacity change?
5. What happens when finger stops?
6. What velocity completes transition?
7. What happens when cancelled?
8. Can animation be interrupted?
9. Is there haptic feedback?
```

Then implement our own behavior from those observations and public platform documentation.

Maintain:

```text
docs/interaction-research/
```

Example:

```text
navigation.md
scrolling.md
sheets.md
buttons.md
haptics.md
materials.md
```

Record assumptions explicitly.

---

# 23. TEST APPLICATION

Create:

```text
sample/
```

The sample app should NOT initially be a huge Instagram clone.

Create an **Interaction Laboratory**.

Home:

```text
iOSFeel Laboratory

Motion
Gestures
Navigation
Scrolling
Haptics
Materials
Sheets
Controls
Performance
```

Each page should allow changing parameters.

For example:

```text
Spring Laboratory

Stiffness      [ slider ]
Damping        [ slider ]
Initial velocity [ slider ]

[ Drag Card ]

Velocity: 1240 px/s
Frame: 7.1 ms
```

This makes tuning scientific instead of guessing constants in source code.

---

# 24. INSTAGRAM-STYLE DEMO

After the foundations are stable, create a second demonstration resembling a generic social-media application.

Do not copy Instagram branding or proprietary assets.

Include:

```text
Feed
Stories-like carousel
Profile
Post viewer
Comments sheet
Messages
Reels-like vertical video prototype
```

Use it to stress-test:

```text
navigation
scrolling
sheets
gestures
materials
haptics
```

---

# 25. API DESIGN PRINCIPLES

The public API must feel natural to Compose developers.

Good:

```kotlin
IOSSheet(
    state = rememberIOSSheetState(),
    detents = listOf(
        IOSDetent.Medium,
        IOSDetent.Large
    )
) {
    Comments()
}
```

Bad:

```kotlin
IOSSheet(
    internalSpringController = ...,
    rawVelocityTracker = ...,
    hapticEngineInternal = ...,
    renderNodeConfiguration = ...
)
```

Complexity belongs internally.

Expose advanced configuration when genuinely useful.

---

# 26. CUSTOMIZATION

Do not force developers into one visual configuration.

Create something similar to:

```kotlin
IOSFeelTheme(
    motion = ...,
    materials = ...,
    haptics = ...,
    shapes = ...,
    typography = ...
) {
    App()
}
```

Allow developers to use only individual pieces:

```kotlin
IOSHaptics

IOSSpring

IOSSheet
```

without adopting the entire framework.

---

# 27. TESTING

Create unit tests for:

```text
spring calculations
velocity decisions
navigation completion logic
detent selection
overscroll resistance
state transitions
gesture cancellation
```

Create Compose tests for:

```text
buttons
navigation
sheets
tab bar
accessibility semantics
```

Critical state machines must be deterministic and testable independently from rendering.

---

# 28. DOCUMENTATION

Every major feature requires documentation.

Example:

```text
docs/

getting-started.md
architecture.md
motion.md
gestures.md
navigation.md
scrolling.md
haptics.md
materials.md
performance.md
accessibility.md
```

Documentation must explain **why**, not just API signatures.

---

# 29. DEVELOPMENT PHASES

Do NOT attempt everything simultaneously.

## Phase 0 — Research + skeleton

Create:

```text
Gradle project
modules
sample laboratory
benchmark module
documentation structure
CI
```

No fancy UI yet.

---

## Phase 1 — Motion

Build:

```text
IOSSpring
IOSMotionState
interruptible animation
velocity preservation
motion laboratory
```

Success condition:

A draggable object can be released into a spring and grabbed again while moving without jumping.

---

## Phase 2 — Haptics

Build:

```text
IOSHaptics
semantic feedback
capability detection
haptic laboratory
```

Success condition:

Feedback is subtle, intentional and synchronized with UI state changes.

---

## Phase 3 — Gestures

Build:

```text
drag
edge swipe
velocity tracking
gesture cancellation
gesture competition
```

Success condition:

Gestures remain stable under rapid direction changes and interruption.

---

## Phase 4 — Navigation

Build:

```text
IOSNavigationStack
interactive back
transition engine
predictive-back integration
```

Success condition:

The screen follows the user's finger continuously and can naturally complete or cancel based on position and velocity.

This is the first major proof-of-concept milestone.

---

## Phase 5 — Scrolling

Build:

```text
IOSScrollPhysics
elastic overscroll
momentum
deceleration
spring-back
nested scrolling
```

Success condition:

Scrolling feels physically coherent and remains performant on large lists.

---

## Phase 6 — Sheets

Build:

```text
IOSSheet
detents
interactive dragging
nested scrolling
background transforms
haptic thresholds
```

---

## Phase 7 — Materials

Build:

```text
IOSMaterial
IOSGlassSurface
blur
translucency
shader experiments
fallback renderer
```

Only keep effects that meet performance targets.

---

## Phase 8 — Components

Build:

```text
IOSButton
IOSSwitch
IOSSlider
IOSTabBar
IOSNavigationBar
IOSContextMenu
IOSSearchField
```

---

## Phase 9 — Social application benchmark

Build the generic social-media demo and stress-test the entire framework.

---

# 30. FIRST PROOF OF CONCEPT

Before building the entire architecture, create ONE experiment containing:

```text
Screen A
   ↓
Screen B
```

Screen B must support interactive edge-swipe back.

Requirements:

1. Finger movement directly controls transition progress.
2. Previous screen is visible underneath.
3. Current screen follows the gesture.
4. Velocity is continuously measured.
5. Release chooses completion/cancellation based on velocity + progress.
6. Completion uses spring physics.
7. Cancellation uses spring physics.
8. User can interrupt the spring.
9. Edge-to-edge works correctly.
10. Frame performance is measurable.

Add optional subtle haptic feedback only where justified.

If this interaction cannot be made excellent, STOP expanding the framework and improve the foundation.

---

# 31. CODE QUALITY RULES

Follow:

```text
small focused classes
immutable state where practical
clear state machines
unidirectional state flow
dependency inversion where useful
KDoc for public APIs
no unexplained magic constants
```

Every tuned constant must have a descriptive name.

Bad:

```kotlin
velocity > 823
```

Better:

```kotlin
velocity > navigationCompletionVelocityThreshold
```

Document where experimentally tuned values came from.

---

# 32. IMPORTANT ENGINEERING RULE

Do NOT produce thousands of lines of code in the first response.

Work incrementally.

For every phase:

1. Explain the problem.
2. Explain relevant Android primitives.
3. Propose architecture.
4. Define state machine.
5. Implement the smallest working version.
6. Write tests.
7. Run/build it.
8. Fix compile/runtime issues.
9. Benchmark.
10. Refactor only after it works.
11. Document findings.
12. Proceed to the next phase.

Never claim code works without building/testing it when a development environment is available.

---

# 33. AI AGENT BEHAVIOR

Act as an engineering partner, not a code generator.

Before implementing something, ask:

```text
Does Android already provide a primitive for this?

Can Compose do this efficiently?

Does this need Canvas?

Does this need a graphicsLayer?

Does this require Android View interoperability?

Does this require AGSL?

Can we achieve it without a custom shader?

Will this allocate during every frame?

What happens at 120 Hz?

What happens during interruption?

What happens when accessibility settings change?
```

Choose the simplest architecture that can achieve the desired behavior.

---

# 34. DO NOT FAKE RESULTS

Never say:

> "This is exactly how iOS works."

unless supported by reliable public information.

Instead say:

> "This implementation is tuned to reproduce the observed interaction."

Never invent undocumented Apple constants.

Our framework is an independent Android implementation.

---

# 35. ULTIMATE DEVELOPER EXPERIENCE

Eventually I want developers to be able to write:

```kotlin
@Composable
fun ProfileScreen() {

    IOSScaffold(
        topBar = {
            IOSNavigationBar(
                title = "Profile"
            )
        }
    ) {

        IOSBouncyScroll {

            ProfileHeader()

            IOSButton(
                onClick = { }
            ) {
                Text("Follow")
            }
        }
    }
}
```

and automatically receive coherent:

```text
motion
gestures
scrolling
navigation
materials
haptics
```

without manually coordinating them.

---

# 36. SUCCESS CRITERIA

The project succeeds if a native Android application using iOSFeel:

* feels responsive to the finger;
* preserves gesture velocity into animations;
* allows animations to be interrupted naturally;
* has coherent spring physics;
* has high-quality elastic interactions;
* uses subtle synchronized haptics;
* provides polished interactive navigation;
* handles edge-to-edge correctly;
* handles keyboard/IME transitions correctly;
* performs smoothly at high refresh rates;
* remains accessible;
* works across multiple Android devices;
* provides graceful fallbacks;
* is reusable as a normal Kotlin/Compose library.

It does **not** need to reproduce iOS perfectly.

The target is to investigate how close native Android can get when motion, gesture, rendering, scrolling and haptic behavior are treated as first-class framework concerns.

---

# 37. START NOW

Do not begin by generating the complete framework.

Start with **Phase 0 and Phase 1 only**.

First:

1. Determine the Gradle/module architecture.
2. Define the minimum Android/API strategy.
3. Create the project skeleton.
4. Create the Interaction Laboratory sample.
5. Design the motion state model.
6. Implement the first interruptible spring system.
7. Create a draggable-card experiment.
8. Preserve release velocity.
9. Allow the moving card to be grabbed again.
10. Add tests for the motion state machine.
11. Add basic frame-performance instrumentation.
12. Build and run the project.
13. Fix all compilation/runtime problems.
14. Explain the architecture and important decisions.

Do **not** implement navigation, glass, sheets, scrolling, or the complete component library yet.

We will only proceed to Phase 2 after the motion foundation is demonstrably stable.
