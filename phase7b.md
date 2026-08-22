Phase 7B — Advanced Glass Rendering

Now we move beyond “translucent rounded rectangle” and build the part that can give iOSFeel its own visual identity.

The major architectural change is this: instead of trying to blur whatever is inside the glass composable, we'll introduce a backdrop host that records the scene underneath into a reusable GraphicsLayer. Compose's GraphicsLayer can record drawing commands and redraw them elsewhere, and its transforms/effects can be changed without rerecording the whole display list.

Our target becomes:

IOSBackdropHost
│
├── app/feed/background
│      ↓
│   recorded GraphicsLayer
│
└── overlays
       │
       ├── IOSGlass navigation bar
       ├── IOSGlass sheet
       └── IOSGlass floating controls
              ↓
       sample recorded backdrop
              ↓
          local crop
              ↓
             blur
              ↓
             tint
              ↓
        lighting / grain
              ↓
        sharp foreground UI

That is much closer to a real backdrop-material architecture.

1. Add the advanced renderer files

Expand iosfeel-material:

iosfeel-material/
├── IOSBackdropHost.kt
├── IOSBackdropState.kt
├── IOSBackdropMaterial.kt
├── IOSGlassRenderConfig.kt
├── IOSGlassPerformanceState.kt
├── IOSGlassLighting.kt
├── IOSGlassNoise.kt
├── IOSGlassRefraction.kt
├── IOSGlassQualityController.kt
└── shaders/
    ├── GlassLightingShader.kt
    └── GlassRefractionShader.kt

The important dependency rule remains:

material
   ↓
Compose graphics

NOT

material
↓
sheet/navigation
2. Introduce IOSBackdropState

Create:

package dev.iosfeel.material

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.layer.GraphicsLayer

@Stable
class IOSBackdropState internal constructor(
    internal val layer: GraphicsLayer
)

Then:

@Composable
fun rememberIOSBackdropState():
    IOSBackdropState {

    val layer =
        rememberGraphicsLayer()

    return remember(layer) {
        IOSBackdropState(
            layer = layer
        )
    }
}

rememberGraphicsLayer() gives us a retained drawing layer that can record Compose drawing commands and then be drawn elsewhere.

