package dev.iosfeel.dayline.feature.insights

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.dayline.core.design.DaylineTheme

@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography
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
            text = "Insights",
            style = typography.largeTitle.copy(
                color = colors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "THIS WEEK",
            style = typography.caption.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCard(
                title = "Tasks Completed",
                value = "0 / 0",
                subtitle = "No tasks completed yet",
                modifier = Modifier.weight(1f)
            )

            InsightCard(
                title = "Focus Time",
                value = "0m",
                subtitle = "Start a focus session",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCard(
                title = "Habits",
                value = "—",
                subtitle = "0 active streaks",
                modifier = Modifier.weight(1f)
            )

            InsightCard(
                title = "Spent",
                value = "₹0",
                subtitle = "0 recorded expenses",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun InsightCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = DaylineTheme.colors
    val typography = DaylineTheme.typography

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = typography.caption.copy(
                    color = colors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = typography.title1.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = typography.caption.copy(
                    color = colors.textTertiary
                )
            )
        }
    }
}
