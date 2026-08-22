Phase 6B — Production-Ready Sheets

Phase 6A gave us the physics. Phase 6B makes IOSSheet survive things real apps do: keyboards appearing, rotation, gesture navigation, state restoration, dismissal, accessibility, and different screen sizes.

One useful fact from the current Compose APIs: WindowInsets animations, including IME changes, are already synchronized and backported through Compose, so we should cooperate with that system instead of manually animating keyboard heights ourselves.

1. First improve the public API

We eventually want developers to write this:

val sheetState =
    rememberIOSSheetState(
        initialDetent =
            IOSSheetDetent.Medium
    )

IOSSheet(
    state = sheetState,

    detents = listOf(
        IOSSheetDetent.Medium,
        IOSSheetDetent.Large
    ),

    onDismissRequest = {
        showComments = false
    }
) {

    CommentsScreen()
}

Not:

IOSSheet(
    randomOffset = 1384f,
    rawVelocityTracker = ...,
    nestedConnection = ...,
    hapticController = ...,
    animationController = ...
)

All of that complexity stays internal.

2. Redesign IOSSheetState

Instead of requiring an initial pixel offset:

IOSSheetState(
    initialOffset = ...
)

we should initialize semantically.

Replace it with:

@Stable
class IOSSheetState internal constructor(
    initialDetent:
        IOSSheetDetent
) {

    val offset =
        Animatable(0f)

    var currentDetent
        by mutableStateOf(
            initialDetent
        )
        internal set

    var targetDetent
        by mutableStateOf(
            initialDetent
        )
        internal set

    var phase
        by mutableStateOf(
            IOSSheetPhase.Idle
        )
        internal set

    var velocity
        by mutableFloatStateOf(0f)
        internal set

    internal var resolved =
        emptyList<IOSResolvedDetent>()

    val isDragging: Boolean
        get() =
            phase ==
                IOSSheetPhase.Dragging

    val isSettling: Boolean
        get() =
            phase ==
                IOSSheetPhase.Settling
}

Now state talks in terms of:

Medium
Large
Compact

while pixels remain an implementation detail.

3. Remember/save sheet state

Create:

@Composable
fun rememberIOSSheetState(
    initialDetent:
        IOSSheetDetent =
            IOSSheetDetent.Medium
): IOSSheetState {

    val restoredId =
        rememberSaveable {
            mutableStateOf(
                initialDetent.id
            )
        }

    return remember {
        IOSSheetState(
            initialDetent =
                initialDetent
        )
    }
}

But let's make this properly restorable with a saver.

Create:

val IOSSheetStateSaver =
    Saver<IOSSheetState, String>(

        save = {
            it.currentDetent.id
        },

        restore = { id ->

            val detent =
                when (id) {

                    "compact" ->
                        IOSSheetDetent.Compact

                    "medium" ->
                        IOSSheetDetent.Medium

                    "large" ->
                        IOSSheetDetent.Large

                    else ->
                        IOSSheetDetent.Medium
                }

            IOSSheetState(
                initialDetent = detent
            )
        }
    )

Then:

@Composable
fun rememberIOSSheetState(
    initialDetent:
        IOSSheetDetent =
            IOSSheetDetent.Medium
): IOSSheetState {

    return rememberSaveable(
        saver =
            IOSSheetStateSaver
    ) {

        IOSSheetState(
            initialDetent =
                initialDetent
        )
    }
}

Now rotation doesn't unexpectedly reset:

Large
↓ rotate
Large

instead of:

Large
↓ rotate
Medium 😐
4. Dynamic resizing

This is extremely important.

Suppose:

portrait:
height = 2400px

Medium = 1080px

Then rotate:

landscape:
height = 1080px

The old:

offset = 1080px

is meaningless.

Because state stores:

currentDetent = Medium

we can resolve its new position.

Create:

fun findResolvedDetent(
    detent: IOSSheetDetent,
    resolved:
        List<IOSResolvedDetent>
): IOSResolvedDetent? {

    return resolved.firstOrNull {
        it.detent.id ==
            detent.id
    }
}

When container size changes:

