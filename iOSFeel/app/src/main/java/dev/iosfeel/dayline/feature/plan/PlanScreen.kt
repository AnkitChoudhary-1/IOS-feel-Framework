package dev.iosfeel.dayline.feature.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.button.IOSButton
import dev.iosfeel.components.button.IOSButtonStyle
import dev.iosfeel.components.segmented.IOSSegmentedControl
import dev.iosfeel.components.segmented.IOSSegmentedItem
import dev.iosfeel.dayline.core.design.DaylineTheme

enum class PlanMode {
    Day,
    Week
}

@Composable
fun PlanScreen(
    onOpenCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography

    var planMode by remember { mutableStateOf(PlanMode.Day) }
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

        Text(
            text = "Plan",
            style = typography.largeTitle.copy(
                color = colors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        IOSSegmentedControl(
            items = listOf(
                IOSSegmentedItem(label = "Day", value = PlanMode.Day),
                IOSSegmentedItem(label = "Week", value = PlanMode.Week)
            ),
            selectedValue = planMode,
            onSelected = { planMode = it },
            containerColor = colors.surfaceSecondary,
            selectedPillColor = colors.surface,
            selectedTextColor = colors.textPrimary,
            unselectedTextColor = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Empty Plan Card
        Column(
            modifier = Modifier
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
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (planMode == PlanMode.Day) "No upcoming tasks for today" else "Your week is clear",
                style = typography.headline.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Schedule tasks or habits ahead of time.",
                style = typography.subheadline.copy(
                    color = colors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            IOSButton(
                text = "Schedule an item",
                onClick = onOpenCapture,
                style = IOSButtonStyle.Filled,
                modifier = Modifier.height(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
