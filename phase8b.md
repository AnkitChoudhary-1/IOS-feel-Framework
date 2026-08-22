Phase 8B — Production Component Kit

Now we finish the component layer so Phase 9 can use iOSFeel like a real UI framework rather than a collection of experiments.

Current Compose testing APIs are also useful here: semantics remain the basis for accessibility/UI testing, and DeviceConfigurationOverride can test components under different font scales, dark mode, and other configurations.

1. Expand the module

Add these packages:

iosfeel-components/
├── menu/
│   ├── IOSMenu.kt
│   ├── IOSMenuItem.kt
│   └── IOSMenuState.kt
├── iconbutton/
│   └── IOSIconButton.kt
├── slider/
│   ├── IOSSlider.kt
│   └── IOSSliderState.kt
├── badge/
│   └── IOSBadge.kt
├── list/
│   ├── IOSListSection.kt
│   ├── IOSListRow.kt
│   └── IOSGroupedList.kt
└── theme/
    ├── IOSFeelTheme.kt
    ├── IOSFeelColors.kt
    ├── IOSFeelTypography.kt
    └── IOSFeelThemeTokens.kt

Our public component layer will now cover the majority of a normal social/mobile application.

2. Build IOSIconButton

This shouldn't be a completely separate interaction implementation.

Reuse:

iosPressEffect()

API:

@Composable
fun IOSIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glass: Boolean = false,
    contentDescription: String?,
    icon: @Composable () -> Unit
)

Implementation:

@Composable
fun IOSIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glass: Boolean = false,
    contentDescription: String?,
    icon: @Composable () -> Unit
) {

    val interactions =
        remember {
            MutableInteractionSource()
        }

    val haptics =
        rememberIOSHaptics()

    val interactionModifier =
        Modifier
            .size(44.dp)
            .iosPressEffect(
                interactionSource =
                    interactions,

                config =
                    IOSPressConfig(
                        pressedScale = 0.91f
                    )
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
            .semantics {
                role = Role.Button

                if (
                    contentDescription != null
                ) {
                    this.contentDescription =
                        contentDescription
                }
            }

    if (glass) {

        IOSGlassSurface(
            modifier =
                modifier.then(
                    interactionModifier
                )
        ) {

            Box(
                Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {
                icon()
            }
        }

    } else {

        Box(
            modifier =
                modifier.then(
                    interactionModifier
                ),

            contentAlignment =
                Alignment.Center
        ) {
            icon()
        }
    }
}

Notice the visual icon can be:

20–24dp

while the hit target remains much larger.

3. Component state vocabulary

We need consistent states across everything.

Create:

enum class IOSComponentState {
    Normal,
    Pressed,
    Disabled,
    Selected,
    Destructive
}

But don't necessarily expose that enum publicly everywhere.

The important thing is for our styling system to understand:

normal
selected
disabled
destructive

consistently.

4. Destructive actions

Components like menus need:

Delete
Remove
Block

to be visually distinguished from ordinary actions.

Create:

enum class IOSActionRole {
    Normal,
    Destructive
}

For example:

IOSMenuItem(
    label = "Delete",
    role = IOSActionRole.Destructive,
    onClick = { ... }
)

The role affects:

content color
semantics where appropriate

not physics.

Destructive actions should not bounce harder or vibrate more aggressively just because they're destructive.

5. Disabled-state policy

Don't just do:

alpha = 0.2f

on everything.

Centralize it:

object IOSComponentAlpha {

    const val Enabled =
        1f

    const val Disabled =
        0.42f

    const val Secondary =
        0.62f
}

Then:

Modifier.graphicsLayer {
    alpha =
        if (enabled)
            IOSComponentAlpha.Enabled
        else
            IOSComponentAlpha.Disabled
}

Disabled components also shouldn't:

animate on press
emit haptics
invoke callbacks
6. IOSBadge

Compose already treats badges as decorative containers for information such as notification counts. We can keep the same semantic idea while styling them through iOSFeel.

API:

@Composable
fun IOSBadge(
    count: Int? = null,
    modifier: Modifier = Modifier
)

Rules:

count == null
→ dot

1–99
→ actual number

100+
→ 99+

Implementation:

@Composable
fun IOSBadge(
    count: Int? = null,
    modifier: Modifier = Modifier
) {

    val label =
        when {
            count == null ->
                null

            count > 99 ->
                "99+"

            else ->
                count.toString()
        }

    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth =
                    if (label == null)
                        8.dp
                    else
                        18.dp,

                minHeight =
                    if (label == null)
                        8.dp
                    else
                        18.dp
            )
            .background(
                color =
                    IOSFeelTheme.colors
                        .destructive,

                shape =
                    CircleShape
            )
            .padding(
                horizontal =
                    if (label == null)
                        0.dp
                    else
                        5.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {

        label?.let {

            Text(
                text = it,
                style =
                    IOSFeelTheme.typography
                        .badge
            )
        }
    }
}
7. Add badges to tab items

Extend:

data class IOSTabItem<T>(
    val value: T,
    val label: String?,
    val icon:
        @Composable () -> Unit,
    val badgeCount: Int? = null,
    val showBadgeDot: Boolean = false
)

Then:

Home

Messages
   ●

Notifications
   12

The badge itself doesn't need custom spring physics.

It should simply participate in the tab layout.

8. IOSSlider

Don't reinvent accessibility/value semantics.

We'll provide custom presentation and motion while retaining proper slider behavior.

API:

@Composable
fun IOSSlider(
    value: Float,
    onValueChange:
        (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange:
        ClosedFloatingPointRange<Float> =
            0f..1f,
    steps: Int = 0,
    onValueChangeFinished:
        (() -> Unit)? = null
)

Our normalized state is:

fun normalizeSliderValue(
    value: Float,
    range:
        ClosedFloatingPointRange<Float>
): Float {

    val distance =
        range.endInclusive -
            range.start

    if (distance == 0f) {
        return 0f
    }

    return (
        (value -
            range.start) /
            distance
        ).coerceIn(
        0f,
        1f
    )
}

Then:

0 → left
1 → right

regardless of actual range.

9. Slider detent haptics

If:

steps > 0

we have logical stops.

For example:

0 ──●──●──●──●── 100

Crossing a step:

*tick*

Use Phase 2's detent logic.

Do not emit haptics continuously with value changes.

10. Slider thumb interaction

Normal:

●

Pressed:

slightly expanded ●

Use:

interaction progress

to drive:

thumb scale
track emphasis

No new physics implementation.

11. IOSMenu

We need lightweight context/action surfaces.

API:

@Composable
fun IOSMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content:
        @Composable IOSMenuScope.() -> Unit
)

Usage:

IOSMenu(
    expanded = menuOpen,
    onDismissRequest = {
        menuOpen = false
    }
) {

    item(
        label = "Save",
        onClick = { ... }
    )

    item(
        label = "Share",
        onClick = { ... }
    )

    separator()

    item(
        label = "Delete",
        role =
            IOSActionRole.Destructive,
        onClick = { ... }
    )
}
12. Menu visuals

Use Phase 7:

Glass Material
│
├── Save
├── Share
├─────────────
└── Delete

Something like:

IOSGlassSurface(
    config =
        IOSMaterialConfig(
            style =
                IOSMaterialStyle.Thick
        )
) {

    Column {
        menuContent()
    }
}

Don't give every menu row separate floating cards.

13. Menu entrance motion

The menu itself can use one normalized entrance value:

0 → hidden
1 → visible

Drive:

alpha
scale 0.96 → 1
translation
material density

from one spring.

Something like:

val visibility =
    remember {
        Animatable(0f)
    }

When opened:

visibility.animateTo(
    targetValue = 1f,
    animationSpec = spring(
        stiffness = 600f,
        dampingRatio = 0.86f
    )
)

Then:

graphicsLayer {

    alpha =
        visibility.value

    scaleX =
        0.96f +
            0.04f *
            visibility.value

    scaleY =
        scaleX
}
14. Don't build nested menus yet

Avoid:

Menu
→ submenu
→ submenu
→ submenu

The social-app benchmark doesn't need a desktop menu framework.

Keep Phase 8 focused.

15. Grouped lists

Create:

@Composable
fun IOSListSection(
    title: String? = null,
    footer: String? = null,
    content:
        @Composable ColumnScope.() -> Unit
)

Visual:

ACCOUNT

┌────────────────────────────┐
│ Profile                  > │
├────────────────────────────┤
│ Password                 > │
├────────────────────────────┤
│ Security                 > │
└────────────────────────────┘

Your account settings...

Unlike ordinary feed rows, grouped settings rows can share one surface.

16. Correct separator placement

Don't do:

────────────────────────────

all the way underneath a leading icon if the visual grouping calls for an inset separator.

Instead:

[icon] Text
       ───────────────────
[icon] Text

Expose:

enum class IOSListSeparatorStyle {
    Full,
    Inset,
    None
}

This becomes a component option rather than custom padding in every screen.

17. Build IOSFeelTheme

Until now we've used scattered defaults.

Time to create an actual theme.

@Immutable
data class IOSFeelColors(

    val background: Color,

    val surface: Color,

    val elevatedSurface: Color,

    val labelPrimary: Color,

    val labelSecondary: Color,

    val separator: Color,

    val accent: Color,

    val destructive: Color
)
18. Light palette

For example:

val IOSFeelLightColors =
    IOSFeelColors(

        background =
            Color(0xFFF7F7F8),

        surface =
            Color.White,

        elevatedSurface =
            Color(0xFFF2F2F4),

        labelPrimary =
            Color(0xFF111113),

        labelSecondary =
            Color(0xFF6E6E73),

        separator =
            Color.Black.copy(
                alpha = 0.08f
            ),

        accent =
            Color(0xFF3478F6),

        destructive =
            Color(0xFFE5484D)
    )

These are our design-system choices, not copied platform constants.

19. Dark palette
val IOSFeelDarkColors =
    IOSFeelColors(

        background =
            Color(0xFF000000),

        surface =
            Color(0xFF1C1C1E),

        elevatedSurface =
            Color(0xFF2C2C2E),

        labelPrimary =
            Color.White,

        labelSecondary =
            Color.White.copy(
                alpha = 0.62f
            ),

        separator =
            Color.White.copy(
                alpha = 0.10f
            ),

        accent =
            Color(0xFF5A92FF),

        destructive =
            Color(0xFFFF5A5F)
    )
20. Theme composition locals
val LocalIOSFeelColors =
    staticCompositionLocalOf {
        IOSFeelLightColors
    }

Similarly:

LocalIOSFeelTypography
LocalIOSFeelShapes
LocalIOSFeelMotion

Then:

object IOSFeelTheme {

    val colors: IOSFeelColors
        @Composable
        @ReadOnlyComposable
        get() =
            LocalIOSFeelColors.current

    val typography: IOSFeelTypography
        @Composable
        @ReadOnlyComposable
        get() =
            LocalIOSFeelTypography.current
}
21. Public theme
@Composable
fun IOSFeelTheme(
    darkTheme: Boolean =
        isSystemInDarkTheme(),
    content:
        @Composable () -> Unit
) {

    val colors =
        if (darkTheme)
            IOSFeelDarkColors
        else
            IOSFeelLightColors

    CompositionLocalProvider(

        LocalIOSFeelColors provides
            colors,

        LocalIOSFeelTypography provides
            IOSFeelTypography.Default,

        LocalIOSFeelShapes provides
            IOSFeelShapes.Default

    ) {

        content()
    }
}

Material 3 also structures theming around colors, typography, shapes and—on newer versions—motion schemes, so keeping our equivalent concepts centralized will make interoperability cleaner.

22. Don't block MaterialTheme interoperability

Some libraries inside an iOSFeel application may still expect:

MaterialTheme.colorScheme

So a practical IOSFeelTheme can internally provide a compatible Material theme too:

IOSFeelTheme {
    MaterialTheme(
        colorScheme =
            iosFeelMaterialColorScheme()
    ) {
        content()
    }
}

Or vice versa.

iOSFeel should coexist with the Android ecosystem, not require developers to rewrite every dependency.

23. Typography

Create:

@Immutable
data class IOSFeelTypography(

    val largeTitle: TextStyle,

    val title: TextStyle,

    val headline: TextStyle,

    val body: TextStyle,

    val callout: TextStyle,

    val caption: TextStyle,

    val badge: TextStyle
)

Use sp:

val Default =
    IOSFeelTypography(

        largeTitle =
            TextStyle(
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight =
                    FontWeight.Bold
            ),

        title =
            TextStyle(
                fontSize = 20.sp,
                lineHeight = 25.sp,
                fontWeight =
                    FontWeight.SemiBold
            ),

        body =
            TextStyle(
                fontSize = 17.sp,
                lineHeight = 22.sp
            ),

        ...
    )

Android's guidance remains to use scalable text units so user font-size settings are respected rather than hardcoding text in dp.

24. Don't fight large font sizes

This is extremely important for our “native feeling” goal.

Bad:

User selects 1.5× font

→ component clips text
→ framework forces 17sp anyway

Instead:

font scales
↓
component expands/reflows

That means avoid fixed component heights around multi-line text.

For instance, IOSListRow should use:

defaultMinSize(
    minHeight = 52.dp
)

rather than:

height(52.dp)
25. Large-font testing

Current Compose testing can override font scale locally through DeviceConfigurationOverride.FontScale(), making this testable without manually changing the whole device.

Test at:

1.0×
1.3×
1.5×
2.0×

especially:

IOSNavigationBar
IOSListRow
IOSButton
IOSMenu
IOSTabBar

At large scales, a tab bar might reasonably prioritize icon-only layouts rather than clipping text.

26. Accessibility semantics audit

Every component needs a semantic contract.

Compose tests inspect the semantics tree, which is also central to how accessibility information is exposed.

Our audit:

IOSButton
→ Button

IOSIconButton
→ Button + contentDescription

IOSToggle
→ Switch + checked

IOSSegmentedControl
→ selectableGroup

Segment
→ selected

IOSTabBar
→ tabs

IOSSlider
→ adjustable value semantics

IOSSearchField
→ editable text

IOSMenuItem
→ Button-like action

Don't bolt accessibility on after Phase 9.

27. State restoration

Most components should remain stateless externally:

IOSToggle(
    checked = checked,
    onCheckedChange = ...
)

This means the application owns the state.

That's good.

But components with internal interaction state such as:

search expanded state
menu state
navigation stack
sheet detent

need deliberate restoration rules.

General rule:

business/selection state
→ caller owns it

temporary visual animation progress
→ component owns it
→ does NOT need restoration

Don't save spring progress into a Bundle.

28. Example: don't save this
pressProgress = 0.437125f

across process death.

Meaningless.

Save:

selected tab = Profile
current sheet = Medium
navigation route = Post
search query = "compose"

not animation frames.

29. Component tests

Now every component should receive three categories of tests:

Logic tests
Semantics tests
Visual/screenshot tests

Example button semantics:

composeRule
    .onNodeWithText(
        "Continue"
    )
    .assertHasClickAction()

Toggle:

composeRule
    .onNodeWithTag(
        "notifications-toggle"
    )
    .assertIsOn()

Semantics make these tests much less dependent on pixel positions.

30. Screenshot/golden tests

Create a dedicated package:

component-screenshot-tests/
├── IOSButtonScreenshotTest.kt
├── IOSToggleScreenshotTest.kt
├── IOSSegmentedScreenshotTest.kt
├── IOSMenuScreenshotTest.kt
├── IOSTabBarScreenshotTest.kt
└── IOSSheetScreenshotTest.kt

For each important component capture:

Light
Dark

Normal
Pressed
Selected
Disabled

1× font
1.5× font

phone
tablet

Do not generate every combinatorial possibility.

Choose representative states.

31. Golden tests aren't enough

A screenshot can tell us:

looks wrong

but not:

gesture feels wrong
haptic mistimed
fling velocity wrong
TalkBack cannot activate it

So our validation stack becomes:

Pure tests
↓
behavior

Semantics tests
↓
accessibility/interaction

Golden tests
↓
visual regression

Macrobenchmark/JankStats
↓
performance

Physical device
↓
actual feel

That's much stronger than screenshot matching alone.

32. Clean the public package

Right now we may have dozens of internal classes.

Developers should mainly see:

dev.iosfeel

IOSFeelTheme

components.*
IOSButton
IOSIconButton
IOSToggle
IOSSegmentedControl
IOSListRow
IOSSearchField
IOSTabBar
IOSNavigationBar
IOSSlider
IOSMenu
IOSBadge

navigation.*
IOSNavigationStack
rememberIOSNavigationState

sheet.*
IOSSheet
rememberIOSSheetState

scroll.*
rememberIOSFlingBehavior

material.*
IOSGlassSurface
IOSBackdropLayout

They should not need:

IOSHapticRateLimiter
IOSScrollInteractionState
IOSGestureDecisionMathInternal
IOSGlassPerformanceInternals

Those become:

internal

where appropriate.

33. Create an umbrella module

Eventually add:

iosfeel/

with dependencies:

api(
    project(
        ":iosfeel-components"
    )
)

api(
    project(
        ":iosfeel-navigation"
    )
)

api(
    project(
        ":iosfeel-sheet"
    )
)

api(
    project(
        ":iosfeel-scroll"
    )
)

api(
    project(
        ":iosfeel-material"
    )
)

Then developers can choose:

implementation(
    "dev.iosfeel:iosfeel:..."
)

or individual smaller modules.

34. Keep experimental APIs marked

Some of Phase 7's shader behavior isn't stable enough yet.

Create:

@RequiresOptIn
annotation class ExperimentalIOSFeelApi

Then:

@ExperimentalIOSFeelApi
fun enableRefraction(...)

Don't freeze experimental shader parameters into our stable public API too early.

35. Add documentation samples

Each public component needs a tiny example.

For example:

var notifications
    by rememberSaveable {
        mutableStateOf(true)
    }

IOSToggle(
    checked =
        notifications,

    onCheckedChange = {
        notifications = it
    }
)

and:

val sheetState =
    rememberIOSSheetState(
        IOSSheetDetent.Medium
    )

IOSSheet(
    state =
        sheetState,

    detents =
        listOf(
            IOSSheetDetent.Medium,
            IOSSheetDetent.Large
        ),

    onDismissRequest = {
        ...
    }
) {

    Comments()
}

A good framework should be understandable without reading its internals.

36. Final Component Laboratory

Our Laboratory should now look like a real design-system catalog:

iOSFeel

FOUNDATIONS
Motion
Haptics
Gestures
Scrolling
Materials

NAVIGATION
Navigation
Sheets

CONTROLS
Buttons
Icon Buttons
Toggle
Slider
Segmented
Search

CONTENT
List Rows
Grouped Lists
Badges

OVERLAYS
Menus
Glass Surfaces

STRESS TESTS
1000-row Feed
Navigation Regrab
Sheet + List
IME + Sheet
Glass 120Hz
Accessibility
Large Text
Dark Mode

This becomes extremely useful when tuning the framework later.

37. Build the complete fake app screen

Now make the integrated profile prototype much closer to an actual app:

┌────────────────────────────────┐
│ ‹              ankit       ⋯   │
│                                │
│           avatar               │
│                                │
│  128        3.4K       412     │
│  Posts    Followers  Following │
│                                │
│ [ Follow ]  [ Message ]  [⌄]   │
│                                │
│ Posts       Reels       Tagged │
│ ─────                          │
│                                │
│  □   □   □                     │
│  □   □   □                     │
│  □   □   □                     │
│                                │
│                                │
├────────────────────────────────┤
│ Home  Search   +   Reels   ●   │
└────────────────────────────────┘

And interactions should already use:

Navigation engine
Scroll physics
Glass tab bar
Press engine
Haptics
Segmented control
Menu
Sheet

At this point, do not add any new engine because the profile screen needs something slightly different.

If a primitive is missing, improve the reusable primitive.

Phase 8 complete

Our framework now has:

✅ unified press interactions
✅ button
✅ icon button
✅ toggle
✅ segmented control
✅ slider
✅ search field
✅ list row
✅ grouped lists
✅ badges
✅ menu/context surfaces
✅ navigation bar
✅ tab bar
✅ destructive states
✅ disabled states
✅ light theme
✅ dark theme
✅ typography system
✅ shape/spacing tokens
✅ large-text adaptation
✅ accessibility semantics
✅ state-restoration strategy
✅ semantics tests
✅ screenshot/golden-test architecture
✅ public API cleanup
✅ experimental API boundary
✅ full component catalog

So we have reached:

Phase 0  Foundation              ✅
Phase 1  Motion                  ✅
Phase 2  Haptics                 ✅
Phase 3  Gestures                ✅
Phase 4  Navigation              ✅
Phase 5  Scrolling               ✅
Phase 6  Sheets                  ✅
Phase 7  Materials / Glass       ✅
Phase 8  Components              ✅

Phase 9  Social-App Benchmark    ← NEXT