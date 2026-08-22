package dev.iosfeel.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.components.interaction.iosPressEffect
import dev.iosfeel.core.tokens.IOSSpacing
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics

@Composable
fun IOSListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showDivider: Boolean = true,
    dividerIndent: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = rememberIOSHaptics()

    val rowModifier = if (onClick != null) {
        modifier
            .semantics { role = Role.Button }
            .iosPressEffect(
                interactionSource = interactionSource,
                config = IOSPressConfig(pressedScale = 0.995f, pressedAlpha = 0.85f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.impact(IOSImpact.Light)
                onClick()
            }
    } else {
        modifier
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(rowModifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .padding(
                    horizontal = IOSSpacing.Large,
                    vertical = IOSSpacing.Medium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(IOSSpacing.Medium))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(IOSSpacing.Small))
                trailing()
            } else if (onClick != null) {
                Spacer(modifier = Modifier.width(IOSSpacing.Small))
                Text(
                    text = "›",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.35f)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(
                    start = if (dividerIndent && leading != null) 56.dp else IOSSpacing.Large
                ),
                thickness = 0.5.dp,
                color = Color.White.copy(alpha = 0.12f)
            )
        }
    }
}
