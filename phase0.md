For the current Android stack, I’d use Kotlin + Jetpack Compose, compileSdk 37, current stable Compose BOM 2026.08.00, and a practical minSdk 26 for this experimental framework. Compose itself supports lower APIs, but API 26 gives us a cleaner baseline for graphics/haptics while still leaving room for capability-based fallbacks. Current Compose 1.12 targets API 37.

Our 10 phases
Phase	What we build
0	Project architecture + Interaction Laboratory
1	Motion engine + interruptible springs
2	Haptic engine
3	Gesture engine
4	Interactive navigation
5	iOS-like scroll physics
6	Sheets + detents
7	Blur/material/glass renderer
8	Full component library
9	Social-media/Instagram-style stress-test app

We're doing the bold ones now.

Phase 0 — Create the foundation

Don't create all ten modules immediately. That would make the project annoying before we have anything working.

Start with:

iOSFeel/
│
├── app/                    ← Interaction Laboratory
│
├── iosfeel-core/
│
├── iosfeel-motion/
│
├── benchmark/              ← add after basic motion works
│
├── docs/
│
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties

Later we'll introduce:

iosfeel-gesture
iosfeel-haptics
iosfeel-navigation
iosfeel-scroll
iosfeel-material
iosfeel-components

But not yet.

Step 1 — Create the Android Studio project

Create:

New Project
→ Empty Activity

Use approximately:

Name:
iOSFeel

Package:
dev.iosfeel

Language:
Kotlin

Minimum SDK:
26

For the current stable Compose release, use the Compose BOM:

implementation(
    platform("androidx.compose:compose-bom:2026.08.00")
)

Google's August 2026 Compose release is 1.12 and requires a modern API-37/AGP toolchain.

Step 2 — Add our library modules

Android Studio:

File
→ New
→ New Module
→ Android Library

Create:

iosfeel-core

Package:

dev.iosfeel.core

Then another:

iosfeel-motion

Package:

dev.iosfeel.motion

Your project should now look approximately like:

iOSFeel
│
├── app
│   └── src/main/java/dev/iosfeel/lab/
│
├── iosfeel-core
│   └── src/main/java/dev/iosfeel/core/
│
└── iosfeel-motion
    └── src/main/java/dev/iosfeel/motion/
Step 3 — Dependency direction

This is important.

We want:

app
 ↓
iosfeel-motion
 ↓
iosfeel-core

Not:

core ↔ motion

and definitely not:

everything ↔ everything

Inside iosfeel-motion/build.gradle.kts:

dependencies {

    implementation(project(":iosfeel-core"))

    implementation(
        platform("androidx.compose:compose-bom:2026.08.00")
    )

    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.animation:animation-core")
}

And in app/build.gradle.kts:

dependencies {

    implementation(project(":iosfeel-core"))
    implementation(project(":iosfeel-motion"))

    implementation(
        platform("androidx.compose:compose-bom:2026.08.00")
    )
}
Step 4 — Build the Interaction Laboratory

The application isn't supposed to be Instagram yet.

When it launches, I want something like:

┌─────────────────────────────┐
│                             │
│       iOSFeel Laboratory    │
│                             │
│   Motion                    │
│   Gestures          soon    │
│   Navigation        soon    │
│   Scrolling         soon    │
│   Haptics           soon    │
│   Materials         soon    │
│   Sheets            soon    │
│                             │
└─────────────────────────────┘

For Phase 1, pressing:

Motion

opens:

┌─────────────────────────────┐
│ ← Motion Laboratory         │
│                             │
│      ┌─────────────┐        │
│      │             │        │
│      │  DRAG ME    │        │
│      │             │        │
│      └─────────────┘        │
│                             │
│ Position:   184 px          │
│ Velocity:   1374 px/s       │
│ Target:     0               │
│ State:      SPRINGING       │
│                             │
│ Stiffness  ───●────         │
│ Damping    ─────●──         │
│                             │
└─────────────────────────────┘

This ugly little test screen is much more important than building beautiful components right now.

Phase 1 — Our first actual engine

Now things get interesting.

Create:

iosfeel-motion/src/main/java/dev/iosfeel/motion/

with:

IOSMotionState.kt
IOSSpringSpec.kt
IOSMotionPreset.kt
IOSMotionController.kt
Step 5 — Define motion phases

Create IOSMotionState.kt.

package dev.iosfeel.motion

enum class IOSMotionPhase {
    Idle,
    Dragging,
    Springing
}

data class IOSMotionState(
    val position: Float = 0f,
    val velocity: Float = 0f,
    val target: Float = 0f,
    val phase: IOSMotionPhase = IOSMotionPhase.Idle
)

Notice something important.

We're storing:

position
velocity
target
phase

instead of:

animationProgress = 63%

That's intentional.

Our UI should behave like a moving physical object.

Step 6 — Spring specification

Create:

package dev.iosfeel.motion

data class IOSSpringSpec(
    val stiffness: Float,
    val dampingRatio: Float
)

Then:

object IOSMotionPreset {

    val Snappy = IOSSpringSpec(
        stiffness = 520f,
        dampingRatio = 0.78f
    )