LaunchedEffect(
    containerHeightPx,
    resolvedDetents
) {

    state.resolved =
        resolvedDetents

    val newTarget =
        findResolvedDetent(
            state.currentDetent,
            resolvedDetents
        ) ?: resolvedDetents.first()

    state.offset.snapTo(
        newTarget.offsetPx
    )
}

Now the sheet stays conceptually at:

MEDIUM

regardless of screen dimensions.

5. Tablets need different detents

Hardcoding:

Medium = 45% from top

for every device isn't ideal.

Create a resolver abstraction:

fun interface IOSSheetDetentResolver {

    fun resolve(
        detent: IOSSheetDetent,
        containerWidthPx: Float,
        containerHeightPx: Float
    ): Float
}

Default:

object IOSDefaultDetentResolver :
    IOSSheetDetentResolver {

    override fun resolve(
        detent: IOSSheetDetent,
        containerWidthPx: Float,
        containerHeightPx: Float
    ): Float {

        return when (detent) {

            IOSSheetDetent.Large ->
                containerHeightPx *
                    0.08f

            IOSSheetDetent.Medium ->
                containerHeightPx *
                    0.45f

            IOSSheetDetent.Compact ->
                containerHeightPx *
                    0.78f

            is IOSSheetDetent.Fraction ->
                containerHeightPx *
                    (1f -
                        detent.fraction)
        }
    }
}

Later:

phone portrait
tablet portrait
landscape
foldable

can use different resolvers without rewriting sheet physics.

6. System insets

Current Android/Compose expects edge-to-edge layouts, especially with modern target SDKs. Android 15+ enforces edge-to-edge behavior when targeting API 35+, so components need to understand system bars and cutouts.

For our sheet, use inset-aware content.

Something like:

Column(
    modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(
            WindowInsets.safeDrawing
                .only(
                    WindowInsetsSides.Horizontal +
                        WindowInsetsSides.Bottom
                )
        )
) {
    content()
}

But notice something important:

We probably don't want to inset the entire sheet away from the top.

The sheet itself can extend edge-to-edge.

Interactive content should be protected.

Architecture:

Sheet background
    ↓
can extend behind system areas

Sheet content
    ↓
safe inset handling
7. Gesture-safe areas

Android exposes safeGestures specifically to help prevent application gestures from conflicting with system gestures—for example bottom sheets and similar drag surfaces.

So our sheet should avoid putting critical drag/tap targets underneath mandatory gesture regions.

You can obtain:

WindowInsets.safeGestures

for those areas.

Don't globally use:

systemGestureExclusion()

unless we later prove there's a specific conflict requiring it.

Cooperate with Android first.

8. Keyboard / IME behavior

Now imagine an Instagram-like comments sheet:

┌───────────────────────┐
│ Comments              │
│                       │
│ Comment 1             │
│ Comment 2             │
│                       │
│ [ Write a comment ]   │
└───────────────────────┘

User taps the input.

Keyboard appears.

Bad implementation:

keyboard
↑
covers input

or:

sheet jumps instantly upward

We want:

keyboard animates
       ↓
sheet content responds
       ↓
input remains visible

Compose's IME insets animate automatically, and imePadding() is designed to adjust UI with those animations.

9. Apply IME padding inside sheet content

For a comments sheet:

Column(
    modifier = Modifier
        .fillMaxSize()
        .imePadding()
) {

    IOSScrollableLazyColumn(
        modifier =
            Modifier.weight(1f)
    ) {
        // comments
    }

    CommentInput()
}

Now:

keyboard hidden
↓
normal bottom

keyboard visible
↓
padding updates with IME animation

No custom keyboard-height animation needed.

10. IME nested scrolling

Current Compose also exposes:

Modifier.imeNestedScroll()

on Android 11+, allowing the keyboard itself to participate in nested scrolling and be dragged with content.

For keyboard-heavy sheet content:

Modifier
    .imePadding()
    .imeNestedScroll()

can become an optional integration.

Don't force it globally, because not every sheet wants an interactively draggable keyboard.

Add configuration:

data class IOSSheetConfig(

    val dismissOnScrimTap:
        Boolean = true,

    val dismissible:
        Boolean = true,

    val useImePadding:
        Boolean = true,

    val useImeNestedScroll:
        Boolean = false
)
11. What happens when keyboard opens at Medium?

Interesting question.

Suppose:

Sheet = Medium
keyboard opens

We have two possible policies.

Policy A

Keep the sheet detent unchanged and shrink/pad its usable content.

Policy B

Automatically promote:

Medium → Large

for editing.

For our framework, support both.

Add:

enum class IOSSheetImeBehavior {
    KeepDetent,
    ExpandToLarge
}

Then:

data class IOSSheetConfig(
    ...
    val imeBehavior:
        IOSSheetImeBehavior =
            IOSSheetImeBehavior.KeepDetent
)
12. Detect IME

In Compose:

val density =
    LocalDensity.current

val imeBottom =
    WindowInsets.ime
        .getBottom(density)

val imeVisible =
    imeBottom > 0

Then:

LaunchedEffect(
    imeVisible
) {

    if (
        imeVisible &&
        config.imeBehavior ==
        IOSSheetImeBehavior
            .ExpandToLarge
    ) {

        state.animateTo(
            IOSSheetDetent.Large
        )
    }
}

Don't manually animate based on keyboard pixels.

Use the sheet's normal detent physics.

13. Programmatic sheet API

Add methods to IOSSheetState.

suspend fun animateTo(
    detent: IOSSheetDetent,
    initialVelocity: Float = 0f
) {

    val target =
        resolved.firstOrNull {
            it.detent.id ==
                detent.id
        } ?: return

    targetDetent =
        detent

    phase =
        IOSSheetPhase.Settling

    offset.animateTo(
        targetValue =
            target.offsetPx,

        initialVelocity =
            initialVelocity,

        animationSpec =
            spring(
                stiffness = 300f,
                dampingRatio = 0.78f
            )
    )

    currentDetent =
        detent

    velocity = 0f

    phase =
        IOSSheetPhase.Idle
}

Eventually:

sheetState.expand()

sheetState.collapse()

sheetState.animateTo(
    IOSSheetDetent.Medium
)
14. Convenience APIs
suspend fun expand() {

    resolved
        .firstOrNull()
        ?.let {
            animateTo(
                it.detent
            )
        }
}

And:

suspend fun collapse() {

    resolved
        .lastOrNull()
        ?.let {
            animateTo(
                it.detent
            )
        }
}

Now developers don't need to know which detent is top/bottom.

15. Dismissal

Add another logical state:

data object Hidden :
    IOSSheetDetent {

    override val id =
        "hidden"
}

But I'd rather keep hidden separate from detents.

Why?

Because:

Compact

is still visible sheet UI.

Hidden

means the sheet isn't part of the interaction anymore.

So add:

var visible by
    mutableStateOf(true)
    internal set

and:

suspend fun dismiss(
    containerHeightPx: Float
) {

    phase =
        IOSSheetPhase.Settling

    offset.animateTo(
        targetValue =
            containerHeightPx,

        initialVelocity =
            velocity,

        animationSpec =
            spring(
                stiffness = 320f,
                dampingRatio = 0.85f
            )
    )

    visible = false
}
16. Drag-to-dismiss

If:

Sheet at Compact

and the user throws it downward sufficiently:

Compact
↓
fast downward gesture
↓
DISMISS

Extend target decision:

sealed interface IOSSheetTarget {

    data class Detent(
        val value:
            IOSResolvedDetent
    ) : IOSSheetTarget

    data object Dismiss :
        IOSSheetTarget
}

Then:

fun chooseSheetTarget(
    ...,
    dismissible: Boolean,
    dismissVelocityThreshold: Float
): IOSSheetTarget

If:

at lowest detent
+
velocity > threshold

return:

Dismiss
17. Don't dismiss too easily

A sheet disappearing accidentally would feel awful.

So require something like:

lowest detent
+
clear downward intention
+
velocity threshold

not simply:

current position slightly below Compact

Our future Laboratory can tune:

dismiss velocity:
1400 px/s
1800 px/s
2200 px/s

Again—not Apple numbers.

