Phase 8 — Reusable Component System

Now we finally stop thinking mostly in terms of engines and start building what an Android developer would actually import.

The key rule for Phase 8 is:

Components orchestrate existing engines. They do not invent their own physics.

So an IOSButton shouldn't contain a new spring algorithm, an IOSTabBar shouldn't create its own haptic system, and IOSSegmentedControl shouldn't invent another gesture recognizer.

Current Compose still gives us the right low-level building blocks here: InteractionSource exposes pressed/dragged interaction state, while Modifier.indication() allows interaction visuals to be separated from input handling.

Our architecture becomes:

                 Components
                     │
     ┌───────────────┼────────────────┐
     ↓               ↓                ↓
   Motion         Haptics          Materials
     ↓               ↓                ↓
 Gestures        semantics         rendering
     │
     ↓
 Compose / Android
1. Create iosfeel-components

Add:

iosfeel-components/
└── src/main/java/dev/iosfeel/components/
    ├── interaction/
    │   ├── IOSPressState.kt
    │   ├── IOSPressConfig.kt
    │   └── IOSPressModifier.kt
    │
    ├── button/
    │   ├── IOSButton.kt
    │   ├── IOSButtonStyle.kt
    │   └── IOSButtonDefaults.kt
    │
    ├── toggle/
    │   ├── IOSToggle.kt
    │   └── IOSToggleDefaults.kt
    │
    ├── segmented/
    │   ├── IOSSegmentedControl.kt
    │   └── IOSSegmentedItem.kt
    │
    ├── navigation/
    │   ├── IOSNavigationBar.kt
    │   └── IOSNavigationBarItem.kt
    │
    ├── tab/
    │   ├── IOSTabBar.kt
    │   └── IOSTabItem.kt
    │
    ├── list/
    │   └── IOSListRow.kt
    │
    └── search/
        └── IOSSearchField.kt

Dependencies:

iosfeel-components
        │
        ├── iosfeel-core
        ├── iosfeel-motion
        ├── iosfeel-haptics
        ├── iosfeel-gesture
        └── iosfeel-material

Notice:

components
↓
engines

Never:

motion
↓
button
2. Build one shared press engine first

Before making buttons, rows, tabs, etc., solve pressing once.

We want:

finger down
   ↓
press progress 0 → 1
   ↓
slight scale/tint response

finger up
   ↓
progress 1 → 0
   ↓
spring return

Create:

interaction/IOSPressState.kt
package dev.iosfeel.components.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable

@Stable
class IOSPressState {

    val progress =
        Animatable(0f)

    val isPressed: Boolean
        get() =
            progress.value > 0f
}

Don't make separate mutable booleans for:

pressed
scaled
highlighted
darkened

One:

progress 0..1

should drive all of them.

3. Press configuration
package dev.iosfeel.components.interaction

import androidx.compose.runtime.Immutable

@Immutable
data class IOSPressConfig(

    val pressedScale: Float =
        0.975f,

    val pressStiffness: Float =
        700f,

    val pressDampingRatio: Float =
        0.88f,

    val releaseStiffness: Float =
        500f,

    val releaseDampingRatio: Float =
        0.72f
)

These values are experimental iOSFeel tuning values.

4. Use InteractionSource

Instead of writing raw pointer handling for normal buttons:

val interactionSource =
    remember {
        MutableInteractionSource()
    }

val pressed by
    interactionSource
        .collectIsPressedAsState()

This is exactly what Compose's interaction infrastructure is intended for: components can observe press/drag state and derive visuals from the same interaction stream.

Then:

LaunchedEffect(pressed) {

    if (pressed) {

        pressState.progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                stiffness =
                    config.pressStiffness,

                dampingRatio =
                    config.pressDampingRatio
            )
        )

    } else {

        pressState.progress.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                stiffness =
                    config.releaseStiffness,

                dampingRatio =
                    config.releaseDampingRatio
            )
        )
    }
}

Now every clickable component can share this behavior.

5. Convert press progress to scale
fun calculateIOSPressScale(
    progress: Float,
    pressedScale: Float
): Float {

    val p =
        progress.coerceIn(
            0f,
            1f
        )

    return 1f +
        (pressedScale - 1f) * p
}

Example:

progress 0.0
scale 1.000

progress 0.5
scale 0.9875

progress 1.0
scale 0.975

Very subtle.

Not:

1.0 → 0.85
6. Create reusable iosPressEffect
@Composable
fun Modifier.iosPressEffect(
    interactionSource:
        MutableInteractionSource,
    config:
        IOSPressConfig =
            IOSPressConfig()
): Modifier {

    val pressed by
        interactionSource
            .collectIsPressedAsState()

    val progress =
        remember {
            Animatable(0f)
        }

    LaunchedEffect(pressed) {

        progress.animateTo(
            targetValue =
                if (pressed) 1f else 0f,

            animationSpec =
                spring(
                    stiffness =
                        if (pressed)
                            config.pressStiffness
                        else
                            config.releaseStiffness,

                    dampingRatio =
                        if (pressed)
                            config.pressDampingRatio
                        else
                            config.releaseDampingRatio
                )
        )
    }

    val scale =
        calculateIOSPressScale(
            progress =
                progress.value,

            pressedScale =
                config.pressedScale
        )

    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

Then future components simply do:

.iosPressEffect(
    interactionSource
)
7. IOSButton

Create:

button/IOSButtonStyle.kt
enum class IOSButtonStyle {
    Filled,
    Tinted,
    Glass,
    Plain
}

Then:

@Composable
fun IOSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style:
        IOSButtonStyle =
            IOSButtonStyle.Filled,
    hapticsEnabled: Boolean = true,
    content:
        @Composable RowScope.() -> Unit
)

Implementation architecture:

IOSButton
   │
   ├── InteractionSource
   ├── iosPressEffect
   ├── clickable semantics
   ├── IOSHaptics
   └── optional IOSGlassSurface
8. Button implementation
@Composable
fun IOSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style:
        IOSButtonStyle =
            IOSButtonStyle.Filled,
    content:
        @Composable RowScope.() -> Unit
) {

    val interactions =
        remember {
            MutableInteractionSource()
        }

    val haptics =
        rememberIOSHaptics()

    val clickModifier =
        Modifier
            .iosPressEffect(
                interactionSource =
                    interactions
            )
            .clickable(
                enabled = enabled,
                interactionSource =
                    interactions,
                indication = null
            ) {

                haptics.impact(
                    IOSImpact.Light
                )

                onClick()
            }

    when (style) {

        IOSButtonStyle.Glass -> {

            IOSGlassSurface(
                modifier =
                    modifier
                        .then(
                            clickModifier
                        )
            ) {

                IOSButtonContent(
                    content
                )
            }
        }

        else -> {

            Surface(
                modifier =
                    modifier
                        .then(
                            clickModifier
                        ),

                shape =
                    RoundedCornerShape(
                        14.dp
                    ),

                color =
                    buttonColor(style)
            ) {

                IOSButtonContent(
                    content
                )
            }
        }
    }
}

Notice:

no ripple

because the interaction is conveyed by our material/scale system.

Compose allows indications to be controlled separately or omitted using the interaction/indication API.

But accessibility/click semantics still come from clickable.

9. Do not vibrate on finger-down

Use haptics on the meaningful action:

press down
→ visual response

release/click
→ tiny impact

Not:

finger down → buzz
finger up → buzz

for every button.

That would become exhausting.

10. IOSToggle

Now make a component where the thumb follows actual state and the switch itself transitions using our motion concepts.

API:

@Composable
fun IOSToggle(
    checked: Boolean,
    onCheckedChange:
        (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)

State representation:

OFF
progress = 0

ON
progress = 1
11. Toggle thumb
val progress =
    remember {
        Animatable(
            if (checked)
                1f
            else
                0f
        )
    }

LaunchedEffect(checked) {

    progress.animateTo(
        targetValue =
            if (checked)
                1f
            else
                0f,

        animationSpec =
            spring(
                stiffness = 550f,
                dampingRatio = 0.82f
            )
    )
}

Then:

val thumbX =
    maxThumbTravelPx *
        progress.value

One progress drives:

thumb position
track color
thumb scale
highlight
12. Toggle haptic

On actual state change:

haptics.selection()

not on every animation frame.

So:

OFF
   ↓ tap
*tick*
   ↓
ON
13. Dragging the toggle

A polished switch should eventually support:

tap
+
drag thumb

not only tapping.

Use our Gesture Engine rather than raw pointer input.

Conceptually:

drag progress
0 → 1

Then on release:

progress > .5
→ ON

otherwise
→ OFF

plus velocity influence later.

Don't create a new drag framework inside IOSToggle.

14. IOSSegmentedControl

Modern Compose Material already has dedicated single-choice and multi-choice segmented-button APIs; that's useful confirmation that selectable semantics should remain the foundation even though our component uses different motion/material styling.

Our API:

data class IOSSegmentedItem<T>(
    val value: T,
    val label: String
)

Then:

@Composable
fun <T> IOSSegmentedControl(
    items:
        List<IOSSegmentedItem<T>>,
    selectedValue: T,
    onSelected:
        (T) -> Unit,
    modifier: Modifier = Modifier
)
15. Don't animate individual segment backgrounds

Bad:

Segment A selected background fade out

+

Segment B selected background fade in

Better:

ONE selection pill
     ↓
moves between segments

Architecture:

┌───────────────────────────────┐
│ ┌───────┐                     │
│ │ pill  │                     │
│ └───────┘                     │
│ Today    Week    Month        │
└───────────────────────────────┘

Tap Week:

pill
──────→
16. Segmented selection position

If there are 3 equal segments:

val segmentWidth =
    containerWidth /
        items.size

Selected index:

val selectedIndex =
    items.indexOfFirst {
        it.value ==
            selectedValue
    }

Target:

targetX =
    selectedIndex *
        segmentWidth

Animate with a spring.

17. Better: normalized selected position

Keep:

selectedFraction =
    selectedIndex.toFloat() /
        (items.size - 1)

when possible.

Then resizing:

portrait
↓
landscape

doesn't invalidate semantic selection.

Again:

state
→ normalized/logical

pixels
→ rendering detail
18. Segmented haptic

Only when selection changes:

haptics.selection()

So dragging across:

Today
Week
Month

could produce:

tick
tick

at segment boundaries.

That's exactly where the Phase 2 detent idea becomes reusable.

19. IOSListRow

This will be used everywhere.

API:

@Composable
fun IOSListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading:
        (@Composable () -> Unit)? =
            null,
    trailing:
        (@Composable () -> Unit)? =
            null,
    onClick:
        (() -> Unit)? = null
)

