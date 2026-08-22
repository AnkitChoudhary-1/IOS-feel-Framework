Phase 7 — Materials / Glass Engine

This phase should not start by trying to clone Apple's Liquid Glass pixel-for-pixel. Instead, we'll build a reusable rendering system that gives us the properties we're after:

background separation
+ translucency
+ blur where supported
+ tint
+ edge highlights
+ adaptive contrast
+ interaction response
+ graceful fallback

I checked the current Compose graphics APIs. Modifier.blur() and RenderEffect are supported on Android 12/API 31+, while older Android versions ignore those blur effects. Compose also gives us drawWithCache for efficiently caching drawing objects, and AGSL/RuntimeShader is available for more advanced shader work.

For that reason, Phase 7 should have three rendering tiers:

Tier A
Android 13+
Advanced shader experiments

Tier B
Android 12+
RenderEffect / Compose blur

Tier C
Older / low-power devices
Translucency + tint + border,
no expensive blur

That way IOSGlass never becomes:

"requires flagship Android phone"
1. Create the materials module

Add:

iosfeel-material/
└── src/main/java/dev/iosfeel/material/
    ├── IOSMaterial.kt
    ├── IOSMaterialStyle.kt
    ├── IOSMaterialConfig.kt
    ├── IOSMaterialCapabilities.kt
    ├── IOSMaterialQuality.kt
    ├── IOSMaterialColors.kt
    ├── IOSGlassSurface.kt
    ├── IOSGlassModifier.kt
    ├── IOSGlassHighlight.kt
    └── RememberIOSMaterialCapabilities.kt

Dependencies should stay light:

iosfeel-material
      │
      ├── iosfeel-core
      └── Compose UI graphics

Do not make materials depend on:

navigation
sheets
scrolling

Instead:

IOSSheet
   ↓
uses IOSGlassSurface

Navigation bar
   ↓
uses IOSGlassSurface
2. Define material quality

Create:

package dev.iosfeel.material

enum class IOSMaterialQuality {
    Automatic,
    High,
    Balanced,
    Reduced
}

Meaning:

High
→ blur + highlights + advanced rendering

Balanced
→ blur + tint, cheaper decorations

Reduced
→ translucency/tint only

Automatic
→ capability/performance based

This becomes extremely useful when testing lower-end devices.

3. Define material styles

Create IOSMaterialStyle.kt:

package dev.iosfeel.material

enum class IOSMaterialStyle {
    UltraThin,
    Thin,
    Regular,
    Thick
}

This is semantic rather than saying:

blur20
blur35
blur55

Then map styles internally.

4. Material configuration
package dev.iosfeel.material

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class IOSMaterialConfig(

    val style: IOSMaterialStyle =
        IOSMaterialStyle.Regular,

    val quality: IOSMaterialQuality =
        IOSMaterialQuality.Automatic,

    val tint: Color =
        Color.White,

    val tintAlpha: Float =
        0.12f,

    val saturation: Float =
        1f,

    val highlightAlpha: Float =
        0.22f,

    val borderAlpha: Float =
        0.16f,

    val cornerRadius: Dp =
        24.dp
)

We deliberately expose semantic controls, not GPU implementation details.

5. Resolve material parameters

Create:

data class IOSResolvedMaterial(
    val blurRadius: Dp,
    val tintAlpha: Float,
    val highlightAlpha: Float,
    val borderAlpha: Float
)

Then:

fun resolveIOSMaterial(
    config: IOSMaterialConfig
): IOSResolvedMaterial {

    return when (
        config.style
    ) {

        IOSMaterialStyle.UltraThin ->
            IOSResolvedMaterial(
                blurRadius = 14.dp,
                tintAlpha =
                    config.tintAlpha * 0.65f,
                highlightAlpha =
                    config.highlightAlpha,
                borderAlpha =
                    config.borderAlpha
            )

        IOSMaterialStyle.Thin ->
            IOSResolvedMaterial(
                blurRadius = 20.dp,
                tintAlpha =
                    config.tintAlpha * 0.8f,
                highlightAlpha =
                    config.highlightAlpha,
                borderAlpha =
                    config.borderAlpha
            )

        IOSMaterialStyle.Regular ->
            IOSResolvedMaterial(
                blurRadius = 28.dp,
                tintAlpha =
                    config.tintAlpha,
                highlightAlpha =
                    config.highlightAlpha,
                borderAlpha =
                    config.borderAlpha
            )

        IOSMaterialStyle.Thick ->
            IOSResolvedMaterial(
                blurRadius = 40.dp,
                tintAlpha =
                    config.tintAlpha * 1.25f,
                highlightAlpha =
                    config.highlightAlpha,
                borderAlpha =
                    config.borderAlpha
            )
    }
}

These are experimental iOSFeel values.

We're not claiming Apple uses those radii.

6. Detect rendering capabilities

Create:

data class IOSMaterialCapabilities(
    val supportsBlur: Boolean,
    val supportsRuntimeShader: Boolean,
    val recommendedQuality:
        IOSMaterialQuality
)

Then:

fun detectIOSMaterialCapabilities():
    IOSMaterialCapabilities {

    val supportsBlur =
        Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S

    val supportsRuntimeShader =
        Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU

    val quality =
        when {

            supportsRuntimeShader ->
                IOSMaterialQuality.High

            supportsBlur ->
                IOSMaterialQuality.Balanced

            else ->
                IOSMaterialQuality.Reduced
        }

    return IOSMaterialCapabilities(
        supportsBlur =
            supportsBlur,

        supportsRuntimeShader =
            supportsRuntimeShader,

        recommendedQuality =
            quality
    )
}

AGSL RuntimeShader was introduced with Android 13/API 33, while Compose blur/RenderEffect depends on Android 12+.

7. Remember capabilities
@Composable
fun rememberIOSMaterialCapabilities():
    IOSMaterialCapabilities {

    return remember {
        detectIOSMaterialCapabilities()
    }
}

Now every glass surface doesn't repeatedly evaluate device support.

8. Important distinction: blur vs backdrop blur

This matters a lot.

If you write:

Modifier.blur(30.dp)

on:

[ Glass Surface ]

you are blurring that composable's rendered content.

You're not magically doing:

everything physically behind this surface
↓
sample
↓
blur
↓
display through glass

Compose's blur renders the corresponding composable into a separate graphics layer and applies the effect there.

So don't build:

Surface(
    Modifier.blur(...)
) {
    Text("Settings")
}

because you'll blur:

Settings

too.

That isn't glass.

9. First practical glass architecture

Instead structure components:

┌──────────────────────────┐
│ Glass surface            │
│                          │
│ background treatment     │
│ ──────────────────────── │
│ foreground content       │
│                          │
│     "Comments"           │
└──────────────────────────┘

Foreground content must remain sharp.

Conceptually:

Box {

    IOSGlassBackground(...)

    Box {
        content()
    }
}

This distinction will save us many rendering problems later.

10. Build the base surface

Create:

@Composable
fun IOSGlassSurface(
    modifier: Modifier = Modifier,
    config: IOSMaterialConfig =
        IOSMaterialConfig(),
    content:
        @Composable BoxScope.() -> Unit
) {

    val capabilities =
        rememberIOSMaterialCapabilities()

    val resolved =
        remember(config) {
            resolveIOSMaterial(config)
        }

    val quality =
        when (config.quality) {

            IOSMaterialQuality.Automatic ->
                capabilities
                    .recommendedQuality

            else ->
                config.quality
        }

    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    config.cornerRadius
                )
            )
    ) {

        IOSGlassBackground(
            config = config,
            resolved = resolved,
            quality = quality,
            capabilities = capabilities
        )

        content()
    }
}
11. Start with a cheap fallback

