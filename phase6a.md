Phase 6 — Sheets & Detents

Now we build the first component that really combines the whole framework:

Gesture Engine
      +
Motion Engine
      +
Haptic Engine
      +
Scroll/Nested Scroll
      ↓
IOSSheet

The target is a draggable bottom sheet with:

Compact
Medium
Large

detents, velocity-aware snapping, interruption, threshold haptics, and proper cooperation with a LazyColumn inside.

1. Create the module
iosfeel-sheet/
└── src/main/java/dev/iosfeel/sheet/
    ├── IOSSheet.kt
    ├── IOSSheetState.kt
    ├── IOSSheetDetent.kt
    ├── IOSSheetConfig.kt
    ├── IOSSheetPhysics.kt
    ├── IOSSheetNestedConnection.kt
    └── RememberIOSSheetState.kt

Dependencies:

iosfeel-sheet
   ├── iosfeel-core
   ├── iosfeel-motion
   ├── iosfeel-gesture
   ├── iosfeel-haptics
   └── iosfeel-scroll
2. Define detents

IOSSheetDetent.kt

package dev.iosfeel.sheet

sealed interface IOSSheetDetent {

    val id: String

    data object Compact : IOSSheetDetent {
        override val id = "compact"
    }

    data object Medium : IOSSheetDetent {
        override val id = "medium"
    }

    data object Large : IOSSheetDetent {
        override val id = "large"
    }

    data class Fraction(
        override val id: String,
        val fraction: Float
    ) : IOSSheetDetent {
        init {
            require(fraction in 0f..1f)
        }
    }
}

Eventually developers can do:

IOSSheetDetent.Fraction(
    id = "preview",
    fraction = 0.28f
)
3. Resolve detents to actual positions

Suppose screen height is:

2400px

We might define:

Large   → y = 150
Medium  → y = 1050
Compact → y = 1900

Remember: for a bottom sheet, a smaller Y means higher/open.

Create:

data class IOSResolvedDetent(
    val detent: IOSSheetDetent,
    val offsetPx: Float
)

Then:

fun resolveSheetDetents(
    containerHeightPx: Float,
    detents: List<IOSSheetDetent>
): List<IOSResolvedDetent> {

    return detents.map { detent ->

        val offset =
            when (detent) {

                IOSSheetDetent.Large ->
                    containerHeightPx * 0.08f

                IOSSheetDetent.Medium ->
                    containerHeightPx * 0.45f

                IOSSheetDetent.Compact ->
                    containerHeightPx * 0.78f

                is IOSSheetDetent.Fraction ->
                    containerHeightPx *
                        (1f - detent.fraction)
            }

        IOSResolvedDetent(
            detent = detent,
            offsetPx = offset
        )
    }.sortedBy {
        it.offsetPx
    }
}

Again, these are our tuning values, not Apple internals.

4. Sheet state

Create IOSSheetState.kt:

package dev.iosfeel.sheet

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class IOSSheetPhase {
    Idle,
    Dragging,
    Settling
}

@Stable
class IOSSheetState(
    initialOffset: Float
) {

    val offset =
        Animatable(initialOffset)

    var phase by mutableStateOf(
        IOSSheetPhase.Idle
    )
        internal set

    var velocity by mutableFloatStateOf(0f)
        internal set

    var activeDetentId by mutableStateOf<String?>(null)
        internal set
}
5. Remember API
@Composable
fun rememberIOSSheetState(
    initialOffset: Float = 0f
): IOSSheetState {

    return remember {
        IOSSheetState(
            initialOffset = initialOffset
        )
    }
}
6. Find the nearest detent

Create:

fun nearestDetent(
    currentOffset: Float,
    detents: List<IOSResolvedDetent>
): IOSResolvedDetent {

    require(detents.isNotEmpty())

    return detents.minBy {
        kotlin.math.abs(
            it.offsetPx -
                currentOffset
        )
    }
}

But nearest isn't enough.

Velocity matters.

7. Velocity-aware detent selection

Suppose the sheet is closest to Medium, but the user throws it downward very fast.

It should probably continue toward Compact.

Create:

fun chooseSheetTarget(
    currentOffset: Float,
    velocityY: Float,
    detents: List<IOSResolvedDetent>,
    velocityThreshold: Float = 900f
): IOSResolvedDetent {

    require(detents.isNotEmpty())

    val nearestIndex =
        detents.indices.minBy { index ->
            kotlin.math.abs(
                detents[index].offsetPx -
                    currentOffset
            )
        }

    if (
        kotlin.math.abs(velocityY) <
        velocityThreshold
    ) {
        return detents[nearestIndex]
    }

    return if (velocityY > 0f) {

        detents[
            (nearestIndex + 1)
                .coerceAtMost(
                    detents.lastIndex
                )
        ]

    } else {

        detents[
            (nearestIndex - 1)
                .coerceAtLeast(0)
        ]
    }
}

So:

fast upward throw
→ next higher detent

fast downward throw
→ next lower detent
8. Sheet drag