Layout:

┌───────────────────────────────┐
│ [icon]  Account          >    │
│         Subtitle              │
└───────────────────────────────┘

Press feedback:

very subtle tint

Not:

big card shrinking animation

because list rows are dense.

Use:

IOSPressConfig(
    pressedScale = 0.995f
)

or just tint response.

20. Don't make everything a Card

This is important for the visual identity.

Avoid:

┌──────────────┐
│ Row 1        │
└──────────────┘

┌──────────────┐
│ Row 2        │
└──────────────┘

for basic lists.

Instead:

Row 1
────────────────
Row 2
────────────────
Row 3

with grouping where appropriate.

That helps avoid the generic “AI-generated Compose dashboard” look.

21. IOSNavigationBar

Now use our actual Navigation Engine.

API:

@Composable
fun IOSNavigationBar(
    title: String,
    modifier: Modifier = Modifier,
    backButtonVisible: Boolean = false,
    onBack:
        (() -> Unit)? = null,
    trailing:
        (@Composable RowScope.() -> Unit)? =
            null,
    glass: Boolean = true
)

Structure:

┌───────────────────────────────┐
│ < Back       Profile     ⋯    │
└───────────────────────────────┘

When interactive navigation back progress changes:

0 → 1

the bar should eventually interpolate too.

22. Navigation bar must consume navigation progress

We already have:

backTransition.progress

So later:

Current title
opacity 1 → 0

Previous title
opacity 0 → 1

Back label
position changes

glass density
subtle response

all derive from:

navigation progress

Don't run an unrelated title animation.

23. IOSTabBar

This is one of the most important components for our social-app benchmark.

API:

data class IOSTabItem<T>(
    val value: T,
    val label: String?,
    val icon:
        @Composable () -> Unit
)

Then:

@Composable
fun <T> IOSTabBar(
    items: List<IOSTabItem<T>>,
    selected: T,
    onSelected:
        (T) -> Unit,
    backdrop:
        IOSBackdropState? = null
)

Visually:

┌─────────────────────────────────┐
│   Home   Search   +   Reels 👤  │
└─────────────────────────────────┘

Optionally rendered through Phase 7:

IOSGlassSurface(...)
24. Tab interactions

Tap another tab:

icon:
small spring response

selection:
haptic tick

material:
subtle selection emphasis

Don't animate:

giant bouncing icon

unless a product deliberately wants that style.

25. Re-tapping selected tab

We should expose:

onReselect:
    ((T) -> Unit)?

Why?

Social apps often use:

Home selected
↓
tap Home again
↓
scroll feed to top

That's a useful component-level behavior.

So:

if (item.value == selected) {
    onReselect?.invoke(
        item.value
    )
} else {
    onSelected(
        item.value
    )
}
26. IOSSearchField

Don't build a new text input engine.

Use Compose's actual text-field infrastructure and style/orchestrate it.

API:

@Composable
fun IOSSearchField(
    value: String,
    onValueChange:
        (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onCancel:
        (() -> Unit)? = null
)

Target:

┌────────────────────────────┐
│ 🔍 Search                  │
└────────────────────────────┘

Focused:

┌───────────────────────┐ Cancel
│ 🔍 kotlin             │
└───────────────────────┘
27. Focus should drive one interaction progress

States:

unfocused = 0
focused   = 1

Use that to control:

field width
cancel visibility
material density
icon position

rather than four independent animations.

28. Keyboard remains Android-native

This is important.

Don't try to make Android's:

IME
selection handles
clipboard
autofill
accessibility

behave like iOS by replacing the text system.

Our framework styles the container and interaction around the field while keeping Android's robust input stack.

That is the same strategy we used for LazyColumn.

29. Build component defaults

Every component needs centralized defaults.

For example:

object IOSButtonDefaults {

    val CornerRadius =
        14.dp

    val Height =
        50.dp

    val HorizontalPadding =
        18.dp

    val PressConfig =
        IOSPressConfig(
            pressedScale = 0.975f
        )
}

And:

object IOSTabBarDefaults {

    val Height =
        64.dp
}

Don't scatter:

14.dp
16.dp
18.dp

across 40 files.

30. Introduce tokens

Actually, at this point we should add:

iosfeel-core/
└── tokens/
    ├── IOSSpacing.kt
    ├── IOSShapes.kt
    ├── IOSMotionTokens.kt
    └── IOSTypographyTokens.kt

Example:

object IOSSpacing {

    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XLarge = 24.dp
}

And:

object IOSShapes {

    val Control =
        RoundedCornerShape(12.dp)

    val Button =
        RoundedCornerShape(14.dp)

    val LargeSurface =
        RoundedCornerShape(24.dp)
}

This is where visual consistency starts becoming manageable.

31. But don't make tokens Apple constants

Name them:

IOSFeelButtonRadius

conceptually.

Don't document:

"Apple uses 14dp."

We're creating a behavior-inspired Android design system, not claiming access to Apple's internal specifications.

32. Component semantics matter

Custom visuals must retain proper semantics.

Button:

Modifier.semantics {
    role = Role.Button
}

Toggle:

Role.Switch
checked state

Segmented control:

selectableGroup
selected semantics

Tab:

Role.Tab
selected state

Search:

real editable text semantics

A UI that looks polished but works badly with TalkBack has failed.

33. Don't replace Compose primitives unnecessarily

Our rule should be:

Need click semantics?
→ clickable/selectable/toggleable

Need text input?
→ Compose text-field stack

Need Lazy list?
→ LazyColumn

Need accessibility?
→ Compose semantics

Need visual/physics differentiation?
→ iOSFeel engines

That's how the project remains sustainable.

Material 3 itself follows the same broad component approach—semantic controls such as segmented buttons are built on selectable/toggleable interaction models rather than bespoke pointer systems.

34. Component Laboratory

Add:

Components ✅

Screen:

Component Laboratory

BUTTONS
────────────────────

[ Continue ]

[ Glass Button ]

Plain Action


TOGGLE
────────────────────

Notifications       ●────


SEGMENTED
────────────────────

┌────────┬────────┬────────┐
│ Posts  │ Reels  │ Tagged │
└────────┴────────┴────────┘


LIST
────────────────────

Account                >
Privacy                >
Notifications          >


SEARCH
────────────────────

[ 🔍 Search ]


TAB BAR
────────────────────

Home   Search   +   Reels   Profile

Every component should have a debug panel that can optionally show:

press progress
selected progress
current velocity
haptic event
material quality
35. Component stress tests

Don't just press them slowly.

Test:

rapid repeated tapping

press → drag outside → release

disable while pressed

orientation change

TalkBack

large font scale

dark/light backgrounds

120Hz

glass reduced mode

keyboard open

navigation gesture running

Real components need to survive weird combinations.

36. First integration screen

Now create something that resembles an actual profile screen:

┌─────────────────────────────────┐
│ <                ankit       ⋯  │
│                                 │
│        profile image            │
│                                 │
│ 128        3.4K        412      │
│ Posts      Followers   Following│
│                                 │
│ [ Follow ] [ Message ]          │
│                                 │
│ ┌───────┬───────┬───────┐      │
│ │ Posts │ Reels │Tagged  │      │
│ └───────┴───────┴───────┘      │
│                                 │
│ grid content...                 │
│                                 │
├─────────────────────────────────┤
│ Home Search  +  Reels Profile  │
└─────────────────────────────────┘

This uses:

IOSNavigationBar
IOSButton
IOSSegmentedControl
IOSTabBar
IOSGlassSurface
IOSScroll physics

Now we're starting to see whether all phases actually produce a coherent system.

37. Don't clone Instagram yet

Even though that profile screen resembles a social app, Phase 8 is still a component validation phase.

Use fake content.

Don't spend time reproducing:

Instagram icons
exact spacing
exact feeds
real network/backend

yet.

Phase 9 is where we benchmark against your original social-app goal.

38. Phase 8A checkpoint

At this point we want these primitives working:

✅ shared press engine
✅ IOSButton
✅ glass button
✅ IOSToggle
✅ draggable toggle architecture
✅ IOSSegmentedControl
✅ moving selection pill
✅ IOSListRow
✅ IOSNavigationBar
✅ navigation-progress hooks
✅ IOSTabBar
✅ tab reselection
✅ IOSSearchField
✅ centralized tokens
✅ proper semantics
✅ component laboratory
✅ integrated profile prototype