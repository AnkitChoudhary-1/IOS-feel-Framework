package dev.iosfeel.material

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp

val LocalIOSMaterialOverride = compositionLocalOf<IOSMaterialConfig?> { null }
val LocalIOSDarkTheme = compositionLocalOf { false }

@Composable
fun IOSMaterialSurface(
    modifier: Modifier = Modifier,
    backdrop: IOSBackdropState? = null,
    config: IOSMaterialConfig = IOSMaterialConfig(),
    darkTheme: Boolean = LocalIOSDarkTheme.current,
    content: @Composable () -> Unit
) {
    val override = LocalIOSMaterialOverride.current
    val effectiveConfig = remember(config, override) {
        if (override != null) {
            config.copy(
                style = override.style,
                customBlurRadius = override.customBlurRadius ?: config.customBlurRadius,
                customTintAlpha = override.customTintAlpha ?: config.customTintAlpha,
                tint = override.tint ?: config.tint,
                borderColor = override.borderColor ?: config.borderColor,
                borderStroke = if (override.borderStroke != 0.5.dp) override.borderStroke else config.borderStroke,
                enabled = override.enabled && config.enabled
            )
        } else {
            config
        }
    }

    if (!effectiveConfig.enabled) {
        Box(modifier = modifier) { content() }
        return
    }

    val resolved = remember(effectiveConfig.style) { resolveIOSMaterial(effectiveConfig.style) }
    val safeCornerRadius = effectiveConfig.cornerRadius.coerceAtLeast(0.dp)
    val shape = remember(safeCornerRadius) { RoundedCornerShape(safeCornerRadius) }
    val effectiveBlurRadius = (effectiveConfig.customBlurRadius ?: resolved.blurRadius).coerceAtLeast(0.dp)
    val effectiveTintAlpha = (effectiveConfig.customTintAlpha ?: resolved.tintAlpha).coerceIn(0f, 1f)
    val safeBorderStroke = effectiveConfig.borderStroke.coerceAtLeast(0.dp)

    val tintColor = effectiveConfig.tint ?: if (darkTheme) {
        IOSBlurDefaults.DarkTint.copy(alpha = if (Build.VERSION.SDK_INT >= 31) effectiveTintAlpha else 0.85f)
    } else {
        IOSBlurDefaults.LightTint.copy(alpha = if (Build.VERSION.SDK_INT >= 31) effectiveTintAlpha else 0.85f)
    }

    val borderColor = effectiveConfig.borderColor ?: if (darkTheme) {
        IOSBlurDefaults.DarkBorder
    } else {
        IOSBlurDefaults.LightBorder
    }

    var positionInRoot by remember { mutableStateOf(Offset.Zero) }

    val surfaceModifier = modifier
        .onGloballyPositioned { coordinates ->
            val pos = coordinates.positionInRoot()
            if (pos != positionInRoot) {
                positionInRoot = pos
            }
        }
        .clip(shape)
        .border(
            width = safeBorderStroke,
            color = borderColor,
            shape = shape
        )

    Box(
        modifier = surfaceModifier
    ) {
        // Layer 1: Frosted Background Layer (Isolated to prevent content blur/flicker)
        if (backdrop != null && Build.VERSION.SDK_INT >= 31) {
            val blurMod = if (effectiveBlurRadius > 0.dp) Modifier.blur(effectiveBlurRadius) else Modifier
            // 1a. Hardware Blurred Backdrop Layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(blurMod)
                    .drawWithContent {
                        try {
                            drawContext.canvas.save()
                            drawContext.transform.translate(-positionInRoot.x, -positionInRoot.y)
                            drawLayer(backdrop.layer)
                            drawContext.canvas.restore()
                        } catch (_: Throwable) {
                            // Defensive guard against recursive backdrop recording passes
                        }
                    }
            )
            // 1b. Crisp Scrim Tint Overlay (Maintains high-contrast text legibility without spreading/bleeding)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(tintColor)
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(tintColor)
            )
        }

        // Layer 2: Foreground Content (Always 100% sharp and unblurred)
        content()
    }
}

/**
 * Convenience alias for [IOSMaterialSurface]
 */
@Composable
fun IOSBlurSurface(
    modifier: Modifier = Modifier,
    backdrop: IOSBackdropState? = null,
    config: IOSMaterialConfig = IOSMaterialConfig(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    IOSMaterialSurface(
        modifier = modifier,
        backdrop = backdrop,
        config = config,
        darkTheme = darkTheme,
        content = content
    )
}
