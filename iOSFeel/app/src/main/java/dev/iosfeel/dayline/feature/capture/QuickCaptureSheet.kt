package dev.iosfeel.dayline.feature.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.button.IOSButton
import dev.iosfeel.components.button.IOSButtonStyle
import dev.iosfeel.dayline.core.design.DaylineTheme
import dev.iosfeel.dayline.core.model.TaskPriority
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics

@Composable
fun QuickCaptureSheet(
    viewModel: CaptureViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography
    val draft by viewModel.draft.collectAsState()
    val haptics = rememberIOSHaptics()
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Item",
                style = typography.headline.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "Cancel",
                style = typography.body.copy(
                    color = colors.accent,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.reset()
                    onDismiss()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceSecondary)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (draft.title.isEmpty()) {
                Text(
                    text = "What do you need to do?",
                    style = typography.body.copy(
                        color = colors.textTertiary
                    )
                )
            }

            BasicTextField(
                value = draft.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                textStyle = typography.body.copy(
                    color = colors.textPrimary
                ),
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (draft.isValid) {
                            viewModel.save(onSuccess = onDismiss)
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Scheduled Date
        Text(
            text = "WHEN",
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DatePreset.entries.forEach { preset ->
                val selected = draft.datePreset == preset
                SelectableChip(
                    label = preset.name,
                    selected = selected,
                    onClick = {
                        haptics.impact(IOSImpact.Light)
                        viewModel.onDatePresetChanged(preset)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: Time Preset
        Text(
            text = "TIME",
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimePreset.entries.forEach { preset ->
                val selected = draft.timePreset == preset
                val label = when (preset) {
                    TimePreset.None -> "None"
                    TimePreset.Morning -> "9:00 AM"
                    TimePreset.Afternoon -> "2:00 PM"
                    TimePreset.Evening -> "7:00 PM"
                }
                SelectableChip(
                    label = label,
                    selected = selected,
                    onClick = {
                        haptics.impact(IOSImpact.Light)
                        viewModel.onTimePresetChanged(preset)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: Priority
        Text(
            text = "PRIORITY",
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskPriority.entries.forEach { priority ->
                val selected = draft.priority == priority
                SelectableChip(
                    label = priority.name,
                    selected = selected,
                    onClick = {
                        haptics.impact(IOSImpact.Light)
                        viewModel.onPriorityChanged(priority)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Save Button
        IOSButton(
            text = "Save Task",
            onClick = {
                viewModel.save(onSuccess = onDismiss)
            },
            enabled = draft.isValid,
            style = IOSButtonStyle.Filled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography

    val bgColor = if (selected) colors.accent else colors.surfaceSecondary
    val textColor = if (selected) androidx.compose.ui.graphics.Color.White else colors.textPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = typography.caption.copy(
                color = textColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}