Before blur, make Reduced quality excellent.

@Composable
private fun IOSReducedGlassBackground(
    config: IOSMaterialConfig,
    resolved: IOSResolvedMaterial
) {

    Box(
        Modifier
            .matchParentSize()
            .background(
                config.tint.copy(
                    alpha =
                        resolved.tintAlpha
                )
            )
    )
}

Then add a subtle border:

.border(
    width = 0.75.dp,

    color =
        Color.White.copy(
            alpha =
                resolved.borderAlpha
        ),

    shape =
        RoundedCornerShape(
            config.cornerRadius
        )
)

Even without blur we can get:

transparency
+ separation
+ edge definition

instead of falling back to an ugly solid rectangle.

12. Add an edge highlight

Now the material starts becoming much more interesting.

Use drawWithCache.

Compose recommends drawWithCache when you're constructing reusable drawing objects such as brushes, because they can be cached until relevant inputs change.

Create:

fun Modifier.iosGlassHighlight(
    alpha: Float
): Modifier {

    return drawWithCache {

        val highlight =
            Brush.linearGradient(
                colors =
                    listOf(
                        Color.White.copy(
                            alpha = alpha
                        ),

                        Color.Transparent,

                        Color.White.copy(
                            alpha =
                                alpha * 0.25f
                        )
                    ),

                start =
                    Offset.Zero,

                end =
                    Offset(
                        size.width,
                        size.height
                    )
            )

        onDrawWithContent {

            drawContent()

            drawRect(
                brush =
                    highlight
            )
        }
    }
}

Now the surface isn't uniformly translucent.

It gets subtle directional light response.

13. But don't make it look glossy

Avoid:

massive white streak
huge bright border
rainbow refraction
lens flare

Our target is:

barely visible highlight
+
soft separation

If the first thing someone notices is:

"Look at that shiny effect!"

we probably went too far.

They should notice the interface first.

14. Make highlights position-aware

Eventually we want:

light source near top
        ↓
top edge slightly brighter

surface moves
        ↓
highlight subtly changes

Create:

data class IOSGlassLighting(
    val lightX: Float = 0.35f,
    val lightY: Float = 0f,
    val intensity: Float = 1f
)

Fractions:

0,0 = top left
1,1 = bottom right

Then highlight direction is derived from that.

This is far better than every glass component having identical highlights.

15. Interaction response

Glass should react subtly when a component is pressed.

Not:

press
→ giant scale animation

Instead:

normal
blur/tint/highlight

        ↓ press

slightly denser tint
slightly reduced highlight
maybe 0.985 scale

Add:

data class IOSMaterialInteractionState(
    val pressed: Boolean = false,
    val progress: Float = 0f
)

Eventually buttons can feed this into material rendering.

16. Material interpolation

Create pure helpers:

fun lerpMaterialAlpha(
    normal: Float,
    pressed: Float,
    progress: Float
): Float {

    val p =
        progress.coerceIn(
            0f,
            1f
        )

    return normal +
        (pressed - normal) * p
}

For example:

normal tint = .12
pressed tint = .17

At:

progress = .5

we get:

.145

One interaction progress can control all visual material response.

Same design principle as navigation and sheets.

17. Android 12 blur tier

For Android 12+, we can experiment with Compose blur/RenderEffect.

Current Compose blur is explicitly supported on Android 12+ and ignored on older versions. It also creates a separate graphics layer, so we need to use it selectively rather than slapping large blur modifiers everywhere.

For a controlled background layer:

if (
    capabilities.supportsBlur &&
    quality !=
        IOSMaterialQuality.Reduced
) {

    backgroundContent(
        Modifier.blur(
            radius =
                resolved.blurRadius,

            edgeTreatment =
                BlurredEdgeTreatment(
                    RoundedCornerShape(
                        config.cornerRadius
                    )
                )
        )
    )
}

But again: this only makes sense where we control the content being sampled/rendered.