18. Scrim

Behind a modal sheet:

App content
   ↓
Scrim
   ↓
Sheet

Create:

Box(
    modifier = Modifier
        .fillMaxSize()
        .background(
            Color.Black.copy(
                alpha =
                    0.30f *
                    expansionProgress
            )
        )
)

For now it's a simple translucent overlay.

Phase 7 will improve the visual treatment.

19. Scrim tap
Modifier.clickable(
    enabled =
        config.dismissOnScrimTap
) {

    scope.launch {
        state.dismiss(
            containerHeightPx
        )
    }
}

But don't use a normal clickable if it gives unwanted ripple.

Prefer:

Modifier.pointerInput(...)

or a no-indication interaction source if appropriate.

The scrim isn't visually a button.

20. Accessibility semantics

This matters a lot.

A draggable handle that only works with gestures is inaccessible.

We should expose semantic actions.

For example:

Modifier.semantics {

    contentDescription =
        "Sheet"

    stateDescription =
        state.currentDetent.id

    customActions =
        listOf(

            CustomAccessibilityAction(
                label =
                    "Expand sheet"
            ) {

                scope.launch {
                    state.expand()
                }

                true
            },

            CustomAccessibilityAction(
                label =
                    "Collapse sheet"
            ) {

                scope.launch {
                    state.collapse()
                }

                true
            }
        )
}

Now TalkBack users aren't required to reproduce a drag gesture precisely.

21. Dismiss semantics

If dismissible:

dismiss {

    scope.launch {
        state.dismiss(
            containerHeightPx
        )
    }

    true
}

So accessibility services understand:

this surface can be dismissed

instead of seeing an anonymous draggable box.

22. Grabber touch target

Visually:

────

might be only:

36 × 5dp

But the actual gesture target should be much larger.

Something like:

visual:
36×5dp

touch region:
full 48dp+ header

Don't make users precisely hit a 5dp strip.

23. Back button behavior

If sheet is modal and visible:

Android Back
↓
dismiss sheet

instead of:

pop navigation screen underneath it

Use:

BackHandler(
    enabled =
        state.visible &&
        config.dismissible
) {

    scope.launch {

        state.dismiss(
            containerHeightPx
        )
    }
}

Eventually we can also integrate Predictive Back with sheet dismissal.

24. Predictive back + sheets

Imagine:

Comments sheet open

User performs system back gesture.

Rather than:

nothing → suddenly disappear

we can eventually map:

back progress 0→1
        ↓
sheet offset
        ↓
screen height

For example:

PredictiveBackHandler(
    enabled =
        state.visible &&
        config.dismissible
) { events ->

    val start =
        state.offset.value

    try {

        events.collect { event ->

            val destination =
                containerHeightPx

            val offset =
                start +
                    (
                        destination -
                            start
                    ) *
                    event.progress

            state.offset.snapTo(
                offset
            )
        }

        state.dismiss(
            containerHeightPx
        )

    } catch (
        cancellation:
            CancellationException
    ) {

        state.animateTo(
            state.currentDetent
        )
    }
}

That can be extremely polished.

25. Sheet layout structure

The public component should become approximately:

@Composable
fun IOSSheet(
    state: IOSSheetState,
    detents:
        List<IOSSheetDetent>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    config:
        IOSSheetConfig =
            IOSSheetConfig(),
    content:
        @Composable IOSSheetScope.() -> Unit
)
26. Create IOSSheetScope
@Stable
interface IOSSheetScope {

    val state:
        IOSSheetState

    val expansionProgress:
        Float
}

Implementation:

private class IOSSheetScopeImpl(
    override val state:
        IOSSheetState,

    override val expansionProgress:
        Float
) : IOSSheetScope

Then developers could eventually do:

IOSSheet(...) {

    Text(
        "Progress: $expansionProgress"
    )
}

without touching internal offset calculations.

27. Complete visual hierarchy

Conceptually:

Box
│
├── background application
│
├── scrim
│
└── sheet graphics layer
     │
     ├── rounded surface
     │
     ├── grabber region
     │
     └── sheet content
          │
          └── optional
              nested scrolling