3. Create the backdrop host
@Composable
fun IOSBackdropHost(
    state: IOSBackdropState =
        rememberIOSBackdropState(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    Box(
        modifier = modifier
            .drawWithContent {

                state.layer.record {

                    this@drawWithContent
                        .drawContent()
                }

                drawLayer(
                    state.layer
                )
            }
    ) {
        content()
    }
}

Conceptually:

Compose scene
     ↓
GraphicsLayer.record()
     ↓
retained scene representation
     ↓
visible scene

We're now able to reference that same scene for glass rendering.

Compose officially documents this record-and-redraw pattern with GraphicsLayer.

4. Important rule: don't capture the glass itself

Suppose:

BackdropHost
│
├── Feed
└── GlassNavBar

If the host records both:

feed + glass

and then the glass samples that capture:

glass samples itself
↓
samples itself again
↓
feedback loop

Bad.

So structure it as:

Box
├── IOSBackdropCapture
│      └── Feed
│
└── overlay layer
       └── GlassNavBar

Create:

@Composable
fun IOSBackdropLayout(
    state: IOSBackdropState,
    backdrop: @Composable () -> Unit,
    overlay: @Composable () -> Unit
) {

    Box {

        Box(
            Modifier.drawWithContent {

                state.layer.record {
                    this@drawWithContent
                        .drawContent()
                }

                drawLayer(
                    state.layer
                )
            }
        ) {

            backdrop()
        }

        overlay()
    }
}

Now only:

feed/background

is captured.

5. Track each glass surface's coordinates

A navigation bar only needs:

bottom ~100dp

of the scene.

A small floating button only needs its local area.

Don't render the entire backdrop into every glass component.

Track coordinates:

data class IOSBackdropRegion(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

Inside a glass surface:

var coordinates
    by remember {
        mutableStateOf<
            LayoutCoordinates?
        >(null)
    }

Then:

Modifier.onGloballyPositioned {
    coordinates = it
}

Use:

val position =
    coordinates
        ?.positionInRoot()

So the renderer knows:

Glass bounds:

x = 0
y = 2110
width = 1080
height = 190
6. Create a backdrop-material primitive

The eventual API:

IOSBackdropMaterial(
    backdropState = backdropState,
    config = config
) {

    BottomNavigation()
}

Internally:

recorded background
       ↓
translate so correct region aligns
       ↓
clip
       ↓
blur
       ↓
material effects
       ↓
foreground UI
7. Localized rendering is critical

Imagine:

1080 × 2400 display

but navigation glass is:

1080 × 180

Don't repeatedly process:

2,592,000 pixels

when the actual glass region is roughly:

194,400 pixels

That's over 13× less area.

This is why localized backdrop rendering matters much more than fancy shader mathematics.

RenderEffect also forces offscreen rendering of a graphics layer, so minimizing affected layer area is particularly important.

8. Create a render configuration
@Immutable
data class IOSGlassRenderConfig(

    val blurRadiusDp: Float = 24f,

    val tintAlpha: Float = 0.12f,

    val saturation: Float = 1.08f,

    val highlightStrength: Float = 0.10f,

    val grainStrength: Float = 0.015f,

    val refractionStrength: Float = 0f,

    val dynamicLighting: Boolean = true
)

Keep refraction:

0 by default

initially.

9. Android 12 blur implementation

Our captured layer can have a RenderEffect.

Conceptually:

if (
    Build.VERSION.SDK_INT >= 31
) {

    backdropState.layer.renderEffect =
        BlurEffect(
            radiusX =
                blurRadiusPx,

            radiusY =
                blurRadiusPx,

            edgeTreatment =
                TileMode.Clamp
        )
}

But don't permanently mutate the master backdrop layer.

Why?

Because another glass component may want:

14dp blur

while another wants:

30dp

So the master capture should remain:

raw scene

and localized derived layers should receive the effect.

GraphicsLayer.renderEffect is supported on Android 12+, and applying one causes the layer's rendered content to be processed through that effect.

10. Think in terms of source and material layers

Architecture:

BackdropLayer
RAW
│
├── GlassLayer A
│     crop navigation region
│     blur 24
│
├── GlassLayer B
│     crop sheet region
│     blur 30
│
└── GlassLayer C
      reduced material
      no blur

Don't change:

BackdropLayer.blurRadius

globally.

That would couple unrelated surfaces.

11. Dynamic lighting

Now give the glass a subtle sense of volume.

Create:

@Immutable
data class IOSGlassLighting(
    val x: Float = 0.35f,
    val y: Float = 0.0f,
    val intensity: Float = 1f
)

Then use:

drawWithCache {

    val start =
        Offset(
            x =
                size.width *
                    lighting.x,

            y =
                size.height *
                    lighting.y
        )

    val highlight =
        Brush.radialGradient(
            colors =
                listOf(
                    Color.White.copy(
                        alpha =
                            0.08f *
                            lighting.intensity
                    ),

                    Color.Transparent
                ),

            center = start,

            radius =
                size.maxDimension *
                    0.8f
        )

    onDrawWithContent {

        drawContent()

        drawRect(
            brush =
                highlight
        )
    }
}

drawWithCache is appropriate here because the Brush can be retained until size or relevant state changes rather than recreated unnecessarily every draw.

12. Make lighting react to interaction—not constantly animate

For a sheet:

dragging upward
       ↓
highlight shifts subtly

For a button:

press
 ↓
surface becomes slightly denser

But idle:

nothing moves

Create:

data class IOSGlassInteraction(
    val progress: Float = 0f,
    val velocity: Float = 0f
)

Then:

fun calculateDynamicLighting(
    base: IOSGlassLighting,
    interaction:
        IOSGlassInteraction
): IOSGlassLighting {

    val normalizedVelocity =
        (
            interaction.velocity /
                5000f
        ).coerceIn(
            -1f,
            1f
        )

    return base.copy(
        y =
            (
                base.y +
                    normalizedVelocity *
                    0.08f
            ).coerceIn(
                0f,
                1f
            )
    )
}

Subtle.

13. Add restrained grain

Perfectly smooth transparency can look somewhat synthetic.

A tiny amount of grain can break up gradients.

But:

grain ≠ visible static

Create:

@Immutable
data class IOSGlassNoise(
    val strength: Float = 0.012f,
    val scale: Float = 1f
)

Do not regenerate a random bitmap every frame.

Instead either:

cached procedural shader

or:

tiny cached noise texture

and reuse it.

No frame-to-frame random allocation.

14. AGSL experimental refraction

Now the fun part.

Android's RuntimeShader/AGSL path allows custom pixel shaders on supported devices, so we can experiment with localized distortion rather than only tint/blur.

Keep it experimental and capability-gated.

Conceptual shader:

uniform shader backdrop;

uniform float2 resolution;
uniform float refractionStrength;

half4 main(float2 position) {

    float2 uv =
        position /
        resolution;

    float2 center =
        float2(
            0.5,
            0.5
        );

    float2 delta =
        uv -
        center;

    float distanceFromCenter =
        length(delta);

    float edge =
        smoothstep(
            0.15,
            0.75,
            distanceFromCenter
        );

    float2 distorted =
        position +
        delta *
        edge *
        refractionStrength;

    return backdrop.eval(
        distorted
    );
}

The idea:

center
→ nearly unchanged

edge
→ tiny displacement

Not:

funhouse lens
15. Refraction values must stay tiny

Laboratory range:

0.00
0.01
0.02
0.03

Not:

0.5

The goal is to create something you mostly notice while the surface moves.

If screenshots look obviously warped, it's probably too strong.

16. Edge refraction only

Even better:

glass interior
→ stable

glass boundary
→ subtle distortion

Create an edge mask:

distance from rounded boundary
        ↓
0 near center
1 near edge

Then multiply refraction by it.

Conceptually:

┌────────────────────────┐
│ //// edge zone ////    │
│                        │
│     stable center      │
│                        │
│ //// edge zone ////    │
└────────────────────────┘

That gives depth without destroying readability.

17. Never refract foreground UI

Same rule as blur.

This:

Icons
Text
Labels

must remain sharp.

Pipeline:

backdrop
   ↓
blur
   ↓
refraction
   ↓
tint
   ↓
highlight
   ↓
grain
   ↓
────────────
foreground text/icons

Never:

everything
↓
shader
18. Scroll-aware material response

Now use our Phase 5 scroll state.

Imagine the feed underneath a glass tab bar.

We can feed:

scroll velocity

into the material.

Not to make it wobble—but to slightly adjust:

tint density
highlight direction
refraction amount

For example:

fun glassVelocityResponse(
    velocityPxPerSecond: Float
): Float {

    return (
        kotlin.math.abs(
            velocityPxPerSecond
        ) /
        8000f
    ).coerceIn(
        0f,
        1f
    )
}

Then:

idle:
refraction 0.005

fast scroll:
refraction 0.015

Very small changes.

19. One master interaction value

Just like navigation:

scroll energy
       ↓
┌──────┼────────┐
↓      ↓        ↓
grain  tint   lighting

Not:

three unrelated animations

The UI feels more coherent when everything derives from the same physical input.

20. Create automatic quality control

This is one of the most important Phase 7B additions.

Create:

enum class IOSGlassRuntimeLevel {
    Full,
    BlurOnly,
    Reduced
}

Then:

@Stable
class IOSGlassPerformanceState {

    var runtimeLevel
        by mutableStateOf(
            IOSGlassRuntimeLevel.Full
        )
        internal set

    var recentJankRatio
        by mutableFloatStateOf(0f)
        internal set
}
21. Don't use our old FPS counter to decide quality

Phase 1's FPS counter is just debugging.

For actual frame-quality signals use:

JankStats
+
Macrobenchmark

Android's JankStats library reports slow/janky frames and lets us attach UI state—such as whether our glass surface is animating or a list is scrolling—to those frame reports.

This is perfect for:

GlassMode = Full
ScrollState = Flinging
SheetState = Dragging

Then:

Jank occurred

has context.

22. Automatic degradation policy

Conceptually:

Full
│
│ repeated significant jank
↓
BlurOnly
│
│ still struggling
↓
Reduced

And recovery should be slower:

Reduced
│
│ stable for a while
↓
BlurOnly
│
│ stable
↓
Full

Avoid:

Full
Reduced
Full
Reduced
Full

every few seconds.

That visual switching would itself feel terrible.

23. Make degradation conservative

Pseudo-policy:

fun chooseRuntimeGlassLevel(
    jankRatio: Float,
    current:
        IOSGlassRuntimeLevel
): IOSGlassRuntimeLevel {

    return when {

        jankRatio > 0.15f ->
            IOSGlassRuntimeLevel.Reduced

        jankRatio > 0.07f ->
            IOSGlassRuntimeLevel.BlurOnly

        else ->
            current
    }
}

These thresholds are placeholders for lab testing.

Not production constants yet.

24. What each runtime level means
FULL
────────────────────
localized blur
dynamic highlight
tiny grain
optional AGSL refraction


BLUR ONLY
────────────────────
localized blur
tint
edge highlight
no refraction
no dynamic grain


REDUCED
────────────────────
translucent tint
border
static highlight
no blur
no shader

The component remains attractive in every tier.

That is critical.

25. Avoid unnecessary offscreen layers

Compose documentation notes that RenderEffect, alpha, overscroll and certain compositing operations can force offscreen rasterization.

So avoid chains like:

graphicsLayer
↓
alpha layer
↓
blur layer
↓
mask layer
↓
another graphicsLayer

if one localized layer can do the job.

Offscreen buffers aren't automatically bad.

Huge unnecessary ones are.

26. Reduce overdraw

Glass naturally encourages:

background
+
blurred copy
+
tint
+
highlight
+
border
+
grain

That's several drawing passes.

Android's current rendering guidance specifically recommends reducing unnecessary backgrounds and transparency where they contribute to overdraw.

So if:

glass tint is opaque enough

don't also render two invisible background layers underneath it.

Our renderer should be deliberate.

27. Create an advanced public API

Eventually:

IOSGlassSurface(
    backdrop = backdropState,

    config =
        IOSMaterialConfig(
            style =
                IOSMaterialStyle.Regular,

            quality =
                IOSMaterialQuality.Automatic
        ),

    interaction =
        IOSGlassInteraction(
            velocity =
                scrollVelocity
        )
) {

    BottomNavigation()
}

That's all the app should see.

Everything else stays internal.

28. Real app structure

A feed screen could eventually look like:

val backdrop =
    rememberIOSBackdropState()

IOSBackdropLayout(
    state = backdrop,

    backdrop = {

        Feed(
            state = feedState
        )
    },

    overlay = {

        IOSGlassSurface(
            backdrop = backdrop,

            interaction =
                IOSGlassInteraction(
                    velocity =
                        feedVelocity
                )
        ) {

            NavigationBar()
        }
    }
)

Now the glass system actually knows what is underneath it.

29. Material Laboratory v2

Upgrade it to:

Advanced Material Laboratory

Backdrop
──────────────────
[ Feed ]
[ Photo ]
[ Gradient ]
[ Moving shapes ]

Renderer
──────────────────
Runtime: FULL
Blur: 24dp
Refraction: 0.012
Grain: 0.010
Lighting: Dynamic

Performance
──────────────────
Display: 120Hz
Jank ratio: 1.8%
Runtime level: FULL

[ Force Full ]
[ Force Blur ]
[ Force Reduced ]

Interaction
──────────────────
Scroll velocity: 4310px/s
Sheet velocity: 0px/s

Then compare side by side:

A — normal translucent surface
B — blur material
C — full iOSFeel glass
30. Test the worst case

Don't only test:

static gradient

Test:

1000-item feed
+
fast fling
+
glass navbar
+
glass header
+
sheet opening
+
sheet list scrolling
+
keyboard opening

while capturing JankStats.

JankStats is designed to associate janky frames with application UI state, which makes it useful for finding whether the expensive period is, say, Feed=Scrolling and Glass=Full.

31. Our performance target

The objective isn't:

zero frame ever misses anything

That's unrealistic across every Android device.

The objective is:

iOSFeel disabled
vs
iOSFeel enabled

→ minimal measurable regression

and when the GPU can't keep up:

quality automatically degrades

rather than:

animations become visibly choppy
32. Don't dynamically inspect device brand

Still avoid:

if (
    Build.MODEL ==
    "SM-G990B"
)

Instead detect:

API support
+
actual runtime performance

That means your S21 FE or any future phone can naturally end up at the quality level it can actually sustain.

33. Tests

Backdrop quality:

@Test
fun unsupportedShaderFallsBackToBlur() {

    val capabilities =
        IOSMaterialCapabilities(
            supportsBlur = true,
            supportsRuntimeShader = false,
            recommendedQuality =
                IOSMaterialQuality.Balanced
        )

    val quality =
        resolveMaterialQuality(
            IOSMaterialQuality.High,
            capabilities
        )

    assertEquals(
        IOSMaterialQuality.Balanced,
        quality
    )
}

Performance degradation:

@Test
fun seriousJankDisablesAdvancedEffects() {

    val level =
        chooseRuntimeGlassLevel(
            jankRatio = 0.20f,
            current =
                IOSGlassRuntimeLevel.Full
        )

    assertEquals(
        IOSGlassRuntimeLevel.Reduced,
        level
    )
}

Velocity response:

@Test
fun velocityResponseIsClamped() {

    assertEquals(
        1f,
        glassVelocityResponse(
            50_000f
        )
    )
}
34. Phase 7B checkpoint

At this point the architecture supports:

✅ retained backdrop capture
✅ localized glass regions
✅ independent material layers
✅ Android 12+ blur
✅ sharp foreground content
✅ dynamic lighting
✅ restrained grain
✅ experimental AGSL refraction
✅ edge-focused refraction
✅ scroll-aware material response
✅ interaction-aware rendering
✅ runtime capability fallback
✅ JankStats integration architecture
✅ automatic quality degradation
✅ overdraw awareness
✅ 120Hz stress testing

And importantly, Compose's current graphics stack supports the core pieces we're relying on: retained GraphicsLayer recording/drawing, RenderEffect on Android 12+, cached draw resources through drawWithCache, and offscreen compositing where effects require it.

Phase 7 is now complete enough to freeze
Phase 0  Foundation             ✅
Phase 1  Motion                 ✅
Phase 2  Haptics                ✅
Phase 3  Gestures               ✅
Phase 4  Navigation             ✅
Phase 5  Scrolling              ✅
Phase 6  Sheets                 ✅
Phase 7  Materials / Glass      ✅

Phase 8  Components             ← NEXT
Phase 9  Social-app benchmark