18. Don't blur an entire feed

Imagine:

Instagram feed
+
floating glass tab bar

The worst implementation would render:

entire 120Hz feed
↓
giant blur layer
↓
every frame

while the feed is scrolling.

That can be expensive.

Instead, we need quality strategies.

For example:

High:
real-time blur

Balanced:
smaller/localized blur

Reduced:
translucent tint, no blur

Performance is a feature.

19. Glass quality resolver

Create:

fun resolveMaterialQuality(
    requested:
        IOSMaterialQuality,

    capabilities:
        IOSMaterialCapabilities
): IOSMaterialQuality {

    if (
        requested ==
        IOSMaterialQuality.Automatic
    ) {
        return capabilities
            .recommendedQuality
    }

    if (
        requested ==
            IOSMaterialQuality.High &&
        !capabilities
            .supportsRuntimeShader
    ) {

        return if (
            capabilities.supportsBlur
        ) {
            IOSMaterialQuality.Balanced
        } else {
            IOSMaterialQuality.Reduced
        }
    }

    return requested
}

So a developer can ask:

High

but we still don't crash or render nonsense on unsupported hardware.

20. Adaptive contrast

Glass can create a problem:

white text
+
very bright image behind glass
=
terrible readability

We need the material to protect content readability.

Add:

enum class IOSMaterialContrast {
    Automatic,
    LightContent,
    DarkContent
}

Then:

data class IOSMaterialConfig(
    ...
    val contrast:
        IOSMaterialContrast =
            IOSMaterialContrast.Automatic
)

In Phase 7A, automatic can simply use theme/background information.

Don't attempt real-time per-pixel luminance sampling yet.

21. Content color

Expose:

data class IOSMaterialColors(
    val contentColor: Color,
    val secondaryContentColor: Color
)

For dark material:

primary → white-ish
secondary → translucent white

For light material:

primary → near-black
secondary → translucent dark

Then IOSGlassSurface can provide:

CompositionLocalProvider(
    LocalContentColor provides
        colors.contentColor
) {
    content()
}

Now components inherit correct text/icon colors.

22. AGSL belongs behind an experimental flag

This is where we can eventually get more ambitious.

AGSL/RuntimeShader allows custom per-pixel shading and can be used from Compose graphics/brushes.

Potential experiments:

subtle light refraction
edge distortion
dynamic highlight warping
noise/grain
localized lens behavior

But don't make the whole framework dependent on it.

Create:

data class IOSMaterialExperimental(
    val enableRuntimeShader:
        Boolean = false,

    val enableRefraction:
        Boolean = false
)

Default:

OFF
23. First AGSL experiment

Create a simple lighting shader, not a giant Liquid Glass shader.

Conceptually:

uniform float2 resolution;
uniform float2 lightPosition;
layout(color) uniform half4 tint;

half4 main(float2 fragCoord) {

    float2 uv =
        fragCoord /
        resolution;

    float distanceToLight =
        distance(
            uv,
            lightPosition
        );

    float highlight =
        smoothstep(
            0.8,
            0.0,
            distanceToLight
        );

    return tint +
        half4(
            highlight * 0.06
        );
}

The purpose is simply to establish:

Compose
↓
RuntimeShader
↓
uniforms
↓
real-time surface response

not to finish the glass effect.

24. Material Laboratory

Add:

Materials ✅

Create a screen containing:

Material Laboratory

Background:
[ photo / gradient / colorful cards ]

Quality
[ Auto ] [ High ] [ Balanced ] [ Reduced ]

Style
[ UltraThin ]
[ Thin ]
[ Regular ]
[ Thick ]

Blur
28dp

Tint
0.12

Highlight
0.22

[ Glass Card ]

┌────────────────────────────┐
│  Now Playing               │
│                            │
│  Track name                │
│  Artist                    │
│                            │
│      ◀   ▶   ▷             │
└────────────────────────────┘