The sheet should directly follow the finger.

Create:

suspend fun IOSSheetState.beginDrag() {

    velocity =
        offset.velocity

    offset.stop()

    phase =
        IOSSheetPhase.Dragging
}

Then:

suspend fun IOSSheetState.dragBy(
    deltaY: Float,
    minOffset: Float,
    maxOffset: Float,
    gestureVelocity: Float
) {

    phase =
        IOSSheetPhase.Dragging

    velocity =
        gestureVelocity

    val next =
        (
            offset.value +
                deltaY
        ).coerceIn(
            minOffset,
            maxOffset
        )

    offset.snapTo(next)
}
9. Settle to detent
suspend fun IOSSheetState.settleTo(
    target: IOSResolvedDetent,
    initialVelocity: Float,
    springSpec: IOSSpringSpec =
        IOSMotionPreset.Smooth
) {

    phase =
        IOSSheetPhase.Settling

    velocity =
        initialVelocity

    offset.animateTo(
        targetValue =
            target.offsetPx,

        initialVelocity =
            initialVelocity,

        animationSpec =
            spring(
                stiffness =
                    springSpec.stiffness,

                dampingRatio =
                    springSpec.dampingRatio
            )
    ) {
        velocity =
            this.velocity
    }

    activeDetentId =
        target.detent.id

    velocity = 0f

    phase =
        IOSSheetPhase.Idle
}

Now the pattern is familiar:

finger owns sheet
    ↓ release
velocity preserved
    ↓
spring owns sheet
10. Haptic detent crossing

Use Phase 2.

Create an IOSHapticDetents instance with resolved positions.

As the sheet moves:

Compact
   ↓
*tick*
Medium
   ↓
*tick*
Large

But don't trigger on every frame.

Only when the nearest logical detent changes.

11. First IOSSheet

Conceptually:

@Composable
fun IOSSheet(
    state: IOSSheetState,
    detents: List<IOSSheetDetent>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)

Container:

BoxWithConstraints(
    modifier = modifier.fillMaxSize()
) {

    val density =
        LocalDensity.current

    val heightPx =
        with(density) {
            maxHeight.toPx()
        }

    val resolved =
        remember(
            heightPx,
            detents
        ) {
            resolveSheetDetents(
                containerHeightPx =
                    heightPx,
                detents = detents
            )
        }

    val minOffset =
        resolved.first().offsetPx

    val maxOffset =
        resolved.last().offsetPx
}
12. Render the sheet with graphicsLayer
Box(
    modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
            translationY =
                state.offset.value
        }
) {
    content()
}

Don't relayout the entire sheet every drag frame.

One layer translation is cheaper.

13. Rounded sheet surface

For now:

Surface(
    shape = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp
    )
) {
    content()
}

Don't build the glass renderer here.

Phase 7 will replace/enhance the surface treatment.

14. Add the grabber
Box(
    Modifier
        .padding(top = 8.dp)
        .size(
            width = 36.dp,
            height = 5.dp
        )
        .clip(
            RoundedCornerShape(50)
        )
        .background(
            Color.Gray
        )
)

This is mostly a visual affordance.

15. Gesture handling

For the initial prototype, use vertical gesture tracking:

Modifier.iosGesture(
    state = gestureState,

    config = IOSGestureConfig(
        direction =
            IOSGestureDirection.Vertical,
        progressDistancePx =
            heightPx
    ),

    onStarted = {

        scope.launch {
            state.beginDrag()
        }
    },

    onChanged = {

        scope.launch {

            state.dragBy(
                deltaY =
                    gestureState.translationY -
                        previousTranslation,

                minOffset =
                    minOffset,

                maxOffset =
                    maxOffset,

                gestureVelocity =
                    gestureState.velocityY
            )
        }
    }
)

Track only the delta since the previous callback.

Do not repeatedly add the total translation.

16. Release

On gesture end:

val target =
    chooseSheetTarget(
        currentOffset =
            state.offset.value,

        velocityY =
            gestureState.velocityY,

        detents =
            resolved
    )

Then:

scope.launch {

    state.settleTo(
        target =
            target,

        initialVelocity =
            gestureState.velocityY
    )
}
17. Now the hard part: scrolling inside the sheet

Imagine:

Comments Sheet

┌─────────────────────────┐
│         grabber         │
│                         │
│ Comment 1               │
│ Comment 2               │
│ Comment 3               │
│ ...                     │
└─────────────────────────┘

We need this behavior:

Sheet at Medium

finger scrolls UP
        ↓
sheet should first expand toward Large
        ↓
then content scrolls

And when at Large:

list is scrolled down

finger pulls DOWN
        ↓
list scrolls toward top first
        ↓
when list reaches top
        ↓
sheet starts collapsing

This is exactly why we built nested scrolling in Phase 5.

18. Sheet nested connection

Create:

IOSSheetNestedConnection.kt

Pseudo-architecture:

class IOSSheetNestedConnection(
    private val sheetState: IOSSheetState,
    private val detents:
        () -> List<IOSResolvedDetent>
) : NestedScrollConnection
onPreScroll

