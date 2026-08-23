package dev.iosfeel.dayline.feature.today.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.dayline.core.design.DaylineTheme
import dev.iosfeel.dayline.core.model.TaskPriority
import dev.iosfeel.dayline.core.model.TimelineItem
import java.time.format.DateTimeFormatter

@Composable
fun TimelineRow(
    item: TimelineItem,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleCompleted: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography

    val textAlpha by animateFloatAsState(
        targetValue = if (item.isCompleted) 0.45f else 1.0f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
        label = "rowAlpha"
    )

    val timeString = remember(item.time) {
        item.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "—"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Time column (44dp fixed width)
        Text(
            text = timeString,
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            modifier = Modifier.width(44.dp)
        )

        // 2. Timeline vertical track & node
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Top connecting line
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .fillMaxHeight(0.5f)
                        .align(Alignment.TopCenter)
                        .background(colors.surfaceSecondary)
                )
            }

            // Bottom connecting line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .fillMaxHeight(0.5f)
                        .align(Alignment.BottomCenter)
                        .background(colors.surfaceSecondary)
                )
            }

            // Node indicator
            when (item) {
                is TimelineItem.TaskItem -> {
                    val priorityColor = when (item.task.priority) {
                        TaskPriority.High -> colors.destructive
                        TaskPriority.Medium -> colors.warning
                        else -> colors.accent
                    }
                    TimelineCheckbox(
                        checked = item.task.completed,
                        onCheckedChange = { onToggleCompleted() },
                        accentColor = priorityColor
                    )
                }
                is TimelineItem.HabitItem -> {
                    TimelineCheckbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onToggleCompleted() },
                        accentColor = colors.success
                    )
                }
                is TimelineItem.EventItem -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                }
                is TimelineItem.ExpenseItem -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.warning)
                    )
                }
                is TimelineItem.FocusItem -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 3. Content Body
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = typography.body.copy(
                        color = colors.textPrimary,
                        fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    modifier = Modifier.alpha(textAlpha)
                )

                if (item is TimelineItem.TaskItem && item.task.description?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.task.description ?: "",
                        style = typography.caption.copy(
                            color = colors.textSecondary
                        ),
                        modifier = Modifier.alpha(textAlpha)
                    )
                }
            }

            // Priority badge
            if (item is TimelineItem.TaskItem && item.task.priority != TaskPriority.None && !item.isCompleted) {
                Spacer(modifier = Modifier.width(8.dp))
                val priorityBadgeBg = when (item.task.priority) {
                    TaskPriority.High -> colors.destructive.copy(alpha = 0.12f)
                    TaskPriority.Medium -> colors.warning.copy(alpha = 0.12f)
                    else -> colors.surfaceSecondary
                }
                val priorityTextColor = when (item.task.priority) {
                    TaskPriority.High -> colors.destructive
                    TaskPriority.Medium -> colors.warning
                    else -> colors.textSecondary
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(priorityBadgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.task.priority.name,
                        style = typography.caption.copy(
                            color = priorityTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