The important part is having busy backgrounds.

Testing glass over plain white tells us almost nothing.

25. Test over several backgrounds

Use at least:

Bright photo
Dark photo
High-contrast image
Color gradient
Moving feed
Solid background

A material that's beautiful over:

purple gradient

but unreadable over:

white photo

isn't robust.

26. Test motion simultaneously

Take the sheet from Phase 6:

IOSSheet

and replace the plain surface with:

IOSGlassSurface(
    config =
        IOSMaterialConfig(
            style =
                IOSMaterialStyle.Regular
        )
) {

    CommentsContent()
}

Now test:

sheet dragging
+
glass rendering
+
scrolling
+
haptics
+
120Hz

That's where performance problems become visible.

27. Build a glass navigation bar test

Also create:

Feed
────────────────────

content scrolling

────────────────────
  Home Search + Profile
────────────────────
glass navigation bar

The content should scroll beneath it.

This tests exactly the kind of social-media UI we're ultimately targeting.

28. Avoid shadows

This also fits the visual direction we've been using for this project.

Don't rely on:

big elevation shadow

for separation.

Use:

material tint
edge highlight
thin border
backdrop separation

This produces a cleaner contemporary result.

29. Performance tiers

Our renderer should eventually behave like:

Automatic
    ↓
detect capability

Powerful modern device
    ↓
High

Android 12 capable
    ↓
Balanced

Unsupported / expensive
    ↓
Reduced

But don't use device brand checks like:

if (Build.MANUFACTURER == "Samsung")

in the generic material system.

Capability-based logic is much healthier.

30. Reduced-motion / accessibility consideration

Material itself shouldn't create distracting constant animation.

Avoid:

glass continuously wobbling
highlight constantly swimming
background constantly refracting

unless the effect is directly responding to interaction.

The best version should mostly be:

stable when idle
responsive when touched/moved
31. Tests

Pure material resolution tests:

@Test
fun thickMaterialHasMoreBlurThanThin() {

    val thin =
        resolveIOSMaterial(
            IOSMaterialConfig(
                style =
                    IOSMaterialStyle.Thin
            )
        )

    val thick =
        resolveIOSMaterial(
            IOSMaterialConfig(
                style =
                    IOSMaterialStyle.Thick
            )
        )

    assertTrue(
        thick.blurRadius >
            thin.blurRadius
    )
}

Quality fallback:

@Test
fun highFallsBackWithoutShaderSupport() {

    val capabilities =
        IOSMaterialCapabilities(
            supportsBlur = true,
            supportsRuntimeShader = false,
            recommendedQuality =
                IOSMaterialQuality.Balanced
        )

    val result =
        resolveMaterialQuality(
            requested =
                IOSMaterialQuality.High,

            capabilities =
                capabilities
        )

    assertEquals(
        IOSMaterialQuality.Balanced,
        result
    )
}

And interpolation:

@Test
fun materialProgressInterpolatesAlpha() {

    val result =
        lerpMaterialAlpha(
            normal = 0.1f,
            pressed = 0.2f,
            progress = 0.5f
        )

    assertEquals(
        0.15f,
        result,
        0.001f
    )
}
Phase 7A checkpoint

At this stage we want:

✅ semantic material styles
✅ quality levels
✅ capability detection
✅ Android 12 blur path
✅ older-device fallback
✅ tint/translucency layer
✅ thin edge treatment
✅ directional highlights
✅ interaction interpolation
✅ adaptive-content-color architecture
✅ AGSL experimental path
✅ material laboratory
✅ glass sheet test
✅ glass navigation bar test
✅ performance-aware rendering

Phase 7 is not fully finished yet.

Phase 7B next

Phase 7B should tackle the harder rendering work:

actual backdrop capture strategy
+ localized blur
+ dynamic lighting
+ AGSL refraction experiments
+ noise/grain
+ scroll-aware material response
+ performance profiling
+ automatic degradation under load