When child content is trying to scroll upward and the sheet isn't fully expanded:

finger up
↓
expand sheet first

So:

override fun onPreScroll(
    available: Offset,
    source: NestedScrollSource
): Offset {

    val deltaY =
        available.y

    if (deltaY >= 0f) {
        return Offset.Zero
    }

    val top =
        detents()
            .first()
            .offsetPx

    if (
        sheetState.offset.value <=
        top
    ) {
        return Offset.Zero
    }

    val availableExpansion =
        sheetState.offset.value -
            top

    val requested =
        -deltaY

    val consumed =
        minOf(
            requested,
            availableExpansion
        )

    return Offset(
        x = 0f,
        y = -consumed
    )
}

In implementation, you'll also update the sheet position.

19. onPostScroll

When child can't consume downward movement because it's already at the top:

list top reached
↓
remaining downward delta
↓
collapse sheet

So:

override fun onPostScroll(
    consumed: Offset,
    available: Offset,
    source: NestedScrollSource
): Offset {

    if (available.y <= 0f) {
        return Offset.Zero
    }

    // apply available.y to sheet
}

That's the core of the sheet/list handoff.

20. Nested fling

We also need:

fast downward fling

when the list is at the top to be able to collapse the sheet.

Use:

override suspend fun onPostFling(
    consumed: Velocity,
    available: Velocity
): Velocity

Then choose a detent using:

available.y

as the initial sheet velocity.

This lets momentum transfer from:

list
↓
sheet

instead of suddenly stopping.

21. Sheet backdrop transform

Now add subtle background response.

Suppose sheet progresses from Compact → Large.

Calculate:

fun calculateSheetExpansionProgress(
    offset: Float,
    minOffset: Float,
    maxOffset: Float
): Float {

    if (maxOffset == minOffset) {
        return 1f
    }

    return (
        1f -
            (
                offset -
                    minOffset
            ) /
            (
                maxOffset -
                    minOffset
            )
        ).coerceIn(
        0f,
        1f
    )
}

Now:

Compact → 0
Large   → 1

Use that for subtle transforms:

background scale:
1.00 → 0.97

corner radius:
0 → 18dp

dark overlay:
0 → slight

Don't overdo this.

22. One master progress again

Just like navigation:

sheet expansion progress
       ↓
┌──────┼────────┐
↓      ↓        ↓
scale  overlay  corner radius

Don't start three separate animations.

23. Haptic threshold strategy

For detents, use:

Large
────────────
*tick*
Medium
────────────
*tick*
Compact

But trigger at the logical snap boundary, not every time the sheet happens to pass directly over the exact detent coordinate.

This avoids noisy feedback during rapid movement.

24. Sheet Laboratory

Add:

Sheets ✅

Create:

┌─────────────────────────────┐
│ Sheet Laboratory            │
│                             │
│ Background content          │
│                             │
│                             │
│ ┌─────────────────────────┐ │
│ │        ─────            │ │
│ │ Comments                │ │
│ │                         │ │
│ │ Comment 1               │ │
│ │ Comment 2               │ │
│ │ ...                     │ │
│ └─────────────────────────┘ │
└─────────────────────────────┘

Debug:

Offset: 1038 px
Velocity: -1280 px/s
Phase: Dragging
Nearest: Medium
Expansion: 0.53
25. Test these exact scenarios

Test 1:

Medium
↓ slow drag upward
↓ release near Large
→ Large

Test 2:

Medium
↓ tiny movement downward
↓ fast fling
→ Compact

Test 3:

sheet settling toward Compact
↓ touch it
→ animation stops
→ finger owns it immediately

Test 4:

Large
↓ scroll comments
→ list scrolls

Test 5:

Large
list at top
↓ pull downward
→ sheet moves

Test 6:

Medium
↓ scroll upward
→ sheet expands first
→ then list scrolls

These are the important ones.

26. Tests

Pure target-selection tests:

@Test
fun slowReleaseChoosesNearestDetent() {
    ...
}

Fast upward:

@Test
fun fastUpwardVelocityChoosesHigherDetent() {
    ...
}

Fast downward:

@Test
fun fastDownwardVelocityChoosesLowerDetent() {
    ...
}

Expansion progress:

@Test
fun largeDetentProducesFullExpansion() {
    ...
}
27. Do NOT add yet

Still avoid:

❌ glass sheet
❌ dynamic Liquid Glass
❌ search bar morphing
❌ context menu sheets
❌ keyboard-heavy forms
❌ reels comments clone

First, make the plain sheet physics excellent.

Phase 6A checkpoint

At the end of this step:

✅ sheet detents
✅ custom detents
✅ draggable sheet
✅ direct finger tracking
✅ velocity-aware snapping
✅ spring settling
✅ animation interruption
✅ detent haptics
✅ expansion progress
✅ backdrop transformation hooks
✅ nested-scroll architecture
✅ list → sheet handoff
✅ sheet → list handoff
✅ nested fling handoff