Simple and understandable.

28. Don't apply IME padding twice

A common bug:

parent Scaffold
applies keyboard padding

+

sheet
applies keyboard padding

=

massive empty space

Compose inset modifiers consume inset portions specifically to prevent repeated application in many nested situations, but we should still clearly document who owns IME handling in our sheet API.

Recommended:

IOSSheet owns:
bottom sheet IME behavior

Sheet content should normally
not apply another outer imePadding()

unless the developer deliberately opts out of framework handling.

29. Updated configuration
@Immutable
data class IOSSheetConfig(

    val dismissible:
        Boolean = true,

    val dismissOnScrimTap:
        Boolean = true,

    val dismissVelocityThreshold:
        Float = 1800f,

    val useImePadding:
        Boolean = true,

    val useImeNestedScroll:
        Boolean = false,

    val imeBehavior:
        IOSSheetImeBehavior =
            IOSSheetImeBehavior.KeepDetent,

    val showGrabber:
        Boolean = true
)
30. Testing

Add tests for state restoration.

@Test
fun mediumDetentCanBeSaved() {

    val state =
        IOSSheetState(
            IOSSheetDetent.Medium
        )

    val saved =
        IOSSheetStateSaver
            .save(state)

    assertEquals(
        "medium",
        saved
    )
}

Test resize resolution:

@Test
fun detentRecalculatesAfterResize() {

    val phone =
        resolveSheetDetents(
            2400f,
            listOf(
                IOSSheetDetent.Medium
            )
        ).first()

    val landscape =
        resolveSheetDetents(
            1000f,
            listOf(
                IOSSheetDetent.Medium
            )
        ).first()

    assertNotEquals(
        phone.offsetPx,
        landscape.offsetPx
    )
}
31. Test dismissal
@Test
fun fastDownwardFlingAtLowestDetentDismisses() {

    val result =
        chooseSheetTarget(
            currentOffset =
                compactOffset,

            velocityY =
                2500f,

            detents =
                resolved,

            dismissible =
                true,

            dismissVelocityThreshold =
                1800f
        )

    assertEquals(
        IOSSheetTarget.Dismiss,
        result
    )
}
32. Test that Medium doesn't accidentally dismiss
@Test
fun downwardFlingFromMediumTargetsCompactFirst() {

    val result =
        chooseSheetTarget(
            currentOffset =
                mediumOffset,

            velocityY =
                2000f,

            ...
        )

    assertTrue(
        result is
            IOSSheetTarget.Detent
    )
}

That's a crucial behavioral distinction.

33. Real-world test: Comments sheet

Now make the Laboratory much more realistic.

Feed screen

[ Open Comments ]
      ↓

Comments Sheet

 ─────

 932 comments

 Alex
 This looks great...

 Jordan
 Nice work...

 ...

 [ Add a comment... ]

Test:

Medium
↓
drag up
↓
Large

scroll comments
↓
works

tap text field
↓
keyboard animates
↓
input remains visible

swipe keyboard down
↓
IME follows where supported

scroll list to top
↓
pull down
↓
sheet collapses

throw from Compact
↓
sheet dismisses

This is the first truly app-like iOSFeel interaction.

Phase 6 complete

We now have:

IOSSheet
   │
   ├── semantic detents
   ├── custom detents
   ├── gesture tracking
   ├── real velocity
   ├── spring settling
   ├── interruption
   ├── re-grabbing
   ├── detent haptics
   ├── nested scrolling
   ├── nested fling
   ├── IME handling
   ├── edge-to-edge
   ├── safe gesture regions
   ├── state restoration
   ├── rotation resizing
   ├── tablet resizing
   ├── accessibility
   ├── scrim dismissal
   └── drag dismissal

So our status is now:

Phase 0  Foundation             ✅
Phase 1  Motion                 ✅
Phase 2  Haptics                ✅
Phase 3  Gestures               ✅
Phase 4  Navigation             ✅
Phase 5  Scrolling              ✅
Phase 6  Sheets                 ✅

Phase 7  Materials / Glass      ← NEXT
Phase 8  Components
Phase 9  Social-app benchmark