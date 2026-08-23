package dev.iosfeel.dayline.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.button.IOSButton
import dev.iosfeel.components.button.IOSButtonStyle
import dev.iosfeel.dayline.core.design.DaylineTheme
import dev.iosfeel.dayline.feature.today.components.NowCard
import dev.iosfeel.dayline.feature.today.components.TimelineRow
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onOpenCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography
    val uiState by viewModel.uiState.collectAsState()

    val formattedDate = remember(uiState.selectedDate) {
        uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
    }

    val greeting = remember {
        val hour = LocalTime.now().hour
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Header Section
        Text(
            text = greeting,
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formattedDate,
            style = typography.largeTitle.copy(
                color = colors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. NOW Section Card
        NowCard(
            state = uiState.nowState,
            onCompleteClicked = {
                uiState.nowState.item?.let { viewModel.toggleTask(it) }
            },
            onActionClicked = onOpenCapture
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Timeline Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIMELINE",
                style = typography.caption.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "${uiState.completedCount}/${uiState.totalCount} completed",
                style = typography.caption.copy(
                    color = colors.textTertiary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Timeline Rows or Empty State
        if (uiState.items.isEmpty()) {
            TodayEmptyState(onAddClicked = onOpenCapture)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.items.forEachIndexed { index, item ->
                    TimelineRow(
                        item = item,
                        isFirst = index == 0,
                        isLast = index == uiState.items.lastIndex,
                        onToggleCompleted = {
                            viewModel.toggleTask(item)
                        },
                        onClick = {
                            // Item details navigation in subsequent phase
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp)) // Space for bottom tab bar
    }
}

@Composable
private fun TodayEmptyState(
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.surfaceSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nothing planned yet.",
            style = typography.headline.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your day is open.",
            style = typography.subheadline.copy(
                color = colors.textSecondary
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        IOSButton(
            text = "Add something",
            onClick = onAddClicked,
            style = IOSButtonStyle.Filled,
            modifier = Modifier.height(44.dp)
        )
    }
}
