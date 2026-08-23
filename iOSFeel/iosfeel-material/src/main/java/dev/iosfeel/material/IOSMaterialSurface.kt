package dev.iosfeel.material

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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

@Composable
fun IOSMaterialSurface(
    modifier: Modifier = Modifier,
    backdrop: IOSBackdropState? = null,
    config: IOSMaterialConfig = IOSMaterialConfig(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    if (!config.enabled) {
        Box(modifier = modifier) { content() }
        return
    }

    val resolved = remember(config.style) { resolveIOSMaterial(config.style) }
    val shape = remember(config.cornerRadius) { RoundedCornerShape(config.cornerRadius) }
    val effectiveBlurRadius = config.customBlurRadius ?: resolved.blurRadius
    val effectiveTintAlpha = config.customTintAlpha ?: resolved.tintAlpha

    val tintColor = config.tint ?: if (darkTheme) {
        IOSBlurDefaults.DarkTint.copy(alpha = if (Build.VERSION.SDK_INT >= 31) effectiveTintAlpha else 0.85f)
    } else {
        IOSBlurDefaults.LightTint.copy(alpha = if (Build.VERSION.SDK_INT >= 31) effectiveTintAlpha else 0.85f)
    }

    val borderColor = config.borderColor ?: if (darkTheme) {
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
            width = config.borderStroke,
            color = borderColor,
            shape = shape
        )

    Box(
        modifier = surfaceModifier
    ) {
        // Layer 1: Frosted Background Layer (Isolated to prevent content blur/flicker)
        if (backdrop != null && Build.VERSION.SDK_INT >= 31) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(effectiveBlurRadius)
                    .drawWithContent {
                        drawContext.canvas.save()
                        drawContext.transform.translate(-positionInRoot.x, -positionInRoot.y)
                        drawLayer(backdrop.layer)
                        drawContext.canvas.restore()
                    }
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
