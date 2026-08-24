package dev.iosfeel.components.list

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState
import dev.iosfeel.core.tokens.IOSSpacing
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Standard iOS Settings/Navigation Disclosure Row with chevron.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSDisclosureRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    leading: (@Composable () -> Unit)? = null,
    showDivider: Boolean = true,
    dividerIndent: Boolean = true
) {
    val haptics = rememberIOSHaptics()
    val pressState = rememberIOSPressSurfaceState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { role = Role.Button }
            .iosPressSurface(
                state = pressState,
                pressedScale = 0.995f,
                pressedAlpha = 0.85f,
                onClick = {
                    haptics.impact(IOSImpact.Light)
                    onClick()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
                .padding(
                    horizontal = IOSSpacing.Large,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(IOSSpacing.Medium))
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            if (value != null) {
                Text(
                    text = value,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = "›",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.35f)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(
                    start = if (dividerIndent && leading != null) 56.dp else IOSSpacing.Large
                ),
                color = Color.White.copy(alpha = 0.12f),
                thickness = 0.5.dp
            )
        }
    }
}