    val Smooth = IOSSpringSpec(
        stiffness = 320f,
        dampingRatio = 0.82f
    )

    val Gentle = IOSSpringSpec(
        stiffness = 180f,
        dampingRatio = 0.88f
    )
}

These are our experimental tuning values.

We're explicitly not claiming these are Apple's numbers.

Later the Laboratory will let us tune them by feel and measurement.

Step 7 — Understand what we're actually building

Most simple Android animation looks conceptually like:

0
↓
animate for 300ms
↓
1

Our system instead thinks:

object position
       +
object velocity
       +
target
       +
spring physics
       ↓
next frame

Imagine you drag something quickly:

Finger →

velocity = 1700 px/s

Then release it.

We don't throw away:

1700 px/s

The spring inherits it.

So:

finger velocity
      ↓
spring initial velocity
      ↓
movement continues naturally

This is one of the biggest differences between a polished interaction and something that feels disconnected.

Step 8 — First controller

We'll initially use Compose's animation machinery rather than immediately writing a numerical physics solver ourselves.

That's deliberate.

Compose Foundation is specifically designed to provide primitives developers can extend into their own design-system behavior.

Create:

package dev.iosfeel.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring

class IOSMotionController(
    initialPosition: Float = 0f
) {

    val position = Animatable(initialPosition)

    suspend fun snapTo(
        value: Float
    ) {
        position.snapTo(value)
    }

    suspend fun springTo(
        target: Float,
        velocity: Float,
        spec: IOSSpringSpec = IOSMotionPreset.Smooth
    ) {
        position.animateTo(
            targetValue = target,
            animationSpec = spring(
                stiffness = spec.stiffness,
                dampingRatio = spec.dampingRatio
            ),
            initialVelocity = velocity
        )
    }

    suspend fun stop() {
        position.stop()
    }
}

This isn't our final engine.

It's our first experimental foundation.

Step 9 — The killer feature: interruptibility

Imagine the card is here:

                     □ →

moving toward its target.

You touch it while it's still moving.

A mediocre UI does:

touch ignored

or

animation abruptly resets

We want:

SPRINGING

position = 172
velocity = -830

       ↓ touch

STOP animation

       ↓

DRAGGING

position = 172

       ↓

your finger now owns it

So visually:

throw card
   →→→→

       touch it

          ↓

        ← drag

          ↓

      release

       → spring

No jump.

No reset.

No fighting with the animation.

That behavior is our first major milestone.

Step 10 — Motion Laboratory draggable card

Conceptually:

@Composable
fun MotionLaboratory() {

    val scope = rememberCoroutineScope()

    val controller = remember {
        IOSMotionController()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        controller.position.value.roundToInt(),
                        0
                    )
                }
                .size(110.dp)
                .background(
                    Color.Black,
                    RoundedCornerShape(24.dp)
                )
                .pointerInput(Unit) {

                    detectDragGestures(
                        onDragStart = {

                            scope.launch {
                                controller.stop()
                            }
                        },

                        onDrag = { change, dragAmount ->

                            change.consume()

                            scope.launch {
                                controller.snapTo(
                                    controller.position.value +
                                        dragAmount.x
                                )
                            }
                        },

                        onDragEnd = {

                            scope.launch {

                                controller.springTo(
                                    target = 0f,
                                    velocity = 0f
                                )
                            }
                        }
                    )
                }
        )
    }
}

But there is an intentional problem here.

We're currently doing:

velocity = 0f

That means:

drag quickly →

release

spring starts as though
the object wasn't moving

That will not feel right.

Our next task is therefore to introduce VelocityTracker.

Step 11 — Preserve finger velocity

The pipeline must become:

Touch samples
     ↓
VelocityTracker
     ↓
drag movement
     ↓
finger released
     ↓
calculate velocity
     ↓
feed velocity into spring

For example:

slow release

velocity = 120 px/s
        ↓
gentle spring

versus:

fast flick

velocity = 2450 px/s
        ↓
spring carries momentum

Interestingly, current Compose releases have also been moving their velocity tracking toward Android framework behavior, so this is a very appropriate primitive for us to study rather than inventing velocity estimation from scratch.

What we're NOT doing yet

Do not start writing:

IOSButton()
IOSTabBar()
IOSSheet()
IOSNavigationBar()
IOSGlass()
IOSSwitch()

yet.

Otherwise you'll end up with:

beautiful library

but

mediocre physics

We want the opposite:

boring laboratory
+
excellent physics

        ↓ later

beautiful components
+
excellent physics

That's how this project has a chance to become genuinely different from merely creating an iOS theme.

Phase 0 checkpoint

At the end of Phase 0 you should have:

✅ Kotlin Compose project
✅ app module
✅ iosfeel-core module
✅ iosfeel-motion module
✅ dependency direction
✅ Interaction Laboratory home
✅ Motion Laboratory screen
Phase 1 checkpoint

At the end of Phase 1 we'll require:

✅ draggable object
✅ real velocity measurement
✅ velocity-preserving release
✅ spring motion
✅ interrupt spring by touching
✅ re-grab without jumping
✅ configurable stiffness
✅ configurable damping
✅ real-time debug values
✅ unit tests
✅ frame-time/jank measurement