# iOSFeel Architecture

## Module Structure

```
iOSFeel/
│
├── app/                    ← Interaction Laboratory (test app)
│   └── dev.iosfeel.lab
│
├── iosfeel-core/           ← Shared primitives
│   └── dev.iosfeel.core
│
└── iosfeel-motion/         ← Spring physics + motion controller
    └── dev.iosfeel.motion
```

## Dependency Direction

```
app (Interaction Laboratory)
 ↓
iosfeel-motion
 ↓
iosfeel-core
```

Dependencies flow strictly downward. No circular dependencies.

## Design Philosophy

iOSFeel is an **interaction and motion framework**, not a theme.

Most "iOS-style Android" libraries change corner radii, colors, and typography.
That's insufficient. A polished interface feels polished because:

```
Touch → Gesture → Velocity → Interactive movement →
Physical animation → Visual transformation → Haptic response → Final state
```

These systems must work together.

## Core Concepts

### Physical Motion State

Instead of:
```
animationProgress = 63%
```

We track:
```
position = 184 px
velocity = 1374 px/s
target = 0
phase = SPRINGING
```

This allows natural interruption and velocity preservation.

### Interruptibility

Animations can be interrupted at any point. When the user touches an
element during a spring animation:

1. Animation stops immediately
2. Current position is preserved (no jump)
3. The user's finger takes control
4. On release, velocity is measured and fed into the new spring

### Velocity Preservation

When the user releases a dragged element, the finger's velocity is
fed into the spring as initial velocity. This creates natural-feeling
motion where the spring "inherits" the gesture's momentum.

## Technology

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Animation**: Compose Animation Core (`Animatable`, `spring()`)
- **Gestures**: Compose `pointerInput`, `VelocityTracker`
- **Min SDK**: 26
- **Target SDK**: 35

## Future Modules (not yet created)

```
iosfeel-gesture/       ← Phase 3
iosfeel-haptics/       ← Phase 2
iosfeel-navigation/    ← Phase 4
iosfeel-scroll/        ← Phase 5
iosfeel-material/      ← Phase 7
iosfeel-components/    ← Phase 8
benchmark/             ← After basic motion works
```
